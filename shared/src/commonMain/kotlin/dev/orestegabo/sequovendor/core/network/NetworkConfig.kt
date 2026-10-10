package dev.orestegabo.sequovendor.core.network

object NetworkConfig {
    const val ProductionBaseUrl = "https://api.sequoservice.com"
    const val ApiBasePath = "/api"

    fun apiUrl(path: String, baseUrl: String = ProductionBaseUrl): String {
        val cleanPath = path.trimStart('/')
        return "${baseUrl.trimEnd('/')}$ApiBasePath/$cleanPath"
    }
}
