package io.mica.example.issuerdemo.controller

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class User(
    val id:String?,
    val name: String?,
    val lastName: String?,
    val address: Address,
    val dateOfBirth: EpochDate
)

data class Address(
    val streetLine1: String?,
    val streetLine2: String?,
    val city: String,
    val state: String,
    val country: String
)

data class EpochDate(
    val seconds: Int,
    val nanoSeconds: Int
)