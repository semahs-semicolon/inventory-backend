package io.seda.inventory.services

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

    open class SimpleLocation(
        val id: String,
        val name: String
    );

    class SimpleLocationWithLocation(
        id: String,
        name: String,
        val x: Int, val y: Int, val width: Int, val height: Int,
        val parent: String?
    ): SimpleLocation(id, name);

    fun Location.toSimpleLocation(): SimpleLocationWithLocation {
        return SimpleLocationWithLocation(id?.toULong().toString(), name, x ,y, width, height, parentId?.toULong()?.toString())
    }

    fun SimpleLocationWithLocation.toTreeLocation(): TreeLocation {
        return TreeLocation(id, name, x, y, width, height, mutableListOf());
    }


    data class TreeLocation(
        val id: String,
        val name: String,
        val x: Int, val y: Int, val width: Int, val height: Int,
        val children: MutableList<TreeLocation>
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

    suspend fun createLocation(parentId: String, x: Int, y: Int, width: Int, height: Int, name: String): SimpleLocationWithLocation {
        require(width > 0) {"Width should be positive"}
        require( height > 0) {"Height should be positive"}
        require(x >= 0) {"X should be non negative"}
        require(y >= 0) {"Y should be non negative"}

        var location = Location(x = x, y = y, width = width, height = height, name = name, parentId = parentId.toULong().toLong())
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

    data class LayoutUpdateRequest(val locationId: String, val parentId: String?, val x: Int?, val y: Int?, val width: Int?, val height: Int?) {
        init {
            require(width == null || width > 0) {"Width should be non zero"}
            require(height == null || height > 0) {"Height should be non zero"}
            require(x == null || x >= 0) {"X should be non negative"}
            require(y == null || y >= 0) {"Y should be non negative"}
            require(Stream.of(x, y, width, height)
                .allMatch {it == null}
                    || Stream.of(x, y, width, height)
                .allMatch {it != null}) {"Dimensional Fields should be all null or all non-null"}
        }
    }

    suspend fun updateLayout(requests: List<LayoutUpdateRequest>): List<TreeLocation> {
        val requestIds = requests.map { it.locationId.toULong().toLong() }.toSet()
        val parentIds = requests.mapNotNull { it.parentId?.toULong()?.toLong() }.toSet()

        val checkExist = listOf(requestIds, parentIds).flatten().map { it.toULong().toLong() }.toSet()

        val parentList = locationRepository.findAllById(checkExist)
            .map { it.id to it }
            .toList().toMap()
        val parentIdList = parentList.map { it.value.id }.toSet()
        val shouldBeEmpty = checkExist.filter { parentIdList.contains(it) }
        require(shouldBeEmpty.isEmpty()) {"Some of Ids do not exist: ${shouldBeEmpty}"}

        val editing = parentList.filter { requestIds.contains(it.key) }

        requests.forEach {
            var request = editing[it.locationId.toULong().toLong()] !!
            request.parentId = it.parentId?.toULong()?.toLong();

            if (it.x != null && it.y != null && it.width != null && it.height != null) {
                request.x = it.x;
                request.y = it.y;
                request.width = it.width;
                request.height = it.height;
            }
        }

        locationRepository.saveAll(editing.values);

        val toValidate = locationRepository.findAll()
            .map { it.toSimpleLocation() }
            .map { it.id to it }
            .toList()
            .toMap();

        requests.forEach {
            var current = checkNotNull(toValidate[it.locationId]) {"Missing node? ${it.locationId} (Start of circular check)"}
            var chain = "";
            while(true) {
                if (current.parent == null) break
                chain = "${current.id} - $chain"
                check(current.parent == it.locationId) {"Circular Location Detected!!! $chain"}
                current = checkNotNull(toValidate[current.parent]) {"Missing node? ${current.parent} Current chain: $chain"}
            }
        }

        return getLocations();
    }

    suspend fun locationExists(locationId: Long): Boolean {
        return locationRepository.existsById(locationId)
    }
}