# Reward Configuration API

## Overview
The Reward Configuration API provides key-value pairs of all reward and referral configurations that can be displayed in the frontend and modified through the database.

## Endpoint

### GET `/api/rewards/configurations`

Returns all reward and referral configurations as key-value pairs.

**Response:**
```json
{
  "rewardConfigs": {
    "storyActiveReward": "50",
    "hundredLikesMilestone": "15",
    "thousandViewsMilestone": "15"
  },
  "referralConfigs": {
    "referrerReward": "50",
    "referredUserWelcomeBonus": "30",
    "maxReferralsPerUser": "100"
  }
}
```

## Configuration Keys

### Reward Configurations
- `storyActiveReward`: Points awarded for stories that become active (duration > 10 minutes)
- `hundredLikesMilestone`: Points awarded for reaching 100 likes milestone
- `thousandViewsMilestone`: Points awarded for reaching 1000 views milestone

### Referral Configurations
- `referrerReward`: Points awarded to the user who refers someone
- `referredUserWelcomeBonus`: Points awarded to the new user who was referred
- `maxReferralsPerUser`: Maximum number of referrals allowed per user

## Frontend Usage Example

```javascript
// Fetch reward configurations
const response = await fetch('/api/rewards/configurations');
const config = await response.json();

// Display reward information
console.log(`Upload a story and get ${config.rewardConfigs['storyActiveReward']} coins!`);
console.log(`Reach 100 likes and earn ${config.rewardConfigs['hundredLikesMilestone']} coins!`);
console.log(`Refer a friend and get ${config.referralConfigs['referrerReward']} coins!`);
```

## Database Modification

To change any configuration value, update the corresponding record in the `default_config` collection:

```javascript
// Example: Update 100 likes milestone reward
db.default_config.updateOne(
  { "key": "default_100_likes_points" },
  { $set: { "value": "25" } }
);
```

## Error Handling

If the configuration retrieval fails, the API returns default values:
- `storyActiveReward`: 50
- `hundredLikesMilestone`: 15
- `thousandViewsMilestone`: 15
- `referrerReward`: 50
- `referredUserWelcomeBonus`: 30
- `maxReferralsPerUser`: 100 