# Complete Delivery System Mocking Guide

## Overview
Since you cannot integrate with ONdelivery API (not recognized/deployed yet), we'll create a **fully functional mock delivery system** that simulates all delivery operations internally.

---

## 🎯 Changes Required

### 1. **Database Schema Changes**

#### Current Schema Issues:
The current `deliveries` table references `ondelivery_tracking_id` which assumes external API integration.

#### ✅ Recommended Schema Updates:

```sql
-- Updated deliveries table structure
ALTER TABLE deliveries 
DROP COLUMN IF EXISTS ondelivery_tracking_id;

ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(255) UNIQUE NOT NULL,
ADD COLUMN IF NOT EXISTS delivery_notes TEXT,
ADD COLUMN IF NOT EXISTS estimated_delivery TIMESTAMP,
ADD COLUMN IF NOT EXISTS actual_delivery TIMESTAMP,
ADD COLUMN IF NOT EXISTS driver_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS current_location TEXT;

-- Update status to include more detailed statuses
-- Possible values: PROCESSING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, CANCELLED

-- Add index for tracking number
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_number ON deliveries(tracking_number);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
```

**Updated SQL Schema:**
```sql
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    tracking_number VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PROCESSING',
    address TEXT NOT NULL,
    delivery_notes TEXT,
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    driver_name VARCHAR(255),
    driver_phone VARCHAR(20),
    pickup_time TIMESTAMP,
    current_location TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_tracking_number ON deliveries(tracking_number);
CREATE INDEX idx_deliveries_status ON deliveries(status);
```

---

### 2. **Entity Changes** ✅ (Already Done!)

Your `Delivery.java` entity is **already perfect** for the mock system! It includes:
- ✅ Tracking number generation (internal)
- ✅ Status management (PROCESSING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED)
- ✅ Driver information (driver_name, driver_phone)
- ✅ Estimated and actual delivery times
- ✅ Delivery address and notes

**No changes needed to Delivery.java!**

---

### 3. **Service Layer Enhancements**

#### Current Implementation Status:
Your `DeliveryServiceImpl.java` is **90% ready**! Just needs a few enhancements:

#### ✅ Add These Methods:

```java
// Add to DeliveryServiceImpl.java

/**
 * Simulate delivery progress (mock the delivery company's work)
 * This can be called by a scheduled task or manually by admin
 */
@Override
@Transactional
public Delivery simulateDeliveryProgress(UUID deliveryId) {
    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + deliveryId));

    // Simulate progression through delivery stages
    switch (delivery.getStatus()) {
        case Delivery.STATUS_PROCESSING:
            // Assign a mock driver
            delivery.setStatus(Delivery.STATUS_IN_TRANSIT);
            delivery.setDriverName(generateRandomDriverName());
            delivery.setDriverPhone(generateRandomPhone());
            delivery.setPickupTime(LocalDateTime.now());
            delivery.setCurrentLocation("Warehouse - Package picked up");
            break;
            
        case Delivery.STATUS_IN_TRANSIT:
            delivery.setStatus(Delivery.STATUS_OUT_FOR_DELIVERY);
            delivery.setCurrentLocation("Local distribution center - Out for delivery");
            break;
            
        case Delivery.STATUS_OUT_FOR_DELIVERY:
            delivery.markAsDelivered();
            delivery.setCurrentLocation("Delivered to customer");
            break;
    }
    
    delivery.setUpdatedAt(LocalDateTime.now());
    return deliveryRepository.save(delivery);
}

/**
 * Auto-assign mock driver when delivery is created
 */
private String generateRandomDriverName() {
    String[] names = {"Ahmed Ben Ali", "Mohamed Gharbi", "Karim Trabelsi", "Youssef Ben Salem", 
                      "Sami Jbeli", "Rami Mansour", "Hichem Bouazizi", "Nabil Mejri"};
    return names[(int) (Math.random() * names.length)];
}

private String generateRandomPhone() {
    return String.format("+216 %d%d %d%d%d %d%d%d", 
        (int)(Math.random() * 10), (int)(Math.random() * 10),
        (int)(Math.random() * 10), (int)(Math.random() * 10), (int)(Math.random() * 10),
        (int)(Math.random() * 10), (int)(Math.random() * 10), (int)(Math.random() * 10));
}

/**
 * Get delivery timeline/tracking events
 */
@Override
@Transactional(readOnly = true)
public List<DeliveryEvent> getDeliveryTimeline(UUID deliveryId) {
    Delivery delivery = getDeliveryById(deliveryId);
    List<DeliveryEvent> events = new ArrayList<>();
    
    // Create timeline based on delivery status
    events.add(new DeliveryEvent("Order Confirmed", delivery.getCreatedAt(), 
                                  "Your order has been confirmed and is being prepared"));
    
    if (delivery.getPickupTime() != null) {
        events.add(new DeliveryEvent("Picked Up", delivery.getPickupTime(), 
                                      "Package picked up by " + delivery.getDriverName()));
    }
    
    if (Delivery.STATUS_IN_TRANSIT.equals(delivery.getStatus()) || 
        Delivery.STATUS_OUT_FOR_DELIVERY.equals(delivery.getStatus()) ||
        Delivery.STATUS_DELIVERED.equals(delivery.getStatus())) {
        events.add(new DeliveryEvent("In Transit", delivery.getPickupTime().plusHours(2), 
                                      "Package is on the way"));
    }
    
    if (Delivery.STATUS_OUT_FOR_DELIVERY.equals(delivery.getStatus()) ||
        Delivery.STATUS_DELIVERED.equals(delivery.getStatus())) {
        events.add(new DeliveryEvent("Out for Delivery", delivery.getEstimatedDelivery().minusHours(2), 
                                      "Package is out for delivery"));
    }
    
    if (delivery.isDelivered() && delivery.getActualDelivery() != null) {
        events.add(new DeliveryEvent("Delivered", delivery.getActualDelivery(), 
                                      "Package delivered successfully"));
    }
    
    return events;
}

/**
 * Cancel delivery
 */
@Override
@Transactional
public Delivery cancelDelivery(UUID deliveryId, String reason) {
    Delivery delivery = getDeliveryById(deliveryId);
    
    if (delivery.isDelivered()) {
        throw new IllegalStateException("Cannot cancel a delivered package");
    }
    
    delivery.setStatus(Delivery.STATUS_FAILED);
    delivery.setDeliveryNotes(delivery.getDeliveryNotes() + " | CANCELLED: " + reason);
    delivery.setUpdatedAt(LocalDateTime.now());
    
    return deliveryRepository.save(delivery);
}
```

#### Create DeliveryEvent DTO:

```java
// Create: DeliveryEvent.java in dto package
package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    private String event;
    private LocalDateTime timestamp;
    private String description;
}
```

---

### 4. **Controller Enhancements**

#### Add to `DeliveryController.java`:

```java
/**
 * Simulate delivery progress (ADMIN only - for testing/demo)
 * POST /api/delivery/{id}/simulate-progress
 */
@PostMapping("/{id}/simulate-progress")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public ResponseEntity<Delivery> simulateProgress(@PathVariable UUID id) {
    Delivery delivery = deliveryService.simulateDeliveryProgress(id);
    return ResponseEntity.ok(delivery);
}

/**
 * Get delivery timeline/tracking events
 * GET /api/delivery/{id}/timeline
 */
@GetMapping("/{id}/timeline")
public ResponseEntity<List<DeliveryEvent>> getDeliveryTimeline(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal currentUser) {
    
    Delivery delivery = deliveryService.getDeliveryById(id);
    
    // Verify access
    boolean isOwner = delivery.getOrder().getUser().getId().equals(currentUser.getId());
    boolean isStaff = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().matches("ROLE_(ADMIN|SUPER_ADMIN)"));
    
    if (!isOwner && !isStaff) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    List<DeliveryEvent> timeline = deliveryService.getDeliveryTimeline(id);
    return ResponseEntity.ok(timeline);
}

/**
 * Get delivery timeline by tracking number (public)
 * GET /api/delivery/track/{trackingNumber}/timeline
 */
@GetMapping("/track/{trackingNumber}/timeline")
@PreAuthorize("permitAll()")
public ResponseEntity<List<DeliveryEvent>> getTimelineByTracking(
        @PathVariable String trackingNumber) {
    
    Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
    List<DeliveryEvent> timeline = deliveryService.getDeliveryTimeline(delivery.getId());
    return ResponseEntity.ok(timeline);
}

/**
 * Cancel delivery (ADMIN only)
 * POST /api/delivery/{id}/cancel
 */
@PostMapping("/{id}/cancel")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public ResponseEntity<Delivery> cancelDelivery(
        @PathVariable UUID id,
        @RequestBody Map<String, String> cancellationData) {
    
    String reason = cancellationData.get("reason");
    if (reason == null || reason.isBlank()) {
        return ResponseEntity.badRequest().build();
    }
    
    Delivery delivery = deliveryService.cancelDelivery(id, reason);
    return ResponseEntity.ok(delivery);
}
```

---

### 5. **Service Interface Updates**

Add these methods to `DeliveryService.java`:

```java
/**
 * Simulate delivery progress through stages
 */
Delivery simulateDeliveryProgress(UUID deliveryId);

/**
 * Get delivery timeline/tracking events
 */
List<DeliveryEvent> getDeliveryTimeline(UUID deliveryId);

/**
 * Cancel delivery
 */
Delivery cancelDelivery(UUID deliveryId, String reason);
```

---

### 6. **Optional: Automated Delivery Simulation (Background Task)**

Create a scheduled task to automatically progress deliveries:

```java
// Create: DeliverySimulationScheduler.java
package com.example.Backend.scheduler;

import com.example.Backend.entity.Delivery;
import com.example.Backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliverySimulationScheduler {

    private final DeliveryService deliveryService;

    /**
     * Auto-progress deliveries every hour (configurable)
     * This simulates the delivery company updating statuses
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void autoProgressDeliveries() {
        log.info("Running automated delivery simulation...");
        
        // Get all active deliveries (not delivered or failed)
        deliveryService.getAllDeliveries(Pageable.unpaged())
            .getContent()
            .stream()
            .filter(d -> !d.isDelivered() && 
                        !Delivery.STATUS_FAILED.equals(d.getStatus()))
            .forEach(delivery -> {
                try {
                    deliveryService.simulateDeliveryProgress(delivery.getId());
                    log.info("Progressed delivery: {}", delivery.getTrackingNumber());
                } catch (Exception e) {
                    log.error("Error progressing delivery {}: {}", 
                             delivery.getTrackingNumber(), e.getMessage());
                }
            });
    }
}
```

**Enable Scheduling** in `BackendApplication.java`:
```java
@SpringBootApplication
@EnableScheduling  // Add this annotation
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

---

### 7. **Repository Enhancements**

Add to `DeliveryRepository.java` (if not already present):

```java
@Query("SELECT d FROM Delivery d WHERE d.status IN :statuses")
List<Delivery> findByStatusIn(List<String> statuses);

@Query("SELECT d FROM Delivery d WHERE d.createdAt >= :startDate AND d.createdAt <= :endDate")
List<Delivery> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
```

---

## 🎨 Frontend Integration Considerations

### Tracking Page Features:
1. **Public tracking**: Enter tracking number → see timeline
2. **Real-time updates**: Show current status and location
3. **Driver info**: Display driver name and phone (when assigned)
4. **Estimated delivery**: Show countdown to estimated delivery
5. **Timeline visualization**: Visual progress bar with events

### Admin Dashboard Features:
1. **Delivery management**: View all deliveries with filters
2. **Manual status updates**: Change delivery status manually
3. **Simulate progress**: Test delivery flow with one click
4. **Statistics**: Delivery performance metrics
5. **Assign drivers**: Manually assign/change drivers

---

## 📊 Database Migration Script

Run this SQL to update your existing database:

```sql
-- Connect to database
\c ecommercespareparts;

-- Update deliveries table
ALTER TABLE deliveries 
DROP COLUMN IF EXISTS ondelivery_tracking_id;

ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(255),
ADD COLUMN IF NOT EXISTS delivery_notes TEXT,
ADD COLUMN IF NOT EXISTS estimated_delivery TIMESTAMP,
ADD COLUMN IF NOT EXISTS actual_delivery TIMESTAMP,
ADD COLUMN IF NOT EXISTS driver_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS current_location TEXT;

-- Add unique constraint if tracking_number exists
ALTER TABLE deliveries
ADD CONSTRAINT unique_tracking_number UNIQUE (tracking_number);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_number ON deliveries(tracking_number);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);

-- Update status values if needed
UPDATE deliveries SET status = 'PROCESSING' WHERE status = 'PENDING';

COMMIT;
```

---

## ✅ Summary of Changes

### What You Already Have (No Changes Needed):
1. ✅ **Delivery Entity** - Perfect structure with all needed fields
2. ✅ **DeliveryServiceImpl** - 90% complete, just needs enhancement methods
3. ✅ **DeliveryController** - Comprehensive endpoints already in place
4. ✅ **Tracking number generation** - Already implemented

### What Needs to Be Added:
1. 🔄 **Database schema updates** - Remove ONdelivery reference, add tracking fields
2. ➕ **Enhanced service methods** - Add simulation, timeline, cancellation
3. ➕ **DeliveryEvent DTO** - For timeline visualization
4. ➕ **Controller enhancements** - Add timeline and simulation endpoints
5. ⚙️ **Optional: Scheduler** - Auto-progress deliveries for demo purposes

### What Needs to Be Removed:
1. ❌ Remove all references to "ONdelivery" in comments/documentation
2. ❌ Remove `ondelivery_tracking_id` from database
3. ❌ Remove TODO comments about API integration

---

## 🚀 Implementation Steps

1. **Update Database** (5 minutes)
   ```bash
   psql -h localhost -U lasmer -d ecommercespareparts -f migration.sql
   ```

2. **Add DeliveryEvent DTO** (2 minutes)
   - Create the DTO class as shown above

3. **Update DeliveryService Interface** (3 minutes)
   - Add the three new method signatures

4. **Enhance DeliveryServiceImpl** (15 minutes)
   - Add simulation, timeline, and cancellation methods

5. **Update DeliveryController** (10 minutes)
   - Add timeline and simulation endpoints

6. **Optional: Add Scheduler** (10 minutes)
   - Create the scheduler class
   - Enable scheduling in main application

7. **Test Everything** (20 minutes)
   - Build: `mvn clean compile`
   - Test tracking number generation
   - Test delivery progression
   - Test timeline retrieval

---

## 🎯 Benefits of This Approach

1. ✅ **Fully Functional** - Complete delivery system without external dependencies
2. ✅ **Demo Ready** - Can showcase all delivery features
3. ✅ **Easy to Replace** - When you get ONdelivery access, just swap implementation
4. ✅ **Realistic** - Simulates real delivery company behavior
5. ✅ **Testable** - No network calls, faster tests
6. ✅ **Cost Effective** - No API fees during development

---

## 🔮 Future: Switching to Real ONdelivery API

When you're ready to integrate the real API:

1. Keep the same entity structure
2. Replace `simulateDeliveryProgress()` with real API call
3. Add API credentials to `application.yml`
4. Update `trackDelivery()` to sync with external system
5. Remove the scheduler (or make it sync instead of simulate)

The rest of your code stays the same! 🎉

---

**Ready to implement? Let me know if you need help with any specific part!**
