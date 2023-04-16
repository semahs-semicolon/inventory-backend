package io.seda.inventory.services

import io.seda.inventory.data.Item
import io.seda.inventory.data.ItemRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ItemService {
    @Autowired lateinit var itemRepository: ItemRepository;

    @Autowired lateinit var productService: ProductService;
    @Autowired lateinit var locationService: LocationService;

    data class InjectableItem(val id: String, val productId: String, val locationId: String, val count: Int, var product: ProductService.SimpleProduct?, var location: LocationService.SimpleLocation?);

    suspend fun InjectableItem.lookupProduct(): InjectableItem {
        product = productService.getSimpleProduct(productId)
        return this
    }
    suspend fun InjectableItem.lookupLocation(): InjectableItem {
        location = locationService.getSimpleLocation(locationId);
        return this
    }

    fun Item.toInjectableItem(): InjectableItem {
        return InjectableItem(id?.toULong().toString(), productId.toULong().toString(), locationId.toULong().toString(), count,
            product, location);
    }


    suspend fun createItem(productId: String, locationId: String, count: Int): InjectableItem {
        if (!productService.exists(productId.toULong().toLong())) throw NotFoundException("Product with id $productId not found");
        if (!locationService.locationExists(locationId.toULong().toLong())) throw NotFoundException("Location with id $locationId not found")
        require(count > 0) {"Count must be positive"}

        var item: Item = Item(productId = productId.toULong().toLong(), locationId = locationId.toULong().toLong(), count = count)
        item = itemRepository.save(item);
        return item.toInjectableItem()
            .lookupProduct()
            .lookupLocation();
    }

    suspend fun deleteItem(id: String) {
        if (!itemRepository.existsById(id.toULong().toLong())) throw NotFoundException("Item with id $id not found")
        return itemRepository.deleteById(id.toULong().toLong());
    }

    suspend fun setCount(id: String, count: Int): InjectableItem {
        var item = itemRepository.findById(id.toULong().toLong()) ?: throw NotFoundException("Item with id $id not found")
        require(count > 0) { "Count must be positive"}
        item.count = count;
        item = itemRepository.save(item);
        return item.toInjectableItem();
    }

    suspend fun findItemsByLocation(locationId: String): List<InjectableItem> {
        return itemRepository.findAllByLocation(location = locationId.toULong().toLong()).map { it.toInjectableItem() }
            .toList();
    }

    suspend fun findItemsByProduct(productId: String): List<InjectableItem> {
        return itemRepository.findAllByProduct(product = productId.toULong().toLong()).map { it.toInjectableItem() }
            .toList();
    }

}