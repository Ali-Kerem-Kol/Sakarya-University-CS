# Frontend Refactor Summary

Backend API sözleşmesine uyum sağlamak amacıyla yapılan değişiklikler aşağıdadır.

## 1. API Katmanı (`src/api`)

### `user.ts`
- **Refactor**: Kullanıcı dökümanları endpointleri `/me/documents` yerine `/users/me/documents` tabanına taşındı.
- **Added**: `uploadCv` fonksiyonu eklendi (`POST /users/me/documents/cv`).
- **Updated**: `fetchMyDocuments` ve `uploadSchedule` path'leri düzeltildi.

### `admin.ts`
- **Refactor**: Tekil `createMailJob` fonksiyonu kaldırıldı.
- **Added**: Backend ile birebir eşleşen 3 yeni mail fonksiyonu eklendi:
  - `createMailJobByCategory`
  - `createMailJobByPosting`
  - `createMailJobAllStudents`
- **Updated**: `createAdminUser` fonksiyonu `email`, `password`, `firstName`, `lastName` alacak şekilde güncellendi.
- **Interface**: `MailPayload` interface'i eklendi.

## 2. Admin Sayfaları (`src/pages/admin`)

### `AdminMailPage.tsx`
- **Refactor**: Form gönderim mantığı 3 ayrı endpoint'i kullanacak şekilde güncellendi.
- **Logic**: Kategori, İlan ve Tüm Öğrenciler sekmelerine göre doğru API fonksiyonu çağırılıyor.

### `AdminUsersPage.tsx`
- **UI**: Form alanlarına `Password`, `First Name` ve `Last Name` inputları eklendi.
- **Logic**: API çağrısı tüm kullanıcı bilgilerini gönderecek şekilde güncellendi.

## 3. Genel Kontroller
- **Register Page**: Multipart form yapısı (`data` JSON + `cv` File) kontrol edildi ve backend beklentisine uygun olduğu doğrulandı.
- **Public Postings**: Kapalı ilanların listelenmesi ve detay sayfasında başvuru butonunun pasif olması mantığı doğrulandı.
- **User Submissions**: Başvuru oluşturma (`POST /me/submissions`) yapısının `{ postingId }` gövdesi ile çalıştığı doğrulandı.

Frontend artık belirtilen Backend API Contract ile %100 uyumludur.
