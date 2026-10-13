package org.example.web.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.example.storage.exposed.repository.impl.UserRepositoryImpl
import org.example.web.dto.ErrorResponse
import org.example.web.dto.UserDto
import org.example.web.dto.toDto
import org.example.web.dto.toModel

fun Route.userRoutes() {
    authenticate("auth-jwt") {
        route("/api/users") {
            get {
                val users = UserRepositoryImpl.getInstance().findAll().orEmpty()
                call.respond(users.map { it.toDto() })
            }

            post {
                val dto = call.receive<UserDto>()
                val user = dto.toModel() ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Неизвестная группа: ${dto.groupName}")
                )
                call.respond(HttpStatusCode.Created, UserRepositoryImpl.getInstance().create(user).toDto())
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                val dto = call.receive<UserDto>()
                val user = dto.toModel(id) ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Неизвестная группа: ${dto.groupName}")
                )
                call.respond(UserRepositoryImpl.getInstance().update(user).toDto())
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                UserRepositoryImpl.getInstance().delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
