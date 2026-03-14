package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Kegiatan(
    var id        : String  = UUID.randomUUID().toString(),
    var userId    : String,
    var judul     : String,
    var deskripsi : String? = null,
    var kategori  : String,
    var tanggal   : String,
    var waktu     : String,
    var lokasi    : String? = null,
    var cover     : String? = null,

    @Contextual val createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)
