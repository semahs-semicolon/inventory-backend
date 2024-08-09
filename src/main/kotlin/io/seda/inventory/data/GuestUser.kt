package io.seda.inventory.data

import org.springframework.data.annotation.Id

data class GuestUser(
    @Id
    var id: Long? = null,
    var authority: List<String> = listOf()
)
