# SmartE-Vent API Documentation

This project includes comprehensive API documentation using Swagger/OpenAPI 3.0. The documentation is automatically generated from the annotations in the code and is accessible through a web interface.

## Accessing the API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

And the OpenAPI specification at:

```
http://localhost:8080/api-docs
```

## Features

- **Interactive Documentation**: Test API endpoints directly from the browser
- **Detailed Descriptions**: Each endpoint includes descriptions, parameters, and response schemas
- **Authentication Support**: JWT authentication is integrated with the Swagger UI
- **Categorized Endpoints**: APIs are grouped by functional categories

## API Categories

The API is organized into the following main categories:

1. **Account Management**: User account operations (create, update, search)
2. **Authentication**: Login, registration, token refresh, and password management
3. **Activity Management**: Operations for creating and managing activities
4. **Feedback Management**: Feedback submission and retrieval
5. **Statistics**: Various statistics about activities and participation
6. **Organization Management**: Organization-related operations

## Using the API Documentation

### Authentication

1. To authenticate, first use the `/auth/login` endpoint to obtain a JWT token
2. Click the "Authorize" button at the top of the Swagger UI
3. Enter your JWT token in the format: `Bearer your-token-here`
4. Now you can access protected endpoints

### Testing Endpoints

1. Click on an endpoint to expand it
2. Fill in the required parameters
3. Click "Execute" to send the request
4. View the response below

## Development

When adding new endpoints or modifying existing ones, please update the OpenAPI annotations to keep the documentation current and accurate.

### Key Annotation Types

- `@Tag`: Groups endpoints by functional area
- `@Operation`: Documents endpoint purpose and behavior
- `@Parameter`: Documents endpoint parameters
- `@ApiResponse`: Documents possible responses
- `@Schema`: Documents data models

## Exporting the API Documentation

You can export the OpenAPI specification as JSON or YAML for use in external tools:

1. Visit `http://localhost:8080/api-docs`
2. Save the JSON output
3. This file can be imported into tools like Postman or used to generate client libraries

## Common Status Codes

- **200**: Successful operation
- **201**: Resource created
- **204**: Successful operation with no response body
- **400**: Invalid input
- **401**: Unauthorized
- **403**: Forbidden
- **404**: Resource not found
- **409**: Conflict (e.g., resource already exists)
- **500**: Internal server error 
