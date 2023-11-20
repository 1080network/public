package io.mica.example.issuerdemo.datalayer

import java.time.LocalDate
import java.util.Locale.IsoCountryCode
import java.util.UUID


data class UserData(
    val id:String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,
    val addressLine1: String,
    val addressLine2: String = "",
    val city: String,
    val state: String,
    val country: String,
    val dateOfBirth: LocalDate,
    var micaIdKey : String
)

data class UserAccount(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val shortName: String,
    val balance: Long = 10000,
    var micaIdKey: String,
)