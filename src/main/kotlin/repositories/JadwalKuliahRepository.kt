package org.delcom.repositories

import org.delcom.dao.JadwalKuliahDAO
import org.delcom.entities.JadwalKuliah
import org.delcom.helpers.jadwalKuliahDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.JadwalKuliahTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class JadwalKuliahRepository : IJadwalKuliahRepository {

    override suspend fun getAll(
        userId: String, search: String, hari: String, semester: String
    ): List<JadwalKuliah> = suspendTransaction {
        var query = JadwalKuliahDAO.find { JadwalKuliahTable.userId eq UUID.fromString(userId) }

        if (hari.isNotBlank()) {
            query = JadwalKuliahDAO.find {
                (JadwalKuliahTable.userId eq UUID.fromString(userId)) and
                (JadwalKuliahTable.hari eq hari)
            }
        }

        if (semester.isNotBlank()) {
            query = JadwalKuliahDAO.find {
                (JadwalKuliahTable.userId eq UUID.fromString(userId)) and
                (JadwalKuliahTable.semester eq semester)
            }
        }

        if (hari.isNotBlank() && semester.isNotBlank()) {
            query = JadwalKuliahDAO.find {
                (JadwalKuliahTable.userId eq UUID.fromString(userId)) and
                (JadwalKuliahTable.hari eq hari) and
                (JadwalKuliahTable.semester eq semester)
            }
        }

        val result = query.orderBy(JadwalKuliahTable.createdAt to SortOrder.DESC)
            .map(::jadwalKuliahDAOToModel)

        if (search.isBlank()) result
        else {
            val kw = search.lowercase()
            result.filter {
                it.mataKuliah.lowercase().contains(kw) ||
                it.dosen.lowercase().contains(kw) ||
                it.ruangan.lowercase().contains(kw)
            }
        }
    }

    override suspend fun getById(jadwalId: String): JadwalKuliah? = suspendTransaction {
        JadwalKuliahDAO
            .find { JadwalKuliahTable.id eq UUID.fromString(jadwalId) }
            .limit(1)
            .map(::jadwalKuliahDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(jadwal: JadwalKuliah): String = suspendTransaction {
        JadwalKuliahDAO.new {
            userId     = UUID.fromString(jadwal.userId)
            mataKuliah = jadwal.mataKuliah
            dosen      = jadwal.dosen
            hari       = jadwal.hari
            jamMulai   = jadwal.jamMulai
            jamSelesai = jadwal.jamSelesai
            ruangan    = jadwal.ruangan
            semester   = jadwal.semester
            keterangan = jadwal.keterangan
            cover      = jadwal.cover
            createdAt  = jadwal.createdAt
            updatedAt  = jadwal.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, jadwalId: String, new: JadwalKuliah): Boolean = suspendTransaction {
        val dao = JadwalKuliahDAO
            .find {
                (JadwalKuliahTable.id eq UUID.fromString(jadwalId)) and
                (JadwalKuliahTable.userId eq UUID.fromString(userId))
            }
            .limit(1).firstOrNull() ?: return@suspendTransaction false

        dao.mataKuliah = new.mataKuliah
        dao.dosen      = new.dosen
        dao.hari       = new.hari
        dao.jamMulai   = new.jamMulai
        dao.jamSelesai = new.jamSelesai
        dao.ruangan    = new.ruangan
        dao.semester   = new.semester
        dao.keterangan = new.keterangan
        dao.cover      = new.cover
        dao.updatedAt  = new.updatedAt
        true
    }

    override suspend fun delete(userId: String, jadwalId: String): Boolean = suspendTransaction {
        JadwalKuliahTable.deleteWhere {
            (JadwalKuliahTable.id eq UUID.fromString(jadwalId)) and
            (JadwalKuliahTable.userId eq UUID.fromString(userId))
        } >= 1
    }
}
