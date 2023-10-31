package io.mica.example.issuerdemo.datalayer

import io.mica.example.issuerdemo.controller.Address
import io.mica.example.issuerdemo.controller.User
import java.util.Date
import java.util.Locale.IsoCountryCode


data class UserData(
    val id:String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val state: String,
    val country: IsoCountryCode,
    val dateOfBirth: Date,
    var micaIdKey : String
)