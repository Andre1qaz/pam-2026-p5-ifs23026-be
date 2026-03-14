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
import org.delcom.data.KegiatanRequest
import org.delcom.data.MetaResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IKegiatanRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class KegiatanService(
    private val userRepo     : IUserRepository,
    private val kegiatanRepo : IKegiatanRepository,
) {
    suspend fun getAll(call: ApplicationCall) {
        val user     = ServiceHelper.getAuthUser(call, userRepo)
        val search   = call.request.queryParameters["search"]   ?: ""
        val tanggal  = call.request.queryParameters["tanggal"]  ?: ""
        val kategori = call.request.queryParameters["kategori"] ?: ""
        val page     = (call.request.queryParameters["page"]    ?: "1").toIntOrNull() ?: 1
        val perPage  = (call.request.queryParameters["perPage"] ?: "10").toIntOrNull() ?: 10

        val all   = kegiatanRepo.getAll(user.id, search, tanggal, kategori)
        val total = all.size
        val last  = if (total == 0) 1 else ((total + perPage - 1) / perPage)
        val items = all.drop((page - 1) * perPage).take(perPage)

        call.respond(DataResponse(
            "success",
            "Berhasil mengambil daftar kegiatan",
            mapOf(
                "kegiatans" to items,
                "meta" to MetaResponse(total, perPage, page, last)
            )
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID kegiatan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val data = kegiatanRepo.getById(id)
        if (data == null || data.userId != user.id) throw AppException(404, "Kegiatan tidak ditemukan!")
        call.respond(DataResponse("success", "Berhasil mengambil data kegiatan", mapOf("kegiatan" to data)))
    }

    suspend fun post(call: ApplicationCall) {
        val user    = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<KegiatanRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("judul",    "Judul kegiatan tidak boleh kosong")
        v.required("kategori", "Kategori tidak boleh kosong")
        v.required("tanggal",  "Tanggal tidak boleh kosong")
        v.required("waktu",    "Waktu tidak boleh kosong")
        v.validate()

        val newId = kegiatanRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil menambahkan kegiatan", mapOf("kegiatanId" to newId)))
    }

    suspend fun put(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID kegiatan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<KegiatanRequest>()
        request.userId = user.id

        val v = ValidatorHelper(request.toMap())
        v.required("judul",    "Judul kegiatan tidak boleh kosong")
        v.required("kategori", "Kategori tidak boleh kosong")
        v.required("tanggal",  "Tanggal tidak boleh kosong")
        v.required("waktu",    "Waktu tidak boleh kosong")
        v.validate()

        val old = kegiatanRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Kegiatan tidak ditemukan!")
        request.cover = old.cover

        if (!kegiatanRepo.update(user.id, id, request.toEntity()))
            throw AppException(400, "Gagal memperbarui kegiatan!")

        call.respond(DataResponse("success", "Berhasil mengubah kegiatan", null))
    }

    suspend fun putCover(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID kegiatan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        var coverPath: String? = null
        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext  = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val path = "uploads/kegiatans/${UUID.randomUUID()}$ext"
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

        val old = kegiatanRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Kegiatan tidak ditemukan!")

        val req = KegiatanRequest(
            userId = user.id, judul = old.judul, deskripsi = old.deskripsi,
            kategori = old.kategori, tanggal = old.tanggal, waktu = old.waktu,
            lokasi = old.lokasi, cover = coverPath,
        )
        if (!kegiatanRepo.update(user.id, id, req.toEntity()))
            throw AppException(400, "Gagal memperbarui cover!")

        old.cover?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil mengubah cover kegiatan", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID kegiatan tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val old = kegiatanRepo.getById(id)
        if (old == null || old.userId != user.id) throw AppException(404, "Kegiatan tidak ditemukan!")

        if (!kegiatanRepo.delete(user.id, id)) throw AppException(400, "Gagal menghapus kegiatan!")

        old.cover?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Berhasil menghapus kegiatan", null))
    }

    suspend fun getCover(call: ApplicationCall) {
        val id   = call.parameters["id"] ?: throw AppException(400, "ID kegiatan tidak valid!")
        val data = kegiatanRepo.getById(id) ?: return call.respond(HttpStatusCode.NotFound)
        val file = data.cover?.let { File(it) } ?: throw AppException(404, "Cover belum tersedia")
        if (!file.exists()) throw AppException(404, "File cover tidak ditemukan")
        call.respondFile(file)
    }
}
