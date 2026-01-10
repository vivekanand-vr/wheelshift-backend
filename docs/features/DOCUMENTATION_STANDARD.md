# Feature Documentation Standard

This document defines the standard structure for documenting features in WheelShift Pro.

## Purpose

- Provide **single-source-of-truth** documentation for each feature
- Keep documentation **crisp, practical, and reference-focused**
- Eliminate redundancy across multiple files
- Focus on **what developers need** rather than verbose explanations

## File Structure

Each feature should have **ONE consolidated README.md** containing all essential information:

```
docs/features/
  feature-name/
    README.md          # Single comprehensive documentation
```

❌ **Avoid:**
- Multiple files (IMPLEMENTATION.md, API_RESPONSES.md, QUICK_START.md, etc.)
- Verbose architecture diagrams
- Frontend framework-specific examples (we use Swagger for API docs)
- Lengthy prose explanations

## Standard Sections

### 1. Title & Overview (Required)
```markdown
# Feature Name

## Overview
2-3 sentence summary of what the feature does and its purpose.
```

**Example:**
```markdown
# Role-Based Access Control (RBAC)

## Overview
Comprehensive RBAC system with hierarchical roles, fine-grained permissions, 
data scopes, and resource-level ACLs.
```

---

### 2. Core Components (Required)
List the main components/concepts with brief descriptions.

```markdown
## Core Components

1. **Component 1** - Brief description
2. **Component 2** - Brief description
3. **Component 3** - Brief description
```

**Example:**
```markdown
## Core Components

1. **Roles** - User roles in the system (SUPER_ADMIN, ADMIN, SALES, etc.)
2. **Permissions** - Fine-grained access controls (`resource:action` format)
3. **Data Scopes** - Filter data by location, department, or assignment
4. **Resource ACLs** - Individual resource-level access control
```

---

### 3. Key Concepts/Properties (If Applicable)
Tables, enums, or structured data definitions.

```markdown
## [Entity] Properties

### Enums
List enums with values and descriptions in table format

### Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
```

**Example:**
```markdown
## Task Properties

### Enums

**TaskStatus:**
- `TODO` - Pending, not started
- `IN_PROGRESS` - Currently being worked on
- `DONE` - Completed and approved

### Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Auto | Unique identifier |
| title | String | Yes | Task title (max 128 chars) |
```

---

### 4. Quick Start (Required)
Practical, copy-paste examples for immediate use.

```markdown
## Quick Start

### Use Case 1
\`\`\`bash
# HTTP example
POST /api/v1/endpoint
\`\`\`

### Use Case 2
\`\`\`java
// Code example
@Autowired
private Service service;

service.method();
\`\`\`
```

**Guidelines:**
- Show both HTTP/curl AND Java code examples
- Focus on most common use cases (2-4 examples)
- Use realistic data in examples
- Keep examples short and focused

---

### 5. API Endpoints (Required for REST APIs)
Clean table format with all endpoints.

```markdown
## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/resource` | Create resource |
| GET | `/api/v1/resource/{id}` | Get by ID |
| GET | `/api/v1/resource` | Get all (paginated) |

### Query Parameters (if applicable)
List important query parameters with types and descriptions
```

**Example:**
```markdown
## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **CRUD Operations** |
| POST | `/api/v1/tasks` | Create task |
| PUT | `/api/v1/tasks/{id}` | Update task |
| DELETE | `/api/v1/tasks/{id}` | Delete task |
```

---

### 6. Database Schema (If Applicable)
Complete SQL with tables and indexes.

```markdown
## Database Schema

\`\`\`sql
-- Main table
CREATE TABLE table_name (
    id BIGSERIAL PRIMARY KEY,
    field VARCHAR(100) NOT NULL,
    -- ... other fields
);

-- Indexes
CREATE INDEX idx_table_field ON table_name(field);
\`\`\`

**Migrations:**
- `V4__Migration_Name.sql` - Description
```

**Guidelines:**
- Show complete CREATE TABLE statements
- Include important indexes
- List migration file names
- Keep it concise, avoid verbose comments

---

### 7. Implementation Classes (Required)
Organized list of all code artifacts.

```markdown
## Implementation Classes

### Services
- `ServiceName` - Interface
- `ServiceNameImpl` - Implementation

### Controllers
- `ControllerName` - REST endpoints

### Repositories
- `RepositoryName` - Data access

### DTOs
- `RequestDTO` - Request model
- `ResponseDTO` - Response model

### Enums
- `EnumName` - Description
```

---

### 8. Response Format (If Applicable for APIs)
Show standard response structures.

```markdown
## Response Format

All responses use standard wrapper:

\`\`\`json
{
  "success": true,
  "message": "Success",
  "data": { /* response data */ },
  "timestamp": "2025-12-25T10:30:00"
}
\`\`\`

**Paginated Response:**
\`\`\`json
{
  "data": {
    "content": [ /* items */ ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150
  }
}
\`\`\`
```

---

### 9. Use Cases & Integration (Optional but Recommended)
Practical integration scenarios.

```markdown
## Use Cases & Integration

### Scenario 1: [Use Case Name]
\`\`\`
Description of the use case
- Step 1
- Step 2
\`\`\`

### Scenario 2: [Use Case Name]
\`\`\`
Description
\`\`\`
```

**Example:**
```markdown
## Use Cases & Integration

### Kanban Board View
\`\`\`
Display tasks grouped by status columns
- Fetch: GET /api/v1/tasks/status/{status} for each column
- Drag & drop: PUT /api/v1/tasks/{id}/status
\`\`\`
```

---

### 10. Error Handling (Required for APIs)
Common errors with examples.

```markdown
## Error Handling

| Code | Description | Example |
|------|-------------|---------|
| 400 | Bad Request | Missing required field |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Database connection issue |

**Error Response:**
\`\`\`json
{
  "success": false,
  "message": "Error message",
  "error": "ErrorType",
  "timestamp": "2025-12-25T10:30:00"
}
\`\`\`
```

---

### 11. Best Practices (Required)
Numbered list of 5-10 key practices.

```markdown
## Best Practices

1. **Practice 1** - Description
2. **Practice 2** - Description
3. **Practice 3** - Description
```

**Example:**
```markdown
## Best Practices

1. **Always Paginate** - Use pagination for all list endpoints (default: 20 items/page)
2. **Handle Null Values** - Check for null in optional fields
3. **Validate Input** - Validate all user input before processing
```

---

### 12. Security & Permissions (If Applicable)
Security considerations.

```markdown
## Security & Permissions

- **Authentication** - Required for all endpoints
- **RBAC Applied** - Role-based access control enforced
- **Data Scopes** - Users see data within their scope
```

---

### 13. Performance Optimization (Optional)
Performance tips and optimizations.

```markdown
## Performance Optimization

- Pagination enabled by default
- Database indexes on: field1, field2, field3
- Caching strategy: TTL 5 minutes
```

---

### 14. Testing Examples (Recommended)
Practical test examples.

```markdown
## Testing Examples

\`\`\`java
@Test
public void testFeature() {
    // Arrange
    Data data = createTestData();
    
    // Act
    Result result = service.method(data);
    
    // Assert
    assertNotNull(result);
    assertEquals(expected, result.getValue());
}
\`\`\`
```

**Guidelines:**
- Show 1-3 complete test examples
- Use descriptive test names
- Include assertions
- Keep tests focused

---

### 15. API Documentation Link (Required)
```markdown
## API Documentation

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
```

---

## Content Guidelines

### Writing Style

✅ **DO:**
- Use concise, direct language
- Focus on practical examples
- Use tables for structured data
- Show code over explaining code
- Use bullet points and numbered lists
- Keep paragraphs to 2-3 sentences max

❌ **DON'T:**
- Write lengthy explanations
- Include architecture diagrams (unless critical)
- Provide framework-specific frontend examples
- Duplicate information available in Swagger
- Create multiple documentation files
- Include "future enhancements" wishlists

### Code Examples

✅ **DO:**
- Show complete, runnable examples
- Use realistic data
- Include imports when necessary
- Format code properly
- Add brief inline comments for clarity

❌ **DON'T:**
- Use placeholder values like "YOUR_VALUE_HERE"
- Show incomplete code snippets
- Include verbose comments
- Mix multiple concerns in one example

### JSON Examples

✅ **DO:**
- Show complete JSON structures
- Use realistic field values
- Format with proper indentation
- Include comments for clarity (as JSON doesn't support them, use markdown text)

❌ **DON'T:**
- Use `...` to indicate omitted fields
- Show incomplete structures
- Include every possible field (focus on important ones)

---

## Template Structure

Here's the recommended section order:

1. **Title & Overview** ⭐ Required
2. **Core Components** ⭐ Required
3. **Key Concepts/Properties** (if applicable)
4. **Quick Start** ⭐ Required
5. **API Endpoints** (for REST APIs)
6. **Database Schema** (if applicable)
7. **Implementation Classes** ⭐ Required
8. **Response Format** (for APIs)
9. **Use Cases & Integration** (recommended)
10. **Error Handling** (for APIs)
11. **Best Practices** ⭐ Required
12. **Security & Permissions** (if applicable)
13. **Performance Optimization** (optional)
14. **Testing Examples** (recommended)
15. **API Documentation Link** ⭐ Required

---

## Example: Minimal Feature Documentation

```markdown
# Feature Name

## Overview
Brief 2-3 sentence description of the feature.

## Core Components
1. **Component 1** - Description
2. **Component 2** - Description

## Quick Start

\`\`\`bash
# Example usage
POST /api/v1/endpoint
{
  "field": "value"
}
\`\`\`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/resource` | Create resource |

## Implementation Classes

### Services
- `FeatureService` - Main service interface

### Controllers  
- `FeatureController` - REST endpoints

## Best Practices

1. **Practice 1** - Description
2. **Practice 2** - Description

## API Documentation

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
```

---

## Checklist

Before finalizing documentation, verify:

- [ ] Single README.md file (no additional files)
- [ ] Title and overview present
- [ ] Core components listed
- [ ] Quick Start with practical examples
- [ ] API endpoints documented (if applicable)
- [ ] Implementation classes listed
- [ ] Best practices included (5-10 items)
- [ ] Code examples are complete and runnable
- [ ] JSON examples are properly formatted
- [ ] Tables used for structured data
- [ ] No verbose prose or unnecessary explanations
- [ ] Swagger UI link included
- [ ] Total length: 300-500 lines (aim for crisp, focused content)

---

## Reference Examples

See these feature docs for reference:
- [Dashboard](./dashboard/README.md) - ~100 lines
- [Notifications](./notifications/README.md) - ~480 lines  
- [RBAC](./rbac/README.md) - ~350 lines
- [Tasks](./tasks/README.md) - ~400 lines

---

## Version History

- **v1.0** (2026-01-10) - Initial documentation standard
