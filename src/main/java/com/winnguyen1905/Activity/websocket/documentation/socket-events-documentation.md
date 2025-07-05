# Socket.IO Events Documentation

## Overview
This document provides detailed information about all Socket.IO events used in the Activity Management System for real-time communication between the client and server.

## Event Categories

### 1. Connection Events
Events related to client connection management and status.

### 2. Activity Events
Events for activity-related operations and notifications.

### 3. Message Events
Events for real-time messaging within activities.

### 4. Notification Events
Events for system-wide notifications and alerts.

---

## Server to Client Events

### Connection Events

#### `connection_established`
Sent when a client successfully connects and is authenticated.

**Data Structure:**
```json
{
  "userId": 123,
  "timestamp": 1640995200000,
  "message": "Connected successfully"
}
```

### Activity Events

#### `activity_status_changed`
Broadcast when an activity's status changes.

**Data Structure:**
```json
{
  "title": "Activity Status Updated",
  "message": "Activity 'Team Meeting' status changed from PUBLISHED to IN_PROGRESS",
  "type": "ACTIVITY",
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z",
  "activityName": "Team Meeting",
  "activityStartDate": "2024-01-01T14:00:00Z"
}
```

#### `activity_updated`
Sent when activity details (time, location, etc.) are modified.

**Data Structure:**
```json
{
  "activityId": 456,
  "activityName": "Team Meeting",
  "updateType": "TIME_CHANGE",
  "updateMessage": "Meeting time has been changed to 3:00 PM",
  "timestamp": "2024-01-01T10:00:00Z",
  "startDate": "2024-01-01T15:00:00Z",
  "endDate": "2024-01-01T16:00:00Z",
  "venue": "Conference Room A"
}
```

#### `activity_participant_joined`
Broadcast when a new participant joins an activity.

**Data Structure:**
```json
{
  "activityId": 456,
  "activityName": "Team Meeting",
  "participantId": 789,
  "participantName": "John Doe",
  "participantEmail": "john.doe@example.com",
  "updateType": "JOINED",
  "timestamp": "2024-01-01T10:00:00Z",
  "currentParticipantCount": 5,
  "maxParticipants": 20
}
```

#### `activity_participant_left`
Broadcast when a participant leaves an activity.

**Data Structure:**
```json
{
  "activityId": 456,
  "activityName": "Team Meeting",
  "participantId": 789,
  "participantName": "John Doe",
  "participantEmail": "john.doe@example.com",
  "updateType": "LEFT",
  "timestamp": "2024-01-01T10:00:00Z",
  "currentParticipantCount": 4,
  "maxParticipants": 20
}
```

#### `activity_reminder`
Sent as a reminder for upcoming activities.

**Data Structure:**
```json
{
  "title": "Activity Starting Tomorrow!",
  "message": "Your activity 'Team Meeting' starts in 1 days!",
  "type": "ACTIVITY",
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z",
  "daysUntilStart": 1,
  "activityName": "Team Meeting",
  "activityStartDate": "2024-01-02T14:00:00Z"
}
```

#### `activity_welcome`
Sent to new participants when they join an activity.

**Data Structure:**
```json
{
  "title": "Welcome to Team Meeting!",
  "message": "You have successfully joined this activity. Stay tuned for updates and announcements.",
  "type": "ACTIVITY",
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z",
  "activityName": "Team Meeting",
  "activityStartDate": "2024-01-02T14:00:00Z"
}
```

#### `activity_emergency_alert`
Sent for emergency notifications related to activities.

**Data Structure:**
```json
{
  "activityId": 456,
  "activityName": "Team Meeting",
  "alertType": "WEATHER",
  "alertMessage": "Heavy rain expected. Meeting moved to virtual format.",
  "timestamp": "2024-01-01T10:00:00Z",
  "severity": "HIGH"
}
```

### Message Events

#### `activity_message_broadcast`
Broadcast message to all activity participants.

**Data Structure:**
```json
{
  "messageId": 12345,
  "activityId": 456,
  "senderId": 123,
  "senderName": "John Doe",
  "content": "Hello everyone! Looking forward to the meeting.",
  "messageType": "TEXT",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `activity_announcement`
Official announcement from activity organizers.

**Data Structure:**
```json
{
  "messageId": 12346,
  "activityId": 456,
  "senderId": 789,
  "senderName": "Jane Smith",
  "content": "Please bring your laptops to the meeting.",
  "messageType": "ANNOUNCEMENT",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### Room Management Events

#### `joined_activity_room`
Confirmation that client has joined an activity room.

**Data Structure:**
```json
{
  "activityId": 456,
  "roomName": "activity_456",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `left_activity_room`
Confirmation that client has left an activity room.

**Data Structure:**
```json
{
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `join_activity_room_error`
Error when trying to join an activity room.

**Data Structure:**
```json
{
  "message": "Not authorized to join this activity room"
}
```

#### `activity_participants_list`
List of participants in an activity.

**Data Structure:**
```json
{
  "activityId": 456,
  "participants": [
    {
      "id": 123,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "isOnline": true
    },
    {
      "id": 789,
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "isOnline": false
    }
  ],
  "count": 2
}
```

### Status Events

#### `broadcast_success`
Confirmation that a broadcast message was sent successfully.

**Data Structure:**
```json
{
  "messageId": 12345,
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `broadcast_error`
Error when trying to broadcast a message.

**Data Structure:**
```json
{
  "message": "Invalid sender"
}
```

#### `emergency_alert_sent`
Confirmation that an emergency alert was sent.

**Data Structure:**
```json
{
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `emergency_alert_error`
Error when trying to send an emergency alert.

**Data Structure:**
```json
{
  "message": "Not authorized"
}
```

#### `subscribed_to_activity`
Confirmation of subscription to activity updates.

**Data Structure:**
```json
{
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### `unsubscribed_from_activity`
Confirmation of unsubscription from activity updates.

**Data Structure:**
```json
{
  "activityId": 456,
  "timestamp": "2024-01-01T10:00:00Z"
}
```

---

## Client to Server Events

### Room Management

#### `join_activity_room`
Request to join an activity room for real-time updates.

**Data Structure:**
```json
{
  "activityId": 456
}
```

**Requirements:**
- User must be a verified participant of the activity
- Valid activityId must be provided

#### `leave_activity_room`
Request to leave an activity room.

**Data Structure:**
```json
{
  "activityId": 456
}
```

#### `get_activity_participants`
Request the list of participants in an activity.

**Data Structure:**
```json
{
  "activityId": 456
}
```

### Messaging

#### `broadcast_to_activity`
Send a message to all participants in an activity.

**Data Structure:**
```json
{
  "messageId": 12345,
  "activityId": 456,
  "senderId": 123,
  "senderName": "John Doe",
  "content": "Hello everyone!",
  "messageType": "TEXT",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

**Requirements:**
- Sender must be a verified participant or organizer
- Valid activityId and senderId required

#### `send_emergency_alert`
Send an emergency alert (organizer only).

**Data Structure:**
```json
{
  "activityId": 456,
  "message": "Meeting cancelled due to emergency",
  "alertType": "GENERAL"
}
```

**Requirements:**
- Sender must be an organizer of the activity
- Valid activityId required

### Subscriptions

#### `subscribe_activity_updates`
Subscribe to activity updates.

**Data Structure:**
```json
{
  "activityId": 456
}
```

#### `unsubscribe_activity_updates`
Unsubscribe from activity updates.

**Data Structure:**
```json
{
  "activityId": 456
}
```

---

## Authentication

All Socket.IO connections require user authentication through the `userId` query parameter in the connection URL:

```javascript
const socket = io('http://localhost:9092', {
  query: {
    userId: 123
  }
});
```

---

## Error Handling

### Common Error Responses

#### Authentication Errors
```json
{
  "message": "Authentication required"
}
```

#### Authorization Errors
```json
{
  "message": "Not authorized to join this activity room"
}
```

#### Validation Errors
```json
{
  "message": "Invalid sender"
}
```

---

## Event Flow Examples

### Joining an Activity

1. Client sends `join_activity_room` with activityId
2. Server validates user participation
3. Server responds with `joined_activity_room` or `join_activity_room_error`
4. If successful, client receives real-time updates for that activity

### Sending a Message

1. Client sends `broadcast_to_activity` with message data
2. Server validates sender authorization
3. Server broadcasts message to all participants
4. Server responds with `broadcast_success` or `broadcast_error`

### Emergency Alert

1. Organizer sends `send_emergency_alert` with alert data
2. Server validates organizer permissions
3. Server broadcasts `activity_emergency_alert` to all participants
4. Server responds with `emergency_alert_sent` or `emergency_alert_error`

---

## Rate Limiting

To prevent abuse, the following rate limits are applied:

- **Message Broadcasting**: Maximum 10 messages per minute per user
- **Emergency Alerts**: Maximum 5 alerts per hour per organizer
- **Room Join/Leave**: Maximum 20 operations per minute per user

---

## Debugging

### Client-Side Debugging

Enable Socket.IO debug mode:
```javascript
localStorage.debug = 'socket.io-client:socket';
```

### Server-Side Logging

All events are logged with the following levels:
- **DEBUG**: Connection events, room operations
- **INFO**: Message broadcasts, emergency alerts
- **WARN**: Authorization failures
- **ERROR**: System errors, connection failures 
