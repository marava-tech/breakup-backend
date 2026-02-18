🧱 BACKEND — CHANGE ANALYSIS

Reference: 

backend-prd

Your backend is powerful… but overbuilt for your current DAU.

You built for scale + gamification + admin-heavy ecosystem.

Right now, that complexity is hurting focus.

🔴 1. Remove / Deprioritize (Immediate Cleanup)
❌ Coins System

Coins

Wallet

Withdrawals

Referral tracking

Admin coin invalidation

These create:

Unnecessary schema weight

Complex business logic

More moderation overhead

Wrong emotional tone for breakup app

Recommendation:

Soft-disable in config

Don’t delete tables yet

Mark as deprecated in v2

❌ Mixed Media Story Model

Current:

Story supports audio, text, image, video in content list model 

backend-prd

This is over-generic.

You said:

Primarily 3–6 minute audio stories.

You don’t need dynamic “Content List”.

Recommendation:

Move to Audio-First Simplified Story Model:

Story {
  id
  title
  audioUrl
  coverImageUrl
  durationSeconds
  language
  category
  isAnonymous
  status
  playCount
  likeCount
  saveCount
  createdAt
}


Keep text stories only if active usage justifies it.

🟡 2. Refactor (Important)
🔁 OTP Gmail Restriction

Frontend restricts Gmail only 

frontend-prd


Backend supports generic email.

You should:

Remove Gmail-only restriction

Keep OTP-based login

Validate via proper email regex

You're artificially limiting growth.

🔁 Emotions with Scoring

Backend currently supports emotion scoring 

backend-prd

.

For now:
Replace with simple enum:

category: 
- FRESH_BREAKUP
- TOXIC
- HEALING
- LATE_NIGHT
- STRONG
- NUMB


Scoring is unnecessary at 10 DAU.

🔁 AI Consoling Messages

You store AI consoling message per story 

backend-prd

.

Question:
Are users actively using this?

If not:
Move it to P2.
Do not fetch in main story detail response.

🟢 3. Add (Critical for Audio App)
✅ Listening Progress Model

You currently don’t clearly define persistent progress tracking.

Add:

ListeningProgress {
  userId
  storyId
  progressSeconds
  completed
  updatedAt
}


And:

GET /stories/resume

POST /progress

This alone increases retention.

✅ Completion Logic

Add:

Increment playCount after 10 sec

Mark completed after 90%

Use this to power:

Most Listened

Completion-based ranking

✅ Simplified Feed Endpoints

Right now you have:

SIMILAR

LANGUAGE

NEAR_ME

Search

Config-driven feed

Too much.

Reduce to:

GET /stories/featured
GET /stories?category=
GET /stories?language=
GET /stories/most-listened


No geolocation (NEAR_ME).
Not relevant for breakup stories.

✅ Remove Location Tagging

Frontend supports optional location upload 

frontend-prd

.

Not emotionally aligned.
Remove from submission.


TODOS
=====

🔥 P0 — Critical (Must Ship Before UI Launch)
1️⃣ Add Listening Progress Tracking

Priority: P0
Status: Pending

Tasks:

Create ListeningProgress collection

Add POST /progress

Add GET /stories/resume

Upsert logic on update

Mark complete when >90%

Why:
This alone increases session duration and retention.

2️⃣ Simplify Story Model (Audio-First Refactor)

Priority: P0
Status: Pending

Tasks:

Deprecate mixed Content List

Introduce simplified audioUrl structure

Keep backward compatibility for old stories

Remove location dependency

Why:
Your current model is over-generic for 3–6 min audio.

3️⃣ Disable Coins / Wallet / Withdrawals

Priority: P0
Status: Pending

Tasks:

Disable withdrawal endpoints

Remove coin calculation logic triggers

Hide referral reward processing

Keep DB collections but mark deprecated
🧱 BACKEND — CHANGE ANALYSIS

Reference: 

backend-prd

Your backend is powerful… but overbuilt for your current DAU.

You built for scale + gamification + admin-heavy ecosystem.

Right now, that complexity is hurting focus.

🔴 1. Remove / Deprioritize (Immediate Cleanup)
❌ Coins System

Coins

Wallet

Withdrawals

Referral tracking

Admin coin invalidation

These create:

Unnecessary schema weight

Complex business logic

More moderation overhead

Wrong emotional tone for breakup app

Recommendation:

Soft-disable in config

Don’t delete tables yet

Mark as deprecated in v2

❌ Mixed Media Story Model

Current:

Story supports audio, text, image, video in content list model 

backend-prd

This is over-generic.

You said:

Primarily 3–6 minute audio stories.

You don’t need dynamic “Content List”.

Recommendation:

Move to Audio-First Simplified Story Model:

Story {
  id
  title
  audioUrl
  coverImageUrl
  durationSeconds
  language
  category
  isAnonymous
  status
  playCount
  likeCount
  saveCount
  createdAt
}


Keep text stories only if active usage justifies it.

🟡 2. Refactor (Important)
🔁 OTP Gmail Restriction

Frontend restricts Gmail only 

frontend-prd


Backend supports generic email.

You should:

Remove Gmail-only restriction

Keep OTP-based login

Validate via proper email regex

You're artificially limiting growth.

🔁 Emotions with Scoring

Backend currently supports emotion scoring 

backend-prd

.

For now:
Replace with simple enum:

category: 
- FRESH_BREAKUP
- TOXIC
- HEALING
- LATE_NIGHT
- STRONG
- NUMB


Scoring is unnecessary at 10 DAU.

🔁 AI Consoling Messages

You store AI consoling message per story 

backend-prd

.

Question:
Are users actively using this?

If not:
Move it to P2.
Do not fetch in main story detail response.

🟢 3. Add (Critical for Audio App)
✅ Listening Progress Model

You currently don’t clearly define persistent progress tracking.

Add:

ListeningProgress {
  userId
  storyId
  progressSeconds
  completed
  updatedAt
}


And:

GET /stories/resume

POST /progress

This alone increases retention.

✅ Completion Logic

Add:

Increment playCount after 10 sec

Mark completed after 90%

Use this to power:

Most Listened

Completion-based ranking

✅ Simplified Feed Endpoints

Right now you have:

SIMILAR

LANGUAGE

NEAR_ME

Search

Config-driven feed

Too much.

Reduce to:

GET /stories/featured
GET /stories?category=
GET /stories?language=
GET /stories/most-listened


No geolocation (NEAR_ME).
Not relevant for breakup stories.

✅ Remove Location Tagging

Frontend supports optional location upload 

frontend-prd

.

Not emotionally aligned.
Remove from submission.


TODOS
=====

🔥 P0 — Critical (Must Ship Before UI Launch)
1️⃣ Add Listening Progress Tracking

Priority: P0
Status: Pending

Tasks:

Create ListeningProgress collection

Add POST /progress

Add GET /stories/resume

Upsert logic on update

Mark complete when >90%

Why:
This alone increases session duration and retention.

2️⃣ Simplify Story Model (Audio-First Refactor)

Priority: P0
Status: Pending

Tasks:

Deprecate mixed Content List

Introduce simplified audioUrl structure

Keep backward compatibility for old stories

Remove location dependency

Why:
Your current model is over-generic for 3–6 min audio.

3️⃣ Disable Coins / Wallet / Withdrawals

Priority: P0
Status: Pending

Tasks:

Disable withdrawal endpoints

Remove coin calculation logic triggers

Hide referral reward processing

Keep DB collections but mark deprecated

Why:
Emotional product > reward platform.

4️⃣ Remove Gmail-Only Restriction

Priority: P0
Status: Pending

Tasks:

Remove Gmail validation

Use standard email validation

Keep OTP-based auth

Why:
Unnecessary growth limiter.

5️⃣ Simplify Feed Endpoints

Priority: P0
Status: Pending

Tasks:

Consolidate feed logic

Remove NEAR_ME

Remove over-engineered filtering

Standardize to:

featured

category

most-listened

language

Why:
Cleaner contract for redesigned frontend.

🟡 P1 — Retention & Optimization
6️⃣ Completion Tracking & Ranking

Priority: P1
Status: Pending

Tasks:

Increment playCount after 10 seconds

Track completion rate

Add “most-listened” weighted by completion

Why:
Better discovery quality.

7️⃣ Autoplay Support

Priority: P1
Status: Pending

Tasks:

Add GET /stories/next?currentId=

Basic ranking logic

Why:
Supports autoplay next.

8️⃣ Mood-Based Filtering

Priority: P1
Status: Pending

Tasks:

Simplify emotion scoring → category enum

Add mood filter param to feed

Why:
Cleaner emotional mapping.

9️⃣ Performance Hardening

Priority: P1
Status: Pending

Tasks:

Add indexes:

language

category

createdAt

playCount

Ensure pagination is cursor-based

Why:
Prevent future feed slowdown.

🟢 P2 — Smart Layer
🔟 Nightly Curated Feed

Priority: P2
Status: Pending

Tasks:

Scheduled job for curated list

Based on completion + recency

1️⃣1️⃣ Basic Recommendation Scoring

Priority: P2
Status: Pending

Tasks:

Score by:

user language

liked category

completion rate

Store simple preference weight

1️⃣2️⃣ Spotify Playlist Integration (Phase 2 Vision)

Priority: P2
Status: Pending


Why:
Emotional product > reward platform.

4️⃣ Remove Gmail-Only Restriction

Priority: P0
Status: Pending

Tasks:

Remove Gmail validation

Use standard email validation

Keep OTP-based auth

Why:
Unnecessary growth limiter.

5️⃣ Simplify Feed Endpoints

Priority: P0
Status: Pending

Tasks:

Consolidate feed logic

Remove NEAR_ME

Remove over-engineered filtering

Standardize to:

featured

category

most-listened

language

Why:
Cleaner contract for redesigned frontend.

🟡 P1 — Retention & Optimization
6️⃣ Completion Tracking & Ranking

Priority: P1
Status: Pending

Tasks:

Increment playCount after 10 seconds

Track completion rate

Add “most-listened” weighted by completion

Why:
Better discovery quality.

7️⃣ Autoplay Support

Priority: P1
Status: Pending

Tasks:

Add GET /stories/next?currentId=

Basic ranking logic

Why:
Supports autoplay next.

8️⃣ Mood-Based Filtering

Priority: P1
Status: Pending

Tasks:

Simplify emotion scoring → category enum

Add mood filter param to feed

Why:
Cleaner emotional mapping.

9️⃣ Performance Hardening

Priority: P1
Status: Pending

Tasks:

Add indexes:

language

category

createdAt

playCount

Ensure pagination is cursor-based

Why:
Prevent future feed slowdown.

🟢 P2 — Smart Layer
🔟 Nightly Curated Feed

Priority: P2
Status: Pending

Tasks:

Scheduled job for curated list

Based on completion + recency

1️⃣1️⃣ Basic Recommendation Scoring

Priority: P2
Status: Pending

Tasks:

Score by:

user language

liked category

completion rate

Store simple preference weight

1️⃣2️⃣ Spotify Playlist Integration (Phase 2 Vision)

Priority: P2
Status: Pending

NOTE : NO NEED OF BACKWARD COMPATIBILITY . 