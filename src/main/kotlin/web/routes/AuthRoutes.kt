package org.example.web.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.web.dto.ErrorResponse
import org.example.web.dto.LoginRequest
import org.example.web.dto.LoginResponse
import org.example.web.security.JwtConfig

fun Route.authRoutes() {
    val adminUsername = System.getenv("ADMIN_USERNAME") ?: "admin"
    val adminPassword = System.getenv("ADMIN_PASSWORD") ?: "admin"

    post("/api/auth/login") {
        val request = call.receive<LoginRequest>()
        if (request.username == adminUsername && request.password == adminPassword) {
            call.respond(LoginResponse(JwtConfig.generateToken(request.username)))
        } else {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Неверный логин или пароль"))
        }
    }
}
