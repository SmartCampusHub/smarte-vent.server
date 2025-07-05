# SocketIO Gateway Documentation - Activity Management System

## 🚀 Overview

This documentation describes the comprehensive SocketIO Gateway implementation for real-time activity management and communication. The system provides full-featured real-time notifications, messaging, and status updates for activity participants and organizers.

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Components](#components)
3. [Event Types](#event-types)
4. [Business Logic Features](#business-logic-features)
5. [Client Integration](#client-integration)
6. [Configuration](#configuration)
7. [Usage Examples](#usage-examples)
8. [API Reference](#api-reference)

## 🏗️ Architecture Overview

The SocketIO Gateway system consists of several key components:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│           (Web, Mobile, Desktop - Socket.IO clients)        │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 SocketIO Server                             │
│                 (Port 9092)                                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│               SocketIoGateway                               │
│          (Main business logic hub)                          │
├─────────────────────┬───────────────────────────────────────┤
│   Activity Status   │   Participant Mgmt   │   Messaging    │
│   Notifications     │   & Notifications     │   & Alerts     │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│          Supporting Services & Infrastructure                │
├────────────────┬────────────────┬───────────────────────────┤
│  SocketIOService│SocketCacheService│  ActivitySchedulingService│
│   (Connection   │   (Redis Cache)  │   (Scheduled Tasks)       │
│   Management)   │                  │                           │
└────────────────┴────────────────┴───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Data Layer                               │
├─────────────────┬───────────────────┬───────────────────────┤
│    Database     │       Redis       │     Email Service     │
│  (Activities,   │   (Cache, Sessions│    (Notifications)     │
│  Participants)  │   User Status)    │                       │
└─────────────────┴───────────────────┴───────────────────────┘
```

## 🔧 Components

### 1. **SocketIoGateway** (Main Business Logic Hub)
- **Location**: `src/main/java/com/winnguyen1905/activity/websocket/SocketIoGateway.java`
- **Purpose**: Central orchestrator for all activity-related real-time communications
- **Key Features**:
  - Activity status change broadcasting
  - Participant management notifications
  - Real-time messaging and announcements
  - Emergency alert system
  - Activity reminder system

### 2. **SocketIOService** (Connection Management)
- **Location**: `src/main/java/com/winnguyen1905/activity/rest/service/SocketIOService.java`
- **Purpose**: Manages WebSocket connections and basic messaging
- **Key Features**:
  - User connection tracking
  - Session management
  - Basic notification sending
  - Connection health monitoring

### 3. **SocketCacheService** (Redis-Based Caching)
- **Location**: `src/main/java/com/winnguyen1905/activity/rest/service/SocketCacheService.java`
- **Purpose**: Distributed caching for scalable real-time features
- **Key Features**:
  - User online status tracking
  - Activity participant caching
  - Typing indicators
  - Session persistence

### 4. **SocketEventHandlerService** (Event Processing)
- **Location**: `src/main/java/com/winnguyen1905/activity/rest/service/SocketEventHandlerService.java`
- **Purpose**: Handles incoming socket events and processes them
- **Key Features**:
  - Message routing
  - Event validation
  - User authentication
  - Activity authorization

## 📡 Event Types

### **Outgoing Events (Server → Client)**

#### Activity Management Events
| Event Name | Description | Data Structure |
|------------|-------------|----------------|
| `activity_status_changed` | Activity status updates (PENDING → PUBLISHED → IN_PROGRESS → COMPLETED) | `SocketNotificationDto` |
| `activity_updated` | Activity information changes (time, location, details) | `ActivityUpdateDto` |
| `activity_participant_joined` | New participant joined activity | `ParticipantUpdateDto` |
| `activity_participant_left` | Participant left activity | `ParticipantUpdateDto` |
| `activity_welcome` | Welcome message for new participants | `SocketNotificationDto` |

#### Messaging Events
| Event Name | Description | Data Structure |
|------------|-------------|----------------|
| `activity_message_broadcast` | Messages sent to all activity participants | `ActivityChatMessageDto` |
| `activity_announcement` | Announcements from organizers | `ActivityChatMessageDto` |
| `activity_emergency_alert` | Emergency alerts for participants | `EmergencyAlertDto` |

#### Reminder Events
| Event Name | Description | Data Structure |
|------------|-------------|----------------|
| `activity_reminder` | Activity reminders (today, tomorrow, 3 days) | `SocketNotificationDto` |
| `schedule_reminder` | Schedule-specific reminders | `SocketNotificationDto` |

#### Room Management Events
| Event Name | Description | Data Structure |
|------------|-------------|----------------|
| `joined_activity_room` | Confirmation of joining activity room | `Map<String, Object>` |
| `left_activity_room` | Confirmation of leaving activity room | `Map<String, Object>` |
| `activity_participants_list` | List of current activity participants | `Map<String, Object>` |

### **Incoming Events (Client → Server)**

#### Room Management
| Event Name | Parameters | Description |
|------------|------------|-------------|
| `join_activity_room` | `{ activityId: Long }` | Join an activity's real-time room |
| `leave_activity_room` | `{ activityId: Long }` | Leave an activity's real-time room |
| `get_activity_participants` | `{ activityId: Long }` | Get list of activity participants |

#### Messaging
| Event Name | Parameters | Description |
|------------|------------|-------------|
| `broadcast_to_activity` | `ActivityChatMessageDto` | Send message to all activity participants |
| `send_emergency_alert` | `{ activityId, message, alertType }` | Send emergency alert (organizers only) |

#### Subscriptions
| Event Name | Parameters | Description |
|------------|------------|-------------|
| `subscribe_activity_updates` | `{ activityId: Long }` | Subscribe to activity updates |
| `unsubscribe_activity_updates` | `{ activityId: Long }` | Unsubscribe from activity updates |

## 🎯 Business Logic Features

### 1. **Activity Status Management**
Automatically broadcasts status changes to all participants:

```java
// Example: Activity transitions from PUBLISHED to IN_PROGRESS
socketIoGateway.broadcastActivityStatusChange(activity, 
    ActivityStatus.PUBLISHED, ActivityStatus.IN_PROGRESS);
```

**Status Flow:**
- `PENDING` → `PUBLISHED` → `IN_PROGRESS` → `COMPLETED`
- `PENDING` → `CANCELLED` (if insufficient participants)

### 2. **Participant Management**
Real-time notifications when participants join or leave:

```java
// Notify when someone joins
socketIoGateway.broadcastParticipantJoined(activity, participant);

// Notify when someone leaves  
socketIoGateway.broadcastParticipantLeft(activity, participant);
```

### 3. **Activity Reminders**
Automated reminder system based on activity start time:

- **Today**: Activity starting today
- **Tomorrow**: Activity starting in 1 day
- **3 Days**: Activity starting in 3 days

```java
// Send reminder based on days until start
socketIoGateway.sendActivityReminder(activity, daysUntilStart);
```

### 4. **Real-time Messaging**
- **Broadcast Messages**: Send to all activity participants
- **Announcements**: Organizer-only feature for important updates
- **Emergency Alerts**: High-priority alerts with special UI treatment

### 5. **Authorization & Security**
- **Connection Authentication**: User ID required for connection
- **Activity Authorization**: Verify participant membership before room access
- **Organizer Privileges**: Special permissions for emergency alerts and announcements

## 🔌 Client Integration

### JavaScript Client Example

```javascript
// Initialize client
const client = new ActivitySocketClient(userId);

// Handle activity status changes
client.on('activityStatusChanged', (data) => {
    console.log(`Activity ${data.activityName} status: ${data.message}`);
    updateActivityStatusInUI(data);
});

// Handle new participants
client.on('participantJoined', (data) => {
    console.log(`${data.participantName} joined the activity`);
    addParticipantToUI(data);
});

// Handle emergency alerts
client.on('emergencyAlert', (data) => {
    showEmergencyAlert(data.alertMessage);
});

// Join activity room
client.joinActivityRoom(activityId);

// Send message to activity
client.broadcastToActivity(activityId, "Hello everyone!");
```

### React Integration Example

```jsx
import React, { useEffect, useState } from 'react';

function ActivityComponent({ userId, activityId }) {
    const [client, setClient] = useState(null);
    const [messages, setMessages] = useState([]);
    const [activityStatus, setActivityStatus] = useState('');

    useEffect(() => {
        const socketClient = new ActivitySocketClient(userId);
        
        socketClient.on('connected', () => {
            socketClient.joinActivityRoom(activityId);
        });

        socketClient.on('activityStatusChanged', (data) => {
            setActivityStatus(data.message);
        });

        socketClient.on('activityMessage', (data) => {
            setMessages(prev => [...prev, data]);
        });

        setClient(socketClient);

        return () => socketClient.disconnect();
    }, [userId, activityId]);

    return (
        <div>
            <div>Status: {activityStatus}</div>
            <div>
                {messages.map((msg, idx) => (
                    <div key={idx}>{msg.senderName}: {msg.content}</div>
                ))}
            </div>
        </div>
    );
}
```

## ⚙️ Configuration

### Application Configuration (`application.yaml`)

```yaml
# SocketIO Configuration
socket:
  host: 0.0.0.0
  port: 9092

# Redis Configuration for SocketIO Caching
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # Leave empty if no password
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### Environment-Specific Configuration

```yaml
# Production Profile
---
spring:
  config:
    activate:
      on-profile: production
  data:
    redis:
      host: redis-production-host
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: true

# Docker Profile  
---
spring:
  config:
    activate:
      on-profile: docker
  data:
    redis:
      host: redis
      port: 6379
```

## 📚 Usage Examples

### Example 1: Basic Activity Monitoring

```javascript
const client = new ActivitySocketClient(123);

client.on('connected', () => {
    // Join multiple activity rooms
    client.joinActivityRoom(2001);
    client.joinActivityRoom(2002);
    
    // Subscribe to updates
    client.subscribeToActivityUpdates(2001);
});

client.on('activityStatusChanged', (data) => {
    showNotification(`Activity Update: ${data.message}`);
});
```

### Example 2: Organizer Dashboard

```javascript
const organizerClient = new ActivitySocketClient(456);

organizerClient.on('connected', () => {
    // Get participant counts for multiple activities
    [2001, 2002, 2003].forEach(activityId => {
        organizerClient.getActivityParticipants(activityId);
    });
});

organizerClient.on('participantsList', (data) => {
    updateParticipantCount(data.activityId, data.count);
});

// Send emergency alert
function sendWeatherAlert(activityId) {
    organizerClient.sendEmergencyAlert(
        activityId,
        'Weather alert: Activity moved indoors to Building A, Room 101',
        'WEATHER'
    );
}
```

### Example 3: Participant Experience

```javascript
const participantClient = new ActivitySocketClient(789);

participantClient.on('activityReminder', (data) => {
    if (data.daysUntilStart === 0) {
        showUrgentNotification('Your activity is starting today!');
    } else {
        showNotification(`Activity reminder: ${data.message}`);
    }
});

participantClient.on('emergencyAlert', (data) => {
    showEmergencyModal(data.alertMessage);
});

participantClient.on('activityWelcome', (data) => {
    showWelcomeMessage(data.message);
});
```

## 📖 API Reference

### SocketIoGateway Public Methods

#### Activity Status Management
```java
// Broadcast activity status change
public void broadcastActivityStatusChange(EActivity activity, 
    ActivityStatus oldStatus, ActivityStatus newStatus)

// Broadcast activity information update
public void broadcastActivityUpdate(EActivity activity, 
    String updateType, String updateMessage)
```

#### Participant Management
```java
// Notify about new participant
public void broadcastParticipantJoined(EActivity activity, 
    EAccountCredentials participant)

// Notify about participant leaving
public void broadcastParticipantLeft(EActivity activity, 
    EAccountCredentials participant)
```

#### Messaging Features
```java
// Broadcast message to activity participants
public void broadcastMessageToActivity(Long activityId, 
    String senderName, String message, String messageType)

// Send organizer announcement
public void sendActivityAnnouncement(EActivity activity, 
    EAccountCredentials organizer, String announcement)

// Send emergency alert
public void sendEmergencyAlert(EActivity activity, 
    String alertMessage, String alertType)
```

#### Reminder System
```java
// Send activity reminder
public void sendActivityReminder(EActivity activity, long daysUntilStart)
```

#### Public API for Other Services
```java
// For use by other Spring services
public void notifyActivityStatusChange(Long activityId, 
    ActivityStatus oldStatus, ActivityStatus newStatus)

public void notifyActivityReminder(Long activityId, long daysUntilStart)

public void notifyParticipantJoined(Long activityId, Long participantId)

public void notifyParticipantLeft(Long activityId, Long participantId)
```

## 🔒 Security Considerations

### 1. **Authentication**
- All connections require a valid `userId` parameter
- User validation against database records
- Session-based authentication tracking

### 2. **Authorization**
- Activity room access restricted to verified participants
- Organizer-only features (emergency alerts, announcements)
- Permission validation for all sensitive operations

### 3. **Rate Limiting**
- Implement rate limiting for message sending
- Prevent spam and abuse through Redis-based tracking
- Automatic cleanup of stale connections

### 4. **Data Validation**
- Input sanitization for all user messages
- Activity ID validation before room operations
- Secure handling of user session data

## 🚀 Performance Optimizations

### 1. **Redis Caching**
- Activity participant lists cached for quick access
- User online status tracked in distributed cache
- Session data persistence across server restarts

### 2. **Asynchronous Processing**
- All broadcast operations run asynchronously
- Non-blocking notification sending
- Background processing for large participant lists

### 3. **Connection Management**
- Automatic cleanup of stale connections
- Efficient user-to-socket mapping
- Connection pooling for Redis operations

## 🐛 Troubleshooting

### Common Issues

1. **Connection Failures**
   - Check Redis connectivity
   - Verify SocketIO port accessibility
   - Validate user authentication

2. **Message Delivery Issues**
   - Confirm participant membership in activities
   - Check user online status
   - Verify event name spelling

3. **Performance Issues**
   - Monitor Redis memory usage
   - Check connection pool settings
   - Review async operation handling

### Debug Configuration

```yaml
logging:
  level:
    com.winnguyen1905.activity.websocket: DEBUG
    com.winnguyen1905.activity.rest.service.SocketIOService: DEBUG
```

## 📈 Future Enhancements

1. **Message Persistence**: Store chat messages in database
2. **File Sharing**: Support for file attachments in messages
3. **Voice/Video**: Integration with WebRTC for voice/video calls
4. **Push Notifications**: Mobile push notifications for offline users
5. **Analytics**: Real-time analytics dashboard for organizers
6. **Multi-language**: Internationalization support for notifications

---

## 📞 Support

For technical support or questions about the SocketIO Gateway implementation:

1. Check the logs for detailed error messages
2. Verify configuration settings
3. Test connection with the provided client examples
4. Review the frontend integration documentation

The SocketIO Gateway provides a robust foundation for real-time activity management with comprehensive features for both participants and organizers. 
