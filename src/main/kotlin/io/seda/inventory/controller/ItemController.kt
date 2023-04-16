package io.seda.inventory.controller

import io.seda.inventory.services.ItemService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/items")
class ItemController {
    // CREATE, DELETE, UPDATE

    @Autowired lateinit var itemService: ItemService;


    data class ItemCreationRequest(val productId: String, val locationId: String, val count: Int);
    @PostMapping("")
    suspend fun create(request: ItemCreationRequest): ItemService.InjectableItem {
        return itemService.createItem(productId = request.productId, locationId = request.locationId, count = request.count);
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: String) {
        return itemService.deleteItem(id)
    }

    data class SetCountRequest(val count: Int);

    @PutMapping("/{id}/count")
    suspend fun setCount(@PathVariable("id") id: String, request: SetCountRequest): ItemService.InjectableItem {
        return itemService.setCount(id, request.count);
    }
}