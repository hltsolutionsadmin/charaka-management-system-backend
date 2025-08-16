# Implementation Plan

- [ ] 1. Refactor existing controller structure and extract helper methods
  - Extract common business logic into private helper methods
  - Organize imports and improve code formatting
  - Create consistent method naming patterns
  - _Requirements: 4.1, 4.2, 4.3_

- [ ] 2. Implement enhanced validation framework
- [ ] 2.1 Create custom validation annotations
  - Write @ValidBusinessScope annotation with validator
  - Write @ValidRoleAssignment annotation with validator
  - Create validation groups for different operations
  - _Requirements: 1.1, 1.3_

- [ ] 2.2 Add input validation to existing endpoints
  - Add @Valid annotations to request parameters
  - Implement field-level validation for UserBusinessRoleMappingDTO
  - Add validation for business scope and role combinations
  - _Requirements: 1.1, 1.4_

- [ ] 3. Implement structured error handling
- [ ] 3.1 Create custom exception classes
  - Write RoleAssignmentException class
  - Write DuplicateRoleException class
  - Write InvalidBusinessScopeException class
  - _Requirements: 1.2, 1.4_

- [ ] 3.2 Add global exception handler
  - Create @ControllerAdvice class for role mapping exceptions
  - Implement structured error response format
  - Add field-level validation error handling
  - _Requirements: 1.4, 5.2, 5.3_

- [ ] 4. Enhance existing endpoint implementations
- [ ] 4.1 Improve duplicate role assignment prevention
  - Add checks for existing role assignments before creation
  - Implement proper error responses for duplicate assignments
  - Add validation in service layer calls
  - _Requirements: 1.2_

- [ ] 4.2 Enhance business scope validation
  - Strengthen enforceBusinessScope method with additional checks
  - Add validation for cross-business operations
  - Improve error messages for scope violations
  - _Requirements: 1.3, 6.1_

- [ ] 5. Implement role update functionality
- [ ] 5.1 Create role update endpoint
  - Add PUT /api/mappings/users/{userId}/roles/{businessId} endpoint
  - Implement role update validation logic
  - Add authorization checks for role updates
  - _Requirements: 2.1_

- [ ] 5.2 Create role removal endpoint
  - Add DELETE /api/mappings/users/{userId}/roles/{businessId}/{role} endpoint
  - Implement safe role removal with validation
  - Add authorization checks for role removal
  - _Requirements: 2.2_

- [ ] 6. Implement comprehensive role listing
- [ ] 6.1 Create user role listing endpoint
  - Add GET /api/mappings/users/{userId}/roles endpoint
  - Implement filtering and pagination for user roles
  - Add business scope validation for role access
  - _Requirements: 2.3_

- [ ] 6.2 Create business role listing endpoint
  - Add GET /api/mappings/businesses/{businessId}/all-roles endpoint
  - Implement comprehensive role listing with user details
  - Add filtering by role type and status
  - _Requirements: 2.3_

- [ ] 7. Implement role transfer functionality
- [ ] 7.1 Create role transfer endpoint
  - Add POST /api/mappings/users/{userId}/transfer-role endpoint
  - Implement role transfer validation and business logic
  - Add authorization checks for cross-business transfers
  - _Requirements: 2.4_

- [ ] 7.2 Add role transfer validation
  - Validate source and target business permissions
  - Check role compatibility between businesses
  - Implement atomic transfer operations
  - _Requirements: 2.4, 6.3_

- [ ] 8. Implement bulk operations support
- [ ] 8.1 Create bulk assignment endpoint
  - Add POST /api/mappings/bulk-assign endpoint
  - Implement batch processing for multiple role assignments
  - Add validation for bulk operation limits
  - _Requirements: 3.1_

- [ ] 8.2 Create bulk update endpoint
  - Add PUT /api/mappings/bulk-update endpoint
  - Implement batch role updates with validation
  - Add rollback mechanism for failed bulk operations
  - _Requirements: 3.1_

- [ ] 9. Implement audit trail functionality
- [ ] 9.1 Create audit trail data model
  - Write UserRoleAudit entity class
  - Create audit repository interface
  - Add audit service for logging role changes
  - _Requirements: 3.2, 6.2_

- [ ] 9.2 Create audit endpoints
  - Add GET /api/mappings/audit/users/{userId} endpoint
  - Add GET /api/mappings/audit/businesses/{businessId} endpoint
  - Implement audit trail filtering and pagination
  - _Requirements: 3.2, 6.2_

- [ ] 10. Implement reporting functionality
- [ ] 10.1 Create role assignment report endpoint
  - Add GET /api/mappings/reports/assignments endpoint
  - Implement role assignment analytics
  - Add filtering by date range, business, and role type
  - _Requirements: 3.3_

- [ ] 10.2 Add report data aggregation
  - Implement role distribution statistics
  - Add business-wise role assignment counts
  - Create role assignment trend analysis
  - _Requirements: 3.3_

- [ ] 11. Enhance security and authorization
- [ ] 11.1 Implement granular permission checks
  - Add method-level security annotations
  - Implement custom security expressions for business scope
  - Add role hierarchy validation
  - _Requirements: 6.1, 6.3_

- [ ] 11.2 Add security event logging
  - Implement security audit logging for unauthorized access attempts
  - Add logging for sensitive operations
  - Create security event monitoring
  - _Requirements: 6.4_

- [ ] 12. Add comprehensive unit tests
- [ ] 12.1 Write tests for validation logic
  - Test custom validation annotations
  - Test business scope validation methods
  - Test role assignment validation rules
  - _Requirements: 4.4_

- [ ] 12.2 Write tests for new endpoints
  - Test all CRUD operations with various user roles
  - Test bulk operations with edge cases
  - Test audit and reporting endpoints
  - _Requirements: 4.4_

- [ ] 13. Add integration tests
- [ ] 13.1 Write security integration tests
  - Test authorization rules across all endpoints
  - Test business scope enforcement
  - Test cross-business operation restrictions
  - _Requirements: 4.4, 6.1_

- [ ] 13.2 Write end-to-end workflow tests
  - Test complete role assignment workflows
  - Test role transfer and update scenarios
  - Test bulk operation workflows
  - _Requirements: 4.4_

- [ ] 14. Optimize performance and add caching
- [ ] 14.1 Implement caching for frequently accessed data
  - Add caching for user role lookups
  - Implement business scope caching
  - Add cache invalidation for role changes
  - _Requirements: 5.4_

- [ ] 14.2 Optimize database queries
  - Add database indexes for role queries
  - Optimize pagination queries
  - Implement efficient bulk operation queries
  - _Requirements: 5.4_

- [ ] 15. Update API documentation
- [ ] 15.1 Add OpenAPI specifications
  - Document all new endpoints with request/response schemas
  - Add authentication and authorization documentation
  - Include error response documentation
  - _Requirements: 5.1, 5.2, 5.3_

- [ ] 15.2 Create usage examples
  - Add code examples for common use cases
  - Document bulk operation patterns
  - Create integration guide for API consumers
  - _Requirements: 5.1_