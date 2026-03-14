package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.JadwalKuliah

@Serializable
data class JadwalKuliahRequest(
    var userId     : String  = "",
    var mataKuliah : String  = "",
    var dosen      : String  = "",
    var hari       : String  = "",
    var jamMulai   : String  = "",
    var jamSelesai : String  = "",
    var ruangan    : String  = "",
    var semester   : String? = null,
    var keterangan : String? = null,
    var cover      : String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"     to userId,
        "mataKuliah" to mataKuliah,
        "dosen"      to dosen,
        "hari"       to hari,
        "jamMulai"   to jamMulai,
        "jamSelesai" to jamSelesai,
        "ruangan"    to ruangan,
        "semester"   to semester,
        "keterangan" to keterangan,
    )

    fun toEntity() = JadwalKuliah(
        userId     = userId,
        mataKuliah = mataKuliah,
        dosen      = dosen,
        hari       = hari,
        jamMulai   = jamMulai,
        jamSelesai = jamSelesai,
        ruangan    = ruangan,
        semester   = semester,
        keterangan = keterangan,
        cover      = cover,
        updatedAt  = Clock.System.now(),
    )
}
