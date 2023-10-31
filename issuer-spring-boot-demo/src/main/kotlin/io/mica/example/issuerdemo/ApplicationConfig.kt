package io.mica.example.issuerdemo

import io.grpc.Grpc
import io.grpc.TlsChannelCredentials
import io.mica.sdk.utils.buildGrpcMtlsCredentials
import io.mica.serviceprovider.administration.v1.ServiceProviderAdministrationServiceGrpcKt
import io.mica.serviceprovider.service.v1.ServiceProviderToMicaServiceGrpcKt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Base64

@Configuration
@EnableConfigurationProperties(MicaClientConfig::class)
class ApplicationConfig() {

    private val b64decoder = Base64.getDecoder()

    @Bean
    fun buildMicaClient(micaClientConfig: MicaClientConfig) : ServiceProviderToMicaServiceGrpcKt.ServiceProviderToMicaServiceCoroutineStub {
        logger.info("Initializing Mica Service Provider Client for host: ${micaClientConfig.host} and port ${micaClientConfig.port}")
        val rootCAPem = b64decoder.decode(micaClientConfig.rootCAPEM)
        val clientCertificatePem = b64decoder.decode(micaClientConfig.clientCertificatePEM)
        val clientKeyPem = b64decoder.decode(micaClientConfig.clientKeyPEM)
        val tlsCrendentials = TlsChannelCredentials.newBuilder().let {
            it.trustManager(rootCAPem.inputStream()).keyManager(clientCertificatePem.inputStream(), clientKeyPem.inputStream()).build()
        }
        val micaClient = Grpc.newChannelBuilderForAddress(micaClientConfig.host, micaClientConfig.port, tlsCrendentials)
            .build().let {
                ServiceProviderToMicaServiceGrpcKt.ServiceProviderToMicaServiceCoroutineStub(it)
            }
        return micaClient
    }

    @Bean
    fun buildMicaAdminClient(micaClientConfig: MicaClientConfig) : ServiceProviderAdministrationServiceGrpcKt.ServiceProviderAdministrationServiceCoroutineStub{
        logger.info("Initializing Mica Admin Client for host: ${micaClientConfig.host} and port ${micaClientConfig.port}")
        val rootCAPem = b64decoder.decode(micaClientConfig.rootCAPEM)
        val clientCertificatePem = b64decoder.decode(micaClientConfig.clientCertificatePEM)
        val clientKeyPem = b64decoder.decode(micaClientConfig.clientKeyPEM)
        val tlsCrendentials = TlsChannelCredentials.newBuilder().let {
            it.trustManager(rootCAPem.inputStream()).keyManager(clientCertificatePem.inputStream(), clientKeyPem.inputStream()).build()
        }
        val micaClient = Grpc.newChannelBuilderForAddress(micaClientConfig.host, micaClientConfig.port, tlsCrendentials)
            .build().let {
                ServiceProviderAdministrationServiceGrpcKt.ServiceProviderAdministrationServiceCoroutineStub(it)
            }
        return micaClient
    }

    companion object{
        private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)
    }
}