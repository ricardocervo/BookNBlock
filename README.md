# BookNBlock: A Java-Based RESTful Webservice for Property Booking and Management

Below is the documentation of my solution for the proposed test.

## My Assumptions for Booking and Block Management

1. **Manager and Owner Interchangeability**:
   - A manager can change a block that the owner created and vice versa.
     
2. **Roles of Property Managers and Guestss**
  - A Manager of one property can be the Guest of another property and vice versa.

3. **Multiple Guests in a Booking**:
   - A booking can include more than one guest.

4. **Booking Ownership**:
   - The user who creates the booking is considered the owner of the booking. Only the booking owner can make modifications such as updating dates, cancellation, rebooking, etc.

5. **Booking Owner and Guest Distinction**:
   - The booking owner is not necessarily one of the guests. For example, a user might make a reservation for someone else in their family.

6. **Date Control Methodology**:
   - The API's date control is day-based. For example, if a user checks in today in the property and checks out tomorrow (a one-night stay), only today's date will be considered occupied in the database.

7. **Pre-existing User and Property Registration**:
   - It is assumed that the User and Property registration system already exists (potentially in another service). Therefore, this part of the system was not implemented. A class named `DBPopulator` populates some **Properties** with their **owners/managers** when the application starts, providing sufficient data to test the main functionalities of the API.

## Application Architecture 

The BookNBlock application is built using Spring Framework version 3.2, Java 17, and Maven. It follows Spring Boot standards and adopts a layered architecture for clarity and separation of concerns. Below is an overview of the key components:

### Controllers:
- **AuthenticationController**: Manages user authentication processes.
- **BookingController**: Handles operations related to bookings, including creation, updating, cancellation, rebooking, and retrieval.
- **BlockController**: Manages operations associated with property blocks, such as creation, updating, and deletion.

These controllers serve as the primary points of communication between the backend and the frontend client. They do not contain business logic but delegate operations to the respective services.

### Services:
- **AuthenticationService**: Implements the business logic for user authentication, supporting the AuthenticationController.
- **BookingService**: Contains the core logic for handling booking operations as defined in the BookingController.
- **BlockService**: Manages the business rules and operations for blocks, working in tandem with the BlockController.

Each service is responsible for executing specific business logic, ensuring separation from the API layer represented by the controllers.

### Data Access Layer:
- **Entities**: Each database table is represented by an entity class, reflecting the table's structure and relationships.
- **Repositories**: For each entity, there is a corresponding repository interface that provides data access and manipulation capabilities.

### Additional Components:
- **ModelMapper**: Utilized for converting between DTOs (Data Transfer Objects) and entity objects, aiding in data encapsulation and API response formation.
- **SecurityService**: Manages security aspects, such as authorization and access control, used across various parts of the application.
- **GlobalExceptionHandler**: A global exception handling mechanism that manages application-wide error handling and responses.
- **JWTService: Manages JSON Web Token operations for secure authentication and authorization.

### Database:
- **In-memory Database**: The application is configured to use the in-memory volatile database H2, facilitating rapid development and testing without the need for external database setup.

### Validation and Error Handling:
- **Field-Level Validation**: Input validation at the field level is performed using Hibernate Validator annotations such as `@NotNull`, `@Size`, `@Email`, etc. These annotations ensure that the data conforms to basic constraints directly in the DTOs and entity classes.
- **Service Layer Validation**: Additional validation logic is implemented in the service layer. This includes more complex business rules like controlling date overlaps in bookings. This layer ensures that data not only adheres to basic constraints but also aligns with specific business logic and operational rules.

### Testing:
- **Unit Tests**: Key components are covered by unit tests, ensuring individual parts function as expected in isolation.
- **Integration Tests**: These tests cover the interaction between different layers, particularly focusing on controllers, services, and the database.


## Running the application

Ensure you have **Java 17** and **Apache Maven** installed on your system. Verify that port 8080 is available. From the project's root directory, run the application using the following Maven command:

```
mvn spring-boot:run
```

We may also compile the application to generate a `jar` file in the `target` folder by running:

```
mvn clean install
```
and then we can run the compiled `jar` file:

```
java -jar target/booknblock-0.0.1-SNAPSHOT.jar
```

### Sending requests

In this section, we will see some examples of how to interact with the API to create Bookings and Blocks. A complete documentation of the API can be found further ahead in this document.

After starting the application, we can use tools like Postman to send requests and interact with the API. The first request to be sent is for authentication:

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

If the email or password is invalid (the user doesn't exist in the database), an error of Unauthorized HTTP 401 is returned:

```
{
    "httpStatus": 401,
    "httpError": "Unauthorized",
    "timestamp": "2024-01-28T11:22:32.806065",
    "message": "Bad credentials"
}
```


If the user exists with the provided credentials, the response will contain the authentication token (HTTP 200 - OK):

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
Headers: 
```
"Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJjdXMud2VsbGZvcmRAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDYyOTU2NzYsImV4cCI6MTcwNjI5OTI3Nn0.sk1l-zPGh3nOOA0oCY8BBN-0rMI0dVXMnBZmlwSIjkE"
```

Request body:

```
{
    "propertyId":"601e0800-a069-4672-a1e4-0f5eec0f9e9c",
    "startDate": "2024-02-10",
    "endDate": "2024-02-13",
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

If all input fields are valid and the Property exists in the database, the response should be (HTTP 201 - Created):

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

If an invalid or expired token is sent, an error response will be returned (HTTP 401 - Unauthorized):

```
{
    "httpStatus": "401",
    "httpError": "Unauthorized",
    "timestamp": "2024-01-28T11:27:56.890381",
    "message": "Not logged in: JWT expired at 2024-01-28T14:27:53Z. Current time: 2024-01-28T14:27:56Z, a difference of 3887 milliseconds.  Allowed clock skew: 0 milliseconds."
}
```

If a user tries to create a booking that conflicts with another non-canceled booking or an existing block for the same property, they will receive an HTTP 409 - Conflict response:

```
{
    "httpStatus": 409,
    "httpError": "Conflict",
    "timestamp": "2024-01-28T12:34:52.576029",
    "loggedUser": "manager1@gmail.com",
    "message": "Booking dates are overlapping with an existing booking."
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

Expected Response (HTTP 201 - Created):

```
{
    "id": "47c96c59-ccb4-4d5a-8fce-9013216271c7",
    "propertyId": "601e0800-a069-4672-a1e4-0f5eec0f9e9c",
    "startDate": "2024-02-19",
    "endDate": "2024-02-20",
    "reason": "Property renovation"
}
```
### Overlapping dates control

If a user tries to create a booking whose dates conflict with a block or another non-cancelled booking, they will receive a HTTP 409 - Conflict response:
```
{
    "httpStatus": 409,
    "httpError": "Conflict",
    "timestamp": "2024-01-26T18:12:45.744822",
    "message": "Booking dates are overlapping with an existing booking."
}
``` 


### Authorization control

When a user tries to perform an action for which they don't have permission (for example, trying to create a Block for a property where they are neither the Manager nor the Owner), they will receive an HTTP Forbidden 403 as a response:

```
{
    "httpStatus": 403,
    "httpError": "Forbidden",
    "timestamp": "2024-01-28T11:31:36.242877",
    "loggedUser": "alexa.richmond@example.com",
    "message": "You are not allowed to access this resource"
}
```

This also happens if a User tries to update, cancel, or delete a Booking that they didn't create.

## Atomated tests

Several unit and integration tests were implemented to ensure app runs smoothly. 
- The integration tests focus on checking the JSON data input and output, as well as the HTTP response codes.
- The unit tests concentrate on examining the internal behavior of each public method in the app, making sure everything works as it should internally.

The following command should be used to run the automated tests:

```
mvn spring-boot:run
```

Expected result:

```
...
INFO] Results:
[INFO] 
[INFO] Tests run: 73, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.307 s
[INFO] Finished at: 2024-01-28T11:33:04-03:00
[INFO] ------------------------------------------------------------------------
```

## BookNBlock API Documentation

Below is the API documentation for all controllers of the application: **AuthenticationController**, **BookingController** and **BlockController**.

## Authentication API Documentation

### Overview
This section details the Authentication API which should be used for user authentication.

### Endpoints

### `POST /api/v1/auth/authenticate`

#### Description
Authenticates a user and generates a JWT token.

#### Request Body
- `AuthenticationRequest`
  - `email`: User's email
  - `password`: User's password

#### Response
- `AuthenticationResponse`
  - `token`: JWT token
  - `user`: User details (name, email)

#### Status Codes
- `200 OK`: Successfully authenticated
- `400 Bad Request`: Input validation error
- `401 Unauthorized`: Authentication failed

### Endpoints

## Booking Reservation API Documentation

### Overview
This section details the Booking Reservation API, covering operations for creating, updating, canceling, and retrieving bookings.

### Endpoints

### `POST /api/v1/bookings`
- **Description**: Creates a new booking.
- **Request Body**: `BookingRequestDto`
  - `propertyId`: UUID
  - `startDate`: LocalDate (NotNull)
  - `endDate`: LocalDate (NotNull)
  - `guests`: List of `GuestDto` (NotNull, Valid, Min Size: 1)
    - `id`: UUID
    - `name`: String (NotNull, Size: Min 3, Max 255)
    - `email`: String (NotNull, Email format)
- **Response**: `BookingResponseDto`
  - `id`: UUID
  - `startDate`: LocalDate
  - `endDate`: LocalDate
  - `status`: BookingStatus
  - `owner`: `UserDto`
    - `name`: String
    - `email`: String
  - `property`: `PropertyDto`
    - `id`: UUID
    - `name`: String
    - `location`: String
    - `description`: String
  - `guests`: List of `GuestDto`
    - `id`: UUID
    - `name`: String (Min: 3, Max: 255)
    - `email`: String (Email format)
- **Status Codes**:
  - `201 Created`: Booking successfully created.
  - `400 Bad Request`: Input validation error.
  - `403 Forbidden`: User not authorized to create a booking.
  - `409 Conflict`: StartDate / EndDate conflicts with another existing Block or Booking for the same property.


### `PATCH /api/v1/bookings/{bookingId}/cancel`
- **Description**: Cancels an existing booking.
- **Path Variable**: `bookingId` (UUID)
- **Response**: Same as `POST /api/v1/bookings`
- **Status Codes**:
  - `200 OK`: Booking successfully canceled.
  - `403 Forbidden`: User not authorized to cancel the booking.
  - `404 Not Found`: Booking not found.

### `PATCH /api/v1/bookings/{bookingId}/dates`
- **Description**: Updates the dates of an existing booking.
- **Path Variable**: `bookingId` (UUID)
- **Request Body**: `BookingDateUpdateDto`
  - `startDate`: LocalDate (NotNull)
  - `endDate`: LocalDate (NotNull)
- **Response**: Same as `POST /api/v1/bookings`
- **Status Codes**:
  - `200 OK`: Booking dates successfully updated.
  - `400 Bad Request`: Input validation error or Invalid date range.
  - `403 Forbidden`: User not authorized to update booking dates.
  - `404 Not Found`: Booking not found.
  - `409 Conflict`: StartDate / EndDate conflicts with another existing Block or Booking for the same property.

### `PATCH /api/v1/bookings/{bookingId}/guests`
- **Description**: Updates the guests of an existing booking.
- **Path Variable**: `bookingId` (UUID)
- **Request Body**: `BookingGuestUpdateDto`
  - `guests`: List of `GuestDto` (NotNull, Valid)
    - `id`: UUID
    - `name`: String (NotNull, Size: Min 3, Max 255)
    - `email`: String (NotNull, Email format)
- **Response**: Same as `POST /api/v1/bookings`
- **Status Codes**:
  - `200 OK`: Booking guests successfully updated.
  - `403 Forbidden`: User not authorized to update booking guests.
  - `404 Not Found`: Booking not found.

### `PATCH /api/v1/bookings/{bookingId}/rebook`
- **Description**: Rebooks a canceled booking.
- **Path Variable**: `bookingId` (UUID)
- **Response**: Same as `POST /api/v1/bookings`
- **Status Codes**:
  - `200 OK`: Booking successfully rebooked.
  - `400 Bad Request`: Booking cannot be rebooked.
  - `403 Forbidden`: User not authorized to rebook the booking.
  - `404 Not Found`: Booking not found.

### `DELETE /api/v1/bookings/{bookingId}`
- **Description**: Deletes an existing booking.
- **Path Variable**: `bookingId` (UUID)
- **Status Codes**:
  - `204 No Content`: Booking successfully deleted.
  - `403 Forbidden`: User not authorized to delete the booking.
  - `404 Not Found`: Booking not found.

### `GET /api/v1/bookings/{bookingId}`
- **Description**: Retrieves a specific booking by ID.
- **Path Variable**: `bookingId` (UUID)
- **Response**: Same as `POST /api/v1/bookings`
- **Status Codes**:
  - `200 OK`: Booking successfully retrieved.
  - `403 Forbidden`: User not authorized to view the booking.
  - `404 Not Found`: Booking not found.

# DTOs

## BookingRequestDto
This DTO is used for creating new bookings.
- `propertyId`: UUID - Identifier of the property.
- `startDate`: LocalDate (NotNull) - Start date of the booking.
- `endDate`: LocalDate (NotNull) - End date of the booking.
- `guests`: List of `GuestDto` - List of guests. Each `GuestDto` must be valid, not null, with a minimum size of 1.

## BookingResponseDto
This DTO is used as a response for booking-related operations.
- `id`: UUID - Unique identifier of the booking.
- `startDate`: LocalDate - Start date of the booking.
- `endDate`: LocalDate - End date of the booking.
- `status`: BookingStatus - Current status of the booking (e.g., CONFIRMED, CANCELED).
- `owner`: `UserDto` - Information about the owner of the booking.
- `property`: `PropertyDto` - Details of the property.
- `guests`: List of `GuestDto` - List of guests included in the booking.

## BookingGuestUpdateDto
Used for updating guests in an existing booking.
- `guests`: List of `GuestDto` - List of updated guest information. Each `GuestDto` must be valid and not null.

## BookingDateUpdateDto
Used for updating the dates of an existing booking.
- `startDate`: LocalDate (NotNull) - Updated start date of the booking.
- `endDate`: LocalDate - (NotNull) Updated end date of the booking.

## GuestDto
Included in several DTOs for guest details.
- `id`: UUID - Unique identifier of the guest.
- `name`: String - Name of the guest (Min: 3 characters, Max: 255 characters).
- `email`: String - Email address of the guest (valid email format).

## UserDto
Part of `BookingResponseDto` to provide details about the booking owner.
- `name`: String - Name of the user.
- `email`: String - Email address of the user.

## PropertyDto
Included in `BookingResponseDto` for property details.
- `id`: UUID - Unique identifier of the property.
- `name`: String - Name of the property.
- `location`: String - Location of the property.
- `description`: String - Description of the property.




## Block Management API Documentation

### Overview
This document outlines the Block Management API, which handles operations related to creating, updating, and deleting blocks in a property.

### Endpoints

### `POST /api/v1/blocks`
- **Description**: Creates a new block.
- **Request Body**: `BlockRequestDto`
  - `propertyId`: UUID
  - `startDate`: LocalDate (NotNull)
  - `endDate`: LocalDate (NotNull)
  - `reason`: String
- **Response**: `BlockResponseDto`
  - `id`: UUID
  - `propertyId`: UUID
  - `startDate`: LocalDate
  - `endDate`: LocalDate
  - `reason`: String
- **Status Codes**:
  - `201 Created`: Block successfully created.
  - `400 Bad Request`: Input validation error.
  - `403 Forbidden`: User not authorized to create a block, i.e. user is not owner neither manager of the property.
  - `409 Conflict`: StartDate / EndDate conflicts with another existing Block or Booking for the same property.

### `PUT /api/v1/blocks/{blockId}`
- **Description**: Updates an existing block.
- **Path Variable**: `blockId` (UUID)
- **Request Body**: `BlockUpdateDto`
  - `startDate`: LocalDate (NotNull)
  - `endDate`: LocalDate (NotNull)
  - `reason`: String (NotNull)
- **Response**: `BlockResponseDto` (Same as `POST /api/v1/blocks`)
- **Status Codes**:
  - `200 OK`: Block successfully updated.
  - `400 Bad Request`: Input validation error.
  - `403 Forbidden`: User not authorized to update the block.
  - `404 Not Found`: Block not found.
  - `409 Conflict`: StartDate / EndDate conflicts with another existing Block or Booking for the same property.

### `DELETE /api/v1/blocks/{blockId}`
- **Description**: Deletes an existing block.
- **Path Variable**: `blockId` (UUID)
- **Status Codes**:
  - `204 No Content`: Block successfully deleted.
  - `403 Forbidden`: User not authorized to delete the block.
  - `404 Not Found`: Block not found.

## DTOs

### BlockRequestDto
- `propertyId`: UUID
- `startDate`: LocalDate (NotNull)
- `endDate`: LocalDate (NotNull)
- `reason`: String

### BlockResponseDto
- `id`: UUID
- `propertyId`: UUID
- `startDate`: LocalDate
- `endDate`: LocalDate
- `reason`: String

### BlockUpdateDto
- `startDate`: LocalDate (NotNull)
- `endDate`: LocalDate (NotNull)
- `reason`: String (NotNull)
