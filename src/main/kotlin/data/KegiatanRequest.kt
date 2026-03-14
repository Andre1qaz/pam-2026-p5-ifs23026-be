package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Kegiatan

@Serializable
data class KegiatanRequest(
    var userId    : String  = "",
    var judul     : String  = "",
    var deskripsi : String? = null,
    var kategori  : String  = "",
    var tanggal   : String  = "",
    var waktu     : String  = "",
    var lokasi    : String? = null,
    var cover     : String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"    to userId,
        "judul"     to judul,
        "deskripsi" to deskripsi,
        "kategori"  to kategori,
        "tanggal"   to tanggal,
        "waktu"     to waktu,
        "lokasi"    to lokasi,
    )

    fun toEntity() = Kegiatan(
        userId    = userId,
        judul     = judul,
        deskripsi = deskripsi,
        kategori  = kategori,
        tanggal   = tanggal,
        waktu     = waktu,
        lokasi    = lokasi,
        cover     = cover,
        updatedAt = Clock.System.now(),
    )
}
