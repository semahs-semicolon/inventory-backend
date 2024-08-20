package io.seda.inventory.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table(name = "verify_codes")
data class VerifyCode(
    @Id
    var code: String,
    var identifier: String,
    var authority: List<String> = listOf()
)

interface VerifyCodeRepository: CoroutineCrudRepository<VerifyCode, String> {
    suspend fun findByCode(code: String): VerifyCode?;
    suspend fun findByIdentifier(identifier: String): VerifyCode?;
}
