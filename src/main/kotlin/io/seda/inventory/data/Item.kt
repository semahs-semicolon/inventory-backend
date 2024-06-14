package io.seda.inventory.data

import io.r2dbc.postgresql.codec.Vector
import io.r2dbc.spi.Row
import io.seda.inventory.services.LocationService
import io.seda.inventory.services.ProductService
import kotlinx.coroutines.flow.Flow
import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


@Table(name = "items")
data class Item(@Id var id: Long? = null, val productId: Long, var locationId: Long, var count: Int,

                @Transient val product: ProductService.SimpleProduct? = null,
                @Transient val location: LocationService.SimpleLocation? = null)

interface ItemRepository: CoroutineCrudRepository<Item, Long> {
    @Query("SELECT * from inventory.item_view WHERE location_id = :location")
    fun findAllByLocation(location: Long): Flow<Item>
    @Query("SELECT * from inventory.item_view WHERE product_id = :product")
    fun findAllByProduct(product: Long): Flow<Item>
    @Query("SELECT * from inventory.item_view WHERE product_id = :product AND location_id = :location")
    fun findAllByLocationAndProduct(location: Long, product: Long): Flow<Item>


    @Query("SELECT * from inventory.item_view")
    fun findAllByAnyhow(): Flow<Item>
}

@ReadingConverter
object ItemReadConverter : Converter<Row, Item?> {
    override fun convert(source: Row): Item {

        val product: ProductService.SimpleProduct? = if (source.metadata.contains("product_name")) {
            ProductService.SimpleProduct((source.get("product_id") as Long).toULong().toString(), source.get("product_name") as String, null, source.get("product_primary_image") as String?, null, null, null)
        } else null;
        val location: LocationService.SimpleLocation? = if (source.metadata.contains("location_name")) {
            LocationService.SimpleLocation((source.get("location_id") as Long).toULong().toString(), source.get("location_name") as String)
        } else null;
        return Item(
            id = source.get("id") as Long,
            productId =  source.get("product_id") as Long,
            locationId =  source.get("location_id") as Long,
            count = source.get("count") as Int,
            product = product,
            location = location
        )
    }
}

@ReadingConverter
object VectorFloatConverter : Converter<Vector, FloatArray> {
    override fun convert(source: Vector): FloatArray {
        return source.vector;
    }
}