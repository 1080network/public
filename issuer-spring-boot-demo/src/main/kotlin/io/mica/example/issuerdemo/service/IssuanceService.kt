package io.mica.example.issuerdemo.service

import io.mica.example.issuerdemo.datalayer.InMemoryRepository
import io.mica.serviceprovider.service.v1.ServiceProviderToMicaServiceGrpcKt
import io.mica.serviceprovider.uuek.v1.ProvisionServiceProviderUUEKProto.ProvisionServiceProviderUUEKResponse
import io.mica.serviceprovider.uuek.v1.provisionServiceProviderUUEKRequest

class IssuanceService(val repository: InMemoryRepository, val micaClient : ServiceProviderToMicaServiceGrpcKt.ServiceProviderToMicaServiceCoroutineStub) {

    suspend fun issueUUEK(instrumentId: String): String {
        val request = repository.getAccountById(instrumentId)?.let {
            if (it.micaIdKey.isEmpty()) {
                provisionServiceProviderUUEKRequest {
                    serviceProviderInstrumentRef = instrumentId
                    numberOfUses = 1
                }
            }else{
                provisionServiceProviderUUEKRequest {
                    serviceProviderInstrumentKey = it.micaIdKey
                    numberOfUses = 1
                }
            }
        }?:throw IllegalStateException("instrument does not exist")
        val response = micaClient.provisionServiceProviderUUEK(request)
        if (response.status != ProvisionServiceProviderUUEKResponse.Status.STATUS_SUCCESS) {
            throw IllegalStateException("unable to create UUEK, status= ${response.status}")
        }
        return response.serviceProviderUuek
    }
}