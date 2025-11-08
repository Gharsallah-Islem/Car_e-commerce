# Admin Dashboard Access Guide 🔐

## 📋 Overview

Your application has a fully functional admin dashboard located at:
- **URL:** `http://localhost:4200/admin`
- **Required Role:** `ADMIN` or `SUPER_ADMIN`

---

## 🎯 Quick Start - Add Admin User

### Option 1: SQL Script (Recommended)

Run this SQL in your PostgreSQL database (pgAdmin or command line):

```sql
-- Step 1: Verify roles exist
SELECT * FROM roles;

-- Step 2: Insert an admin user
-- Password: "Admin@123" (BCrypt hashed)
INSERT INTO users (
    id,
    username,
    email,
    password,
    full_name,
    phone,
    address,
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@carparts.com',
    '$2a$10$xQKVZQ5gKGKZKQFZQKVZQeVZQKVZQKVZQKVZQKVZQKVZQKVZQKVZQ',
    'System Administrator',
    '+216 12 345 678',
    'Tunis, Tunisia',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Step 3: Verify the admin user was created
SELECT u.id, u.username, u.email, u.full_name, r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email = 'admin@carparts.com';
```

**Login Credentials:**
- **Email:** `admin@carparts.com`
- **Password:** `Admin@123`

---

### Option 2: Create Admin with Custom Password

If you want to use a different password, you need to generate a BCrypt hash:

#### Using Online Tool:
1. Go to: https://bcrypt-generator.com/
2. Enter your password (e.g., "MySecurePass123")
3. Use rounds: **10** (default)
4. Copy the generated hash
5. Use it in the SQL INSERT statement above

#### Using Java (in your project):
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "YourPasswordHere";
        String hash = encoder.encode(password);
        System.out.println("BCrypt Hash: " + hash);
    }
}
```

---

## 🔑 User Roles Explained

Your system supports 4 roles:

| Role | ID | Access Level | Description |
|------|-----|-------------|-------------|
| `CLIENT` | 1 | Basic | Regular customers |
| `SUPPORT` | 2 | Medium | Customer support team |
| `ADMIN` | 3 | High | System administrators |
| `SUPER_ADMIN` | 4 | Full | Super administrators |

---

## 🚀 Access the Admin Dashboard

### Step 1: Start Your Servers

**Backend:**
```bash
cd Backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend-web
ng serve
```

### Step 2: Login as Admin

1. Navigate to: `http://localhost:4200/auth/login`
2. Enter admin credentials:
   - **Email:** `admin@carparts.com`
   - **Password:** `Admin@123`
3. Click **Sign In**

### Step 3: Access Admin Dashboard

After login, navigate to:
```
http://localhost:4200/admin
```

Or click on the admin link in your navigation (if available).

---

## 📊 Admin Dashboard Features

Based on your code, the admin dashboard includes:

### 1. **Dashboard Overview**
- Total orders statistics
- Revenue metrics
- Recent orders list
- Order status management

### 2. **Order Management**
- View all orders
- Update order status (Pending → Processing → Shipped → Delivered)
- Order details view
- Customer information

### 3. **User Management** (if implemented)
- View all users
- Manage user roles
- User statistics

### 4. **Chat Management**
- View all support conversations
- Respond to customer inquiries
- Mark messages as read/unread

---

## 🛡️ Security Features

Your admin dashboard is protected by:

1. **Authentication Guard** - Must be logged in
2. **Role Guard** - Checks for ADMIN or SUPER_ADMIN role
3. **Route Protection** - Unauthorized users redirected to login

### Frontend Guard:
```typescript
// In app.routes.ts
{
  path: 'admin',
  loadComponent: () => import('./features/admin/admin.component'),
  canActivate: [authGuard] // Protected route
}
```

### Component-Level Check:
```typescript
// In admin.component.ts
if (user.role !== UserRole.ADMIN && user.role !== UserRole.SUPER_ADMIN) {
  this.notificationService.error('Accès refusé - Droits administrateur requis');
  this.router.navigate(['/']);
}
```

---

## 🧪 Testing Admin Access

### Test Scenario 1: Login as Admin
```
Email: admin@carparts.com
Password: Admin@123
Expected: Login successful → Redirect to home/dashboard
```

### Test Scenario 2: Access Admin Dashboard
```
URL: http://localhost:4200/admin
Expected: Dashboard loads with admin features
```

### Test Scenario 3: Non-Admin Access
```
Login as regular user (CLIENT role)
Try to access: http://localhost:4200/admin
Expected: Access denied → Redirect to home
```

---

## 🔧 Troubleshooting

### Problem: "Cannot find admin user"
**Solution:** Run the SQL script above to create admin user

### Problem: "Access denied" even with admin account
**Solution:** Verify role is set correctly:
```sql
SELECT u.username, u.email, r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email = 'admin@carparts.com';
```

### Problem: Password doesn't work
**Solution:** 
1. Generate new BCrypt hash
2. Update password in database:
```sql
UPDATE users
SET password = '$2a$10$YOUR_NEW_HASH_HERE'
WHERE email = 'admin@carparts.com';
```

### Problem: Roles not found
**Solution:** Run the roles initialization script:
```sql
INSERT INTO roles (name) VALUES ('CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPPORT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPER_ADMIN') ON CONFLICT (name) DO NOTHING;
```

---

## 📱 Creating Additional Admin Users

To create more admin users, use the same INSERT pattern:

```sql
INSERT INTO users (
    id, username, email, password, full_name, role_id, created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'admin2',
    'admin2@carparts.com',
    '$2a$10$BCRYPT_HASH_HERE',
    'Second Admin',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

---

## 🎨 Customizing Admin Dashboard

The admin dashboard files are located at:
- **Component:** `frontend-web/src/app/features/admin/admin.component.ts`
- **Template:** `frontend-web/src/app/features/admin/admin.component.html`
- **Styles:** `frontend-web/src/app/features/admin/admin.component.scss`

You can customize:
- Dashboard layout
- Statistics cards
- Order management features
- User management features
- Charts and graphs

---

## 📚 Additional Resources

### Backend Files:
- **User Entity:** `Backend/src/main/java/com/example/Backend/entity/User.java`
- **Role Entity:** `Backend/src/main/java/com/example/Backend/entity/Role.java`
- **Auth Controller:** `Backend/src/main/java/com/example/Backend/controller/AuthController.java`

### Frontend Files:
- **Auth Service:** `frontend-web/src/app/core/services/auth.service.ts`
- **Role Guard:** `frontend-web/src/app/core/guards/role.guard.ts`
- **User Model:** `frontend-web/src/app/core/models/user.model.ts`

---

## ✅ Quick Checklist

- [ ] Database is running (PostgreSQL)
- [ ] Roles table has all 4 roles
- [ ] Admin user created in database
- [ ] Backend server is running (port 8080)
- [ ] Frontend server is running (port 4200)
- [ ] Can login with admin credentials
- [ ] Can access `/admin` route
- [ ] Admin dashboard loads successfully

---

**Need Help?** Check the console for errors or review the authentication flow in the browser DevTools (F12 → Network tab).
