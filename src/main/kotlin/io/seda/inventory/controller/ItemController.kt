package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class ItemController {
    @Autowired lateinit var itemService: ItemService;

    data class ItemCreationRequest(val productId: String, val locationId: String, val count: Int);
    @PostMapping("")
    suspend fun create(@RequestBody request: ItemCreationRequest): ItemService.InjectableItem {
        return itemService.createItem(productId = request.productId, locationId = request.locationId, count = request.count);
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String) {
        return itemService.deleteItem(id)
    }

    data class SetCountRequest(val count: Int);

    @PutMapping("/{id}/count")
    suspend fun setCount(@PathVariable("id") id: String, @RequestBody request: SetCountRequest): ItemService.InjectableItem {
        return itemService.setCount(id, request.count);
    }

    data class ItemMoveRequest(val itemId: String, val locationId: String, val count: Int);
    @PatchMapping("/move")
    suspend fun move(@RequestBody request: ItemMoveRequest): ItemService.InjectableItem {
        return itemService.move(request.itemId, request.locationId, request.count);
    }
}