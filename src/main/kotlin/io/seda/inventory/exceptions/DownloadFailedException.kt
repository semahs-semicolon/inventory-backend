package io.seda.inventory.exceptions

import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import software.amazon.awssdk.core.SdkResponse
import java.util.*

@AllArgsConstructor
class DownloadFailedException(response: SdkResponse) : RuntimeException() {
    private var statusCode = 0
    private var statusText: Optional<String>? = null

    init {
        val httpResponse = response.sdkHttpResponse()
        if (httpResponse != null) {
            statusCode = httpResponse.statusCode()
            statusText = httpResponse.statusText()
        } else {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
            statusText = Optional.of("UNKNOWN")
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}