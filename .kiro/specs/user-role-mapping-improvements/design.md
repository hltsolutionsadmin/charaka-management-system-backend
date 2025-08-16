# Design Document

## Overview

This design enhances the UserBusinessRoleMappingController by implementing comprehensive validation, improved error handling, new role management endpoints, and better code organization. The design maintains backward compatibility while adding robust new functionality for user role management in a healthcare system context.

## Architecture

### Controller Layer Improvements
- **Enhanced Validation**: Input validation using Spring Boot validation annotations and custom validators
- **Structured Error Handling**: Consistent error response format with detailed field-level validation messages
- **Method Organization**: Logical grouping of endpoints and extraction of common business logic into helper methods
- **Security Enhancement**: Granular authorization checks with improved business scope validation

### New Endpoint Categories
1. **CRUD Operations**: Complete lifecycle management for role assignments
2. **Bulk Operations**: Batch processing for multiple role assignments
3. **Audit & Reporting**: Role assignment history and analytics
4. **Advanced Management**: Role transfers, deactivation, and cross-hospital assignments

## Components and Interfaces

### Enhanced Validation Layer
```java
// Custom validation annotations
@ValidBusinessScope
@ValidRoleAssignment
@ValidUserContext

// Validation groups for different operations
interface CreateValidation {}
interface UpdateValidation {}
interface BulkValidation {}
```

### New DTOs and Request Objects
```java
// Enhanced request objects
UserRoleUpdateRequest
BulkRoleAssignmentRequest
RoleTransferRequest
RoleAuditRequest

// Enhanced response objects
RoleAssignmentResponse
BulkOperationResponse
AuditTrailResponse
```

### Service Layer Enhancements
```java
// New service methods
updateUserRole(Long userId, Long businessId, ERole newRole)
removeUserRole(Long userId, Long businessId, ERole role)
transferUserRole(Long userId, Long fromBusinessId, Long toBusinessId)
bulkAssignRoles(List<RoleAssignmentRequest> requests)
getAuditTrail(Long userId, Long businessId, DateRange dateRange)
```

## Data Models

### Enhanced Role Assignment Tracking
```java
@Entity
public class UserRoleAudit {
    private Long id;
    private Long userId;
    private Long businessId;
    private ERole role;
    private String operation; // CREATE, UPDATE, DELETE, TRANSFER
    private Long performedBy;
    private LocalDateTime timestamp;
    private String reason;
    private Map<String, Object> metadata;
}
```

### Validation Context Objects
```java
public class BusinessScopeContext {
    private UserModel currentUser;
    private Long targetBusinessId;
    private ERole requiredRole;
    private boolean isCrossBusinessOperation;
}

public class RoleAssignmentContext {
    private Long userId;
    private Long businessId;
    private ERole role;
    private String operation;
    private Map<String, Object> validationData;
}
```

## Error Handling

### Structured Error Response Format
```java
public class ValidationErrorResponse {
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private List<FieldError> fieldErrors;
    private Map<String, Object> metadata;
}

public class FieldError {
    private String field;
    private Object rejectedValue;
    private String message;
    private String errorCode;
}
```

### Custom Exception Hierarchy
```java
// Specific exceptions for role management
RoleAssignmentException
DuplicateRoleException
InvalidBusinessScopeException
RoleTransferException
BulkOperationException
```

## New Endpoints Design

### Role Management CRUD
```java
// Update existing role assignment
PUT /api/mappings/users/{userId}/roles/{businessId}
// Remove role assignment
DELETE /api/mappings/users/{userId}/roles/{businessId}/{role}
// Get user's role assignments
GET /api/mappings/users/{userId}/roles
// Transfer role between businesses
POST /api/mappings/users/{userId}/transfer-role
```

### Bulk Operations
```java
// Bulk role assignments
POST /api/mappings/bulk-assign
// Bulk role updates
PUT /api/mappings/bulk-update
// Bulk role removal
DELETE /api/mappings/bulk-remove
```

### Audit and Reporting
```java
// Get audit trail for user
GET /api/mappings/audit/users/{userId}
// Get audit trail for business
GET /api/mappings/audit/businesses/{businessId}
// Generate role assignment report
GET /api/mappings/reports/assignments
```

### Advanced Management
```java
// Get all role assignments for business
GET /api/mappings/businesses/{businessId}/all-roles
// Deactivate user across all businesses
POST /api/mappings/users/{userId}/deactivate
// Reactivate user
POST /api/mappings/users/{userId}/reactivate
```

## Testing Strategy

### Unit Testing
- **Validation Logic**: Test all custom validators with edge cases
- **Business Logic**: Test role assignment rules and business scope validation
- **Error Handling**: Test exception scenarios and error response formatting
- **Helper Methods**: Test extracted utility methods independently

### Integration Testing
- **Endpoint Testing**: Test all new and modified endpoints with various user roles
- **Security Testing**: Verify authorization rules and business scope enforcement
- **Database Integration**: Test audit trail creation and data persistence
- **Bulk Operations**: Test batch processing with large datasets

### Test Data Setup
```java
// Test fixtures for different user types
@TestConfiguration
class RoleMappingTestConfig {
    SuperAdminUser superAdmin;
    HospitalAdminUser hospitalAdmin;
    MultiBusinessUser multiBusinessUser;
    SingleBusinessUser singleBusinessUser;
}
```

## Implementation Phases

### Phase 1: Core Improvements
1. Extract and organize existing helper methods
2. Implement enhanced validation framework
3. Add structured error handling
4. Improve existing endpoint responses

### Phase 2: New CRUD Operations
1. Add role update endpoints
2. Implement role removal functionality
3. Add comprehensive role listing
4. Implement role transfer operations

### Phase 3: Advanced Features
1. Add bulk operations support
2. Implement audit trail functionality
3. Add reporting endpoints
4. Implement advanced management features

### Phase 4: Security and Performance
1. Enhanced authorization checks
2. Performance optimization for bulk operations
3. Comprehensive security testing
4. Documentation and API specification updates