package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.JadwalKuliahRequest
import org.delcom.data.MetaResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IJadwalKuliahRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class JadwalKuliahService(
    private val userRepo    : IUserRepository,
    private val jadwalRepo  : IJadwalKuliahRepository,
) {
    suspend fun getAll(call: ApplicationCall) {
        val user     = ServiceHelper.getAuthUser(call, userRepo)
        val search   = call.request.queryParameters["search"]   ?: ""
        val hari     = call.request.queryParameters["hari"]     ?: ""
        val semester = call.request.queryParameters["semester"] ?: ""
        val page     = (call.request.queryParameters["page"]    ?: "1").toIntOrNull() ?: 1
        val perPage  = (call.request.queryParameters["perPage"] ?: "10").toIntOrNull() ?: 10

        val all   = jadwalRepo.getAll(user.id, search, hari, semester)
        val total = all.size
        val last  = if (total == 0) 1 else ((total + perPage - 1) / perPage)
        val items = all.drop((page - 1) * perPage).take(perPage)

        call.respond(DataResponse(
            "success",
            "Berhasil mengambil daftar jadwal kuliah",
            mapOf(
                "jadwalKuliahs" to items,
                "meta" to MetaResponse(total, perPage, page, last)
            )
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID jadwal tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val data = jadwalRepo.getById(id)
        if (data == null || data.userId != user.id) throw AppException(404, "Jadwal kuliah tidak ditemukan!")
        call.respond(DataResponse("success", "Berhasil mengambil data jadwal kuliah", mapOf("jadwalKuliah" to data)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<JadwalKuliahRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("mataKuliah", "Mata kuliah tidak boleh kosong")
        v.required("dosen",      "Dosen tidak boleh kosong")
        v.required("hari",       "Hari tidak boleh kosong")
        v.required("jamMulai",   "Jam mulai tidak boleh kosong")
        v.required("jamSelesai", "Jam selesai tidak boleh kosong")
        v.required("ruangan",    "Ruangan tidak boleh kosong")
        v.validate()

        val newId = jadwalRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan jadwal kuliah", mapOf("jadwalKuliahId" to newId)))
    }

    suspend fun put(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID jadwal tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<JadwalKuliahRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("mataKuliah", "Mata kuliah tidak boleh kosong")
        v.required("dosen",      "Dosen tidak boleh kosong")
        v.required("hari",       "Hari tidak boleh kosong")
        v.required("jamMulai",   "Jam mulai tidak boleh kosong")
        v.required("jamSelesai", "Jam selesai tidak boleh kosong")
        v.required("ruangan",    "Ruangan tidak boleh kosong")
        v.validate()

        val old = jadwalRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Jadwal kuliah tidak ditemukan!")
        request.cover = old.cover

        if (!jadwalRepo.update(user.id, id, request.toEntity()))
            throw AppException(400, "Gagal memperbarui jadwal kuliah!")

        call.respond(DataResponse("success", "Berhasil mengubah jadwal kuliah", null))
    }

    suspend fun putCover(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID jadwal tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        var coverPath: String? = null
        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext  = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val path = "uploads/jadwal-kuliahs/${UUID.randomUUID()}$ext"
                withContext(Dispatchers.IO) {
                    val file = File(path); file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    coverPath = path
                }
            }
            part.dispose()
        }

        if (coverPath == null || !File(coverPath!!).exists())
            throw AppException(400, "Cover gagal diunggah!")

        val old = jadwalRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Jadwal kuliah tidak ditemukan!")

        val req = JadwalKuliahRequest(
            userId = user.id, mataKuliah = old.mataKuliah, dosen = old.dosen,
            hari = old.hari, jamMulai = old.jamMulai, jamSelesai = old.jamSelesai,
            ruangan = old.ruangan, semester = old.semester, keterangan = old.keterangan,
            cover = coverPath,
        )
        if (!jadwalRepo.update(user.id, id, req.toEntity()))
            throw AppException(400, "Gagal memperbarui cover!")

        old.cover?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil mengubah cover jadwal kuliah", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID jadwal tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val old = jadwalRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Jadwal kuliah tidak ditemukan!")

        if (!jadwalRepo.delete(user.id, id)) throw AppException(400, "Gagal menghapus jadwal kuliah!")

        old.cover?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil menghapus jadwal kuliah", null))
    }

    suspend fun getCover(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID jadwal tidak valid!")
        val data = jadwalRepo.getById(id) ?: return call.respond(HttpStatusCode.NotFound)
        val file = data.cover?.let { File(it) } ?: throw AppException(404, "Cover belum tersedia")
        if (!file.exists()) throw AppException(404, "File cover tidak ditemukan")
        call.respondFile(file)
    }
}
