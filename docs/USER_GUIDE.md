# User Guide

> Feature documentation by user role for the AutoParts Store platform

## Table of Contents
- [Getting Started](#getting-started)
- [Customer Features](#customer-features)
- [Admin Features](#admin-features)
- [Support Features](#support-features)
- [Driver Features](#driver-features)

---

## Getting Started

### Creating an Account

1. Navigate to the application homepage
2. Click **"Register"** in the top-right corner
3. Fill in your details:
   - Username (unique)
   - Email address
   - Password (min. 6 characters)
   - Full name
4. Click **"Create Account"**
5. Check your email for verification link
6. Click the verification link to activate your account

### Logging In

**Standard Login:**
1. Click **"Login"** in the navigation
2. Enter your email and password
3. Click **"Sign In"**

**Google Login:**
1. Click **"Continue with Google"**
2. Select your Google account
3. Authorize the application

### Password Recovery

1. Click **"Forgot Password"** on the login page
2. Enter your email address
3. Check your email for reset link
4. Click the link and set a new password

---

## Customer Features

### Browsing Products

**Home Page:**
- View featured products
- Browse by category
- See trending items
- Quick search bar

**Product Catalog:**
- Filter by category, brand, price range
- Sort by price, name, or date
- Grid/list view toggle
- Pagination for large results

**Product Details:**
- High-resolution images with gallery
- Full description
- Price and availability
- Add to cart button
- Related/similar products

### Using AI Mechanic

The AI Mechanic helps identify car parts from photos:

1. Navigate to **"AI Mechanic"** from the menu
2. Upload an image:
   - Click **"Upload Image"** to select a file
   - Or click **"Use Camera"** to take a photo
3. Wait for AI analysis (2-5 seconds)
4. View results:
   - Top predicted part with confidence %
   - Alternative predictions
   - Matching products from our store
5. Click any matched product to view/purchase

**Tips for Best Results:**
- Use clear, well-lit images
- Center the part in the frame
- Avoid cluttered backgrounds
- Single part per image works best

### Shopping Cart

**Adding Items:**
- Click **"Add to Cart"** on any product
- Adjust quantity before adding

**Managing Cart:**
- Click cart icon to view cart
- Adjust quantities with +/- buttons
- Remove items with trash icon
- See subtotal and estimated total

### Checkout Process

1. Review your cart and click **"Checkout"**
2. Enter/select delivery address
3. Choose payment method:
   - **Credit Card (Stripe):** Enter card details
   - **Cash on Delivery:** Pay when delivered
4. Add any special notes
5. Click **"Place Order"**
6. Receive order confirmation with tracking number

### Order Tracking

**View Orders:**
1. Go to **"My Orders"** in profile menu
2. See list of all orders with status
3. Click any order for details

**Track Delivery:**
1. Click **"Track"** on an active order
2. Or enter tracking number at `/track/YOUR-TRACKING-NUMBER`
3. View:
   - Real-time driver location on map
   - Delivery timeline/status updates
   - Estimated arrival time
   - Driver contact information

### Profile Management

**Edit Profile:**
1. Click your name → **"Profile"**
2. Update:
   - Full name
   - Phone number
   - Profile picture (click to upload)
3. Click **"Save Changes"**

**Change Password:**
1. Go to Profile → **"Security"**
2. Enter current password
3. Enter new password twice
4. Click **"Update Password"**

### AI Chat Support

Get instant help from our AI assistant:

1. Click the chat icon (bottom-right)
2. Type your question or request
3. AI provides:
   - Product recommendations
   - Navigation help
   - Order assistance
   - General car parts advice
4. Create support ticket if AI can't help

### Submitting Reclamations

For issues requiring human support:

1. Go to **"My Reclamations"** or click **"Create Ticket"**
2. Fill in:
   - Subject (brief title)
   - Category (delivery, product, payment, etc.)
   - Detailed description
   - Related order (if applicable)
3. Submit and receive ticket number
4. Track status: OPEN → IN_PROGRESS → RESOLVED

---

## Admin Features

### Dashboard Overview

The admin dashboard (`/admin/dashboard`) provides:

- **Sales Metrics:** Today, week, month, year
- **Order Statistics:** Pending, processing, delivered
- **Revenue Charts:** Trend graphs
- **Top Products:** Best sellers
- **Recent Orders:** Quick access
- **Low Stock Alerts:** Inventory warnings

### Product Management

**View Products:**
- Navigate to **Admin → Products**
- Search, filter, and sort products
- View stock levels and status

**Add Product:**
1. Click **"Add Product"**
2. Fill in details:
   - Name, description, SKU
   - Category and brand
   - Price and stock quantity
3. Upload product images
4. Set active/inactive status
5. Click **"Save"**

**Edit Product:**
1. Click edit icon on product row
2. Modify any fields
3. Add/remove images
4. Save changes

**Bulk Operations:**
- Select multiple products
- Update stock, status, or category
- Export to CSV/Excel

### Category & Brand Management

**Categories:**
- Create product categories
- Add descriptions
- Organize product catalog

**Brands:**
- Add manufacturer brands
- Upload brand logos
- Link products to brands

### Order Management

**View Orders:**
- Navigate to **Admin → Orders**
- Filter by status, date, customer
- Search by order ID or tracking number

**Process Order:**
1. Click order to view details
2. Review items, address, payment status
3. Update status:
   - PENDING → CONFIRMED (after payment verification)
   - CONFIRMED → SHIPPED (when assigning delivery)
4. Assign to driver for delivery
5. Add internal notes if needed

**Order Timeline:**
- View complete order history
- See all status changes
- Track who made changes

### Inventory Management

**Dashboard:**
- Total products and stock value
- Low stock items
- Out of stock alerts
- Recent stock movements

**Suppliers:**
- Add/manage suppliers
- Contact information
- Order history per supplier

**Stock Movements:**
- View all inventory transactions
- Filter by product, type, date
- See movement reasons

**Purchase Orders:**
1. Click **"Create PO"**
2. Select supplier
3. Add products and quantities
4. Submit order
5. Mark as received when stock arrives

**Auto-Reorder:**
1. Go to **Reorder Settings**
2. Set minimum stock thresholds
3. Configure reorder quantities
4. Enable automatic ordering
5. System creates POs when stock is low

### Delivery Management

**Active Deliveries:**
- Real-time map with all drivers
- Delivery status overview
- Click delivery for details

**Assign Delivery:**
1. Open order requiring delivery
2. Click **"Assign Driver"**
3. Select available driver
4. Set estimated delivery time
5. Driver receives notification

**Delivery History:**
- Complete delivery records
- Filter by date, status, driver
- Performance metrics

### Driver Management

**View Drivers:**
- List of all delivery personnel
- Current status (available, busy, offline)
- Contact information

**Add Driver:**
1. Click **"Add Driver"**
2. Enter driver details
3. License and vehicle information
4. Create associated user account
5. Activate driver

**Monitor Drivers:**
- Real-time location on map
- Current assignment
- Today's deliveries
- Performance history

### User Management

**View Users:**
- All registered users
- Filter by role
- Search by name/email

**Manage User:**
- View user details
- See order history
- Activate/deactivate account
- Change role (Super Admin only)

### Analytics & Reports

**Sales Analytics:**
- Revenue by period
- Growth comparisons
- Product performance

**User Analytics:**
- New registrations
- Active users
- Customer retention

**Export Reports:**
- Download as PDF
- Export to Excel
- Custom date ranges

---

## Support Features

### Support Dashboard

Access at `/support/dashboard`:
- Open tickets count
- Average response time
- Resolution metrics
- Priority breakdown

### Ticket Management

**View Tickets:**
- Navigate to **Support → Tickets**
- Filter by status, priority, category
- Sort by date or priority

**Handle Ticket:**
1. Click ticket to open details
2. Review customer message
3. Check related order if applicable
4. Write response
5. Update status:
   - OPEN → IN_PROGRESS (when working on it)
   - IN_PROGRESS → RESOLVED (when fixed)
   - RESOLVED → CLOSED (after customer confirmation)
6. Set priority if needed

### AI Conversation Review

**Monitor Conversations:**
- View all AI chat sessions
- See full message history
- Identify issues AI couldn't resolve

**Intervene:**
- Take over conversation from AI
- Send human response
- Escalate to ticket if needed

### Performance Metrics

**Track KPIs:**
- Average response time
- Resolution rate
- Customer satisfaction
- Tickets per category

---

## Driver Features

### Driver Dashboard

Access at `/driver`:
- Today's assigned deliveries
- Map with route overview
- Delivery queue

### Managing Deliveries

**View Assignment:**
- See assigned orders
- Customer address and contact
- Delivery notes/instructions

**Start Delivery:**
1. Click **"Start Delivery"**
2. GPS tracking activates
3. Customer sees live location

**Update Status:**
- IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
- Add delivery notes if needed
- Capture signature if required

**Complete Delivery:**
1. Arrive at destination
2. Hand over package
3. Click **"Mark as Delivered"**
4. System updates order status
5. Customer receives notification

### Location Sharing

Your location is automatically shared:
- When delivery is active
- Updates every 10-30 seconds
- Customers see on tracking map
- Admin can monitor all drivers

### Delivery History

- View past deliveries
- Performance statistics
- Earnings (if applicable)

---

## Mobile App

The Android mobile app provides the same customer features:

| Feature | Location |
|---------|----------|
| Browse Products | Home → Products |
| Search | Top search bar |
| Cart | Shopping cart icon |
| Orders | Profile → My Orders |
| Track Delivery | Orders → Track |
| AI Mechanic | Menu → AI Mechanic |
| Chat Support | Menu → Chat |
| Profile | Bottom nav → Profile |

**Camera Features:**
- Use device camera for AI Mechanic
- Quick scan car parts
- Instant product matching

**Notifications:**
- Order confirmation
- Status updates
- Delivery alerts
