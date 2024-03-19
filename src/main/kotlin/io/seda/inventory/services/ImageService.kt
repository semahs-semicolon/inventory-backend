package io.seda.inventory.services

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.File

@Service
class ImageService {
    @Autowired
    lateinit var minioService: MinioService;
    data class StoredImage(val id: String, val url: String, val mime: String?, val size: Long);
    suspend fun uploadImage(part: FilePart): StoredImage {
        val id = System.currentTimeMillis().toULong().toString();

        minioService.upload(part, id).awaitSingleOrNull();

        val size = minioService.getSizeOfObject(id).awaitSingle();
        val mime = part.headers().contentType?.type;

        return StoredImage(id, "https://inventory.seda.club/s3/images/${id}", mime, size);
    }

//    suspend fun getImage(id: String): Resource {
//        if (id.contains("/") || id.contains(".")) throw IllegalArgumentException("Smh");
//        return FileSystemResource("$path/$id")
//    }
}