# Auditing System Documentation

## Overview

The Breakup Stories API now includes a comprehensive auditing system that tracks user interactions with stories, comments, likes, and audio playback. This system provides detailed insights into user behavior and helps with analytics, security monitoring, and compliance.

## 🎯 Features

### Tracked Interactions
- **Story Views**: When users view story details
- **Story Likes/Unlikes**: When users like or unlike stories
- **Comment Creation**: When users add comments to stories
- **Audio Playback**: Play, pause, and stop events with position tracking
- **Bookmark Actions**: Create and delete bookmarks

### Audit Data Captured
- **User Information**: User ID for authenticated users
- **Client Information**: User agent, IP address, session ID
- **Interaction Details**: Entity type, action type, entity ID
- **Metadata**: Additional context-specific information
- **Timestamps**: Creation and update times

## 📊 Audit Model

### Audit Entity
```java
public class Audit {
    private String id;
    private String userId;
    private EntityType entityType;
    private ActionType actionType;
    private String entityId;
    private String userAgent;
    private String ipAddress;
    private String sessionId;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Entity Types
- `STORY`: Story-related interactions
- `COMMENT`: Comment-related interactions
- `LIKE`: Like-related interactions
- `BOOKMARK`: Bookmark-related interactions
- `FEEDBACK`: Feedback-related interactions
- `USER`: User-related interactions
- `AUDIO`: Audio playback interactions
- `VIEW`: View-related interactions

### Action Types
- `CREATE`: Creation of new entities
- `UPDATE`: Updates to existing entities
- `DELETE`: Deletion of entities
- `VIEW`: Viewing entities
- `LIKE`: Liking entities
- `UNLIKE`: Unliking entities
- `COMMENT`: Commenting on entities
- `PLAY`: Starting audio playback
- `PAUSE`: Pausing audio playback
- `STOP`: Stopping audio playback
- `SHARE`: Sharing entities
- `DOWNLOAD`: Downloading entities

## 🔧 Implementation

### Services

#### AuditService
The main service for creating and querying audit records:

```java
@Service
public class AuditService {
    // Convenience methods for common interactions
    public void logStoryView(String userId, String storyId, String userAgent, String ipAddress, String sessionId);
    public void logStoryLike(String userId, String storyId, String userAgent, String ipAddress, String sessionId);
    public void logStoryUnlike(String userId, String storyId, String userAgent, String ipAddress, String sessionId);
    public void logCommentCreate(String userId, String commentId, String storyId, String userAgent, String ipAddress, String sessionId);
    public void logAudioPlay(String userId, String storyId, String userAgent, String ipAddress, String sessionId, Long duration, Long position);
    public void logAudioPause(String userId, String storyId, String userAgent, String ipAddress, String sessionId, Long duration, Long position);
    public void logAudioStop(String userId, String storyId, String userAgent, String ipAddress, String sessionId, Long duration, Long position);
}
```

#### ClientInfoService
Extracts client information from HTTP requests:

```java
@Service
public class ClientInfoService {
    public ClientInfo extractClientInfo();
    public ClientInfo extractClientInfo(HttpServletRequest request);
}
```

### Controllers Integration

#### StoryController
- **Story Views**: Audited when users view story details
- **Story Likes**: Audited when users like stories
- **Story Unlikes**: Audited when users unlike stories

#### CommentController
- **Comment Creation**: Audited when users create comments



## 📡 API Endpoints

### Audit Management (Admin Only)

#### Get All Audits
```http
GET /api/audits?page=0&size=10
Authorization: Bearer <admin-token>
```

#### Get Audits by User
```http
GET /api/audits/user/{userId}?page=0&size=10
Authorization: Bearer <admin-token>
```

#### Get Audits by Entity Type
```http
GET /api/audits/entity-type/{entityType}?page=0&size=10
Authorization: Bearer <admin-token>
```

#### Get Audits by Action Type
```http
GET /api/audits/action-type/{actionType}?page=0&size=10
Authorization: Bearer <admin-token>
```

#### Get Audits by User and Entity Type
```http
GET /api/audits/user/{userId}/entity-type/{entityType}?page=0&size=10
Authorization: Bearer <admin-token>
```

### Analytics Endpoints (Admin Only)

#### Story View Analytics
```http
GET /api/audits/analytics/story-views?storyId={storyId}&userId={userId}
Authorization: Bearer <admin-token>
```



#### User Activity Analytics
```http
GET /api/audits/analytics/user-activity?userId={userId}&dateRange={dateRange}
Authorization: Bearer <admin-token>
```



## 📈 Analytics Examples

### Story View Analytics Response
```json
{
  "total_views": 1250,
  "unique_users": 450,
  "story_id": "story123",
  "user_id": "user456"
}
```



### User Activity Analytics Response
```json
{
  "total_actions": 1250,
  "story_views": 450,
  "likes": 234,
  "comments": 89,

  "user_id": "user456",
  "date_range": "last_30_days"
}
```

## 🔍 Querying Audit Data

### MongoDB Queries

#### Get all story views for a specific story
```javascript
db.audits.find({
  entityType: "STORY",
  actionType: "VIEW",
  entityId: "story123"
})
```



#### Get user activity in the last 7 days
```javascript
db.audits.find({
  userId: "user456",
  createdAt: {
    $gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
  }
})
```

#### Get most viewed stories
```javascript
db.audits.aggregate([
  { $match: { entityType: "STORY", actionType: "VIEW" } },
  { $group: { _id: "$entityId", viewCount: { $sum: 1 } } },
  { $sort: { viewCount: -1 } },
  { $limit: 10 }
])
```

## 🛡️ Security Considerations

### Data Privacy
- **IP Addresses**: Stored for security monitoring but should be anonymized for long-term storage
- **User Agents**: Stored for analytics but can be truncated
- **Session IDs**: Used for tracking but should be rotated regularly

### Access Control
- **Admin Only**: All audit endpoints require ADMIN authority
- **User Data**: Users can only see their own audit data through specific endpoints
- **Data Retention**: Implement data retention policies for audit logs

### Compliance
- **GDPR**: Ensure audit data doesn't contain unnecessary personal information
- **Data Minimization**: Only collect necessary audit information
- **Right to Deletion**: Provide mechanisms to delete user audit data

## 📊 Performance Considerations

### Database Indexing
```javascript
// Create indexes for common queries
db.audits.createIndex({ userId: 1, createdAt: -1 });
db.audits.createIndex({ entityType: 1, actionType: 1 });
db.audits.createIndex({ entityId: 1, createdAt: -1 });
db.audits.createIndex({ createdAt: -1 });
```

### Data Archiving
- Implement automatic archiving of old audit data
- Use separate collections for different time periods
- Consider using MongoDB TTL indexes for automatic deletion

### Caching
- Cache frequently accessed analytics data
- Use Redis for real-time analytics
- Implement query result caching for admin dashboards

## 🔧 Configuration

### Environment Variables
```yaml
# Audit Configuration
audit.enabled: true
audit.retention-days: 365
audit.anonymize-ip: false
audit.log-user-agent: true
audit.log-session-id: true
```

### Application Properties
```properties
# Audit settings
audit.enabled=true
audit.retention-days=365
audit.anonymize-ip=false
audit.log-user-agent=true
audit.log-session-id=true
```

## 🚀 Usage Examples

### Frontend Integration

#### Track Audio Events
```javascript
// Track audio play event
async function trackAudioPlay(storyId, duration, position) {
  await fetch(`/api/audio/play/${storyId}?duration=${duration}&position=${position}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
}

// Track audio pause event
async function trackAudioPause(storyId, duration, position) {
  await fetch(`/api/audio/pause/${storyId}?duration=${duration}&position=${position}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
}
```

#### Analytics Dashboard
```javascript
// Get user activity analytics
async function getUserAnalytics(userId, dateRange) {
  const response = await fetch(`/api/audits/analytics/user-activity?userId=${userId}&dateRange=${dateRange}`, {
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
  return response.json();
}
```

## 📋 Monitoring and Alerts

### Key Metrics to Monitor
- **Audit Log Volume**: Number of audit entries per day
- **Database Performance**: Query response times for audit data
- **Storage Usage**: Audit data storage growth
- **Error Rates**: Failed audit log entries

### Alert Conditions
- **High Volume**: Unusual spike in audit log volume
- **Performance Issues**: Slow audit queries
- **Storage Alerts**: Approaching storage limits
- **Error Alerts**: High rate of audit logging failures

## 🔄 Future Enhancements

### Planned Features
1. **Real-time Analytics**: Live dashboard for user activity
2. **Advanced Filtering**: Complex query filters for audit data
3. **Export Functionality**: Export audit data for external analysis
4. **Machine Learning**: Anomaly detection in user behavior
5. **Integration**: Connect with external analytics platforms

### Performance Optimizations
1. **Streaming Analytics**: Real-time processing of audit events
2. **Data Warehousing**: Move old data to data warehouse
3. **Compression**: Compress audit data for storage efficiency
4. **Partitioning**: Partition audit data by date

---

This auditing system provides comprehensive tracking of user interactions while maintaining performance and security. It enables detailed analytics and helps ensure compliance with data protection regulations. 