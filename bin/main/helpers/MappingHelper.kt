package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.JadwalKuliahDAO
import org.delcom.dao.KegiatanDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.JadwalKuliah
import org.delcom.entities.Kegiatan
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.createdAt,
    dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun jadwalKuliahDAOToModel(dao: JadwalKuliahDAO) = JadwalKuliah(
    id         = dao.id.value.toString(),
    userId     = dao.userId.toString(),
    mataKuliah = dao.mataKuliah,
    dosen      = dao.dosen,
    hari       = dao.hari,
    jamMulai   = dao.jamMulai,
    jamSelesai = dao.jamSelesai,
    ruangan    = dao.ruangan,
    semester   = dao.semester,
    keterangan = dao.keterangan,
    cover      = dao.cover,
    createdAt  = dao.createdAt,
    updatedAt  = dao.updatedAt,
)

fun kegiatanDAOToModel(dao: KegiatanDAO) = Kegiatan(
    id        = dao.id.value.toString(),
    userId    = dao.userId.toString(),
    judul     = dao.judul,
    deskripsi = dao.deskripsi,
    kategori  = dao.kategori,
    tanggal   = dao.tanggal,
    waktu     = dao.waktu,
    lokasi    = dao.lokasi,
    cover     = dao.cover,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt,
)
