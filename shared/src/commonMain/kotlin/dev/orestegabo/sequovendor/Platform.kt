package dev.orestegabo.sequovendor

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform