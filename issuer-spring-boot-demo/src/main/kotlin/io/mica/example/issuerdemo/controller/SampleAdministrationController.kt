package io.mica.example.issuerdemo.controller

import io.mica.serviceprovider.administration.v1.ServiceProviderAdministrationServiceGrpcKt
import io.mica.serviceprovider.service.v1.ServiceProviderToMicaServiceGrpcKt
import io.micashared.common.ping.v1.PingProto.PingResponse
import io.micashared.common.ping.v1.pingRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class SampleAdministrationController(
    val adminClient: ServiceProviderAdministrationServiceGrpcKt.ServiceProviderAdministrationServiceCoroutineStub,
    val serviceClient: ServiceProviderToMicaServiceGrpcKt.ServiceProviderToMicaServiceCoroutineStub
) {

    @GetMapping("/ping", produces = ["text/plain"])
    suspend fun simplePing(): String = serviceClient.ping(pingRequest {  }).let {
        when (it.status){
            PingResponse.Status.STATUS_SUCCESS -> "${it.status.name}\n buildSha: ${it.buildSha}\n buildVersion${it.buildVersion}"
            else -> "${it.status.name}"
        }
    }

    @GetMapping("/pingexternal", produces = ["text/plain"])
    suspend fun pingExternal(): String = adminClient.pingExternal(pingRequest { }).let {
        when (it.status){
            PingResponse.Status.STATUS_SUCCESS -> "${it.status.name}\n buildSha: ${it.buildSha}\n buildVersion${it.buildVersion}"
            else -> "${it.status.name}"
        }
    }

}