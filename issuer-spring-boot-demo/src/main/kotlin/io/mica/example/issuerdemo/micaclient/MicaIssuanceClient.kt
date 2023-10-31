package io.mica.example.issuerdemo.micaclient

import io.mica.example.issuerdemo.datalayer.UserData
import io.mica.serviceprovider.service.v1.ServiceProviderToMicaServiceGrpcKt
import io.mica.serviceprovider.user.v1.UserProto.RegisterUserRequest
import io.mica.serviceprovider.user.v1.UserProto.RegisterUserResponse
import io.micashared.common.v1.AddressProto.Address
import io.micashared.common.v1.UserProto.UserDemographic
import org.springframework.stereotype.Component

@Component
class MicaIssuanceClient(val micaClient : ServiceProviderToMicaServiceGrpcKt.ServiceProviderToMicaServiceCoroutineStub) {

    suspend fun RegisterUser(userData: UserData) : String {
        val requestBuilder = RegisterUserRequest.newBuilder().apply {
            this.serviceProviderUserRef = userData.id
        }
        userData.toMicaDemographic()?.apply {
            requestBuilder.userDemographic = this
        }
        val response = micaClient.registerUser(requestBuilder.build())
        if (response.status != RegisterUserResponse.Status.STATUS_SUCCESS){
            throw IllegalStateException("error from api: ${response.status.name}")
        }
        return response.serviceProviderUserKey
    }

}

fun UserData.toMicaDemographic(): UserDemographic? {
    if (this.name.isEmpty() && this.lastName.isEmpty() && this.phoneNumber.isEmpty() && this.addressLine1.isEmpty()
        && this.addressLine2.isEmpty() && this.city.isEmpty() && this.state.isEmpty()) {
        return null
    }
    val demographicBuilder = UserDemographic.newBuilder()
    if (this.name.isNotEmpty()){
        demographicBuilder.firstName = this.name
    }
    if (this.lastName.isNotEmpty()){
        demographicBuilder.lastName = this.lastName
    }
    if (this.phoneNumber.isNotEmpty()){
        demographicBuilder.phone = this.phoneNumber
    }
    if(this.city.isNotEmpty() && this.state.isNotEmpty() && this.addressLine1.isNotEmpty()){
        val protoAddressBuilder = Address.newBuilder()
        protoAddressBuilder.streetLinesList.add(this.addressLine1)
        if (this.addressLine2.isNotEmpty()){
            protoAddressBuilder.streetLinesList.add(this.addressLine2)
        }
        if (this.city.isNotEmpty()) {
            protoAddressBuilder.locality = this.city
        }
        demographicBuilder.address = protoAddressBuilder.build()
    }
    return demographicBuilder.build()
}