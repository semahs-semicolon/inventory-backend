package io.seda.inventory.controller

import io.seda.inventory.services.ImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/images")
class ImageController {
    @Autowired lateinit var imageService: ImageService;
//    @GetMapping("/{id}")
//    suspend fun getImage(@PathVariable id: String): Resource {
//        return imageService.getImage(id);
//    }

//    @PostMapping("", consumes = ["image/png", "image/jpeg", "image/gif", "image/jpg", "image/webp"])
    @PostMapping("")
    suspend fun uploadImage(@RequestPart("image") filePart: Part): ImageService.StoredImage {
        return imageService.uploadImage(filePart as FilePart);
    }
}