package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.JadwalKuliahService
import org.delcom.services.KegiatanService
import org.delcom.services.AuthService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService        : AuthService         by inject()
    val userService        : UserService         by inject()
    val jadwalKuliahService: JadwalKuliahService by inject()
    val kegiatanService    : KegiatanService     by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(
                HttpStatusCode.fromValue(cause.code),
                ErrorResponse(
                    status  = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data    = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.fromValue(500),
                ErrorResponse("error", cause.message ?: "Unknown error", "")
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Kampus Manager API – by IFS23026")
        }

        // ── Auth ──────────────────────────────────────────────────────────
        route("/auth") {
            post("/login")         { authService.postLogin(call) }
            post("/register")      { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout")        { authService.postLogout(call) }
        }

        authenticate(JWTConstants.NAME) {
            // ── Users ──────────────────────────────────────────────────────
            route("/users") {
                get("/me")           { userService.getMe(call) }
                put("/me")           { userService.putMe(call) }
                put("/me/password")  { userService.putMyPassword(call) }
                put("/me/photo")     { userService.putMyPhoto(call) }
            }

            // ── Jadwal Kuliah ──────────────────────────────────────────────
            route("/jadwal-kuliahs") {
                get                  { jadwalKuliahService.getAll(call) }
                post                 { jadwalKuliahService.post(call) }
                get("/{id}")         { jadwalKuliahService.getById(call) }
                put("/{id}")         { jadwalKuliahService.put(call) }
                put("/{id}/cover")   { jadwalKuliahService.putCover(call) }
                delete("/{id}")      { jadwalKuliahService.delete(call) }
            }

            // ── Kegiatan ───────────────────────────────────────────────────
            route("/kegiatans") {
                get                  { kegiatanService.getAll(call) }
                post                 { kegiatanService.post(call) }
                get("/{id}")         { kegiatanService.getById(call) }
                put("/{id}")         { kegiatanService.put(call) }
                put("/{id}/cover")   { kegiatanService.putCover(call) }
                delete("/{id}")      { kegiatanService.delete(call) }
            }
        }

        // ── Public image endpoints ─────────────────────────────────────────
        route("/images") {
            get("users/{id}")          { userService.getPhoto(call) }
            get("jadwal-kuliahs/{id}") { jadwalKuliahService.getCover(call) }
            get("kegiatans/{id}")      { kegiatanService.getCover(call) }
        }
    }
}
