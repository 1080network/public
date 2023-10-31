package io.mica.example.issuerdemo.controller

import com.fasterxml.jackson.annotation.JsonInclude
import io.mica.example.issuerdemo.datalayer.UserData
import java.util.Date
import java.util.Locale.IsoCountryCode
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class User(
    val id:String?,
    val name: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val address: Address?,
    val dateOfBirth: Date?
)

data class Address(
    val streetLine1: String?,
    val streetLine2: String?,
    val city: String,
    val state: String,
    val country: String
)



fun User.toUserData():UserData{
    return UserData(
        id = this.id?:UUID.randomUUID().toString(),
        addressLine1 = this.address?.streetLine1 ?: "",
        addressLine2 = this.address?.streetLine2 ?: "",
        name = this.name?: "",
        lastName = this.lastName ?: "",
        phoneNumber = this.phoneNumber ?: "",
        dateOfBirth = this.dateOfBirth ?: Date(0),
        city = this.address?.city ?: "",
        country = IsoCountryCode.valueOf( this.address?.country ?: "US"),
        state = this.address?.state ?: "",
        micaIdKey = "",
    )
}

fun UserData.toUser(): User {
   val address = when{
       this.addressLine1.isNotEmpty() || this.addressLine2.isNotEmpty() || this.city.isNotEmpty() -> Address(this.addressLine1,
           this.addressLine2, this.city,
           this.state,
           this.country.toString())
       else -> null
   }
   return  User(
        id = this.id,
        name = this.name,
        lastName = this.lastName,
        phoneNumber = this.phoneNumber,
        address = address,
        dateOfBirth = this.dateOfBirth
    )
}