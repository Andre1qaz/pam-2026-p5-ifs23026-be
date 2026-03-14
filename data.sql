-- ============================================================
--  Aplikasi Manajemen Kampus – Database Schema
--  PostgreSQL
-- ============================================================

-- Users
CREATE TABLE IF NOT EXISTS users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100)  NOT NULL,
    username   VARCHAR(50)   NOT NULL UNIQUE,
    password   VARCHAR(255)  NOT NULL,
    photo      VARCHAR(255)  NULL,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL
);

-- Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID  NOT NULL,
    refresh_token TEXT  NOT NULL,
    auth_token    TEXT  NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Jadwal Kuliah
CREATE TABLE IF NOT EXISTS jadwal_kuliahs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    mata_kuliah VARCHAR(100) NOT NULL,
    dosen       VARCHAR(100) NOT NULL,
    hari        VARCHAR(10)  NOT NULL,
    jam_mulai   VARCHAR(10)  NOT NULL,
    jam_selesai VARCHAR(10)  NOT NULL,
    ruangan     VARCHAR(100) NOT NULL,
    semester    VARCHAR(20)  NULL,
    keterangan  TEXT         NULL,
    cover       TEXT         NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_jadwal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_jadwal_user_id ON jadwal_kuliahs(user_id);
CREATE INDEX IF NOT EXISTS idx_jadwal_hari    ON jadwal_kuliahs(hari);

-- Kegiatan
CREATE TABLE IF NOT EXISTS kegiatans (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    judul      VARCHAR(100) NOT NULL,
    deskripsi  TEXT         NULL,
    kategori   VARCHAR(20)  NOT NULL,
    tanggal    VARCHAR(20)  NOT NULL,
    waktu      VARCHAR(10)  NOT NULL,
    lokasi     VARCHAR(100) NULL,
    cover      TEXT         NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_kegiatan_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_kegiatan_user_id  ON kegiatans(user_id);
CREATE INDEX IF NOT EXISTS idx_kegiatan_tanggal  ON kegiatans(tanggal);
CREATE INDEX IF NOT EXISTS idx_kegiatan_kategori ON kegiatans(kategori);
