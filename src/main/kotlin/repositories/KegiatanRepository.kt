package org.delcom.repositories

import org.delcom.dao.KegiatanDAO
import org.delcom.entities.Kegiatan
import org.delcom.helpers.kegiatanDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.KegiatanTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class KegiatanRepository : IKegiatanRepository {

    override suspend fun getAll(
        userId: String, search: String, tanggal: String, kategori: String
    ): List<Kegiatan> = suspendTransaction {
        var query = KegiatanDAO.find { KegiatanTable.userId eq UUID.fromString(userId) }

        if (tanggal.isNotBlank()) {
            query = KegiatanDAO.find {
                (KegiatanTable.userId eq UUID.fromString(userId)) and
                (KegiatanTable.tanggal eq tanggal)
            }
        }

        if (kategori.isNotBlank()) {
            query = KegiatanDAO.find {
                (KegiatanTable.userId eq UUID.fromString(userId)) and
                (KegiatanTable.kategori eq kategori)
            }
        }

        if (tanggal.isNotBlank() && kategori.isNotBlank()) {
            query = KegiatanDAO.find {
                (KegiatanTable.userId eq UUID.fromString(userId)) and
                (KegiatanTable.tanggal eq tanggal) and
                (KegiatanTable.kategori eq kategori)
            }
        }

        val result = query.orderBy(KegiatanTable.createdAt to SortOrder.DESC)
            .map(::kegiatanDAOToModel)

        if (search.isBlank()) result
        else {
            val kw = search.lowercase()
            result.filter {
                it.judul.lowercase().contains(kw) ||
                it.deskripsi?.lowercase()?.contains(kw) == true ||
                it.lokasi?.lowercase()?.contains(kw) == true
            }
        }
    }

    override suspend fun getById(kegiatanId: String): Kegiatan? = suspendTransaction {
        KegiatanDAO
            .find { KegiatanTable.id eq UUID.fromString(kegiatanId) }
            .limit(1)
            .map(::kegiatanDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(kegiatan: Kegiatan): String = suspendTransaction {
        KegiatanDAO.new {
            userId    = UUID.fromString(kegiatan.userId)
            judul     = kegiatan.judul
            deskripsi = kegiatan.deskripsi
            kategori  = kegiatan.kategori
            tanggal   = kegiatan.tanggal
            waktu     = kegiatan.waktu
            lokasi    = kegiatan.lokasi
            cover     = kegiatan.cover
            createdAt = kegiatan.createdAt
            updatedAt = kegiatan.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, kegiatanId: String, new: Kegiatan): Boolean = suspendTransaction {
        val dao = KegiatanDAO
            .find {
                (KegiatanTable.id eq UUID.fromString(kegiatanId)) and
                (KegiatanTable.userId eq UUID.fromString(userId))
            }
            .limit(1).firstOrNull() ?: return@suspendTransaction false

        dao.judul     = new.judul
        dao.deskripsi = new.deskripsi
        dao.kategori  = new.kategori
        dao.tanggal   = new.tanggal
        dao.waktu     = new.waktu
        dao.lokasi    = new.lokasi
        dao.cover     = new.cover
        dao.updatedAt = new.updatedAt
        true
    }

    override suspend fun delete(userId: String, kegiatanId: String): Boolean = suspendTransaction {
        KegiatanTable.deleteWhere {
            (KegiatanTable.id eq UUID.fromString(kegiatanId)) and
            (KegiatanTable.userId eq UUID.fromString(userId))
        } >= 1
    }
}
