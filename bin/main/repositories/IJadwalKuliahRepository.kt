package org.delcom.repositories

import org.delcom.entities.JadwalKuliah

interface IJadwalKuliahRepository {
    suspend fun getAll(userId: String, search: String, hari: String, semester: String): List<JadwalKuliah>
    suspend fun getById(jadwalId: String): JadwalKuliah?
    suspend fun create(jadwal: JadwalKuliah): String
    suspend fun update(userId: String, jadwalId: String, new: JadwalKuliah): Boolean
    suspend fun delete(userId: String, jadwalId: String): Boolean
}
