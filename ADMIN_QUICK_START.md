# 🚀 Admin Dashboard - Quick Start Guide

**Ready to test your admin dashboard in 5 minutes!**

---

## ✅ Prerequisites

- ✅ PostgreSQL running
- ✅ Database `ecommercespareparts` exists
- ✅ Admin user created (see below if not)
- ✅ Node.js and npm installed
- ✅ Java 17+ and Maven installed

---

## 🏃 Quick Start (5 Steps)

### Step 1: Start Backend (Terminal 1)
```bash
cd Backend
mvn spring-boot:run
```

Wait for: `Started BackendApplication in X seconds`

Backend URL: `http://localhost:8080`

### Step 2: Start Frontend (Terminal 2)
```bash
cd frontend-web
npm install  # Only first time
ng serve
```

Wait for: `Compiled successfully`

Frontend URL: `http://localhost:4200`

### Step 3: Create Admin User (If Needed)

If you don't have an admin user, run this SQL:

```sql
-- Connect to your database
psql -U postgres -d ecommercespareparts

-- Create admin user
INSERT INTO users (
    id, username, email, password, full_name, 
    address, phone, role_id, created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@carparts.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu',
    'System Administrator',
    'Admin Office',
    '+1234567890',
    3,  -- ADMIN role
    NOW(),
    NOW()
) ON CONFLICT (username) DO NOTHING;
```

**Credentials**: 
- Email: `admin@carparts.com`
- Password: `admin123`

### Step 4: Login
1. Open browser: `http://localhost:4200/auth/login`
2. Enter email: `admin@carparts.com`
3. Enter password: `admin123`
4. Click **Sign In**

### Step 5: Access Admin Dashboard
1. Navigate to: `http://localhost:4200/admin`
2. You should see the admin dashboard with 5 tabs:
   - 📊 **Analytics** - Dashboard with charts and KPIs
   - 📦 **Inventory** - Suppliers, POs, Stock, Reorder
   - 🚚 **Delivery** - Delivery tracking and management
   - 🎫 **Support** - Support tickets (Reclamations)
   - 🛠️ **Management** - Products, Orders, Users

---

## 🧪 Quick Test

### Test Analytics (Should Work Immediately):
1. Click **Analytics** tab
2. You should see:
   - KPI cards with numbers
   - Sales chart
   - Category performance chart
   - Top products table
   - Recent activities

### Test Support Management:
1. Click **Support** tab
2. You should see:
   - Statistics cards
   - Tickets table (may be empty if no tickets)
3. Try creating a test ticket from client side first

### Test Delivery Management:
1. Click **Delivery** tab
2. You should see:
   - Statistics cards
   - Deliveries table (may be empty if no deliveries)
3. Deliveries are created automatically when orders are placed

### Test Inventory Management:
1. Click **Inventory** tab
2. You should see:
   - Statistics cards
   - 4 sub-tabs: Suppliers, Purchase Orders, Stock Movements, Reorder Settings
3. Try adding a supplier:
   - Click **Add Supplier** button
   - Fill in the form
   - Click **Save**
   - Should appear in the table

---

## 🐛 Troubleshooting

### Backend Won't Start

**Error**: Port 8080 already in use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Error**: Database connection failed
- Check PostgreSQL is running
- Verify database name in `Backend/src/main/resources/application.yml`
- Check username/password

### Frontend Won't Start

**Error**: Port 4200 already in use
```bash
# Kill the process and restart
ng serve --port 4201
```

**Error**: Module not found
```bash
cd frontend-web
rm -rf node_modules package-lock.json
npm install
```

### Can't Login

**Error**: 401 Unauthorized
- Admin user doesn't exist → Run SQL script above
- Wrong password → Use `admin123`
- Backend not running → Start backend

**Error**: 403 Forbidden
- User doesn't have admin role
- Check role_id in database:
```sql
SELECT u.email, r.name as role 
FROM users u 
JOIN roles r ON u.role_id = r.id 
WHERE u.email = 'admin@carparts.com';
```
- Should show role = 'ADMIN' or 'SUPER_ADMIN'

### Data Not Loading

**Check Browser Console (F12)**:
- Look for red errors
- Check Network tab for failed requests

**Check Backend Console**:
- Look for exceptions or errors
- Verify endpoints are being called

**Common Issues**:
- CORS error → Backend CORS config issue
- 404 error → Wrong API URL or endpoint doesn't exist
- 500 error → Backend error, check backend console

---

## 📊 What You Should See

### Analytics Dashboard:
- ✅ 6 KPI cards (Revenue, Orders, Users, Products, etc.)
- ✅ Line chart showing sales trend
- ✅ Pie chart showing category performance
- ✅ Doughnut chart showing order status
- ✅ Customer analytics section
- ✅ Top products table
- ✅ Recent activities timeline
- ✅ Inventory alerts

### Inventory Management:
- ✅ 6 statistics cards
- ✅ Suppliers tab with table and forms
- ✅ Purchase Orders tab with PO management
- ✅ Stock Movements tab with movement history
- ✅ Reorder Settings tab with auto-reorder config

### Delivery Management:
- ✅ 8 statistics cards
- ✅ Deliveries table with tracking numbers
- ✅ Status badges and progress bars
- ✅ Courier assignment
- ✅ Tracking search
- ✅ Action menus

### Support Management:
- ✅ 7 statistics cards
- ✅ Tickets table with status/priority
- ✅ Ticket detail view
- ✅ Response system
- ✅ Agent assignment
- ✅ Status management

---

## 🎯 Quick Feature Tests

### Create a Supplier:
1. Go to **Inventory** → **Suppliers** tab
2. Fill in form:
   - Name: "Test Supplier"
   - Contact Person: "John Doe"
   - Email: "test@supplier.com"
   - Phone: "+1234567890"
   - Address: "123 Test St"
   - Status: Active
3. Click **Save**
4. Should appear in table below

### Track a Delivery:
1. Go to **Delivery** tab
2. Enter a tracking number in search box
3. Click **Track**
4. Should show delivery status (or "not found" if doesn't exist)

### View Ticket:
1. Go to **Support** tab
2. Click on any ticket in the table
3. Should show ticket details
4. Try adding a response

---

## 📝 API Endpoints Available

### Analytics:
- `GET /api/analytics/dashboard/growth` - Dashboard stats
- `GET /api/analytics/top-products` - Top products
- `GET /api/analytics/category-performance` - Categories
- `GET /api/analytics/customers` - Customer analytics
- And 7 more...

### Inventory:
- `GET /api/inventory/suppliers` - List suppliers
- `POST /api/inventory/suppliers` - Create supplier
- `GET /api/inventory/purchase-orders` - List POs
- `GET /api/inventory/stock-movements` - List movements
- And 36 more...

### Delivery:
- `GET /api/delivery` - List deliveries
- `GET /api/delivery/track/{trackingNumber}` - Track
- `PATCH /api/delivery/{id}/status` - Update status
- And 17 more...

### Support:
- `GET /api/reclamations` - List tickets
- `POST /api/reclamations/{id}/response` - Add response
- `PATCH /api/reclamations/{id}/status` - Update status
- And 17 more...

---

## 🎨 UI Features

### Purple Gradient Theme:
- Beautiful purple gradient (#667eea → #764ba2)
- Consistent across all admin sections
- Professional Material Design components

### Responsive Design:
- Works on desktop, tablet, and mobile
- Adaptive layouts
- Touch-friendly controls

### Interactive Elements:
- Hover effects on cards and buttons
- Smooth transitions
- Loading spinners
- Success/error notifications
- Confirmation dialogs

---

## 📱 Browser DevTools Tips

### Network Tab (F12):
- See all API calls
- Check request/response
- Verify authentication headers
- Debug failed requests

### Console Tab:
- See JavaScript errors
- View console.log messages
- Check for warnings

### Application Tab:
- View localStorage (JWT token)
- Check cookies
- Inspect session data

---

## ✅ Success Checklist

After following this guide, you should have:

- [ ] Backend running on port 8080
- [ ] Frontend running on port 4200
- [ ] Admin user created
- [ ] Successfully logged in
- [ ] Admin dashboard accessible
- [ ] Analytics tab showing data
- [ ] Inventory tab showing suppliers
- [ ] Delivery tab showing deliveries
- [ ] Support tab showing tickets
- [ ] No errors in browser console
- [ ] No errors in backend console

---

## 🎉 You're Ready!

Your admin dashboard is now fully functional with:
- ✅ Real backend integration
- ✅ 70+ API endpoints connected
- ✅ Beautiful UI with Material Design
- ✅ Full CRUD operations
- ✅ Statistics and analytics
- ✅ Error handling
- ✅ Loading states
- ✅ User notifications

**Start testing and enjoy your admin dashboard!** 🚀

---

## 📚 More Documentation

- `ADMIN_INTEGRATION_COMPLETE.md` - What we built
- `ADMIN_COMPLETE_STATUS_REPORT.md` - Complete status
- `Backend/ADMIN_ACCESS_GUIDE.md` - Detailed access guide
- `Backend/ADMIN_SETUP_GUIDE.md` - Setup instructions

---

**Need Help?** Check the troubleshooting section above or review the detailed documentation.

