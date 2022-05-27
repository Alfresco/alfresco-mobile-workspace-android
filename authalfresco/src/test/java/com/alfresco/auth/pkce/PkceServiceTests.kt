package com.alfresco.auth.pkce

import com.alfresco.auth.AuthConfig
import org.junit.Before as before
import org.junit.Test as test

class PkceServiceTests {

    private lateinit var config: AuthConfig

    @before fun setUp() {
        config = AuthConfig(
            https = false,
            port = "80",
            clientId = "clientId",
            realm = "realm",
            redirectUrl = "redirectUrl",
            contentServicePath = "path"
        )
    }

    @test fun `endpoint - hostname`() {
        val uri = PkceAuthService.endpointWith("sub.test.com", config)
        assert(uri.toString() == "http://sub.test.com")
    }

    @test fun `endpoint - hostname and port`() {
        val uri = PkceAuthService.endpointWith("sub.test.com:8080", config)
        assert(uri.toString() == "http://sub.test.com:8080")
    }

    @test fun `endpoint - schema, hostname and port`() {
        val uri = PkceAuthService.endpointWith("https://sub.test.com:8080", config)
        assert(uri.toString() == "https://sub.test.com:8080")
    }

    @test fun `endpoint - ip`() {
        val uri = PkceAuthService.endpointWith("127.0.0.1", config)
        assert(uri.toString() == "http://127.0.0.1")
    }

    @test fun `endpoint - ip and port`() {
        val uri = PkceAuthService.endpointWith("127.0.0.1:8080", config)
        assert(uri.toString() == "http://127.0.0.1:8080")
    }

    @test fun `endpoint - hostname and path`() {
        val uri = PkceAuthService.endpointWith("sub.test.com/app", config)
        assert(uri.toString() == "http://sub.test.com/app")
    }

    @test fun `endpoint - hostname with port and path`() {
        val uri = PkceAuthService.endpointWith("sub.test.com:8080/app", config)
        assert(uri.toString() == "http://sub.test.com:8080/app")
    }

    @test fun `endpoint - no authority`() {
        val uri = PkceAuthService.endpointWith("http:/sub.test.com", config)
        assert(uri.toString() == "http://http:/sub.test.com")
    }

    @test fun `endpoint - unsupported schema`() {
        val uri = PkceAuthService.endpointWith("htp://sub.test.com", config)
        assert(uri.toString() == "http://sub.test.com")
    }

    @test fun `endpoint - non-hierarchical`() {
        val uri = PkceAuthService.endpointWith("mailto:sub.test.com", config)
        assert(uri.toString() == "http://mailto:sub.test.com")
    }

    @test fun `endpoint - config schema override`() {
        val uri = PkceAuthService.endpointWith("https://sub.test.com", config)
        assert(uri.toString() == "https://sub.test.com")
    }

    @test fun `endpoint - hostname secure`() {
        config.https = true
        config.port = "443"
        val uri = PkceAuthService.endpointWith("sub.test.com", config)
        assert(uri.toString() == "https://sub.test.com")
    }

    @test fun `endpoint - hostname non-standard port`() {
        config.port = "8080"
        val uri = PkceAuthService.endpointWith("sub.test.com", config)
        assert(uri.toString() == "http://sub.test.com:8080")
    }

    @test fun `endpoint - hostname secure non-standard port`() {
        config.https = true
        val uri = PkceAuthService.endpointWith("sub.test.com", config)
        assert(uri.toString() == "https://sub.test.com:80")
    }
}
