# SIAKAD Widyatama - Documentasi Project

## 1. Gambaran Project

**SIAKAD (Sistem Informasi Akademik)** adalah aplikasi mobile Android untuk mahasiswa Universitas Widyatama. Aplikasi ini memungkinkan mahasiswa untuk melihat jadwal kuliah, mencatat kehadiran via QR code, melihat nilai akademik, dan mengelola profil akun mereka.

### Spesifikasi Teknis
| Spec | Value |
|------|-------|
| **Package Name** | `com.widyatama.siakad` |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 35 (Android 15) |
| **Compile SDK** | 36 |
| **Bahasa** | Kotlin |
| **Build System** | Gradle Kotlin DSL |
| **Version Code** | 1 |
| **Version Name** | 1.0 |

---

## 2. Struktur Project

### Ringkasan File
- **40 file Kotlin** source code
- **14 file XML layout**
- **38+ vector drawable** icon custom
- **2 file menu XML** (bottom nav & profile menu)

### Struktur Direktori Lengkap

```
SIAKAD-Widyatama-Mobile-App/
├── build.gradle.kts                  # Root build config (Google Services plugin)
├── settings.gradle.kts               # Project settings & module declaration
├── gradle.properties                 # Gradle JVM properties
├── gradlew / gradlew.bat             # Gradle wrapper scripts
├── local.properties                  # Local SDK path (tidak di-commit)
│
├── gradle/
│   ├── libs.versions.toml            # Version catalog (semua versi dependency)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── app/
│   ├── build.gradle.kts              # App module build config (plugin, dependencies, config)
│   ├── google-services.json          # Firebase project config (excluded from git)
│   ├── proguard-rules.pro
│   │
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # Deklarasi activity, permission, launcher
│       │   │
│       │   ├── java/com/widyatama/siakad/
│       │   │   │
│       │   │   ├── adapter/                             # RecyclerView Adapters
│       │   │   │   ├── AcademicYearAdapter.kt           # Adapter tahun akademik (ResultsFragment)
│       │   │   │   ├── CourseAdapter.kt                 # Adapter card mata kuliah (Dashboard & Schedule)
│       │   │   │   ├── PaymentDetailAdapter.kt          # Adapter detail tagihan (AdminFragment)
│       │   │   │   └── ResultAdapter.kt                 # Adapter hasil nilai (ResultsFragment)
│       │   │   │
│       │   │   ├── core/
│       │   │   │   ├── base/
│       │   │   │   │   ├── BaseActivity.kt              # Base class semua Activity
│       │   │   │   │   └── BaseFragment.kt              # Base class semua Fragment
│       │   │   │   ├── constants/
│       │   │   │   │   └── AppConstants.kt              # Nama koleksi Firestore & SharedPrefs keys
│       │   │   │   ├── extensions/
│       │   │   │   │   ├── StringExtensions.kt          # Extension function untuk String
│       │   │   │   │   └── ViewExtensions.kt            # Extension function untuk View
│       │   │   │   └── utils/
│       │   │   │       ├── DateUtils.kt                 # Format tanggal & hari
│       │   │   │       ├── FormatUtils.kt               # Format angka (IPK, Rupiah, etc)
│       │   │   │       └── ValidationUtils.kt           # Validasi input & hashing password SHA-256
│       │   │   │
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   └── SharedPrefManager.kt         # Session management via SharedPreferences
│       │   │   │   ├── model/                           # Data classes / entities
│       │   │   │   │   ├── AcademicResult.kt            # Hasil nilai per semester
│       │   │   │   │   ├── AcademicYear.kt              # Data tahun akademik
│       │   │   │   │   ├── AttendanceSummary.kt         # Ringkasan kehadiran
│       │   │   │   │   ├── Course.kt                    # Mata kuliah (dari koleksi `courses`)
│       │   │   │   │   ├── CourseResult.kt              # Nilai per mata kuliah (dari `academic_results`)
│       │   │   │   │   ├── JadwalItem.kt                # Item jadwal per hari
│       │   │   │   │   ├── Lecturer.kt                  # Dosen (dari koleksi `lecturers`)
│       │   │   │   │   ├── Payment.kt                   # Data pembayaran
│       │   │   │   │   ├── PaymentDetail.kt             # Detail tagihan (dari `tagihan` subcollection)
│       │   │   │   │   ├── PengaturanUser.kt            # Preferensi pengguna (dari `pengaturan`)
│       │   │   │   │   ├── Pengumuman.kt                # Pengumuman (dari koleksi `pengumuman`)
│       │   │   │   │   ├── Presensi.kt                  # Data kehadiran (dari koleksi `presensi`)
│       │   │   │   │   ├── Room.kt                      # Data ruangan (dari koleksi `rooms`)
│       │   │   │   │   ├── Student.kt                   # Data mahasiswa (dari koleksi `mahasiswa`)
│       │   │   │   │   ├── Tagihan.kt                   # Tagihan pembayaran
│       │   │   │   │   ├── Transkrip.kt                 # Data transkrip (dari `transkrip` subcollection)
│       │   │   │   │   └── UserPreferences.kt           # Preferensi notifikasi & tampilan
│       │   │   │   └── remote/
│       │   │   │       ├── FirestoreManager.kt          # Singleton - semua operasi CRUD Firestore
│       │   │   │       └── FirestoreSeeder.kt           # Utility - seed data dummy untuk development
│       │   │   │
│       │   │   └── ui/                                  # UI Layer (Activity & Fragment)
│       │   │       ├── admin/
│       │   │       │   └── AdminFragment.kt             # Halaman tagihan & pembayaran (Tab: "Admin")
│       │   │       ├── attendance/
│       │   │       │   └── QrScannerActivity.kt         # Scanner QR code presensi (CameraX + ML Kit)
│       │   │       ├── auth/
│       │   │       │   ├── LoginActivity.kt             # Login dengan NPM + password
│       │   │       │   └── RegisterActivity.kt          # Registrasi mahasiswa baru
│       │   │       ├── dashboard/
│       │   │       │   ├── DashboardActivity.kt         # Host Activity untuk bottom navigation
│       │   │       │   └── DashboardFragment.kt         # Dashboard utama (Tab: "Dashboard")
│       │   │       ├── profile/
│       │   │       │   ├── ProfileFragment.kt           # Profil mahasiswa (Tab: "Profil")
│       │   │       │   └── SettingsFragment.kt          # Menu pengaturan (UI only, non-fungsional)
│       │   │       ├── results/
│       │   │       │   └── ResultsFragment.kt           # Nilai akademik (Tab: "Nilai")
│       │   │       └── schedule/
│       │   │           └── ScheduleFragment.kt          # Jadwal kuliah (Tab: "Jadwal")
│       │   │
│       │   └── res/
│       │       ├── drawable/                            # Vector drawables & custom shapes (38+ file)
│       │       │   ├── logo_widyatama.xml               # Logo Universitas Widyatama
│       │       │   ├── logo_diktisaintek.png            # Logo Dikti Saintek
│       │       │   ├── bg_badge_aktif.xml               # Badge status mahasiswa AKTIF
│       │       │   ├── bg_badge_reguler.xml             # Badge kelas REGULER
│       │       │   ├── bg_status_lunas.xml              # Badge status LUNAS
│       │       │   ├── bg_status_tagihan.xml            # Badge status BELUM_LUNAS
│       │       │   ├── bg_status_proses.xml             # Badge status PROSES
│       │       │   ├── bg_tag_wajib.xml                 # Badge tipe WAJIB
│       │       │   ├── bg_zoom_controls.xml             # Background kontrol zoom jadwal
│       │       │   ├── qr_corner_*.xml                  # Decorasi sudut QR scanner (3 file)
│       │       │   ├── popup_menu_background.xml        # Background popup menu kustom
│       │       │   ├── bg_icon_circle.xml               # Background lingkaran icon
│       │       │   ├── bg_logout_border.xml             # Border tombol logout
│       │       │   └── ic_*.xml                         # Icon vector (dashboard, profile, schedule, etc.)
│       │       ├── layout/                              # XML layout (14 file)
│       │       │   ├── activity_login.xml               # Layout halaman login
│       │       │   ├── activity_register.xml            # Layout halaman registrasi
│       │       │   ├── activity_dashboard.xml           # Layout bottom nav container
│       │       │   ├── activity_qr_scanner.xml          # Layout kamera QR scanner
│       │       │   ├── fragment_dashboard.xml           # Layout dashboard utama
│       │       │   ├── fragment_schedule.xml            # Layout jadwal kuliah
│       │       │   ├── fragment_results.xml             # Layout nilai akademik
│       │       │   ├── fragment_admin.xml               # Layout tagihan & pembayaran
│       │       │   ├── fragment_profile.xml             # Layout profil mahasiswa
│       │       │   ├── fragment_settings.xml            # Layout menu pengaturan
│       │       │   ├── item_course_card.xml             # Item card mata kuliah
│       │       │   ├── item_result_course.xml           # Item row hasil nilai
│       │       │   ├── item_installment_card.xml        # Item card cicilan tagihan
│       │       │   └── item_history_card.xml            # Item card riwayat pembayaran
│       │       ├── menu/
│       │       │   ├── bottom_nav_menu.xml              # Menu bottom navigation (5 tab)
│       │       │   └── profile_menu.xml                 # Menu popup profile
│       │       ├── mipmap-*/                            # App icon launcher (multiple densities)
│       │       ├── values/
│       │       │   ├── colors.xml                       # Definisi warna (navy_blue, accent, etc.)
│       │       │   ├── strings.xml                      # Semua string resource
│       │       │   └── themes.xml                       # Theme Material 3 DayNight
│       │       ├── values-night/
│       │       │   └── themes.xml                       # Theme dark mode
│       │       └── xml/
│       │           ├── backup_rules.xml                 # Aturan backup Android
│       │           └── data_extraction_rules.xml        # Aturan ekstraksi data
│       │
│       ├── test/                                        # Unit test (JUnit 4, stub only)
│       │   └── java/com/widyatama/siakad/
│       │       └── ExampleUnitTest.kt
│       │
│       └── androidTest/                                 # Instrumented test (Espresso, stub only)
│           └── java/com/widyatama/siakad/
│               └── ExampleInstrumentedTest.kt
```

---

## 3. Bahasa & Teknologi

### Bahasa Pemrograman
- **Kotlin** (2.1.0) - Bahasa utama untuk semua source code (40 file .kt)
- **XML** - Untuk layout (14 file), drawable (38+ file), menu, values, dan resource definition
- **Kotlin DSL** - Digunakan untuk file build.gradle.kts dan settings.gradle.kts

### Framework & Library (versi spesifik)

| Library | Version | Purpose |
|---------|---------|---------|
| **Android Gradle Plugin** | 8.9.1 | Build system |
| **Kotlin Android Plugin** | 2.1.0 | Kotlin compiler & Android extensions |
| **AndroidX Core KTX** | latest | Kotlin extensions untuk Android |
| **AndroidX AppCompat** | latest | Backward-compatible Android components |
| **Material Design 3** | latest | UI components (Material You / DayNight theme) |
| **AndroidX Activity** | latest | Activity Result APIs |
| **AndroidX ConstraintLayout** | latest | Flexible constraint-based layout system |
| **AndroidX CardView** | latest | Card UI component |
| **AndroidX RecyclerView** | latest | Efficient list/grid display |
| **AndroidX Navigation** | 2.8.5 | Fragment navigation + Bottom Navigation |
| **AndroidX Lifecycle** | latest | ViewModel & lifecycle-aware coroutine scopes |
| **Firebase BoM** | 32.8.0 | Firebase SDK management |
| **Firebase Firestore** | 26.3.0 | NoSQL document database (primary database) |
| **Firebase Auth** | (via BoM) | Authentication (declared; custom auth via Firestore) |
| **Firebase Analytics** | (via BoM) | Analytics tracking |
| **Google Services** | 4.4.1 | Google services Gradle plugin |
| **Glide** | 4.16.0 | Image loading & caching (foto profil) |
| **CircleImageView** | 3.1.0 | Circular ImageView untuk foto profil |
| **CameraX Core** | 1.4.0 | Modern camera API |
| **CameraX Camera2** | 1.4.0 | Camera2 HAL implementation |
| **CameraX Lifecycle** | 1.4.0 | Lifecycle-aware camera binding |
| **CameraX View** | 1.4.0 | PreviewView untuk tampilan kamera |
| **ML Kit Barcode** | 17.3.0 | Google ML Kit QR/Barcode recognition |
| **JUnit 4** | - | Unit testing (stub) |
| **Espresso** | - | Instrumented UI testing (stub) |

### Arsitektur & Pattern
| Aspek | Pilihan |
|-------|---------|
| **Architecture** | MVVM-Lite (View + Model, tanpa formal ViewModel class) |
| **State Management** | Manual callback-based (belum pakai StateFlow/LiveData) |
| **DI (Dependency Injection)** | Manual singleton (`@Volatile` + `synchronized`) |
| **Build Features** | ViewBinding = true, BuildConfig = true |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 35 (Android 15) |
| **Compile SDK** | 36 |
| **Java Compatibility** | Java 17 |
| **Gradle** | Kotlin DSL dengan Version Catalog (`libs.versions.toml`) |
| **Offline Support** | Tidak ada (semua operasi perlu internet) |
| **Dark Mode** | Didukung via `values-night/themes.xml` (Material 3 DayNight) |

### Build Configuration
```kotlin
android {
    namespace = "com.widyatama.siakad"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}
```

---

## 4. Database - Firebase Firestore

### Arsitektur Database
Firestore menggunakan model **document-oriented NoSQL** dengan struktur collection-subcollection.

```
Firestore Root
├── mahasiswa/{npm}              # Student documents (NPM = document ID)
│   ├── academic_results/{id}     # Subcollection: Nilai akademik
│   ├── tagihan/{id}             # Subcollection: Tagihan pembayaran
│   ├── transkrip/{id}            # Subcollection: Transkrip
│   └── pengaturan/               # Subcollection: User settings
├── courses/{code}               # Course documents (code = document ID)
├── lecturers/{nidn}              # Lecturer documents
├── rooms/{id}                    # Room documents
├── presensi/{id}                 # Attendance records
└── pengumuman/{id}               # Announcement documents
```

### Collections Detail

#### 4.1 Collection: `mahasiswa`
**Document ID**: NPM mahasiswa (contoh: `"241111011"`)

| Field | Type | Description |
|-------|------|-------------|
| `npm` | String | Nomor Pokok Mahasiswa |
| `name` | String | Nama lengkap |
| `major` | String | Program studi |
| `campusEmail` | String | Email kampus (@widyatama.ac.id) |
| `passwordHash` | String | Password yang di-hash (SHA-256) |
| `photoUrl` | String | URL foto profil |
| `status` | String | Status mahasiswa (AKTIF, etc) |
| `kelas` | String | Kelas (REGULER, etc) |
| `angkatan` | Integer | Tahun masuk |
| `ipkKumulatif` | Double | IPK kumulatif |
| `totalSksLulus` | Integer | Total SKS yang sudah lulus |
| `totalSksTarget` | Integer | Total SKS target |
| `semesterBerjalan` | Integer | Semester saat ini |

#### 4.2 Collection: `courses`
**Document ID**: Kode mata kuliah (contoh: `"IF301"`)

| Field | Type | Description |
|-------|------|-------------|
| `code` | String | Kode mata kuliah |
| `name` | String | Nama mata kuliah |
| `sks` | Integer | Jumlah SKS |
| `type` | String | Tipe (WAJIB/PILIHAN) |
| `semester` | Integer | Semester |
| `hari` | String | Hari (Senin, Selasa, etc) |
| `jamMulai` | String | Jam kuliah (contoh: "08:00 - 10:30 WIB") |
| `room` | String | Ruangan |
| `lecturer` | String | Nama dosen |
| `enrolledCount` | Integer | Jumlah mahasiswa enrolled |
| `attendance` | Integer | Jumlah kehadiran |
| `totalAttendance` | Integer | Total pertemuan |
| `isActive` | Boolean | Status aktif |

#### 4.3 Collection: `presensi`
**Document ID**: Auto-generated

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Document ID |
| `npm` | String | NPM mahasiswa |
| `mataKuliahId` | String | Kode mata kuliah |
| `pertemuanId` | String | ID pertemuan |
| `status` | String | Status (HADIR, etc) |
| `timestamp` | Timestamp | Waktu presensi |

#### 4.4 Collection: `pengumuman`
**Document ID**: Auto-generated

| Field | Type | Description |
|-------|------|-------------|
| `title` | String | Judul pengumuman |
| `content` | String | Isi pengumuman |
| `isActive` | Boolean | Status aktif |
| `timestamp` | Timestamp | Waktu dibuat |

### Subcollections Detail
Subcollections berada di bawah dokumen `mahasiswa/{npm}`.

#### 4.5 Subcollection: `mahasiswa/{npm}/academic_results`
Menyimpan nilai akademik mahasiswa per mata kuliah.

| Field | Type | Description |
|-------|------|-------------|
| `mataKuliahId` | String | Kode mata kuliah |
| `mataKuliahName` | String | Nama mata kuliah |
| `sks` | Integer | Jumlah SKS mata kuliah |
| `nilaiAngka` | Double | Nilai angka (0-100) |
| `nilaiHuruf` | String | Nilai huruf (A, B, C, D, E) |
| `mutu` | Double | Nilai mutu (0.00 - 4.00) |
| `semester` | Integer | Semester mata kuliah diambil |
| `status` | String | Status (LULUS, MENGULANG, etc) |

#### 4.6 Subcollection: `mahasiswa/{npm}/tagihan`
Menyimpan data pembayaran & tagihan mahasiswa.

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Document ID (auto-generated) |
| `semester` | Integer | Semester tagihan |
| `tanggal` | Timestamp | Tanggal tagihan dibuat |
| `jatuhTempo` | Timestamp | Tanggal jatuh tempo pembayaran |
| `items` | Array\<Map\>| Daftar item tagihan (nama, jumlah) |
| `total` | Double | Total nominal tagihan |
| `status` | String | Status (LUNAS, BELUM_LUNAS, PROSES) |
| `paymentMethod` | String | Metode pembayaran |
| `paymentDate` | Timestamp | Tanggal pelunasan |

#### 4.7 Subcollection: `mahasiswa/{npm}/transkrip`
Menyimpan data transkrip nilai mahasiswa per semester.

| Field | Type | Description |
|-------|------|-------------|
| `semester` | Integer | Semester |
| `sksSemester` | Integer | Total SKS semester tersebut |
| `sksKumulatif` | Integer | Total SKS kumulatif |
| `ipSemester` | Double | IP semester |
| `ipKumulatif` | Double | IPK kumulatif |
| `nilai` | Array\<Map\> | Daftar nilai per mata kuliah |

#### 4.8 Subcollection: `mahasiswa/{npm}/pengaturan`
Menyimpan preferensi pengguna untuk notifikasi & tampilan.

| Field | Type | Description |
|-------|------|-------------|
| `pushNotification` | Boolean | Notifikasi push enabled |
| `emailNotification` | Boolean | Notifikasi email enabled |
| `darkMode` | Boolean | Mode gelap enabled |
| `language` | String | Bahasa pilihan (id/en) |

### Collections Pendukung (Referensi)

#### 4.9 Collection: `lecturers`
**Document ID**: NIDN dosen

| Field | Type | Description |
|-------|------|-------------|
| `nidn` | String | Nomor Induk Dosen Nasional |
| `name` | String | Nama lengkap dosen |
| `title` | String | Gelar (S.Kom., M.T., etc) |
| `email` | String | Email dosen |

#### 4.10 Collection: `rooms`
**Document ID**: Auto-generated

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Kode ruangan (contoh: "B409") |
| `name` | String | Nama ruangan |
| `capacity` | Integer | Kapasitas ruangan |
| `building` | String | Nama gedung |

### Ringkasan Relasi Database

```
Firestore Root
│
├── mahasiswa/{npm}                          ← Dokumen utama mahasiswa
│   ├── academic_results/{id}                ← Nilai per matkul (1 semester)
│   ├── tagihan/{id}                         ← Tagihan pembayaran
│   ├── transkrip/{id}                       ← Transkrip per semester
│   └── pengaturan/{preferences}             ← Preferensi user
│
├── courses/{code}                           ← Mata kuliah (referensi kode)
├── lecturers/{nidn}                         ← Dosen (referensi NIDN)
├── rooms/{id}                               ← Ruangan (referensi kode)
├── presensi/{id}                            ← Catatan kehadiran
└── pengumuman/{id}                          ← Pengumuman kampus
```

### Seed Data (FirestoreSeeder)

File `FirestoreSeeder.kt` adalah **utility standalone** yang dapat digunakan untuk mengisi Firestore dengan data dummy saat development. Data ini **TIDAK** dipakai oleh aplikasi saat runtime — hanya untuk testing/setup awal.

| Entity | Jumlah | Contoh Data |
|--------|--------|-------------|
| Mahasiswa | 3 | Yansen (241111011, IPK 3.82), Jeni (241111038, IPK 3.75), Edo (241111043, IPK 3.90) |
| Dosen | 5 | Dani Hamdani, Virgana Targa, Rikky Wisnu, Ucu Nugraha, Sri Lestari |
| Ruangan | 5 | B409, C206, C218, K106, A101 |
| Mata Kuliah | 8 | IF301-IF308 (semester 5: Pemrograman Mobile, e-Government, AI, dll) |

Semua mahasiswa seed memiliki:
- Program studi: **Sistem Informasi**
- Angkatan: **2024**
- Semester berjalan: **4**
- Password default: **`SeededPassword123`** (SHA-256 hashed)

---

## 5. Status Integrasi Database

Bagian ini merangkum **setiap halaman/fitur** di aplikasi dan status integrasinya dengan database Firebase Firestore. Ini membantu memahami halaman mana yang sudah terhubung ke data nyata vs. yang masih UI placeholder.

### Ringkasan Status Per Halaman

| # | Halaman / Fitur | File | Sumber Data | Status Integrasi |
|---|---|---|---|---|
| 1 | **Login** | `LoginActivity.kt` | Firestore `mahasiswa/{npm}` | Terintegrasi penuh |
| 2 | **Register** | `RegisterActivity.kt` | Firestore `mahasiswa/{npm}` (write) | Terintegrasi penuh |
| 3 | **Dashboard** | `DashboardFragment.kt` | Firestore `mahasiswa/{npm}` + `courses` | Terintegrasi penuh |
| 4 | **Jadwal** | `ScheduleFragment.kt` | Firestore `courses` | Terintegrasi penuh |
| 5 | **Tagihan & Pembayaran** | `AdminFragment.kt` | Firestore `mahasiswa/{npm}/tagihan` | Terintegrasi penuh |
| 6 | **Nilai Akademik** | `ResultsFragment.kt` | Firestore `mahasiswa/{npm}/academic_results` | Terintegrasi penuh |
| 7 | **Profil Mahasiswa** | `ProfileFragment.kt` | Firestore `mahasiswa/{npm}` + Glide | Terintegrasi penuh |
| 8 | **Presensi QR** | `QrScannerActivity.kt` | CameraX + ML Kit + Firestore `presensi` | Terintegrasi penuh |
| 9 | **Pengaturan** | `SettingsFragment.kt` | **Tidak ada** (UI only) | **Non-fungsional** |

### Detail Integrasi Per Fitur

#### 5.1 Login & Register
| Aspek | Detail |
|-------|--------|
| **Login flow** | Input NPM + password → hash SHA-256 dengan salt → query `mahasiswa/{npm}` dari Firestore → verifikasi `passwordHash` → simpan session ke `SharedPreferences` |
| **Register flow** | Validasi input lokal → hash password dengan salt random 16-byte → cek NPM duplikat di Firestore → tulis dokumen baru ke `mahasiswa/{npm}` |
| **Method FirestoreManager** | `loginMahasiswa()`, `registerMahasiswa()` |
| **Keamanan** | Password di-hash dengan format `base64(salt):base64(hash)`, tidak pernah disimpan plain text |

#### 5.2 Dashboard
| Aspek | Detail |
|-------|--------|
| **Data yang di-fetch** | Profil mahasiswa (nama, IPK, semester), jadwal hari ini dari `courses` difilter by `hari` + `semester` |
| **Method FirestoreManager** | `getMahasiswa()`, `getCoursesByDay()` |
| **Fallback** | Jika Firestore gagal, field mahasiswa menggunakan data dari Navigation argument / SharedPreferences |
| **Komputasi lokal** | Tanggal header dihitung dari `Calendar` (bukan dari server) |

#### 5.3 Jadwal Kuliah
| Aspek | Detail |
|-------|--------|
| **Data yang di-fetch** | Semua course untuk semester berjalan dari `courses` |
| **Method FirestoreManager** | `getAllCoursesForSemester()` |
| **Filter** | Client-side filter by hari (Senin-Jumat) dengan chip selector |
| **Komputasi lokal** | Total SKS, jadwal berikutnya |

#### 5.4 Tagihan & Pembayaran (Admin)
| Aspek | Detail |
|-------|--------|
| **Data yang di-fetch** | Tagihan aktif (`status = "BELUM_LUNAS"`) dan riwayat pembayaran (`status = "LUNAS"`) dari `mahasiswa/{npm}/tagihan` |
| **Method FirestoreManager** | `getTagihanAktif()`, `getHistoryPembayaran()` |
| **Komputasi lokal** | Total nominal dihitung client-side |
| **Catatan** | Tombol "Download" belum difungsikan (hanya Toast) |

#### 5.5 Nilai Akademik
| Aspek | Detail |
|-------|--------|
| **Data yang di-fetch** | Academic results per semester dari `mahasiswa/{npm}/academic_results` |
| **Method FirestoreManager** | `getAcademicResults()` |
| **Mapping** | `AcademicResult` → `CourseResult` untuk tampilan RecyclerView |
| **Display** | Nama matkul, SKS, nilai mutu, grade huruf |

#### 5.6 Presensi QR
| Aspek | Detail |
|-------|--------|
| **Teknologi** | CameraX (live preview) + Google ML Kit Barcode Scanning |
| **Format QR** | JSON: `{ "mataKuliahId": "...", "pertemuanId": "..." }` |
| **Validasi** | Hanya format QR code yang diterima (bukan barcode lain), wajib memiliki `mataKuliahId` & `pertemuanId` |
| **Duplicate check** | Sebelum insert, cek apakah mahasiswa sudah presensi untuk pertemuan yang sama |
| **Method FirestoreManager** | `recordPresensi()` → insert dokumen ke koleksi `presensi` |

#### 5.7 Profil Mahasiswa
| Aspek | Detail |
|-------|--------|
| **Data yang di-fetch** | Biodata lengkap: nama, NPM, prodi, angkatan, status, kelas, IPK, progress SKS, semester, foto profil |
| **Method FirestoreManager** | `getMahasiswa()` |
| **Image loading** | Glide untuk memuat `photoUrl` ke `CircleImageView` |
| **Logout** | Menghapus semua data `SharedPreferences` lalu navigasi ke `LoginActivity` |

#### 5.8 Pengaturan (SettingsFragment)
| Aspek | Detail |
|-------|--------|
| **Status** | **Non-fungsional** — semua menu bersifat UI placeholder |
| **Menu yang ada** | Ubah Password, Keamanan, Push Notification, Email Notification, Mode Gelap, Bahasa, Pusat Bantuan, Tentang Aplikasi |
| **Menu yang berfungsi** | Hanya "Keluar" (logout) — menghapus SharedPreferences + navigasi ke Login |
| **TODO** | Implementasikan `updatePassword()`, `getUserPreferences()`, `saveUserPreferences()` yang sudah ada di `FirestoreManager` |

### Summary Status

```
Terintegrasi penuh (Firebase Firestore):  ████████████████████ 8/9 fitur (89%)
UI Only / Non-fungsional:                  ██                   1/9 fitur (11%)
Menggunakan data dummy/hardcoded (runtime): 0                    0 fitur (0%)
```

**Kesimpulan**: 8 dari 9 halaman sudah terintegrasi dengan database. Halaman **Settings** (`SettingsFragment.kt`) adalah satu-satunya halaman yang belum terhubung ke backend — semua menu pengaturan di dalamnya hanya UI tanpa logika bisnis. Tidak ada halaman yang menggunakan data hardcoded saat runtime.

---

## 6. Arsitektur Aplikasi

### Pattern: MVVM-Lite
Aplikasi menggunakan variasi MVVM dengan:
- **View**: Activity & Fragment (XML layouts)
- **Model**: Data classes di `data/model/`
- **ViewModel**: Manual state management via LiveData/StateFlow patterns
- **Repository**: `FirestoreManager` sebagai singleton repository

### Alur Data
```
UI (Activity/Fragment)
    ↓ user action
ViewBinding → calls → FirestoreManager
    ↓ Firebase callbacks
onResult() → updates UI
```

### SharedPreferences Keys
| Key | Type | Purpose |
|-----|------|---------|
| `SIAKAD_PREFS` | - | SharedPreferences name |
| `IS_LOGGED_IN` | Boolean | Login status |
| `NPM` | String | Logged in NPM |
| `STUDENT_NAME` | String | Student name |
| `CURRENT_SEMESTER` | Integer | Current semester |

---

## 7. Fitur Utama

### 7.1 Authentication
- **Login**: NPM + password dengan SHA-256 hash verification
- **Register**: Pendaftaran mahasiswa baru, cek NPM duplikat
- **Session**: SharedPreferences-based session dengan remember-me

### 7.2 Dashboard
- Header dengan nama mahasiswa, NPM, & tanggal hari ini
- Today's schedule (jadwal hari ini) dari Firestore difilter by hari & semester
- IPK & progress SKS display
- QR Scanner button untuk presensi
- Schedule zoom controls (perbesar/perkecil rentang jadwal)
- Weekend detection (tidak ada jadwal di Sabtu/Minggu)

### 7.3 Schedule (Jadwal)
- Filter berdasarkan hari (Senin-Jumat) dengan chip-based day selector
- Total SKS display
- Next course info (mata kuliah berikutnya hari ini)
- Default ke hari yang dipilih (hari ini atau terdekat dengan jadwal)

### 7.4 Attendance / Presensi (QR Scanner)
- **CameraX** untuk live camera preview dengan orientasi portrait
- **Google ML Kit Barcode Scanning** untuk deteksi QR code
- Hanya mendeteksi format QR code (bukan barcode 1D lainnya)
- Format QR yang di-scan harus JSON valid: `{ "mataKuliahId": "...", "pertemuanId": "..." }`
- **Duplicate check**: Sebelum mencatat presensi, cek apakah mahasiswa sudah presensi untuk pertemuan yang sama di koleksi `presensi`
- Feedback visual: overlay QR corner + success/error state

### 7.5 Tagihan & Pembayaran (Admin)
- Dua tab: **Tagihan Aktif** (status BELUM_LUNAS) & **Riwayat** (status LUNAS)
- Total nominal dihitung client-side
- Detail per item tagihan (nama item + jumlah)
- Badge status: LUNAS (hijau), BELUM_LUNAS (merah), PROSES (kuning)
- Tombol download (belum difungsikan)

### 7.6 Results (Nilai)
- Academic results per semester dengan tahun akademik
- Nilai per mata kuliah: nama, SKS, nilai mutu, grade huruf
- IPK & SKS summary
- Transkrip view

### 7.7 Profile
- Student biodata lengkap: nama, NPM, prodi, angkatan, status, kelas
- IPK & progress SKS dengan progress bar
- Foto profil via **Glide** ke **CircleImageView**
- Badge status mahasiswa (AKTIF) & kelas (REGULER)
- Menu ke halaman Pengaturan
- Logout: hapus SharedPreferences → navigasi ke Login

### 7.8 Pengaturan (Settings)
- **Status: Non-fungsional** — semua menu adalah UI placeholder
- Menu yang ditampilkan (semua belum berfungsi):
  - Ubah Password, Keamanan
  - Push Notification, Email Notification
  - Mode Gelap, Bahasa
  - Pusat Bantuan, Tentang Aplikasi
- Fungsional: Hanya tombol "Keluar" (logout)

---

## 8. Security Considerations

### Password Security
- Password di-hash menggunakan SHA-256 dengan salt random 16-byte per password
- Format penyimpanan di Firestore: `base64(salt):base64(hash)`
- Plain password TIDAK pernah disimpan atau dikirim ke Firestore
- `ValidationUtils.hashPassword()` dan `verifyPassword()` menangani hashing & verifikasi

### Data Exposure
- `google-services.json` di-exclude dari git (.gitignore)
- API key Firebase tidak di-commit ke repository
- Firebase Firestore security rules harus dikonfigurasi untuk membatasi akses per-user

### Session Management
- Session disimpan di SharedPreferences lokal (bukan token server)
- Data yang disimpan: NPM, nama, prodi, email, login status, remember-me flag, semester
- Session dihapus sepenuhnya saat logout

---

## 9. Catatan Development

### Running the App
1. Buka project di Android Studio (Hedgehog atau lebih baru)
2. Pastikan `google-services.json` ada di `app/` (download dari Firebase Console)
3. Setup Firebase project dengan Firestore rules yang sesuai
4. Jalankan `FirestoreSeeder` untuk populate data dummy ke Firestore (opsional)
5. Run `app` configuration pada emulator/device dengan min SDK 24

### Menjalankan Seeder
`FirestoreSeeder.kt` adalah utility standalone. Untuk mengisi Firestore dengan data dummy:
1. Panggil `FirestoreSeeder.seedAll(context)` secara manual (misal dari MainActivity sementara)
2. Data yang di-seed: 3 mahasiswa, 5 dosen, 5 ruangan, 8 mata kuliah (semester 5)
3. Password default semua mahasiswa seed: `SeededPassword123`
4. Setelah seeding selesai, hapus panggilan seeder dari production code

### Known Issues (Solved)
1. ~~Plain-text password~~ → Fixed dengan SHA-256 hashing + salt random
2. ~~Semester mismatch~~ → Student seeder menggunakan semester 4, courses semester 5
3. ~~Race condition in ProfileFragment~~ → Menggunakan `viewLifecycleOwner.lifecycleScope`
4. ~~Dead code~~ → Removed unused functions

### Known Issues (Not Yet Solved)
1. **SettingsFragment non-fungsional** — Semua menu pengaturan hanya UI placeholder, termasuk Ubah Password (method `updatePassword()` sudah ada di FirestoreManager tapi belum dipanggil)
2. **Tombol Download di AdminFragment** — Hanya menampilkan Toast, belum mengunduh apapun
3. **Tidak ada pengumuman di Dashboard** — Method `getPengumuman()` sudah ada di FirestoreManager tapi belum digunakan oleh UI manapun
4. **Tidak ada offline support** — Semua operasi Firestore memerlukan koneksi internet
5. **Tidak ada ViewModel** — State management masih manual via callback, tidak ada lifecycle-aware ViewModel

### TODO
- [ ] Implement ViewModel dengan StateFlow untuk state management yang proper
- [ ] Fungsikan semua menu di SettingsFragment (hubungkan ke FirestoreManager)
- [ ] Implement repository pattern untuk decoupling FirestoreManager dari UI
- [ ] Tambahkan pengumuman di Dashboard menggunakan `getPengumuman()`
- [ ] Aktifkan tombol download di AdminFragment
- [ ] Add unit tests untuk ValidationUtils dan FirestoreManager
- [ ] Fix Locale deprecation warnings
- [ ] Tambahkan offline support dengan Firestore offline persistence
- [ ] Implement Hilt/Koin untuk dependency injection

---

## 10. Dependencies Tree

### Libraries Lengkap

```
app
├── Firebase (via BoM 32.8.0)
│   ├── com.google.firebase:firebase-analytics       # Analytics tracking
│   ├── com.google.firebase:firebase-auth             # Authentication (declared, custom auth used)
│   └── com.google.firebase:firebase-firestore(v26.3.0) # NoSQL database (primary)
│
├── AndroidX
│   ├── androidx.core:core-ktx                        # Kotlin extensions
│   ├── androidx.appcompat:appcompat                  # Backward compatibility
│   ├── androidx.activity:activity-ktx                # Activity result APIs
│   ├── androidx.constraintlayout:constraintlayout    # Flexible layout system
│   ├── androidx.cardview:cardview                    # Card UI component
│   ├── androidx.recyclerview:recyclerview            # List/Grid components
│   ├── androidx.lifecycle (runtime + viewmodel)      # Lifecycle-aware components
│   └── androidx.navigation (fragment-ktx + ui-ktx)   # Fragment navigation (v2.8.5)
│
├── UI & Image
│   ├── com.google.android.material:material          # Material Design 3 (Material You)
│   ├── com.github.bumptech.glide:glide:4.16.0        # Image loading & caching
│   └── de.hdodenhof:circleimageview:3.1.0            # Circular ImageView
│
├── Camera & ML
│   ├── androidx.camera:camera-core:1.4.0             # CameraX core
│   ├── androidx.camera:camera-camera2:1.4.0          # Camera2 implementation
│   ├── androidx.camera:camera-lifecycle:1.4.0        # Lifecycle-aware camera
│   ├── androidx.camera:camera-view:1.4.0             # PreviewView
│   └── com.google.mlkit:barcode-scanning:17.3.0      # QR/Barcode recognition
│
└── Build Plugins
    ├── com.android.application:8.9.1                 # Android Gradle Plugin
    ├── org.jetbrains.kotlin.android:2.1.0            # Kotlin Android plugin
    └── com.google.gms:google-services:4.4.1          # Google Services (Firebase)
```

### Konfigurasi Build

| Config | Value |
|--------|-------|
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 35 (Android 15) |
| **Compile SDK** | 36 |
| **Java Target** | 17 |
| **Kotlin** | 2.1.0 |
| **ViewBinding** | Enabled |
| **BuildConfig** | Enabled |

---

## 11. Alur Navigasi Aplikasi

```
LoginActivity (LAUNCHER, entry point)
├── Tombol "Daftar" → RegisterActivity
│   └── Berhasil daftar → finish() → kembali ke LoginActivity
└── Login sukses → DashboardActivity (host bottom navigation)
    ├── Tab 1: DashboardFragment    # Dashboard utama
    ├── Tab 2: AdminFragment        # Tagihan & Pembayaran
    ├── Tab 3: ScheduleFragment     # Jadwal Kuliah
    ├── Tab 4: ResultsFragment      # Nilai Akademik
    └── Tab 5: ProfileFragment      # Profil Mahasiswa
        │
        ├── Tombol Scan QR (di Dashboard) → QrScannerActivity
        │   └── Scan sukses → finish dengan result → kembali ke Dashboard
        │
        └── Tombol Pengaturan (di Profile) → SettingsFragment
            └── Tombol Keluar → hapus session → LoginActivity

Semua navigasi antar tab menggunakan Android Jetpack Navigation (bottom_nav_menu.xml).
Navigasi ke SettingsFragment dan QrScannerActivity menggunakan Intent/startActivity.
```