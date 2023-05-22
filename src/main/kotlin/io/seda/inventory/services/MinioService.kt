package io.seda.inventory.services

import com.fasterxml.jackson.annotation.JsonIgnore
import io.seda.inventory.exceptions.UploadFailedException
import lombok.Data
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Service
class MinioService {
    @Value("\${image.bucket}")
    lateinit var bucket: String

    @Autowired
    lateinit var s3Client: S3AsyncClient


    @Data
    class UploadState {
        var bucket: String? = null
        var fileKey: String? = null
        var uploadId: String? = null
        var partcounter = 0
        var len = 0
        var completedPartMap: MutableMap<Int, CompletedPart> = HashMap()
        var buffered = 0
    }

    @Data
    class SignedPUTRequest {
        private val url: String? = null
        private val method: String? = null

        @JsonIgnore
        private val onUploadComplete: Mono<Long>? = null
    }

    fun getSizeOfObject(name: String?): Mono<Long> {
        val completableFuture = s3Client!!.headObject(
            HeadObjectRequest.builder()
                .bucket(bucket)
                .key(name).build()
        )
        return Mono.fromFuture(completableFuture)
            .map { obj: HeadObjectResponse -> obj.contentLength() }
    }

    fun upload(file: FilePart, name: String?): Mono<UploadState> {
        val uploadResp = s3Client.createMultipartUpload(
            CreateMultipartUploadRequest.builder()
                .contentType((if (file.headers().contentType == null) MediaType.APPLICATION_OCTET_STREAM else file.headers().contentType).toString())
                .bucket(bucket)
                .key(name).build()
        )
        val uploadState = UploadState()
        uploadState.bucket = bucket
        uploadState.fileKey = name
        return Mono.fromFuture(uploadResp)
            .flatMapMany { resp: CreateMultipartUploadResponse ->
                if (resp.sdkHttpResponse() == null || !resp.sdkHttpResponse().isSuccessful) {
                    throw UploadFailedException(resp)
                }
                uploadState.uploadId = resp.uploadId()
                file.content()
            }.bufferUntil { buffer: DataBuffer ->
                uploadState.buffered += buffer.readableByteCount()
                uploadState.buffered >= 1000000000L
            }
            .flatMap { buffers: List<DataBuffer> -> DataBufferUtils.join(Flux.fromIterable(buffers.toMutableList())) }
            .flatMap { buffer: DataBuffer ->
                val partNumber = ++uploadState.partcounter
                uploadState.len += buffer.readableByteCount()
                val request = s3Client!!.uploadPart(
                    UploadPartRequest.builder()
                        .bucket(uploadState.bucket)
                        .key(uploadState.fileKey)
                        .partNumber(partNumber)
                        .uploadId(uploadState.uploadId)
                        .contentLength(buffer.readableByteCount().toLong()).build(),
                    AsyncRequestBody.fromPublisher(Mono.just(buffer.asByteBuffer()))
                )
                Mono.fromFuture(request)
                    .map { uploadFileResult: UploadPartResponse ->
                        if (uploadFileResult.sdkHttpResponse() == null || !uploadFileResult.sdkHttpResponse().isSuccessful) {
                            throw UploadFailedException(uploadFileResult)
                        }
                        CompletedPart.builder()
                            .eTag(uploadFileResult.eTag())
                            .partNumber(partNumber)
                            .build()
                    }
            }
            .reduce(uploadState) { state: UploadState, completedPart: CompletedPart ->
                state.completedPartMap[completedPart.partNumber()] = completedPart
                state
            }.flatMap { state: UploadState ->
                val multipartUpload = CompletedMultipartUpload.builder()
                    .parts(state.completedPartMap.values)
                    .build()
                Mono.fromFuture(
                    s3Client!!.completeMultipartUpload(
                        CompleteMultipartUploadRequest.builder()
                            .bucket(state.bucket)
                            .uploadId(state.uploadId)
                            .multipartUpload(multipartUpload)
                            .key(state.fileKey).build()
                    )
                )
            }.map { resp: CompleteMultipartUploadResponse ->
                if (resp.sdkHttpResponse() == null || !resp.sdkHttpResponse().isSuccessful) {
                    throw UploadFailedException(resp)
                }
                uploadState
            }
    }
}