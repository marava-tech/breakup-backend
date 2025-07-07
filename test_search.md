# Story Search API Implementation

## Overview
I have successfully implemented a unified search API for stories that searches both title and tags using a single search content parameter, with priority given to title matches.

## New API Endpoint

### GET `/api/stories/search`

**Parameters:**
- `searchContent` (required): Search term to look for in story titles and tags (case-insensitive)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Examples:**

1. Search for "breakup" in titles and tags:
```
GET /api/stories/search?searchContent=breakup&page=0&size=10
```

2. Search for "love" in titles and tags:
```
GET /api/stories/search?searchContent=love&page=0&size=10
```

3. Search with authentication (includes likedByMe and bookmarkedByMe):
```
GET /api/stories/search?searchContent=heartbreak&page=0&size=10
Authorization: Bearer <jwt_token>
```

## Implementation Details

### StoryService.searchStoriesByContent()
- Searches all active stories
- **Priority-based search**: Title matches are returned first, then tag matches
- Case-insensitive search across both title and tags
- Supports both authenticated and unauthenticated users
- Returns proper pagination with total count
- Includes comprehensive logging with request IDs

### StoryController.searchStoriesByContent()
- Takes a single `searchContent` parameter
- Handles both authenticated and unauthenticated requests
- Provides proper error handling and logging
- Returns PagedResponse<StoryResponse> with full story details

## Search Logic & Priority

1. **Title Priority**: Stories with matching titles are returned first
2. **Tag Matching**: Stories with matching tags (but no title match) are returned second
3. **Case-Insensitive**: All searches are case-insensitive
4. **Partial Matching**: Supports partial matches in both title and tags
5. **No Duplicates**: Stories that match both title and tags appear only once (in title results)

### Search Priority Order:
1. **Title Matches**: Stories where `searchContent` appears in the title
2. **Tag Matches**: Stories where `searchContent` appears in any tag (but not in title)

## Response Format

```json
{
  "content": [
    {
      "id": "story123",
      "title": "My Breakup Story",
      "content": "This is my story...",
      "tags": ["sad", "heartbreak"],
      "viewCount": 50,
      "likeCount": 10,
      "commentCount": 5,
      "likedByMe": false,
      "bookmarkedByMe": true,
      "username": "john_doe",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "last": false
}
```

## Features

- ✅ **Unified Search**: Single parameter searches both title and tags
- ✅ **Priority-based Results**: Title matches appear before tag matches
- ✅ **Case-insensitive search**
- ✅ **Partial matching** for both title and tags
- ✅ **No duplicates**: Stories appear only once in results
- ✅ **Proper pagination**
- ✅ **Authentication-aware responses**
- ✅ **Comprehensive error handling**
- ✅ **Request ID tracking** for debugging
- ✅ **Performance optimized** (filters active stories only)

## Usage Examples

### cURL Examples

```bash
# Search for "breakup" in titles and tags
curl -X GET "http://localhost:8080/api/stories/search?searchContent=breakup&page=0&size=10"

# Search for "love" in titles and tags
curl -X GET "http://localhost:8080/api/stories/search?searchContent=love&page=0&size=10"

# Search for "sad" in titles and tags
curl -X GET "http://localhost:8080/api/stories/search?searchContent=sad&page=0&size=10"

# Authenticated search
curl -X GET "http://localhost:8080/api/stories/search?searchContent=heartbreak&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Frontend Integration

```javascript
// Search stories by content (title and tags)
const searchStories = async (searchContent, page = 0, size = 10) => {
  const params = new URLSearchParams();
  params.append('searchContent', searchContent);
  params.append('page', page);
  params.append('size', size);
  
  const response = await fetch(`/api/stories/search?${params.toString()}`, {
    headers: {
      'Authorization': `Bearer ${token}` // if authenticated
    }
  });
  
  return response.json();
};

// Example usage
const results = await searchStories('breakup', 0, 20);
console.log('Found', results.totalElements, 'stories');

// Search for different terms
const loveResults = await searchStories('love', 0, 10);
const sadResults = await searchStories('sad', 0, 10);
```

## Search Examples

### Example 1: Search for "breakup"
- **Title matches**: "My Breakup Story", "Breakup Recovery", "Post-Breakup Life"
- **Tag matches**: Stories with tags like "breakup", "post-breakup", "breakup-recovery"

### Example 2: Search for "love"
- **Title matches**: "Love Story", "Finding Love", "True Love"
- **Tag matches**: Stories with tags like "love", "romance", "true-love"

### Example 3: Search for "sad"
- **Title matches**: "Sad Story", "Feeling Sad", "Sad Times"
- **Tag matches**: Stories with tags like "sad", "sadness", "depression"

## Error Handling

- Returns 400 Bad Request for invalid parameters
- Proper logging with request IDs for debugging
- Graceful handling of empty search content
- Safe handling of null/empty story data
- Returns empty results for empty search terms

## Performance Considerations

- Only searches active stories (status = ACTIVE)
- Uses in-memory filtering for small datasets
- Proper pagination to limit result size
- Efficient priority-based sorting
- Early termination for tag search when title match is found 