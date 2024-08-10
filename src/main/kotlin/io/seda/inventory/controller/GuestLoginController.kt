package io.seda.inventory.controller

import io.seda.inventory.auth.HttpExceptionFactory
import io.seda.inventory.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/guestLogin")
class GuestLoginController {
    @Autowired lateinit var userService: UserService

    data class GuestLoginRequest(val token: String)

    @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun guestLogin(@RequestBody guestLoginRequest: GuestLoginRequest): String {
        return try {
            if(guestLoginRequest.token.isEmpty()) throw IllegalArgumentException("Token can not be empty")
            userService.guestLogin(guestLoginRequest.token)
        } catch (e: Exception) {
            throw HttpExceptionFactory.badRequest()
        }
    }
}
