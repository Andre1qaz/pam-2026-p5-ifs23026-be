package org.delcom.repositories

import org.delcom.entities.Kegiatan

interface IKegiatanRepository {
    suspend fun getAll(userId: String, search: String, tanggal: String, kategori: String): List<Kegiatan>
    suspend fun getById(kegiatanId: String): Kegiatan?
    suspend fun create(kegiatan: Kegiatan): String
    suspend fun update(userId: String, kegiatanId: String, new: Kegiatan): Boolean
    suspend fun delete(userId: String, kegiatanId: String): Boolean
}
