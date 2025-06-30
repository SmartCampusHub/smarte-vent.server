# Socket.IO Events Documentation

This document describes all available Socket.IO events for real-time messaging and notifications in the Activity Management System.

## Connection Setup

```javascript
// Frontend connection setup
import io from 'socket.io-client';

const socket = io('http://localhost:9092', {
  query: {
    userId: '123' // Replace with actual user ID
  }
});
```

## 1. Private Messaging Events

### Send Private Message
**Client → Server**
```javascript
// Event: send_private_message
socket.emit('send_private_message', {
  messageId: Date.now(), // Unique message ID
  senderId: 123,
  senderName: "John Doe",
  senderAvatar: "avatar-url",
  receiverId: 456,
  content: "Hello, how are you?",
  messageType: "TEXT", // TEXT, IMAGE, FILE, SYSTEM_NOTIFICATION, ACTIVITY_ANNOUNCEMENT
  attachmentUrl: null,
  activityId: null, // Optional, for activity-related messages
  replyToMessageId: null // Optional, for reply functionality
});
```

**Server → Client**
```javascript
// Event: private_message_received
socket.on('private_message_received', (messageData) => {
  console.log('New private message:', messageData);
  // Update UI with new message
});

// Event: message_delivery_status
socket.on('message_delivery_status', (status) => {
  console.log('Message delivery status:', status);
  // { messageId, delivered: true/false, timestamp }
});
```

### Mark Message as Read
**Client → Server**
```javascript
// Event: mark_message_read
socket.emit('mark_message_read', {
  messageId: 12345,
  senderId: 456 // ID of the message sender
});
```

**Server → Client**
```javascript
// Event: message_read_confirmation
socket.on('message_read_confirmation', (confirmation) => {
  console.log('Message read by:', confirmation);
  // { messageId, readBy: userId, readAt: timestamp }
});
```

## 2. Activity Chat Events

### Send Activity Message
**Client → Server**
```javascript
// Event: send_activity_message
socket.emit('send_activity_message', {
  messageId: Date.now(),
  activityId: 789,
  activityName: "Team Building Event",
  senderId: 123,
  senderName: "John Doe",
  senderRole: "PARTICIPANT", // ORGANIZER, PARTICIPANT
  content: "Looking forward to this event!",
  messageType: "TEXT", // TEXT, IMAGE, FILE, ANNOUNCEMENT, SYSTEM_MESSAGE, POLL, REMINDER
  isPinned: false,
  attachmentUrl: null,
  replyToMessageId: null
});
```

**Server → Client**
```javascript
// Event: activity_message_received
socket.on('activity_message_received', (messageData) => {
  console.log('New activity message:', messageData);
  // Update activity chat UI
});

// Event: activity_message_delivery_status
socket.on('activity_message_delivery_status', (status) => {
  console.log('Activity message status:', status);
  // { messageId, activityId, deliveredToCount, totalParticipants, timestamp }
});
```

### Join/Leave Activity Chat
**Client → Server**
```javascript
// Join activity chat
socket.emit('join_activity_chat', {
  activityId: 789
});

// Leave activity chat
socket.emit('leave_activity_chat', {
  activityId: 789
});
```

**Server → Client**
```javascript
// Activity chat status responses
socket.on('joined_activity_chat', (response) => {
  if (response.status === 'success') {
    console.log('Successfully joined activity chat:', response.activityId);
  } else {
    console.error('Failed to join chat:', response.message);
  }
});

socket.on('left_activity_chat', (response) => {
  console.log('Left activity chat:', response.activityId);
});

// User activity in chat
socket.on('user_joined_chat', (data) => {
  console.log('User joined chat:', data);
  // { userId, activityId, timestamp }
});

socket.on('user_left_chat', (data) => {
  console.log('User left chat:', data);
  // { userId, activityId, timestamp }
});
```

### Activity Announcements (Organizers Only)
**Client → Server**
```javascript
// Event: send_activity_announcement
socket.emit('send_activity_announcement', {
  messageId: Date.now(),
  activityId: 789,
  senderId: 123,
  senderName: "Event Organizer",
  content: "Important update: Event location has changed!",
  messageType: "ANNOUNCEMENT",
  isPinned: true
});
```

**Server → Client**
```javascript
// Event: activity_announcement_received
socket.on('activity_announcement_received', (announcement) => {
  console.log('New announcement:', announcement);
  // Show prominent notification for announcement
});

// Event: announcement_delivery_status
socket.on('announcement_delivery_status', (status) => {
  console.log('Announcement delivered to:', status.deliveredToCount, 'participants');
});
```

## 3. Typing Indicators

### Start/Stop Typing
**Client → Server**
```javascript
// Start typing in private chat
socket.emit('typing_start', {
  userId: 123,
  userName: "John Doe",
  receiverId: 456,
  activityId: null // null for private chat
});

// Start typing in activity chat
socket.emit('typing_start', {
  userId: 123,
  userName: "John Doe",
  receiverId: null,
  activityId: 789 // activity ID for activity chat
});

// Stop typing
socket.emit('typing_stop', {
  userId: 123,
  userName: "John Doe",
  receiverId: 456, // or null for activity chat
  activityId: null // or activity ID for activity chat
});
```

**Server → Client**
```javascript
// Private chat typing indicators
socket.on('user_typing_private', (typingData) => {
  console.log(typingData.userName + ' is typing...');
  // Show typing indicator in private chat
});

socket.on('user_stopped_typing_private', (typingData) => {
  console.log(typingData.userName + ' stopped typing');
  // Hide typing indicator
});

// Activity chat typing indicators
socket.on('user_typing_in_activity', (typingData) => {
  console.log(typingData.userName + ' is typing in activity...');
  // Show typing indicator in activity chat
});

socket.on('user_stopped_typing_in_activity', (typingData) => {
  console.log(typingData.userName + ' stopped typing in activity');
  // Hide typing indicator
});
```

## 4. User Status Events

### Update User Status
**Client → Server**
```javascript
// Event: update_user_status
socket.emit('update_user_status', {
  userId: 123,
  userName: "John Doe",
  status: "ONLINE", // ONLINE, AWAY, BUSY, OFFLINE
  statusMessage: "Working on project" // Optional custom status
});
```

**Server → Client**
```javascript
// Event: user_status_updated
socket.on('user_status_updated', (statusData) => {
  console.log('User status updated:', statusData);
  // Update user status in UI
  // { userId, userName, status, lastSeen, statusMessage }
});
```

### User Heartbeat (Presence)
**Client → Server**
```javascript
// Send heartbeat every 30 seconds to maintain presence
setInterval(() => {
  socket.emit('user_heartbeat', {
    timestamp: new Date().toISOString()
  });
}, 30000);
```

**Server → Client**
```javascript
// Event: heartbeat_ack
socket.on('heartbeat_ack', (response) => {
  console.log('Heartbeat acknowledged:', response);
  // { timestamp, status: "online" }
});
```

## 5. Notification Events

### Acknowledge Notifications
**Client → Server**
```javascript
// Event: notification_acknowledged
socket.emit('notification_acknowledged', {
  notificationId: 12345
});
```

### Update Notification Settings
**Client → Server**
```javascript
// Event: update_notification_settings
socket.emit('update_notification_settings', {
  enableSound: true,
  enableDesktop: true,
  enableEmail: false,
  quietHours: {
    start: "22:00",
    end: "08:00"
  }
});
```

**Server → Client**
```javascript
// Event: notification_settings_updated
socket.on('notification_settings_updated', (response) => {
  console.log('Notification settings updated:', response);
});
```

## 6. Existing Activity Events (Already Implemented)

These events are already being sent from the server:

**Server → Client**
```javascript
// Activity reminders
socket.on('activity_today', (notification) => {
  console.log('Activity starting today:', notification);
});

socket.on('activity_one_day', (notification) => {
  console.log('Activity starting tomorrow:', notification);
});

socket.on('activity_three_days', (notification) => {
  console.log('Activity starting in 3 days:', notification);
});

socket.on('schedule_reminder', (notification) => {
  console.log('Schedule reminder:', notification);
});

socket.on('activity_status_change', (notification) => {
  console.log('Activity status changed:', notification);
});
```

## Error Handling

**Server → Client**
```javascript
// Event: message_error
socket.on('message_error', (error) => {
  console.error('Message error:', error);
  // { error: "Error message description" }
  // Show error notification to user
});
```

## Complete Frontend Example

```javascript
import io from 'socket.io-client';

class SocketService {
  constructor(userId) {
    this.socket = io('http://localhost:9092', {
      query: { userId: userId.toString() }
    });
    
    this.setupEventListeners();
  }

  setupEventListeners() {
    // Connection events
    this.socket.on('connect', () => {
      console.log('Connected to socket server');
      this.updateUserStatus('ONLINE');
    });

    this.socket.on('disconnect', () => {
      console.log('Disconnected from socket server');
    });

    // Message events
    this.socket.on('private_message_received', this.handlePrivateMessage.bind(this));
    this.socket.on('activity_message_received', this.handleActivityMessage.bind(this));
    this.socket.on('activity_announcement_received', this.handleActivityAnnouncement.bind(this));

    // Typing events
    this.socket.on('user_typing_private', this.handleTypingIndicator.bind(this));
    this.socket.on('user_stopped_typing_private', this.handleStopTyping.bind(this));

    // Status events
    this.socket.on('user_status_updated', this.handleUserStatusUpdate.bind(this));

    // Activity events
    this.socket.on('activity_today', this.handleActivityReminder.bind(this));
    this.socket.on('activity_one_day', this.handleActivityReminder.bind(this));
    this.socket.on('schedule_reminder', this.handleScheduleReminder.bind(this));

    // Error handling
    this.socket.on('message_error', this.handleError.bind(this));
  }

  // Send private message
  sendPrivateMessage(receiverId, content, messageType = 'TEXT') {
    this.socket.emit('send_private_message', {
      messageId: Date.now(),
      senderId: this.userId,
      senderName: this.userName,
      receiverId: receiverId,
      content: content,
      messageType: messageType,
      timestamp: new Date().toISOString()
    });
  }

  // Send activity message
  sendActivityMessage(activityId, content, messageType = 'TEXT') {
    this.socket.emit('send_activity_message', {
      messageId: Date.now(),
      activityId: activityId,
      senderId: this.userId,
      senderName: this.userName,
      content: content,
      messageType: messageType,
      timestamp: new Date().toISOString()
    });
  }

  // Update user status
  updateUserStatus(status, statusMessage = '') {
    this.socket.emit('update_user_status', {
      userId: this.userId,
      userName: this.userName,
      status: status,
      statusMessage: statusMessage
    });
  }

  // Start typing indicator
  startTyping(receiverId = null, activityId = null) {
    this.socket.emit('typing_start', {
      userId: this.userId,
      userName: this.userName,
      receiverId: receiverId,
      activityId: activityId
    });
  }

  // Stop typing indicator
  stopTyping(receiverId = null, activityId = null) {
    this.socket.emit('typing_stop', {
      userId: this.userId,
      userName: this.userName,
      receiverId: receiverId,
      activityId: activityId
    });
  }

  // Join activity chat
  joinActivityChat(activityId) {
    this.socket.emit('join_activity_chat', {
      activityId: activityId
    });
  }

  // Leave activity chat
  leaveActivityChat(activityId) {
    this.socket.emit('leave_activity_chat', {
      activityId: activityId
    });
  }

  // Event handlers
  handlePrivateMessage(messageData) {
    // Update private chat UI
    console.log('New private message:', messageData);
  }

  handleActivityMessage(messageData) {
    // Update activity chat UI
    console.log('New activity message:', messageData);
  }

  handleActivityAnnouncement(announcement) {
    // Show prominent announcement notification
    console.log('New announcement:', announcement);
  }

  handleTypingIndicator(typingData) {
    // Show typing indicator
    console.log(`${typingData.userName} is typing...`);
  }

  handleStopTyping(typingData) {
    // Hide typing indicator
    console.log(`${typingData.userName} stopped typing`);
  }

  handleUserStatusUpdate(statusData) {
    // Update user status in UI
    console.log('User status updated:', statusData);
  }

  handleActivityReminder(notification) {
    // Show activity reminder notification
    console.log('Activity reminder:', notification);
  }

  handleScheduleReminder(notification) {
    // Show schedule reminder notification
    console.log('Schedule reminder:', notification);
  }

  handleError(error) {
    // Show error message to user
    console.error('Socket error:', error);
  }
}

// Usage
const socketService = new SocketService(123); // Replace with actual user ID
```

## Notes

1. **Authentication**: Make sure to pass the `userId` parameter when connecting
2. **Error Handling**: Always listen for `message_error` events
3. **Heartbeat**: Implement heartbeat mechanism for presence detection
4. **UI Updates**: Update your frontend UI based on received events
5. **Typing Indicators**: Implement proper debouncing for typing events
6. **Notifications**: Handle both socket events and regular push notifications 
