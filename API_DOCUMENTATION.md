# Hotel Management System — API Documentation

> Base URL: `http://localhost:8080/api`
> Auth: `Authorization: Bearer <JWT_TOKEN>`
> Swagger UI: `http://localhost:8080/api/swagger-ui.html`

---

## 🔐 Authentication (`/auth`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | ❌ Public | Register a new customer |
| POST | `/auth/login` | ❌ Public | Login and receive JWT tokens |
| POST | `/auth/refresh-token` | Header: `Refresh-Token` | Get new access token |
| POST | `/auth/logout` | ✅ JWT | Invalidate session |
| GET  | `/auth/verify-email?token=` | ❌ Public | Verify email address |
| POST | `/auth/forgot-password` | ❌ Public | Request password reset link |
| POST | `/auth/reset-password` | ❌ Public | Reset password via token |

### Register Request Body
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "Password@123",
  "phone": "9876543210",
  "address": "123 Main Street, Mumbai"
}
```

### Login Request Body
```json
{ "email": "john@example.com", "password": "Password@123" }
```

### Auth Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "CUSTOMER"
  }
}
```

---

## 👤 User Management (`/users`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET  | `/users/profile` | ✅ | Any | Get current user profile |
| GET  | `/users/{id}` | ✅ | Any | Get user by ID |
| PUT  | `/users/{id}/profile` | ✅ | Own/Admin | Update profile |
| PUT  | `/users/{id}/change-password` | ✅ | Own | Change password |
| GET  | `/users/all` | ✅ | ADMIN | List all users |
| GET  | `/users/search?keyword=` | ✅ | ADMIN | Search users |
| PATCH | `/users/{id}/deactivate` | ✅ | ADMIN | Deactivate user |
| PATCH | `/users/{id}/activate` | ✅ | ADMIN | Activate user |
| DELETE | `/users/{id}` | ✅ | ADMIN | Delete user |

---

## 🏨 Room Management (`/rooms`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET  | `/rooms` | ❌ | Public | List all active rooms |
| GET  | `/rooms/{id}` | ❌ | Public | Room details by ID |
| GET  | `/rooms/number/{roomNumber}` | ❌ | Public | Room by room number |
| GET  | `/rooms/type/{roomType}` | ❌ | Public | Rooms by type |
| GET  | `/rooms/available` | ❌ | Public | Search available rooms |
| GET  | `/rooms/{id}/availability` | ❌ | Public | Check specific room availability |
| GET  | `/rooms/all` | ✅ | ADMIN | All rooms including inactive |
| GET  | `/rooms/status/{status}` | ✅ | ADMIN | Rooms by status |
| POST | `/rooms/add` | ✅ | ADMIN | Add new room |
| PUT  | `/rooms/update/{id}` | ✅ | ADMIN | Update room |
| DELETE | `/rooms/delete/{id}` | ✅ | ADMIN | Soft delete room |
| PATCH | `/rooms/{id}/status` | ✅ | ADMIN | Update room status |

### Room Availability Search
```
GET /rooms/available?checkIn=2024-12-20&checkOut=2024-12-25&guests=2&roomType=DOUBLE
```

### Room Types
`SINGLE` | `DOUBLE` | `TWIN` | `SUITE` | `DELUXE` | `PRESIDENTIAL` | `FAMILY` | `STUDIO`

### Room Status
`AVAILABLE` | `OCCUPIED` | `MAINTENANCE` | `RESERVED` | `CLEANING`

---

## 📅 Booking Management (`/bookings`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/bookings/create` | ✅ | CUSTOMER | Create new booking |
| GET  | `/bookings/{id}` | ✅ | Own/Admin | Get booking by ID |
| GET  | `/bookings/reference/{ref}` | ✅ | Own/Admin | Get by booking reference |
| GET  | `/bookings/my-bookings` | ✅ | CUSTOMER | My booking history |
| GET  | `/bookings/user/{userId}` | ✅ | Own/Admin | Bookings by user |
| PATCH | `/bookings/{id}/cancel` | ✅ | Own/Admin | Cancel booking |
| GET  | `/bookings/all` | ✅ | ADMIN | All bookings |
| GET  | `/bookings/status/{status}` | ✅ | ADMIN | Bookings by status |
| PATCH | `/bookings/{id}/confirm` | ✅ | ADMIN | Confirm booking |
| PATCH | `/bookings/{id}/check-in` | ✅ | ADMIN | Check in guest |
| PATCH | `/bookings/{id}/check-out` | ✅ | ADMIN | Check out guest |

### Create Booking Request
```json
{
  "roomId": 1,
  "checkInDate": "2024-12-20",
  "checkOutDate": "2024-12-25",
  "numberOfGuests": 2,
  "specialRequests": "High floor room preferred",
  "paymentMethod": "CREDIT_CARD",
  "advancePayment": 2500.00
}
```

### Booking Status Flow
```
PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT
         ↘ CANCELLED
```

### Cancel Booking
```json
{ "reason": "Change of plans" }
```

---

## 💳 Payment Management (`/payments`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/payments/process` | ✅ | Any | Process a payment |
| GET  | `/payments/{id}` | ✅ | Any | Get payment by ID |
| GET  | `/payments/transaction/{txnId}` | ✅ | Any | Get by transaction ID |
| GET  | `/payments/booking/{bookingId}` | ✅ | Any | Payments by booking |
| GET  | `/payments/my-payments` | ✅ | CUSTOMER | My payment history |
| GET  | `/payments/all` | ✅ | ADMIN | All payments |
| GET  | `/payments/status/{status}` | ✅ | ADMIN | Payments by status |
| PATCH | `/payments/{id}/status` | ✅ | ADMIN | Update payment status |
| POST | `/payments/{id}/refund` | ✅ | ADMIN | Process refund |

### Process Payment Request
```json
{
  "bookingId": 1,
  "amount": 5000.00,
  "paymentMethod": "UPI",
  "paymentType": "FULL_PAYMENT",
  "gatewayReference": "UPI-REF-12345",
  "notes": "Full payment via UPI"
}
```

### Payment Methods
`CASH` | `CREDIT_CARD` | `DEBIT_CARD` | `UPI` | `NET_BANKING` | `WALLET` | `BANK_TRANSFER`

### Payment Types
`ADVANCE` | `FULL_PAYMENT` | `BALANCE` | `REFUND` | `SERVICE_CHARGE`

---

## 🛎️ Hotel Services (`/services`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/services/request` | ✅ | CUSTOMER | Request a service |
| GET  | `/services/{id}` | ✅ | Own/Admin | Get service by ID |
| GET  | `/services/booking/{bookingId}` | ✅ | Own/Admin | Services by booking |
| GET  | `/services/my-services` | ✅ | CUSTOMER | My service requests |
| DELETE | `/services/{id}/cancel` | ✅ | Own/Admin | Cancel service |
| GET  | `/services/all` | ✅ | ADMIN | All services |
| GET  | `/services/status/{status}` | ✅ | ADMIN | Services by status |
| GET  | `/services/type/{type}` | ✅ | ADMIN | Services by type |
| PATCH | `/services/{id}/status` | ✅ | ADMIN | Update status + staff notes |
| PATCH | `/services/{id}/amount` | ✅ | ADMIN | Set service charge |

### Request Service
```json
{
  "bookingId": 1,
  "serviceType": "ROOM_SERVICE",
  "serviceDescription": "Dinner for 2 - Paneer Butter Masala, Naan, Dessert"
}
```

### Service Types
`LAUNDRY` | `ROOM_SERVICE` | `RESTAURANT` | `SPA` | `GYM` | `AIRPORT_TRANSFER` | `CONCIERGE` | `HOUSEKEEPING`

### Update Service Status (Admin)
```json
{
  "staffNotes": "Order delivered at 8:30 PM"
}
```
`PATCH /services/{id}/status?status=COMPLETED`

---

## 📩 Enquiry System (`/enquiries`)
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/enquiries/submit` | ❌ | Public | Submit an enquiry |
| GET  | `/enquiries/{id}` | ✅ | Own/Admin | Get enquiry by ID |
| GET  | `/enquiries/my-enquiries` | ✅ | CUSTOMER | My enquiries |
| GET  | `/enquiries/all` | ✅ | ADMIN | All enquiries |
| GET  | `/enquiries/status/{status}` | ✅ | ADMIN | By status |
| POST | `/enquiries/respond/{id}` | ✅ | ADMIN | Respond to enquiry |
| PATCH | `/enquiries/{id}/status` | ✅ | ADMIN | Update status |

### Submit Enquiry (Public)
```json
{
  "guestName": "Priya Patel",
  "guestEmail": "priya@example.com",
  "guestPhone": "9876543210",
  "subject": "Room availability for New Year",
  "message": "Do you have suite availability for Dec 31 - Jan 1?",
  "enquiryType": "BOOKING"
}
```

### Admin Response
```json
{ "response": "Yes, we have 2 suites available. Please call 1800-XXX-XXXX to book." }
```

### Enquiry Types
`BOOKING` | `ROOM_INFO` | `SERVICES` | `COMPLAINT` | `FEEDBACK` | `PRICING` | `OTHER`

---

## 📊 Admin Dashboard (`/admin`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/admin/dashboard` | ✅ ADMIN | Full statistics |
| GET | `/admin/bookings/today-checkins` | ✅ ADMIN | Today's check-ins |
| GET | `/admin/bookings/active` | ✅ ADMIN | Currently checked-in |
| GET | `/admin/bookings/pending` | ✅ ADMIN | Awaiting confirmation |

### Dashboard Response
```json
{
  "totalUsers": 150,
  "totalRooms": 50,
  "availableRooms": 32,
  "occupiedRooms": 12,
  "totalBookings": 420,
  "activeBookings": 12,
  "pendingBookings": 5,
  "cancelledBookings": 30,
  "totalRevenue": 1250000.00,
  "monthlyRevenue": 125000.00,
  "totalEnquiries": 80,
  "openEnquiries": 8,
  "pendingServices": 3
}
```

---

## 📦 Standard API Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2024-01-15T10:30:00",
  "statusCode": 200
}
```

## ❌ Error Response Format
```json
{
  "success": false,
  "message": "Room not available for selected dates",
  "timestamp": "2024-01-15T10:30:00",
  "statusCode": 409
}
```

## 🔒 Default Credentials (Seed Data)
| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@grandhotel.com | Admin@1234 |
| CUSTOMER | rahul@example.com | Admin@1234 |
