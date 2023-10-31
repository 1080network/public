package io.mica.example.issuerdemo

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="mica")
data class MicaClientConfig(
    val host: String = "localhost",
    val port: Int = 443,
    val clientKeyPEM : String = "",
    val clientCertificatePEM: String = "",
    val rootCAPEM : String = ""
)