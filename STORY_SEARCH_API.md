# Story Search and Filter API

This document describes the new search and filter functionality for stories in the Breakup Stories application.

## Overview

The search functionality allows users to filter stories by multiple criteria:
- **Language**: Filter stories by their language
- **Title**: Search for stories with titles containing specific text (case-insensitive)
- **Date Range**: Filter stories by creation date range (both start and end dates must be provided)

## API Endpoints

### 1. GET /api/stories/search

Search and filter stories using query parameters.

**Parameters:**
- `language` (optional): Language code to filter by (e.g., "en", "es", "fr")
- `titleContains` (optional): Text to search in story titles (case-insensitive)
- `createdAtStart` (optional): Start date for filtering (ISO format: YYYY-MM-DDTHH:mm:ss)
- `createdAtEnd` (optional): End date for filtering (ISO format: YYYY-MM-DDTHH:mm:ss)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10, max: 100)

**Example Requests:**

```bash
# Search for English stories
GET /api/stories/search?language=en&page=0&size=10

# Search for stories with "love" in the title
GET /api/stories/search?titleContains=love&page=0&size=10

# Search for stories created between specific dates
GET /api/stories/search?createdAtStart=2024-01-01T00:00:00&createdAtEnd=2024-12-31T23:59:59&page=0&size=10

# Combined search: English stories with "heartbreak" in title
GET /api/stories/search?language=en&titleContains=heartbreak&page=0&size=10
```

### 2. POST /api/stories/search

Search and filter stories using JSON body.

**Request Body:**
```json
{
  "language": "en",
  "titleContains": "love",
  "createdAtStart": "2024-01-01T00:00:00",
  "createdAtEnd": "2024-12-31T23:59:59",
  "page": 0,
  "size": 10
}
```

## Filter Logic

### Language Filter
- Filters stories by the `metadata.language` field
- Exact match (case-sensitive)
- If not provided, all languages are included

### Title Filter
- Searches for stories with titles containing the specified text
- Case-insensitive search using MongoDB regex
- If not provided, all titles are included

### Date Range Filter
- **Both start and end dates must be provided** for date filtering to work
- Filters stories by `createdAt` field
- Start date is inclusive (`>=`)
- End date is inclusive (`<=`)
- If only one date is provided, date filtering is ignored

### Combined Filters
- All filters are applied using AND logic
- Only non-null filters are applied
- Stories must be in ACTIVE status to be included

## Response Format

Both endpoints return the same response format:

```json
{
  "data": [
    {
      "id": "story_id",
      "title": "Story Title",
      "content": "Story content...",
      "audioUrl": "https://example.com/audio.mp3",
      "thumbnailUrl": "https://example.com/thumbnail.jpg",
      "language": "en",
      "tags": ["love", "heartbreak"],
      "viewCount": 150,
      "likeCount": 25,
      "commentCount": 10,
      "likedByMe": false,
      "bookmarkedByMe": true,
      "username": "john_doe",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 150,
  "totalPages": 15
}
```

## Authentication

- **Authenticated users**: Get stories with `likedByMe` and `bookmarkedByMe` status
- **Unauthenticated users**: Get stories without user-specific status (both fields will be false)

## Validation Rules

1. **Date Range**: If both start and end dates are provided, start date must not be after end date
2. **Page Number**: Must be non-negative
3. **Page Size**: Must be between 1 and 100
4. **Date Format**: Must be in ISO format (YYYY-MM-DDTHH:mm:ss)

## Error Responses

### 400 Bad Request
```json
{
  "error": "Start date cannot be after end date",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400
}
```

### 400 Bad Request (Invalid Date Format)
```json
{
  "error": "Invalid date format. Use ISO format: YYYY-MM-DDTHH:mm:ss",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400
}
```

## Usage Examples

### Frontend Integration

```javascript
// Search for English stories with "love" in title
const searchStories = async (filters) => {
  const params = new URLSearchParams();
  
  if (filters.language) params.append('language', filters.language);
  if (filters.titleContains) params.append('titleContains', filters.titleContains);
  if (filters.createdAtStart) params.append('createdAtStart', filters.createdAtStart);
  if (filters.createdAtEnd) params.append('createdAtEnd', filters.createdAtEnd);
  if (filters.page !== undefined) params.append('page', filters.page);
  if (filters.size !== undefined) params.append('size', filters.size);
  
  const response = await fetch(`/api/stories/search?${params.toString()}`, {
    headers: {
      'Authorization': `Bearer ${token}` // if authenticated
    }
  });
  
  return response.json();
};

// Example usage
const results = await searchStories({
  language: 'en',
  titleContains: 'love',
  page: 0,
  size: 20
});
```

### cURL Examples

```bash
# Search for English stories
curl -X GET "http://localhost:8080/api/stories/search?language=en&page=0&size=10"

# Search for stories with "heartbreak" in title
curl -X GET "http://localhost:8080/api/stories/search?titleContains=heartbreak&page=0&size=10"

# Search for stories created in January 2024
curl -X GET "http://localhost:8080/api/stories/search?createdAtStart=2024-01-01T00:00:00&createdAtEnd=2024-01-31T23:59:59&page=0&size=10"

# Combined search with authentication
curl -X GET "http://localhost:8080/api/stories/search?language=en&titleContains=love&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Performance Considerations

1. **Indexing**: Ensure proper MongoDB indexes on:
   - `status` field
   - `metadata.language` field
   - `title` field
   - `createdAt` field

2. **Pagination**: Always use pagination to limit result size
3. **Date Range**: Large date ranges may impact performance
4. **Text Search**: Title search uses regex which may be slower than exact matches

## Database Query

The search functionality uses a custom MongoDB query that:
- Always filters for ACTIVE stories
- Applies filters only when values are provided
- Uses case-insensitive regex for title search
- Handles date range filtering efficiently
- Supports pagination

## Future Enhancements

Potential improvements for future versions:
1. Full-text search across multiple fields
2. Fuzzy matching for title search
3. Advanced filtering by tags, view count, like count
4. Sorting options (by date, popularity, relevance)
5. Search result highlighting
6. Search suggestions/autocomplete 