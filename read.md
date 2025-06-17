# 📋 Panduan Testing API dengan Postman

## 🚀 Persiapan Awal

### 1. Setup Environment
Buat environment baru di Postman dengan variabel:
- `base_url`: `http://localhost:8081`
- `token`: (akan diisi setelah login)
- `user_id`: (akan diisi setelah register/login)

### 2. Urutan Testing yang Disarankan
1. **Register User** → Dapatkan user baru
2. **Login** → Dapatkan token untuk authentication
3. **Create Category** → Buat kategori untuk transaksi
4. **Create Transaction** → Buat transaksi
5. **Test CRUD operations** → Test semua operasi

---

## 👤 USER ENDPOINTS

### 1. Register User
\`\`\`
POST {{base_url}}/api/users/register
Content-Type: application/json

{
  "userName": "Test User",
  "userEmail": "test@example.com",
  "userPassword": "password123"
}
\`\`\`

**Response Success (201):**
\`\`\`json
{
  "userId": 1,
  "userName": "Test User",
  "userEmail": "test@example.com",
  "userProfile": "default.jpg",
  "isDeleted": false
}
\`\`\`

### 2. Login User
\`\`\`
POST {{base_url}}/api/users/login
Content-Type: application/json

{
  "userEmail": "test@example.com",
  "userPassword": "password123"
}
\`\`\`

**Response Success (200):**
\`\`\`json
{
  "user": {
    "userId": 1,
    "userName": "Test User",
    "userEmail": "test@example.com",
    "userProfile": "default.jpg",
    "isDeleted": false
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
\`\`\`

**⚠️ PENTING:** Salin token dari response dan simpan di environment variable `token`

### 3. Get User Profile
\`\`\`
GET {{base_url}}/api/users/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

### 4. Update Profile
\`\`\`
PUT {{base_url}}/api/users/{{user_id}}/profile
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "userName": "Updated Name",
  "userProfile": "new_profile.jpg"
}
\`\`\`

### 5. Upload Profile Photo
\`\`\`
POST {{base_url}}/api/users/{{user_id}}/profile-photo
Authorization: Bearer {{token}}
Content-Type: multipart/form-data

Form Data:
- userName: "User with Photo"
- profilePhoto: [SELECT FILE]
\`\`\`

**Cara di Postman:**
1. Pilih Body → form-data
2. Key: `userName`, Value: `User with Photo`
3. Key: `profilePhoto`, Type: File, Value: [pilih file gambar]

### 6. Update Password
\`\`\`
PUT {{base_url}}/api/users/{{user_id}}/password
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "currentPassword": "password123",
  "newPassword": "newpassword456"
}
\`\`\`

### 7. Logout
\`\`\`
POST {{base_url}}/api/users/logout
Authorization: Bearer {{token}}
\`\`\`

---

## 📂 CATEGORY ENDPOINTS

### 1. Create Category (Expense)
\`\`\`
POST {{base_url}}/api/categories/user/{{user_id}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "categoryName": "Makanan",
  "isExpense": true
}
\`\`\`

### 2. Create Category (Income)
\`\`\`
POST {{base_url}}/api/categories/user/{{user_id}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "categoryName": "Gaji",
  "isExpense": false
}
\`\`\`

### 3. Get All Categories
\`\`\`
GET {{base_url}}/api/categories/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

### 4. Update Category
\`\`\`
PUT {{base_url}}/api/categories/1/user/{{user_id}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "categoryName": "Makanan & Minuman",
  "isExpense": true
}
\`\`\`

### 5. Delete Category
\`\`\`
DELETE {{base_url}}/api/categories/1/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`


### 1. Create Transaction (Expense)
\`\`\`
POST {{base_url}}/api/transactions
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "transactionAmount": 50000,
  "transactionDescription": "Makan siang di restoran",
  "transactionDate": "2024-01-15",
  "categoryId": 1
}
\`\`\`

### 2. Create Transaction (Income)
\`\`\`
POST {{base_url}}/api/transactions
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "transactionAmount": 5000000,
  "transactionDescription": "Gaji bulanan",
  "transactionDate": "2024-01-01",
  "categoryId": 2
}
\`\`\`

### 3. Get All Transactions
\`\`\`
GET {{base_url}}/api/transactions/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

### 4. Get Transaction by ID
\`\`\`
GET {{base_url}}/api/transactions/1/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

### 5. Update Transaction
\`\`\`
PUT {{base_url}}/api/transactions/1/user/{{user_id}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "transactionAmount": 75000,
  "transactionDescription": "Makan malam di restoran mewah",
  "transactionDate": "2024-01-15",
  "categoryId": 1
}
\`\`\`

### 6. Delete Transaction
\`\`\`
DELETE {{base_url}}/api/transactions/1/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

### 7. Monthly Summary
\`\`\`
GET {{base_url}}/api/transactions/summary/monthly/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

---

## 📊 DASHBOARD ENDPOINT

### Get Dashboard Data
\`\`\`
GET {{base_url}}/api/dashboard/user/{{user_id}}
Authorization: Bearer {{token}}
\`\`\`

**Response:**
\`\`\`json
{
  "monthlySummary": {
    "totalIncome": 5000000,
    "totalExpense": 1500000,
    "netBalance": 3500000
  },
  "recentTransactions": [...],
  "totalTransactions": 25
}
\`\`\`

---

## 🔧 Tips Testing

### 1. Environment Variables
Gunakan variabel ini di Postman:
- `{{base_url}}` = `http://localhost:8081`
- `{{token}}` = token dari login response
- `{{user_id}}` = user ID dari register/login response

### 2. Headers yang Diperlukan
- **Authentication:** `Authorization: Bearer {{token}}`
- **JSON:** `Content-Type: application/json`
- **File Upload:** `Content-Type: multipart/form-data`

### 3. Status Code yang Diharapkan
- **200:** Success (GET, PUT, DELETE)
- **201:** Created (POST)
- **400:** Bad Request (validasi gagal)
- **401:** Unauthorized (token salah/expired)
- **403:** Forbidden (akses ditolak)
- **404:** Not Found (resource tidak ditemukan)

### 4. Urutan Testing
1. Register → Login → Get Profile
2. Create Category → Get Categories
3. Create Transaction → Get Transactions
4. Update operations
5. Delete operations
6. Dashboard

### 5. Error Handling
Jika mendapat error, cek:
- Apakah server sudah running?
- Apakah token masih valid?
- Apakah format JSON benar?
- Apakah user_id dan resource_id sesuai?

---

## 📝 Contoh Skenario Testing Lengkap

### Skenario 1: User Baru
1. Register user baru
2. Login dengan user tersebut
3. Update profile dengan foto
4. Buat kategori income dan expense
5. Buat beberapa transaksi
6. Lihat dashboard
7. Update dan delete beberapa data

### Skenario 2: Error Testing
1. Login dengan password salah
2. Akses endpoint tanpa token
3. Akses data user lain
4. Upload file bukan gambar
5. Buat transaksi dengan kategori yang tidak ada

Dengan panduan ini, Anda dapat dengan mudah testing semua endpoint API menggunakan Postman!
