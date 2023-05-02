package io.seda.inventory.services

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.File

@Service
class ImageService {
    @Value("\${image.path}")
    lateinit var path: String;
    data class StoredImage(val id: String, val url: String, val mime: String?, val size: Long);
    suspend fun uploadImage(part: FilePart): StoredImage {
        val id = System.currentTimeMillis().toULong().toString();

        part.transferTo(File(path, id+"")).awaitSingleOrNull();
        val file = File(path, id+"");
        val size = file.length();
        val mime = part.headers().contentType?.type;

        return StoredImage(id, "http://localhost:8080/images/${id}", mime, size);
    }

    suspend fun getImage(id: String): Resource {
        if (id.contains("/") || id.contains(".")) throw IllegalArgumentException("Smh");
        return FileSystemResource("$path/$id")
    }
}