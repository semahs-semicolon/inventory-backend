package io.seda.inventory.controller

import io.seda.inventory.data.Location
import io.seda.inventory.services.ItemService
import io.seda.inventory.services.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import io.seda.inventory.services.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/locations")
class LocationController {
    @Autowired
    lateinit var locationService: LocationService;
    @Autowired
    lateinit var itemService: ItemService;

    @GetMapping("")
    suspend fun getAllLocations(): List<LocationService.TreeLocation> {
        return locationService.getLocations();
    }

    data class LocationCreateRequest(val name: String, val x: Int, val y: Int, val width: Int, val height: Int, val parent: String)

    @PostMapping("")
    suspend fun createLocation(request: LocationCreateRequest): LocationService.SimpleLocationWithLocation {
        return locationService.createLocation(
            parentId = request.parent,
            x = request.x,
            y = request.y,
            width = request.width,
            height = request.height,
            name = request.name
        )
    }

    data class LocationNameChangeRequest(val name: String)

    @PutMapping("/{id}/name")
    suspend fun changeLocationName(@PathVariable("id") id: String, request: LocationNameChangeRequest): LocationService.SimpleLocationWithLocation {
        return locationService.renameLocation(id, request.name);
    }

    @DeleteMapping("/{id}")
    suspend fun deleteLocation(@PathVariable("id") id: String) {
        return locationService.deleteLocation(id);
    }

    @GetMapping("/{id}/items")
    suspend fun getItems(@PathVariable("id") id: String): List<ItemService.InjectableItem> {
        return itemService.findItemsByLocation(id)
    }

    @PatchMapping("/layout")
    suspend fun updateLayout(request: List<LocationService.LayoutUpdateRequest>): List<LocationService.TreeLocation> {
        return locationService.updateLayout(request);
    }

}