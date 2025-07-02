# Reward System Documentation

## Overview

The Reward System provides a comprehensive coin-based reward mechanism for user engagement and referrals. Users can earn coins through various activities and use them for in-app purchases or features.

## Features

- ✅ **Coin Balance Management**: Track user coin balance with transaction history
- ✅ **Automatic Rewards**: Earn coins for story milestones and engagement
- ✅ **Referral System**: Earn coins by referring new users
- ✅ **Transaction History**: Complete audit trail of all coin transactions
- ✅ **Milestone Tracking**: Automatic rewards for likes and views milestones

## Reward Rules

### Story-Based Rewards

1. **Story Active Reward**: 50 coins
   - Triggered when a story becomes ACTIVE and duration > 10 minutes
   - One-time reward per story

2. **Likes Milestone**: 15 coins
   - Triggered when a story reaches 100 likes
   - One-time reward per story

3. **Views Milestone**: 15 coins
   - Triggered when a story reaches 1000 views
   - One-time reward per story

### Referral Rewards

1. **Referrer Bonus**: 50 coins
   - Awarded to the user who referred someone
   - Triggered when referred user completes registration

2. **Referred Welcome**: 30 coins
   - Awarded to the newly registered user
   - Triggered when using a valid referral code during registration

## Data Models

### User Model Updates
```java
public class User {
    // ... existing fields
    private String referralCode;    // Unique referral code
    private String referredBy;      // ID of user who referred this user
    private int coinBalance;        // Current coin balance
}
```

### CoinHistory Model
```java
public class CoinHistory {
    private String id;
    private String userId;
    private int count;              // Positive for earnings, negative for deductions
    private String reason;          // Reason for the transaction
    private String relatedEntityId; // Optional: related story/user ID
    private Long createdAt;
}
```

## API Endpoints

### Get Coin Balance
```http
GET /api/rewards/coins
Authorization: Bearer <token>
```

**Response:**
```json
{
  "totalCoins": 125,
  "coinHistory": [
    {
      "id": "coin123",
      "userId": "user456",
      "count": 50,
      "reason": "story_active",
      "relatedEntityId": "story789",
      "createdAt": 1640995200000
    },
    {
      "id": "coin124",
      "userId": "user456",
      "count": 15,
      "reason": "likes_milestone",
      "relatedEntityId": "story789",
      "createdAt": 1640995300000
    }
  ]
}
```

### Get Referral Statistics
```http
GET /api/rewards/referral-stats
Authorization: Bearer <token>
```

**Response:**
```json
{
  "referralCode": "ABC12345",
  "referredBy": "user123",
  "referredUsersCount": 3,
  "referredUsers": ["user456", "user789", "user012"]
}
```

### Admin Endpoints

#### Get Coin Balance by User ID
```http
GET /api/rewards/coins/{userId}
Authorization: Bearer <admin-token>
```

#### Get Referral Stats by User ID
```http
GET /api/rewards/referral-stats/{userId}
Authorization: Bearer <admin-token>
```

## Service Methods

### RewardService

#### Core Methods
- `getTotalCoins(String userId)`: Calculate total coins from history
- `getCoinBalance(String userId)`: Get balance with transaction history
- `addCoins(String userId, int count, String reason, String relatedEntityId)`: Add coins
- `deductCoins(String userId, int count, String reason)`: Deduct coins

#### Reward Check Methods
- `checkStoryActiveReward(String storyId)`: Check for story active reward
- `checkLikesMilestoneReward(String storyId)`: Check for likes milestone
- `checkViewsMilestoneReward(String storyId)`: Check for views milestone

#### Referral Methods
- `generateReferralCode(String userId)`: Generate unique referral code
- `processReferral(String newUserId, String referralCode)`: Process referral
- `getReferralStats(String userId)`: Get referral statistics

## Integration Points

### Story Processing
- **AsyncStoryProcessingService**: Calls `checkStoryActiveReward()` when story becomes ACTIVE
- **StoryService**: 
  - Calls `checkLikesMilestoneReward()` when story is liked
  - Calls `checkViewsMilestoneReward()` when view count is incremented

### User Registration
- **UserService**: 
  - Generates referral code for new users
  - Processes referral if referral code is provided during registration

## Transaction Reasons

### Earning Reasons
- `story_active`: Story becomes active with duration > 10 minutes
- `likes_milestone`: Story reaches 100 likes
- `views_milestone`: Story reaches 1000 views
- `referral_bonus`: Successfully referred a new user
- `referral_welcome`: Used referral code during registration

### Deduction Reasons
- `purchase`: In-app purchase
- `feature_unlock`: Unlocking premium features
- `penalty`: Penalty for policy violation

## Database Collections

### coin_history Collection
```javascript
{
  "_id": ObjectId("..."),
  "userId": "user123",
  "count": 50,
  "reason": "story_active",
  "relatedEntityId": "story456",
  "createdAt": NumberLong(1640995200000)
}
```

### users Collection Updates
```javascript
{
  "_id": ObjectId("..."),
  // ... existing fields
  "referralCode": "ABC12345",
  "referredBy": "user789",
  "coinBalance": 125
}
```

## Security Considerations

### Duplicate Prevention
- Each reward type is tracked with `relatedEntityId` to prevent duplicate rewards
- Users can only receive each milestone reward once per story

### Validation
- Referral codes are validated before processing
- Coin deductions check for sufficient balance
- All transactions are logged with timestamps

### Access Control
- User endpoints require authentication
- Admin endpoints require ADMIN role
- Users can only access their own coin data

## Performance Optimizations

### Caching
- User coin balance is cached in the User entity
- Total coins are calculated from history for accuracy

### Database Indexing
```javascript
// Create indexes for common queries
db.coin_history.createIndex({ userId: 1, createdAt: -1 });
db.coin_history.createIndex({ userId: 1, reason: 1, relatedEntityId: 1 });
db.users.createIndex({ referralCode: 1 });
db.users.createIndex({ referredBy: 1 });
```

## Monitoring and Analytics

### Key Metrics
- Total coins distributed
- Most common reward reasons
- Referral conversion rates
- User engagement correlation with rewards

### Logging
- All coin transactions are logged with detailed information
- Referral processing is logged for audit purposes
- Failed transactions are logged with reasons

## Future Enhancements

### Planned Features
- **Coin Spending**: Allow users to spend coins on features
- **Leaderboards**: Show top earners and referrers
- **Seasonal Events**: Special rewards for events
- **Tier System**: Different reward rates based on user tier
- **Gift System**: Allow users to gift coins to others

### Configuration
- Make reward amounts configurable
- Add time-based reward multipliers
- Implement reward expiration dates
- Add regional reward variations

## Testing

### Unit Tests
- Test reward calculations
- Test duplicate prevention
- Test referral processing
- Test edge cases (insufficient balance, invalid codes)

### Integration Tests
- Test reward triggers in story processing
- Test referral flow in user registration
- Test API endpoints with authentication

### Performance Tests
- Test coin balance calculation with large history
- Test referral processing under load
- Test concurrent reward processing 