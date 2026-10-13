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
import org.example.storage.exposed.repository.impl.ScheduleRepositoryImpl
import org.example.storage.exposed.repository.impl.UserRepositoryImpl
import org.example.web.dto.ErrorResponse
import org.example.web.dto.ScheduleDto
import org.example.web.dto.toDto
import org.example.web.dto.toModel
import java.time.LocalDate

fun Route.scheduleRoutes() {
    authenticate("auth-jwt") {
        route("/api/schedules") {
            get {
                val dateParam = call.request.queryParameters["date"]
                val schedules = if (dateParam != null) {
                    ScheduleRepositoryImpl.getInstance().findAllByDate(LocalDate.parse(dateParam).atStartOfDay())
                } else {
                    ScheduleRepositoryImpl.getInstance().findAll()
                }
                call.respond(schedules.map { it.toDto() })
            }

            post {
                val dto = call.receive<ScheduleDto>()
                val user = UserRepositoryImpl.getInstance().findById(dto.userId.toInt())
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Пользователь ${dto.userId} не найден")
                    )
                call.respond(HttpStatusCode.Created, ScheduleRepositoryImpl.getInstance().create(dto.toModel(user)).toDto())
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                val dto = call.receive<ScheduleDto>()
                val user = UserRepositoryImpl.getInstance().findById(dto.userId.toInt())
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Пользователь ${dto.userId} не найден")
                    )
                call.respond(ScheduleRepositoryImpl.getInstance().update(dto.toModel(user, id)).toDto())
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный id"))
                ScheduleRepositoryImpl.getInstance().delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
