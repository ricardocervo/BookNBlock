# Booking API Documentation

## Overview
The Booking API provides a set of endpoints for managing bookings, including creating, updating, canceling, and retrieving booking information.

## Base URL
`/api/v1/bookings`

## Endpoints

### Create Booking
- **Method:** POST
- **Path:** /
- **Request Body:** `BookingRequestDto` 
  - Includes details such as property ID, start date, end date, and guest information.
- **Response:** `BookingResponseDto`
  - Includes booking details like booking ID, property ID, dates, status, and guest information.
- **Status Codes:**
  - `200 OK`: Booking successfully created.
  - `400 Bad Request`: Invalid request data.

### Cancel Booking
- **Method:** PATCH
- **Path:** `/{bookingId}/cancel`
- **Path Variable:** `bookingId` (UUID of the booking to cancel)
- **Response:** `BookingResponseDto`
  - Updated booking details with status set to 'CANCELED'.
- **Status Codes:**
  - `200 OK`: Booking successfully canceled.
  - `404 Not Found`: Booking with the given ID not found.

### Update Booking Dates
- **Method:** PATCH
- **Path:** `/{bookingId}/dates`
- **Path Variable:** `bookingId` (UUID of the booking to update)
- **Request Body:** `BookingDateUpdateDto`
  - New start and end dates for the booking.
- **Response:** `BookingResponseDto`
  - Updated booking details with new dates.
- **Status Codes:**
  - `200 OK`: Booking dates successfully updated.
  - `400 Bad Request`: Invalid date range.
  - `404 Not Found`: Booking with the given ID not found.

### Update Booking Guests
- **Method:** PATCH
- **Path:** `/{bookingId}/guests`
- **Path Variable:** `bookingId` (UUID of the booking to update)
- **Request Body:** `BookingGuestUpdateDto`
  - New guest information for the booking.
- **Response:** `BookingResponseDto`
  - Updated booking details with new guest information.
- **Status Codes:**
  - `200 OK`: Booking guests successfully updated.
  - `404 Not Found`: Booking with the given ID not found.

### Rebook Cancelled Booking
- **Method:** PATCH
- **Path:** `/{bookingId}/rebook`
- **Path Variable:** `bookingId` (UUID of the cancelled booking to rebook)
- **Response:** `BookingResponseDto`
  - Details of the rebooked booking.
- **Status Codes:**
  - `200 OK`: Booking successfully rebooked.
  - `400 Bad Request`: Booking is not in a cancellable state.
  - `404 Not Found`: Booking with the given ID not found.

### Delete Booking
- **Method:** DELETE
- **Path:** `/{bookingId}`
- **Path Variable:** `bookingId` (UUID of the booking to delete)
- **Status Codes:**
  - `204 No Content`: Booking successfully deleted.
  - `404 Not Found`: Booking with the given ID not found.

### Get Booking
- **Method:** GET
- **Path:** `/{bookingId}`
- **Path Variable:** `bookingId` (UUID of the booking to retrieve)
- **Response:** `BookingResponseDto`
  - Details of the requested booking.
- **Status Codes:**
  - `200 OK`: Booking successfully retrieved.
  - `404 Not Found`: Booking with the given ID not found.

## Notes
- `BookingRequestDto`, `BookingResponseDto`, `BookingDateUpdateDto`, and `BookingGuestUpdateDto` are data transfer objects (DTOs) used in requests and responses. Their structures depend on the application's business logic.
- Clients should handle potential errors and status codes as per the API's response.
