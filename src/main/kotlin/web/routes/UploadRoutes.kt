package org.example.web.routes

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.utils.io.core.readBytes
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.example.excel.ScheduleUploadService
import org.example.web.dto.ErrorResponse
import org.example.web.dto.UploadResult

fun Route.uploadRoutes() {
    authenticate("auth-jwt") {
        post("/api/schedules/upload") {
            val multipart = call.receiveMultipart()
            var fileBytes: ByteArray? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileBytes = part.provider().readBytes()
                }
                part.dispose()
            }

            val bytes = fileBytes ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Файл не передан")
            )

            val result = ScheduleUploadService.replaceSchedulesFromExcel(bytes)
            call.respond(UploadResult(result.createdCount, result.deletedCount))
        }
    }
}
