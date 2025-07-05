/**
 * Socket.IO Client Library for Activity Management System
 * 
 * This library provides a comprehensive client-side interface for real-time 
 * communication with the activity management backend using Socket.IO.
 * 
 * Features:
 * - Activity status monitoring
 * - Real-time messaging
 * - Participant management
 * - Emergency alerts
 * - Activity room management
 * 
 * @version 1.0.0
 * @author Activity Management Team
 */

import io from 'socket.io-client';

class ActivitySocketClient {
    constructor(serverUrl, userId) {
        this.serverUrl = serverUrl;
        this.userId = userId;
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.eventHandlers = new Map();
        
        // Auto-connect on instantiation
        this.connect();
    }

    /**
     * Connects to the Socket.IO server with user authentication
     */
    connect() {
        try {
            this.socket = io(this.serverUrl, {
                transports: ['websocket', 'polling'],
                query: {
                    userId: this.userId
                },
                reconnection: true,
                reconnectionAttempts: this.maxReconnectAttempts,
                reconnectionDelay: 1000,
                timeout: 20000
            });

            this.setupEventHandlers();
            console.log(`Connecting to ${this.serverUrl} as user ${this.userId}`);
            
        } catch (error) {
            console.error('Failed to connect to Socket.IO server:', error);
        }
    }

    /**
     * Sets up core event handlers for connection management
     */
    setupEventHandlers() {
        // Connection events
        this.socket.on('connect', () => {
            console.log('Connected to Socket.IO server');
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.emit('connected', { userId: this.userId, timestamp: Date.now() });
        });

        this.socket.on('disconnect', (reason) => {
            console.log('Disconnected from Socket.IO server:', reason);
            this.isConnected = false;
            this.emit('disconnected', { reason, timestamp: Date.now() });
        });

        this.socket.on('connect_error', (error) => {
            console.error('Connection error:', error);
            this.reconnectAttempts++;
            this.emit('connectionError', { error, attempts: this.reconnectAttempts });
        });

        this.socket.on('reconnect', (attemptNumber) => {
            console.log('Reconnected after', attemptNumber, 'attempts');
            this.emit('reconnected', { attempts: attemptNumber });
        });

        this.socket.on('reconnect_failed', () => {
            console.error('Failed to reconnect after maximum attempts');
            this.emit('reconnectFailed');
        });

        // Connection establishment confirmation
        this.socket.on('connection_established', (data) => {
            console.log('Connection established:', data);
            this.emit('connectionEstablished', data);
        });

        // Activity-related events
        this.setupActivityEventHandlers();
        
        // Message events
        this.setupMessageEventHandlers();
        
        // Notification events
        this.setupNotificationEventHandlers();
    }

    /**
     * Sets up activity-specific event handlers
     */
    setupActivityEventHandlers() {
        // Activity status changes
        this.socket.on('activity_status_changed', (data) => {
            console.log('Activity status changed:', data);
            this.emit('activityStatusChanged', data);
        });

        // Activity updates (time, location, etc.)
        this.socket.on('activity_updated', (data) => {
            console.log('Activity updated:', data);
            this.emit('activityUpdated', data);
        });

        // Participant notifications
        this.socket.on('activity_participant_joined', (data) => {
            console.log('Participant joined activity:', data);
            this.emit('participantJoined', data);
        });

        this.socket.on('activity_participant_left', (data) => {
            console.log('Participant left activity:', data);
            this.emit('participantLeft', data);
        });

        // Activity reminders
        this.socket.on('activity_reminder', (data) => {
            console.log('Activity reminder:', data);
            this.emit('activityReminder', data);
        });

        // Welcome message for new participants
        this.socket.on('activity_welcome', (data) => {
            console.log('Activity welcome message:', data);
            this.emit('activityWelcome', data);
        });

        // Emergency alerts
        this.socket.on('activity_emergency_alert', (data) => {
            console.log('Emergency alert:', data);
            this.emit('emergencyAlert', data);
        });

        // Room management confirmations
        this.socket.on('joined_activity_room', (data) => {
            console.log('Joined activity room:', data);
            this.emit('joinedActivityRoom', data);
        });

        this.socket.on('left_activity_room', (data) => {
            console.log('Left activity room:', data);
            this.emit('leftActivityRoom', data);
        });

        this.socket.on('join_activity_room_error', (data) => {
            console.error('Failed to join activity room:', data);
            this.emit('joinActivityRoomError', data);
        });

        // Participant list
        this.socket.on('activity_participants_list', (data) => {
            console.log('Activity participants list:', data);
            this.emit('activityParticipantsList', data);
        });
    }

    /**
     * Sets up message-related event handlers
     */
    setupMessageEventHandlers() {
        // Activity messages
        this.socket.on('activity_message_broadcast', (data) => {
            console.log('Activity message received:', data);
            this.emit('activityMessage', data);
        });

        // Activity announcements
        this.socket.on('activity_announcement', (data) => {
            console.log('Activity announcement received:', data);
            this.emit('activityAnnouncement', data);
        });

        // Broadcast confirmations
        this.socket.on('broadcast_success', (data) => {
            console.log('Message broadcast successful:', data);
            this.emit('broadcastSuccess', data);
        });

        this.socket.on('broadcast_error', (data) => {
            console.error('Message broadcast failed:', data);
            this.emit('broadcastError', data);
        });

        // Emergency alert confirmations
        this.socket.on('emergency_alert_sent', (data) => {
            console.log('Emergency alert sent successfully:', data);
            this.emit('emergencyAlertSent', data);
        });

        this.socket.on('emergency_alert_error', (data) => {
            console.error('Emergency alert failed:', data);
            this.emit('emergencyAlertError', data);
        });
    }

    /**
     * Sets up notification-related event handlers
     */
    setupNotificationEventHandlers() {
        // Activity subscriptions
        this.socket.on('subscribed_to_activity', (data) => {
            console.log('Subscribed to activity updates:', data);
            this.emit('subscribedToActivity', data);
        });

        this.socket.on('unsubscribed_from_activity', (data) => {
            console.log('Unsubscribed from activity updates:', data);
            this.emit('unsubscribedFromActivity', data);
        });
    }

    // ===========================================
    // PUBLIC API METHODS
    // ===========================================

    /**
     * Joins an activity room for real-time updates
     * @param {number} activityId - The activity ID to join
     */
    joinActivityRoom(activityId) {
        if (!this.isConnected) {
            console.warn('Cannot join activity room: not connected');
            return false;
        }

        console.log(`Joining activity room: ${activityId}`);
        this.socket.emit('join_activity_room', { activityId });
        return true;
    }

    /**
     * Leaves an activity room
     * @param {number} activityId - The activity ID to leave
     */
    leaveActivityRoom(activityId) {
        if (!this.isConnected) {
            console.warn('Cannot leave activity room: not connected');
            return false;
        }

        console.log(`Leaving activity room: ${activityId}`);
        this.socket.emit('leave_activity_room', { activityId });
        return true;
    }

    /**
     * Gets the list of participants in an activity
     * @param {number} activityId - The activity ID
     */
    getActivityParticipants(activityId) {
        if (!this.isConnected) {
            console.warn('Cannot get participants: not connected');
            return false;
        }

        console.log(`Getting participants for activity: ${activityId}`);
        this.socket.emit('get_activity_participants', { activityId });
        return true;
    }

    /**
     * Broadcasts a message to all participants in an activity
     * @param {Object} messageData - The message data
     * @param {number} messageData.activityId - Target activity ID
     * @param {number} messageData.senderId - Sender's user ID
     * @param {string} messageData.senderName - Sender's name
     * @param {string} messageData.content - Message content
     * @param {string} messageData.messageType - Message type (TEXT, IMAGE, etc.)
     */
    broadcastToActivity(messageData) {
        if (!this.isConnected) {
            console.warn('Cannot broadcast message: not connected');
            return false;
        }

        const message = {
            messageId: Date.now(), // Simple ID generation
            timestamp: new Date().toISOString(),
            ...messageData
        };

        console.log('Broadcasting message to activity:', message);
        this.socket.emit('broadcast_to_activity', message);
        return true;
    }

    /**
     * Sends an emergency alert (organizer only)
     * @param {number} activityId - Target activity ID
     * @param {string} alertMessage - Alert message content
     * @param {string} alertType - Type of alert (WEATHER, SECURITY, etc.)
     */
    sendEmergencyAlert(activityId, alertMessage, alertType = 'GENERAL') {
        if (!this.isConnected) {
            console.warn('Cannot send emergency alert: not connected');
            return false;
        }

        const alertData = {
            activityId,
            message: alertMessage,
            alertType,
            timestamp: new Date().toISOString()
        };

        console.log('Sending emergency alert:', alertData);
        this.socket.emit('send_emergency_alert', alertData);
        return true;
    }

    /**
     * Subscribes to activity updates
     * @param {number} activityId - The activity ID to subscribe to
     */
    subscribeToActivityUpdates(activityId) {
        if (!this.isConnected) {
            console.warn('Cannot subscribe to activity: not connected');
            return false;
        }

        console.log(`Subscribing to activity updates: ${activityId}`);
        this.socket.emit('subscribe_activity_updates', { activityId });
        return true;
    }

    /**
     * Unsubscribes from activity updates
     * @param {number} activityId - The activity ID to unsubscribe from
     */
    unsubscribeFromActivityUpdates(activityId) {
        if (!this.isConnected) {
            console.warn('Cannot unsubscribe from activity: not connected');
            return false;
        }

        console.log(`Unsubscribing from activity updates: ${activityId}`);
        this.socket.emit('unsubscribe_activity_updates', { activityId });
        return true;
    }

    // ===========================================
    // EVENT HANDLING
    // ===========================================

    /**
     * Registers an event handler
     * @param {string} event - Event name
     * @param {Function} handler - Event handler function
     */
    on(event, handler) {
        if (!this.eventHandlers.has(event)) {
            this.eventHandlers.set(event, []);
        }
        this.eventHandlers.get(event).push(handler);
    }

    /**
     * Removes an event handler
     * @param {string} event - Event name
     * @param {Function} handler - Event handler function to remove
     */
    off(event, handler) {
        if (this.eventHandlers.has(event)) {
            const handlers = this.eventHandlers.get(event);
            const index = handlers.indexOf(handler);
            if (index > -1) {
                handlers.splice(index, 1);
            }
        }
    }

    /**
     * Emits an event to registered handlers
     * @param {string} event - Event name
     * @param {*} data - Event data
     */
    emit(event, data) {
        if (this.eventHandlers.has(event)) {
            this.eventHandlers.get(event).forEach(handler => {
                try {
                    handler(data);
                } catch (error) {
                    console.error(`Error in event handler for ${event}:`, error);
                }
            });
        }
    }

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    /**
     * Gets the current connection status
     * @returns {boolean} True if connected
     */
    getConnectionStatus() {
        return this.isConnected;
    }

    /**
     * Gets the socket ID
     * @returns {string|null} Socket ID or null if not connected
     */
    getSocketId() {
        return this.socket?.id || null;
    }

    /**
     * Manually disconnects from the server
     */
    disconnect() {
        if (this.socket) {
            console.log('Manually disconnecting from server');
            this.socket.disconnect();
            this.isConnected = false;
        }
    }

    /**
     * Manually reconnects to the server
     */
    reconnect() {
        if (this.socket) {
            console.log('Manually reconnecting to server');
            this.socket.connect();
        } else {
            this.connect();
        }
    }

    /**
     * Destroys the client instance and cleans up resources
     */
    destroy() {
        if (this.socket) {
            this.socket.removeAllListeners();
            this.socket.disconnect();
        }
        this.eventHandlers.clear();
        this.socket = null;
        this.isConnected = false;
    }
}

// ===========================================
// USAGE EXAMPLES
// ===========================================

/**
 * Basic Usage Example
 */
/*
const socketClient = new ActivitySocketClient('http://localhost:9092', 123);

// Listen for activity status changes
socketClient.on('activityStatusChanged', (data) => {
    console.log('Activity status changed:', data);
    // Update UI accordingly
});

// Listen for new participants
socketClient.on('participantJoined', (data) => {
    console.log('New participant joined:', data);
    // Update participant list in UI
});

// Join an activity room
socketClient.joinActivityRoom(456);

// Send a message to activity participants
socketClient.broadcastToActivity({
    activityId: 456,
    senderId: 123,
    senderName: 'John Doe',
    content: 'Hello everyone!',
    messageType: 'TEXT'
});
*/

/**
 * React Component Integration Example
 */
/*
import React, { useEffect, useState } from 'react';

function ActivityComponent({ activityId, userId }) {
    const [socketClient, setSocketClient] = useState(null);
    const [participants, setParticipants] = useState([]);
    const [messages, setMessages] = useState([]);

    useEffect(() => {
        // Initialize socket client
        const client = new ActivitySocketClient('http://localhost:9092', userId);
        setSocketClient(client);

        // Set up event handlers
        client.on('activityStatusChanged', (data) => {
            // Handle status change
            console.log('Status changed:', data);
        });

        client.on('participantJoined', (data) => {
            setParticipants(prev => [...prev, data]);
        });

        client.on('activityMessage', (data) => {
            setMessages(prev => [...prev, data]);
        });

        // Join the activity room
        client.on('connected', () => {
            client.joinActivityRoom(activityId);
            client.getActivityParticipants(activityId);
        });

        // Cleanup on unmount
        return () => {
            client.destroy();
        };
    }, [activityId, userId]);

    const sendMessage = (content) => {
        if (socketClient) {
            socketClient.broadcastToActivity({
                activityId,
                senderId: userId,
                senderName: 'Current User',
                content,
                messageType: 'TEXT'
            });
        }
    };

    return (
        <div>
            <div>Participants: {participants.length}</div>
            <div>Messages: {messages.length}</div>
            {/* Rest of component */
//         </div>
//     );
// }

/**
 * Organizer Dashboard Example
 */
/*
function OrganizerDashboard({ userId }) {
    const [socketClient, setSocketClient] = useState(null);

    useEffect(() => {
        const client = new ActivitySocketClient('http://localhost:9092', userId);
        setSocketClient(client);

        // Listen for emergency alerts being sent
        client.on('emergencyAlertSent', (data) => {
            console.log('Emergency alert sent successfully:', data);
            // Show success message
        });

        client.on('emergencyAlertError', (data) => {
            console.error('Failed to send emergency alert:', data);
            // Show error message
        });

        return () => client.destroy();
    }, [userId]);

    const sendEmergencyAlert = (activityId, message, type) => {
        if (socketClient) {
            socketClient.sendEmergencyAlert(activityId, message, type);
        }
    };

    return (
        <div>
            <button onClick={() => sendEmergencyAlert(123, 'Weather alert: Rain expected', 'WEATHER')}>
                Send Weather Alert
            </button>
        </div>
    );
}
*/

export default ActivitySocketClient; 
