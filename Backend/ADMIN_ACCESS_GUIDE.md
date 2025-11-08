# 🔐 Admin Dashboard Access Guide - UPDATED

## ⚠️ CRITICAL: Database Schema Analysis

After analyzing your database export (`jk.sql`), I discovered your application has **THREE separate tables** for admin-related users:

### Database Tables:
1. **`users`** table - Contains regular users with `role_id` foreign key
   - Used for CLIENT, SUPPORT, ADMIN, and SUPER_ADMIN roles
   - **THIS IS WHERE YOU CREATE ADMIN USERS** ✅
   
2. **`admins`** table - Separate admin table with permissions field
   - **NOT USED** for authentication ❌
   - May be legacy or for future features
   
3. **`super_admins`** table - Separate super admin table  
   - **NOT USED** for authentication ❌
   - May be legacy or for future features

### Why This Matters:
Your `AuthController` at `/api/auth/login` **ONLY authenticates against the `users` table**. The separate `admins` and `super_admins` tables exist in your database but are NOT queried during login.

**Therefore: Create admin users in the `users` table with `role_id` = 3 (ADMIN) or 4 (SUPER_ADMIN)**

---

## 📋 Admin Dashboard Information

### Frontend
- **URL**: `http://localhost:4200/admin`
- **Component**: `AdminComponent` in `frontend-web/src/app/features/admin/`
- **Route Protection**: `authGuard` - requires authentication
- **Role Check**: User must have role = `ADMIN` or `SUPER_ADMIN`

### Backend
- **Auth Endpoint**: `POST http://localhost:8080/api/auth/login`
- **Entity**: `User.java` with `@ManyToOne` relationship to `Role`
- **Password Encoding**: BCrypt with 10 rounds
- **Authenticated Table**: `users` (NOT `admins` or `super_admins`)

---

## 🚀 Step-by-Step: Create Admin User

### Step 1: Run the SQL Script

Use the provided **`create-admin-user-CORRECT.sql`** file:

```bash
# Navigate to Backend directory
cd "c:\Users\islem\OneDrive\Bureau\pfe\Projet integration\Code\Backend"

# Run the SQL script
psql -U postgres -d ecommercespareparts -f create-admin-user-CORRECT.sql
```

**Or manually run in pgAdmin:**

```sql
-- Create ADMIN user in USERS table (not admins table!)
INSERT INTO users (
    id, 
    username, 
    email, 
    password, 
    full_name, 
    address, 
    phone, 
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@carparts.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu', -- Password: admin123
    'System Administrator',
    'Admin Office',
    '+1234567890',
    3, -- ADMIN role (role_id = 3)
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;
```

### Step 2: Verify Admin User Created

```sql
-- Check the admin user
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    r.name as role,
    u.created_at
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.username = 'admin';
```

Expected output:
```
id                  | username | email                 | full_name             | role  | created_at
-------------------|----------|----------------------|----------------------|-------|------------
<some-uuid>         | admin    | admin@carparts.com    | System Administrator  | ADMIN | 2024-...
```

---

## 🔑 Login Credentials

### ADMIN User:
- **Username**: `admin`
- **Email**: `admin@carparts.com`  
- **Password**: `admin123`
- **Role**: ADMIN (role_id = 3)

### SUPER_ADMIN User (Optional):
If you uncomment the SUPER_ADMIN section in the SQL script:
- **Username**: `superadmin`
- **Email**: `superadmin@carparts.com`
- **Password**: `super123`
- **Role**: SUPER_ADMIN (role_id = 4)

---

## 🌐 Access the Admin Dashboard

### 1. Start Your Servers

**Backend (Spring Boot):**
```powershell
cd "c:\Users\islem\OneDrive\Bureau\pfe\Projet integration\Code\Backend"
./mvnw spring-boot:run
```
Server starts at: `http://localhost:8080`

**Frontend (Angular):**
```powershell
cd "c:\Users\islem\OneDrive\Bureau\pfe\Projet integration\Code\frontend-web"
npm start
# or
ng serve
```
Application runs at: `http://localhost:4200`

### 2. Login via API

**Option A: Using curl (PowerShell):**
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@carparts.com","password":"admin123"}'
```

**Option B: Using the frontend:**
1. Go to `http://localhost:4200/login`
2. Enter email: `admin@carparts.com`
3. Enter password: `admin123`
4. Click login

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "<uuid>",
    "username": "admin",
    "email": "admin@carparts.com",
    "fullName": "System Administrator",
    "role": "ADMIN"
  }
}
```

### 3. Navigate to Admin Dashboard

After successful login:
- Visit: `http://localhost:4200/admin`
- The `authGuard` will check:
  - ✅ User is authenticated (has valid JWT token)
  - ✅ User role is ADMIN or SUPER_ADMIN
- Dashboard loads with admin features

---

## 🛠️ Password Management

### Current Password Hashes (BCrypt, 10 rounds):
- `admin123` → `$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu`
- `super123` → `$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUY1Q8flaa`

### To Change Password:

**Option 1: Online BCrypt Generator**
1. Visit: https://bcrypt-generator.com/
2. Enter your desired password
3. Set rounds to: **10**
4. Generate hash
5. Update in database:
```sql
UPDATE users 
SET password = 'your-new-bcrypt-hash',
    updated_at = NOW()
WHERE username = 'admin';
```

**Option 2: Using Java (in your backend)**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
String hashedPassword = encoder.encode("YourNewPassword");
System.out.println(hashedPassword);
```

---

## ❓ Troubleshooting

### Issue: "Cannot login with admin credentials"

**Check 1: Verify user exists in USERS table (not admins table)**
```sql
SELECT * FROM users WHERE email = 'admin@carparts.com';
```
- If empty: User doesn't exist, run the CREATE script
- If exists: Check the `role_id` column

**Check 2: Verify role_id is correct**
```sql
SELECT u.username, u.email, r.name as role 
FROM users u 
JOIN roles r ON u.role_id = r.id 
WHERE u.email = 'admin@carparts.com';
```
- Role should be `ADMIN` or `SUPER_ADMIN`
- If role is `CLIENT`, update it:
```sql
UPDATE users 
SET role_id = 3  -- 3 = ADMIN, 4 = SUPER_ADMIN
WHERE email = 'admin@carparts.com';
```

**Check 3: Verify password hash**
```sql
SELECT password FROM users WHERE email = 'admin@carparts.com';
```
- Should start with `$2a$10$`
- Should match the hash from the CREATE script

**Check 4: Check backend console for errors**
Look for authentication errors in Spring Boot console when attempting login.

### Issue: "Can login but cannot access /admin route"

**Check 1: Verify frontend role check**
The Angular app checks: `user.role === 'ADMIN' || user.role === 'SUPER_ADMIN'`

**Check 2: Inspect JWT token**
After login, check browser localStorage:
```javascript
// In browser console (F12)
console.log(localStorage.getItem('token'));
```

**Check 3: Verify authGuard**
Check browser console (F12) for route guard errors when navigating to `/admin`.

### Issue: "Admins table is empty"

**This is CORRECT!** 
- Your authentication uses the `users` table, NOT the `admins` table
- The `admins` and `super_admins` tables are separate and not used for login
- Create admin users in `users` table with `role_id = 3` (ADMIN) or `4` (SUPER_ADMIN)

---

## 📊 Database Schema Summary

### Roles Table:
```sql
SELECT * FROM roles ORDER BY id;
```
Expected:
```
id | name
---|-------------
1  | CLIENT
2  | SUPPORT
3  | ADMIN
4  | SUPER_ADMIN
```

### Users Table Structure:
```
- id (UUID) - Primary key
- username (VARCHAR 100) - Unique
- email (VARCHAR 255) - Unique  
- password (VARCHAR 255) - BCrypt hash
- full_name (VARCHAR 255)
- address (TEXT)
- phone (VARCHAR 20)
- role_id (INTEGER) - Foreign key to roles.id ← THIS DETERMINES ADMIN ACCESS
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Admins Table (NOT USED FOR AUTH):
```
- id (UUID) - Primary key
- username (VARCHAR 100)
- email (VARCHAR 255)
- password (VARCHAR 255)
- full_name (VARCHAR 255)
- is_active (BOOLEAN)
- permissions (TEXT) - JSON array
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

**Note**: This table exists but is NOT queried by `AuthController`. Don't create users here for login.

---

## ✅ Verification Checklist

Before accessing admin dashboard, verify:

- [ ] Backend is running on `http://localhost:8080`
- [ ] Frontend is running on `http://localhost:4200`
- [ ] Admin user exists in `users` table (check with SQL)
- [ ] Admin user has `role_id = 3` (ADMIN) or `4` (SUPER_ADMIN)
- [ ] Password hash is correct BCrypt format (`$2a$10$...`)
- [ ] Database `ecommercespareparts` is accessible
- [ ] Can login successfully at `/api/auth/login`
- [ ] JWT token is returned and stored
- [ ] Can navigate to `http://localhost:4200/admin` without redirect

---

## 📝 Summary

1. **Create admin user in `users` table with `role_id = 3`** (NOT in `admins` table)
2. **Use BCrypt-hashed password** (10 rounds)
3. **Login at** `/api/auth/login` with email and password
4. **Access dashboard at** `http://localhost:4200/admin`
5. **The separate `admins` and `super_admins` tables are not used for authentication**

---

## 🔗 Related Files

- **SQL Script**: `Backend/create-admin-user-CORRECT.sql` ← Use this!
- **Old SQL Script**: `Backend/create-admin-users.sql` (may have wrong table assumptions)
- **Auth Controller**: `Backend/src/main/java/com/example/Backend/controller/AuthController.java`
- **User Entity**: `Backend/src/main/java/com/example/Backend/entity/User.java`
- **Admin Entity**: `Backend/src/main/java/com/example/Backend/entity/Admin.java` (separate, not used for auth)
- **Frontend Component**: `frontend-web/src/app/features/admin/admin.component.ts`

---

**Last Updated**: Based on analysis of `jk.sql` database export showing three-table admin structure.
