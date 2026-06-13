# Toyota 32Bit Basvuru Portali

Web tabanli Aday Takip Sistemi (ATS). Ilan yonetimi, basvuru degerlendirme, gorev atama, soru-cevap ve e-posta bildirim modullerini icerir.

## Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| Backend | Java 17, Spring Boot 3.3.4, Spring Security 6.x, Spring Data JPA |
| Veritabani | PostgreSQL 16, Flyway 10.x |
| Frontend | React 19, TypeScript 5.9, Vite, Tailwind CSS 3.4 |
| Guvenlik | JWT (JJWT 0.12.5), BCrypt |
| Dagitim | Docker Compose (5 servis) |

## Hizli Baslangic

### Gereksinimler

- [Docker](https://docs.docker.com/get-docker/) ve Docker Compose

### Calistirma

```bash
# 1. Ortam degiskenlerini hazirla
cp .env.example .env

# 2. Tum servisleri baslat
docker compose up -d --build

# 3. Servislerin hazir olmasini bekle (~1-2 dk)
docker ps
```

### Erisim Adresleri

| Servis | Adres |
|--------|-------|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1 |
| pgAdmin | http://localhost:5050 |
| Mailpit (SMTP UI) | http://localhost:8025 |

### Varsayilan Hesaplar

**Admin:**
- E-posta: `admin@32bit.com.tr`
- Sifre: `Admin12345!`

**Ornek Ogrenciler** (sifre: `12345678`):
- `ali.yilmaz@ogr.sakarya.edu.tr`
- `ayse.demir@ogr.sakarya.edu.tr`
- `mehmet.kaya@ogr.sakarya.edu.tr`
- `zeynep.sahin@ogr.sakarya.edu.tr`
- `emre.celik@ogr.sakarya.edu.tr`
- `selin.akar@ogr.sakarya.edu.tr`

> Ornek veriler `SEED_DATA=true` ve `dev` profili aktifken otomatik olusturulur.

## Mimari

```
                    +-------------------+
                    |    Tarayici       |
                    +--------+----------+
                             |
                    +--------v----------+
                    |  nginx (frontend) |  :3000
                    |  React 19 + TS    |
                    +--------+----------+
                             | /api/*
                    +--------v----------+
                    | Spring Boot 3.3.4 |  :8080
                    | 29 Controller     |
                    | 26 Service        |
                    | 21 Repository     |
                    +--------+----------+
                             |
                    +--------v----------+
                    | PostgreSQL 16     |  :5432
                    | 21 Tablo          |
                    | 12 Flyway Migr.   |
                    +-------------------+
```

### Docker Servisleri

| Konteyner | Image | Port | Aciklama |
|-----------|-------|------|----------|
| ats-db | postgres:16-alpine | 5432 | Veritabani |
| ats-pgadmin | dpage/pgadmin4 | 5050 | Veritabani yonetim araci |
| ats-mailpit | axllent/mailpit | 8025, 1025 | E-posta test sunucusu |
| ats-backend | custom | 8080 | Spring Boot API |
| ats-frontend | custom | 3000 | React + nginx |

## Veritabani

21 tablo, 7 modul:

| Modul | Tablolar |
|-------|----------|
| Kullanici | users, user_profiles, user_applications, user_availability_slots |
| Kimlik Dogrulama | email_verification_tokens, password_reset_tokens |
| Ilan & Basvuru | application_postings, application_submissions, posting_attachments, documents |
| Gorev | project_tasks, task_assignments, task_attachments, task_submission_files |
| Soru-Cevap | posting_questions, question_answers, announcements |
| Mail | mail_jobs, mail_job_attachments |
| Sistem | timeline_events |

Sema versiyonlamasi Flyway ile yapilir (V0-V11, 12 migration dosyasi).

## API Yapisi

RESTful API, 4 ana modul:

| Yol | Erisim | Aciklama |
|-----|--------|----------|
| `/api/v1/auth/**` | Herkese acik | Login, register, verify, forgot/reset password |
| `/api/v1/public/**` | Herkese acik | Ilan listesi |
| `/api/v1/me/**` | Oturum acmis | Profil, basvuru, gorev, soru |
| `/api/v1/admin/**` | Sadece Admin | Ilan CRUD, basvuru degerlendirme, gorev atama |

## Guvenlik

- **JWT kimlik dogrulama:** HMAC-SHA256, 60 dk gecerlilik suresi
- **Rol tabanli erisim:** USER ve ADMIN rolleri, Spring Security filter chain
- **Sifre koruma:** BCrypt hash
- **CORS:** Yapilandirilabilir izin listesi
- **Girdi dogrulama:** Backend (Spring Validation) + Frontend (Zod)

## Backend Yapisi

```
com.project.project
├── config/          # Yapilandirma ve seed data
├── controller/      # 29 REST controller
├── dto/             # Request/Response veri nesneleri
├── entity/          # 21 JPA entity
├── repository/      # 21 Spring Data repository
├── security/        # JWT, Spring Security, BCrypt
├── service/         # 26 servis (interface + impl)
└── util/            # Yardimci siniflar
```

## Frontend Yapisi

```
src/
├── api/             # Axios HTTP istemcisi
├── auth/            # Kimlik dogrulama konteksti
├── components/      # Tekrar kullanilabilir UI bilesenler
├── hooks/           # Ozel React hook'lari
├── lib/             # Yardimci fonksiyonlar
├── pages/           # Sayfa bilesenleri
├── routes/          # React Router yapilandirmasi
├── theme/           # Tema ayarlari
└── types/           # TypeScript tip tanimlari
```

## Kullanisli Komutlar

```bash
# Servisleri baslat
docker compose up -d --build

# Servisleri durdur (verileri koru)
docker compose down

# Servisleri durdur + tum verileri sil
docker compose down -v

# Backend loglarini izle
docker logs -f ats-backend

# Seed verilerinin olusturulup olusturulmadigini kontrol et
docker logs ats-backend --tail 300 | findstr "SEED"

# Veritabanini sifirla
docker compose down -v && docker compose up -d --build
```

## Smoke Test

Sistem baslatildiktan sonra temel fonksiyonlari test etmek icin:

```powershell
# Windows
.\scripts\smoke-test.ps1

# Linux / macOS
bash ./scripts/smoke-test.sh
```

## Proje Bilgileri

- **Ogrenci:** Ali Kerem KOL (B221210042)
- **Danisma:** Prof. Dr. Nilufer YURTAY
- **Universite:** Sakarya Universitesi, Bilgisayar Muhendisligi
- **Donem:** 2025-2026 Bahar
