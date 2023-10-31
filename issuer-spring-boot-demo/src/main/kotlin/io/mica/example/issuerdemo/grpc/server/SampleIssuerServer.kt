package io.mica.example.issuerdemo.grpc.server

import com.google.protobuf.Timestamp
import io.mica.serviceprovider.service.v1.ServiceProviderFromMicaServiceGrpcKt
import io.micashared.common.ping.v1.PingProto
import io.micashared.common.ping.v1.PingProto.PingResponse
import io.micashared.common.ping.v1.pingResponse
import net.devh.boot.grpc.server.service.GrpcService
import java.time.Instant

@GrpcService
class SampleIssuerServer : ServiceProviderFromMicaServiceGrpcKt.ServiceProviderFromMicaServiceCoroutineImplBase() {

    override suspend fun ping(request: PingProto.PingRequest): PingProto.PingResponse =
         pingResponse {
            status = PingResponse.Status.STATUS_SUCCESS
            buildSha = "SAMPLESHA"
            buildTime = "unknown"
            serverStartTime = startTime
            serverTime = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
        }


    companion object{
        private val startTime: Timestamp = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
    }
}