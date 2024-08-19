package io.seda.inventory.controller

import io.seda.inventory.data.Identifier
import io.seda.inventory.data.VerifyCode
import io.seda.inventory.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/verifyCode")
class VerifyCodeController {
    @Autowired lateinit var UserService: UserService;

    @GetMapping("/verifyCode")
    suspend fun getVerifyCodes(): List<VerifyCode>{
        return UserService.findAllVerifyCodes()
    }

    @GetMapping("/verifyCode/{code}")
    suspend fun getVerifyCode(@PathVariable("code") code: String): VerifyCode{
        return UserService.findVerifyCode(code)
    }

    @GetMapping("/identifier")
    suspend fun getVerifyCodeByIdentifier(): List<Identifier>{
        return UserService.findAllIdentifiers()
    }

    @GetMapping("/identifier/{identifier}")
    suspend fun getVerifyCodeByIdentifier(@PathVariable("identifier") identifier: String): Identifier{
        return UserService.findIdentifier(identifier)
    }

    data class CreateVerifyCodeRequest(val identifier: String, val authority: List<String> = listOf())
    @PutMapping("/verifyCode", consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun createVerifyCode(@RequestBody createVerifyCodeRequest: CreateVerifyCodeRequest): VerifyCode{
        return UserService.createVerifyCode(createVerifyCodeRequest.identifier, createVerifyCodeRequest.authority)
    }
    @DeleteMapping("/verifyCode/{code}")
    suspend fun deleteVerifyCode(@PathVariable("code") code: String) {
        UserService.invokeVerifyCode(code)
    }

    data class CreateIdentifierRequest(val metadata: String)
    @PutMapping("/identifier", consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun createIdentifier(@RequestBody createIdentifierRequest: CreateIdentifierRequest): Identifier{
        return UserService.createIdentifier(createIdentifierRequest.metadata)
    }

    @DeleteMapping("/identifier/{identifier}")
    suspend fun deleteIdentifier(@PathVariable("identifier") identifier: String){
        UserService.invokeIdentifier(identifier)
    }
}
