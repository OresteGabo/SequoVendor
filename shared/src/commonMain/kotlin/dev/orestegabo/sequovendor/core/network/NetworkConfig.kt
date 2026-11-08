package dev.orestegabo.sequovendor.core.network

object NetworkConfig {
    const val ProductionBaseUrl = "https://api.sequoservice.com"
    const val BaseUrl = GeneratedSequoBuildConfig.ApiBaseUrl
    const val ReleaseId = GeneratedSequoBuildConfig.ReleaseId
    const val Environment = GeneratedSequoBuildConfig.Environment
    const val ApiBasePath = "/api"

    fun apiUrl(path: String, baseUrl: String = BaseUrl): String {
        val cleanPath = path.trimStart('/')
        return "${baseUrl.trimEnd('/')}$ApiBasePath/$cleanPath"
    }
}
