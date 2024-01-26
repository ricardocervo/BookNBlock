# Assumptions for Booking and Block Management

1. **Manager and Owner Interchangeability**:
   - A manager can change a block that the owner created and vice versa.

2. **Multiple Guests in a Booking**:
   - A booking can include more than one guest.

3. **Booking Ownership**:
   - The user who creates the booking is considered the owner of the booking. Only the booking owner can make modifications such as updating dates, cancellation, rebooking, etc.

4. **Owner and Guest Distinction**:
   - The booking owner is not necessarily one of the guests. For example, a user might make a reservation for someone else in their family.

5. **Date Control Methodology**:
   - The API's date control is day-based. For example, if a user checks in today and checks out tomorrow (a one-night stay), only today's date will be considered occupied in the backend.

6. **Pre-existing User and Property Registration**:
   - It is assumed that the User and Property registration system already exists (potentially in another service). Therefore, this part was not implemented in the current system. A class named `DBPopulator` populates some **Properties** with their **owners/managers** when the application starts, providing sufficient data to test the main functionalities of the API.

## Implementation

I implemented the solution for this challenge using **Java (version 17)**, **Spring Framework (version 3.2)** and **in-memory database H2**.

Below is the ER diagram:

![BookNBlock ER Diagram](https://github.com/ricardocervo/BookNBlock/blob/main/bnb-er.png)

## Running the application

Ensure you have **Java 17** and **Apache Maven** installed on your system. Verify that port 8080 is available. From the project's root directory, run the application using the following Maven command:

### Sending requests

In this section, we will see some examples of how to interact with the API to create Bookings and Blocks. A complete documentation of the API can be found further ahead in this document.

After starting the application, we can use applications like Postman to send requests and interact with the API. The first request to be made is for authentication:

```
POST http://localhost:8080/api/v1/auth/authenticate
```
The request body should contain the email and password of a user registered in the database:

```
{
    "email": "marcus.wellford@example.com",
    "password": "user123"
}

```

If the user exists and the password is correct, the response will contain the authentication token:

```
{
    "user": {
        "name": "Marcus Wellford",
        "email": "marcus.wellford@example.com"
    },
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJjdXMud2VsbGZvcmRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDYyOTU2NzYsImV4cCI6MTcwNjI5OTI3Nn0.sk1l-zPGh3nOOA0oCY8BBN-0rMI0dVXMnBZmlwSIjkE"
}
```
Now we can use this token in the Authorization header of other endpoints, and thus call for example the endpoint to create a Booking:

```
POST http://localhost:8080/api/v1/bookings
```

```
{
    "propertyId":"601e0800-a069-4672-a1e4-0f5eec0f9e9c",
    "startDate": "2024-02-10",
    "endDate": "2024-02-13",
    "includeLoggedUserAsGuest": true,
    "guests": [
        {
            "name": "Guest1",
            "email": "email1@guest1.com"
        },
        {
            "name": "Guest2",
            "email": "email2@email2.com"
        }
    ]
}
```

If all input fields are valid and the Property exists in the database, the response should be something like:

```
{
    "id": "fa50cd3e-10dd-4efd-98de-94854b6b8253",
    "startDate": "2024-02-10",
    "endDate": "2024-02-13",
    "status": "CONFIRMED",
    "owner": {
        "name": "Marcus Wellford",
        "email": "marcus.wellford@example.com"
    },
    "property": {
        "id": "601e0800-a069-4672-a1e4-0f5eec0f9e9c",
        "name": "A property",
        "location": "Porto Alegre",
        "description": "A property - description"
    },
    "guests": [
        {
            "id": "74d547a6-b090-4450-9d51-60f648dc35cc",
            "name": "Guest1",
            "email": "email1@guest1.com"
        },
        {
            "id": "e54e5e77-be2f-4928-9e03-d8884145c811",
            "name": "Guest2",
            "email": "email2@email2.com"
        }
    ]
}
```
### Creating a block:

To create a block, the authenticated user must be either the Owner or one of the Managers of the property. In the DBPopulator class, some properties along with their managers and owners are already created.

Assuming the user is logged in (passing the token in the Authorization header of the request), the following is an example of how to create a Block:

```
POST http://localhost:8080/api/v1/blocks
```

Request Body:

```
{
    "propertyId": "601e0800-a069-4672-a1e4-0f5eec0f9e9c",
    "startDate": "2024-02-19",
    "endDate": "2024-02-20",
    "reason": "Property renovation"
}
```

Expected Response:

```
{
    "id": "47c96c59-ccb4-4d5a-8fce-9013216271c7",
    "propertyId": "601e0800-a069-4672-a1e4-0f5eec0f9e9c",
    "startDate": "2024-02-19",
    "endDate": "2024-02-20",
    "reason": "Property renovation"
}
```

### Authorization control

When a user tries to perform an action for which they don't have permission (for example, trying to create a Block for a property where they are neither the Manager nor the Owner), they will receive an HTTP 401 - Unauthorized as a response:

```
{
    "httpStatus": 401,
    "httpError": "Unauthorized",
    "timestamp": "2024-01-26T18:08:02.429528",
    "message": "You are not allowed to access this resource"
}
```

This also happens if a User tries to update, cancel, or delete a Booking that they didn't create.

## Running automated tests

```bash
mvn spring-boot:run
```

To run the automated tests we can use a maven command:
```bash
mvn test
```
Expected result:
```bash

...
[INFO] Results:
[INFO] 
[INFO] Tests run: 70, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.987 s
[INFO] Finished at: 2024-01-26T17:25:35-03:00
[INFO] ------------------------------------------------------------------------
```
## BookNBlock API Documentation

Below is the API documentation for all controllers of the application: **AuthenticationController**, **BookingController** and **BlockController**.

## Authentication API Endpoints

## Overview
This endpoint is part of the authentication service, responsible for authenticating users. It uses JWT (JSON Web Token) for secure token generation after successful authentication.

## Endpoint: Authenticate User

### Method
`POST`

### Path
`/authenticate`

### Request Body: `AuthenticationRequest`
- **email**: String (User's email)
- **password**: String (User's password)

### Response: `AuthenticationResponse`
- **token**: String (JWT token for the authenticated user)
- **user**: Object
  - **name**: String (User's name)
  - **email**: String (User's email)

### Functionality
- Validates user credentials (email and password) through an `AuthenticationManager`.
- Fetches the user details from the `UserRepository` using the email provided in the request.
- Generates a JWT token using `JwtService` if authentication is successful.
- Constructs and returns an `AuthenticationResponse` containing the JWT token and user details.

### Error Handling
- If authentication fails (due to invalid credentials), an exception is thrown.
- If the user's email is not found in the repository, an exception is thrown.

## Booking API Endpoints 

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


# Block API Endpoints

## Base URL
`/api/v1/blocks`

## Endpoints

### 1. Create a Block
Creates a new block.

- **URL**: `/api/v1/blocks`
- **Method**: `POST`
- **Request Body**: 
  - `BlockRequestDto` object which includes:
    - `propertyId`: UUID of the property.
    - `startDate`: Start date of the block (format: `yyyy-mm-dd`).
    - `endDate`: End date of the block (format: `yyyy-mm-dd`).
    - `reason`: Reason for the block.
- **Success Response**:
  - **Code**: `201 CREATED`
  - **Content**: `BlockResponseDto` object with block details.
- **Error Response**:
  - **Code**: `400 BAD REQUEST` if the request data is invalid.
  - **Code**: `404 NOT FOUND` if the property is not found.
  - **Code**: `409 CONFLICT` if the block dates overlap with existing blocks or bookings.

### 2. Update a Block
Updates an existing block.

- **URL**: `/api/v1/blocks/{blockId}`
- **Method**: `PUT`
- **URL Parameters**: 
  - `blockId` [UUID] - The ID of the block to update.
- **Request Body**: 
  - `BlockUpdateDto` object which includes:
    - `startDate`: New start date of the block (format: `yyyy-mm-dd`).
    - `endDate`: New end date of the block (format: `yyyy-mm-dd`).
    - `reason`: New reason for the block.
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: `BlockResponseDto` object with updated block details.
- **Error Response**:
  - **Code**: `400 BAD REQUEST` if the request data is invalid.
  - **Code**: `404 NOT FOUND` if the block is not found.
  - **Code**: `409 CONFLICT` if the updated block dates overlap with existing blocks or bookings.

### 3. Delete a Block
Deletes an existing block.

- **URL**: `/api/v1/blocks/{blockId}`
- **Method**: `DELETE`
- **URL Parameters**: 
  - `blockId` [UUID] - The ID of the block to delete.
- **Success Response**:
  - **Code**: `204 NO CONTENT`
- **Error Response**:
  - **Code**: `404 NOT FOUND` if the block is not found.

