package org.example.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.example.web.dto.ErrorResponse
import org.example.web.routes.authRoutes
import org.example.web.routes.scheduleRoutes
import org.example.web.routes.uploadRoutes
import org.example.web.routes.userRoutes
import org.example.web.security.JwtConfig
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
import java.net.URI

private val logger = KotlinLogging.logger {}

object WebServer {
    fun start(wait: Boolean = false) {
        val port = System.getenv("ADMIN_API_PORT")?.toIntOrNull() ?: 8081
        logger.info { "Starting admin API on port $port" }
        embeddedServer(Netty, port = port, module = Application::adminModule).start(wait = wait)
    }
}

fun Application.adminModule() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(CallLogging)

    install(CORS) {
        val origins = (System.getenv("ADMIN_CORS_ORIGINS") ?: "http://localhost:5173")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        origins.forEach { origin ->
            val uri = URI(origin)
            val host = if (uri.port != -1) "${uri.host}:${uri.port}" else uri.host
            allowHost(host, schemes = listOf(uri.scheme))
        }

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Требуется авторизация"))
            }
        }
    }

    install(StatusPages) {
        exception<EntityNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Не найдено"))
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Не найдено"))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Некорректный запрос"))
        }
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception in admin API" }
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(cause.message ?: "Внутренняя ошибка сервера"))
        }
    }

    routing {
        authRoutes()
        userRoutes()
        scheduleRoutes()
        uploadRoutes()
    }
}
