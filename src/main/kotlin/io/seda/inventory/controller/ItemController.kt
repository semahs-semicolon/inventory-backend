package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController {
    @Autowired lateinit var itemService: ItemService;

    data class ItemCreationRequest(val productId: String, val locationId: String, val count: Int);
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun create(@RequestBody request: ItemCreationRequest): ItemService.InjectableItem {
        return itemService.createItem(productId = request.productId, locationId = request.locationId, count = request.count);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun delete(@PathVariable("id") id: String) {
        return itemService.deleteItem(id)
    }

    data class SetCountRequest(val count: Int);

    @PutMapping("/{id}/count")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun setCount(@PathVariable("id") id: String, @RequestBody request: SetCountRequest): ItemService.InjectableItem {
        return itemService.setCount(id, request.count);
    }

    data class ItemMoveRequest(val itemId: String, val locationId: String, val count: Int);
    @PatchMapping("/move")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    suspend fun move(@RequestBody request: ItemMoveRequest): ItemService.InjectableItem {
        return itemService.move(request.itemId, request.locationId, request.count);
    }
}
