package org.example.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.util.Date

/**
 * Single-admin JWT issuing/verification. There is no admin user table in the
 * database - credentials are compared against ADMIN_USERNAME/ADMIN_PASSWORD env vars.
 */
object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "change-me-in-production"
    private const val issuer = "accounting-chat-manager"
    private const val audience = "accounting-chat-manager-admin"
    private const val validityMs = 12 * 60 * 60 * 1000L

    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(username: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
            .sign(algorithm)
}
