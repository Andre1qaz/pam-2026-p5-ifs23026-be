package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class JadwalKuliah(
    var id         : String  = UUID.randomUUID().toString(),
    var userId     : String,
    var mataKuliah : String,
    var dosen      : String,
    var hari       : String,
    var jamMulai   : String,
    var jamSelesai : String,
    var ruangan    : String,
    var semester   : String? = null,
    var keterangan : String? = null,
    var cover      : String? = null,

    @Contextual val createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)
