package io.seda.inventory.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.r2dbc.postgresql.codec.Json
import io.seda.inventory.data.Location
import io.seda.inventory.data.LocationRepository
import io.seda.inventory.exceptions.NotFoundException
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.stream.Stream

@Service
class LocationService {
    @Autowired lateinit var locationRepository: LocationRepository;

    @Autowired lateinit var objectMapper: ObjectMapper;
    open class SimpleLocation(
        val id: String,
        val name: String
    );

    class SimpleLocationWithLocation(
        id: String,
        name: String,
        val parent: String?,
        val metadata: Map<String, Any>
    ): SimpleLocation(id, name);

    fun Location.toSimpleLocation(): SimpleLocationWithLocation {
        return SimpleLocationWithLocation(id?.toULong().toString(), name, parentId?.toULong()?.toString(), metadata = objectMapper.readValue(metadata.asString()))
    }

    fun SimpleLocationWithLocation.toTreeLocation(): TreeLocation {
        return TreeLocation(id, name, mutableListOf(), metadata = metadata);
    }


    data class TreeLocation(
        val id: String,
        val name: String,
        val children: MutableList<TreeLocation>,
        val metadata: Map<String, Any>
    )

    private suspend fun createLocationMap(): Pair<Map<String, TreeLocation>, Set<String>> {

        val locationMap = locationRepository.findAll()
            .map { it.toSimpleLocation() }
            .map { it.id to it }
            .toList()
            .toMap();

        val realTreeLocationMap = locationMap.mapValues { it.value.toTreeLocation() }

        locationMap.values.forEach {
            if (it.parent != null)
                (realTreeLocationMap[it.parent] ?: throw IllegalStateException("Parent node disappeared: ${it.parent}")).children.add(realTreeLocationMap[it.id] ?: throw IllegalStateException("Node disappeared: ${it.id}"))
        }
        return Pair(realTreeLocationMap, locationMap.filterValues {it.parent == null}.map { it.key }.toSet());
    }

    suspend fun getLocations(): List<TreeLocation> {
        val (locationMap, root) =  createLocationMap()
        return locationMap.filterValues { root.contains(it.id) }
            .map { it.value }
    }

    suspend fun getLocation(locationId: String): TreeLocation {
        val (locationMap, root) =  createLocationMap()
        return locationMap.get(locationId) ?: throw NotFoundException("Location with id $locationId not found");
    }

    suspend fun getSimpleLocation(locationId: String): SimpleLocationWithLocation {
        return locationRepository.findById(locationId.toULong().toLong())?.toSimpleLocation() ?: throw NotFoundException("Loaction with id $locationId not found")
    }

    suspend fun createLocation(parentId: String?, name: String, metadata: JsonNode): SimpleLocationWithLocation {

        var location = Location(name = name, parentId = parentId?.toULong()?.toLong(), metadata = Json.of(objectMapper.writeValueAsString(metadata)))
        location = locationRepository.save(location);

        return location.toSimpleLocation()
    }

    suspend fun renameLocation(locationId: String, name: String): SimpleLocationWithLocation {
        var loc = locationRepository.findById(locationId.toULong().toLong()) ?: throw NotFoundException("Location with id $locationId not found");
        loc.name = name;
        loc = locationRepository.save(loc);
        return loc.toSimpleLocation()
    }

    suspend fun deleteLocation(locationId: String) {
        var loc = locationRepository.findById(locationId.toULong().toLong()) ?: throw NotFoundException("Location with id $locationId not found")
        locationRepository.delete(loc) // TODO: database side, setup foreign key CONSTRAINT.
    }


    suspend fun updateParent(locationId: String, parentId: String?): SimpleLocationWithLocation {
        var loc = locationRepository.findById(locationId.toULong().toLong()) ?: throw NotFoundException("Location with id $locationId not found");
        val parent = locationRepository.findById(parentId?.toULong()?.toLong() ?: 0);
        loc.parentId = parent?.id;
        loc = locationRepository.save(loc);
        return loc.toSimpleLocation()
    }

    suspend fun updateMetadata(locationId: String, metadata: JsonNode): SimpleLocationWithLocation {
        var loc = locationRepository.findById(locationId.toULong().toLong()) ?: throw NotFoundException("Location with id $locationId not found");
        loc.metadata = Json.of(objectMapper.writeValueAsString(metadata))
        loc = locationRepository.save(loc)
        return loc.toSimpleLocation();
    }

    suspend fun locationExists(locationId: Long): Boolean {
        return locationRepository.existsById(locationId)
    }
}