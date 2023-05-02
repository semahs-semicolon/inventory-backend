package io.seda.inventory.controller

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

@RestController
@RequestMapping("/users")
class UserController {
    @Autowired
    lateinit var userService: UserService;

    data class RegistrationRequest(val username: String, val password: String, val nickname: String);

    @PostMapping("/signup", consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun signup(@RequestBody registrationRequest: RegistrationRequest): String {
        return userService.register(registrationRequest.username, registrationRequest.password, registrationRequest.nickname,);
    }


    data class NicknameChangeRequest(val nickname: String);
    @PutMapping("/nickname")
    @PreAuthorize("isAuthenticated()")
    suspend fun changeNickname(@RequestBody nicknameChangeRequest: NicknameChangeRequest): String {
        return userService.changeName(nicknameChangeRequest.nickname);
    }

    data class PasswordChangeRequest(val oldPassword: String, val newPassword: String);

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    suspend fun changePassword(@RequestBody passwordChangeRequest: PasswordChangeRequest) {
        userService.changePassword(passwordChangeRequest.oldPassword, passwordChangeRequest.newPassword);
    }
}