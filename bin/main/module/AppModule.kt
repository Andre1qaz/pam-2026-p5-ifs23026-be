package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.JadwalKuliahService
import org.delcom.services.KegiatanService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {
    // User
    single<IUserRepository>         { UserRepository() }
    single                          { UserService(get(), get()) }

    // Refresh Token
    single<IRefreshTokenRepository> { RefreshTokenRepository() }

    // Auth
    single                          { AuthService(jwtSecret, get(), get()) }

    // Jadwal Kuliah
    single<IJadwalKuliahRepository> { JadwalKuliahRepository() }
    single                          { JadwalKuliahService(get(), get()) }

    // Kegiatan
    single<IKegiatanRepository>     { KegiatanRepository() }
    single                          { KegiatanService(get(), get()) }
}
