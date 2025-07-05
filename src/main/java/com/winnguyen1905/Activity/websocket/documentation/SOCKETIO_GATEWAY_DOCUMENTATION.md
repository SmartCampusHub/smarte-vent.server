# SocketIO Gateway Documentation

This documentation covers the comprehensive SocketIO Gateway implementation for the Activity Management System.

## Overview

The SocketIO Gateway provides real-time communication capabilities for activity management, including:
- Activity status change notifications
- Participant management
- Real-time messaging
- Emergency alerts
- Activity reminders

## Architecture

The SocketIO system is organized in the websocket package with the following structure:

```
src/main/java/com/winnguyen1905/activity/websocket/
├── config/
│   └── SocketIOConfig.java
├── service/
│   ├── SocketIOService.java
│   ├── SocketCacheService.java
│   ├── SocketEventHandlerService.java
│   └── impl/
│       └── SocketCacheServiceImpl.java
├── dto/
│   ├── SocketNotificationDto.java
│   ├── ChatMessageDto.java
│   ├── ActivityChatMessageDto.java
│   ├── TypingIndicatorDto.java
│   ├── UserStatusDto.java
│   ├── ActivityUpdateDto.java
│   ├── ParticipantUpdateDto.java
│   └── EmergencyAlertDto.java
├── frontend-examples/
│   └── ActivitySocketClient.js
├── SocketIoGateway.java
└── documentation/
    ├── socket-events-documentation.md
    ├── SOCKET_IMPLEMENTATION_SUMMARY.md
    └── SOCKETIO_GATEWAY_DOCUMENTATION.md
```

## Configuration

Socket.IO server configuration is handled in `SocketIOConfig.java` with customizable parameters for host, port, timeouts, and CORS settings.

## Core Components

### SocketIoGateway
Main orchestrator for all socket-related operations including activity status changes, participant management, and message broadcasting.

### SocketIOService  
Handles connection management, user session tracking, and direct notification delivery.

### SocketCacheService
Redis-based caching for user sessions, online status, and activity participants.

### SocketEventHandlerService
Processes real-time events like messaging, typing indicators, and notifications.

## Integration

The socket system integrates seamlessly with:
- Activity scheduling for automated notifications
- User authentication and authorization
- Redis caching for distributed deployments
- Existing notification systems

## Frontend Client

A comprehensive JavaScript client library (`ActivitySocketClient.js`) provides:
- Auto-connection and reconnection
- Event-driven architecture
- Activity room management
- Message broadcasting capabilities

## Security

- User authentication via connection parameters
- Activity membership verification
- Organizer privilege validation
- Input validation and sanitization

## Documentation Files

For detailed information, refer to:
- `socket-events-documentation.md` - Complete event specifications
- `SOCKET_IMPLEMENTATION_SUMMARY.md` - Technical implementation details
- Frontend examples in `ActivitySocketClient.js` 
