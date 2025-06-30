import React, { useState, useEffect, useRef } from 'react';
import io from 'socket.io-client';

const ChatComponent = ({ userId, userName, activityId = null }) => {
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [typingUsers, setTypingUsers] = useState([]);
  const [isTyping, setIsTyping] = useState(false);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [userStatus, setUserStatus] = useState('ONLINE');
  
  const typingTimeoutRef = useRef(null);
  const messageInputRef = useRef(null);

  // Initialize socket connection
  useEffect(() => {
    const newSocket = io('http://localhost:9092', {
      query: { userId: userId.toString() }
    });

    newSocket.on('connect', () => {
      console.log('Connected to socket server');
      // Update user status to online
      newSocket.emit('update_user_status', {
        userId: userId,
        userName: userName,
        status: 'ONLINE'
      });

      // Join activity chat if activityId is provided
      if (activityId) {
        newSocket.emit('join_activity_chat', { activityId });
      }
    });

    // Set up event listeners
    setupSocketEventListeners(newSocket);
    
    setSocket(newSocket);

    // Cleanup on unmount
    return () => {
      if (newSocket) {
        newSocket.emit('update_user_status', {
          userId: userId,
          userName: userName,
          status: 'OFFLINE'
        });
        newSocket.disconnect();
      }
    };
  }, [userId, userName, activityId]);

  // Set up heartbeat
  useEffect(() => {
    if (!socket) return;

    const heartbeatInterval = setInterval(() => {
      socket.emit('user_heartbeat', {
        timestamp: new Date().toISOString()
      });
    }, 30000);

    return () => clearInterval(heartbeatInterval);
  }, [socket]);

  const setupSocketEventListeners = (socket) => {
    // Private message events
    socket.on('private_message_received', (messageData) => {
      setMessages(prev => [...prev, {
        ...messageData,
        type: 'private',
        isIncoming: true
      }]);
      
      // Send delivery confirmation
      socket.emit('message_delivered', {
        messageId: messageData.messageId,
        senderId: messageData.senderId
      });

      // Show notification
      showNotification(`New message from ${messageData.senderName}`, messageData.content);
    });

    // Activity message events
    socket.on('activity_message_received', (messageData) => {
      setMessages(prev => [...prev, {
        ...messageData,
        type: 'activity',
        isIncoming: true
      }]);

      showNotification(`New message in ${messageData.activityName}`, messageData.content);
    });

    // Activity announcements
    socket.on('activity_announcement_received', (announcement) => {
      setMessages(prev => [...prev, {
        ...announcement,
        type: 'announcement',
        isIncoming: true
      }]);

      showNotification(`📢 Announcement: ${announcement.activityName}`, announcement.content, 'announcement');
    });

    // Typing indicators
    socket.on('user_typing_private', (typingData) => {
      setTypingUsers(prev => {
        const filtered = prev.filter(user => user.userId !== typingData.userId);
        return [...filtered, typingData];
      });
    });

    socket.on('user_stopped_typing_private', (typingData) => {
      setTypingUsers(prev => prev.filter(user => user.userId !== typingData.userId));
    });

    socket.on('user_typing_in_activity', (typingData) => {
      if (typingData.activityId === activityId) {
        setTypingUsers(prev => {
          const filtered = prev.filter(user => user.userId !== typingData.userId);
          return [...filtered, typingData];
        });
      }
    });

    socket.on('user_stopped_typing_in_activity', (typingData) => {
      if (typingData.activityId === activityId) {
        setTypingUsers(prev => prev.filter(user => user.userId !== typingData.userId));
      }
    });

    // User status updates
    socket.on('user_status_updated', (statusData) => {
      setOnlineUsers(prev => {
        const filtered = prev.filter(user => user.userId !== statusData.userId);
        if (statusData.status !== 'OFFLINE') {
          return [...filtered, statusData];
        }
        return filtered;
      });
    });

    // Message delivery confirmations
    socket.on('message_delivery_status', (status) => {
      setMessages(prev => prev.map(msg => 
        msg.messageId === status.messageId 
          ? { ...msg, delivered: status.delivered }
          : msg
      ));
    });

    socket.on('message_read_confirmation', (confirmation) => {
      setMessages(prev => prev.map(msg => 
        msg.messageId === confirmation.messageId 
          ? { ...msg, read: true, readAt: confirmation.readAt }
          : msg
      ));
    });

    // Activity events (existing)
    socket.on('activity_today', (notification) => {
      showNotification('Activity Today!', notification.message, 'activity');
    });

    socket.on('activity_one_day', (notification) => {
      showNotification('Activity Tomorrow!', notification.message, 'activity');
    });

    socket.on('schedule_reminder', (notification) => {
      showNotification('Schedule Reminder', notification.message, 'reminder');
    });

    // Error handling
    socket.on('message_error', (error) => {
      showNotification('Error', error.error, 'error');
    });

    // Heartbeat acknowledgment
    socket.on('heartbeat_ack', (response) => {
      console.log('Heartbeat acknowledged at:', response.timestamp);
    });
  };

  const sendMessage = () => {
    if (!newMessage.trim() || !socket) return;

    const messageData = {
      messageId: Date.now(),
      senderId: userId,
      senderName: userName,
      content: newMessage.trim(),
      messageType: 'TEXT',
      timestamp: new Date().toISOString()
    };

    if (activityId) {
      // Send activity message
      socket.emit('send_activity_message', {
        ...messageData,
        activityId: activityId,
        senderRole: 'PARTICIPANT' // You might want to determine this dynamically
      });
    } else {
      // Send private message (you'd need to specify receiverId)
      const receiverId = 456; // This should come from props or state
      socket.emit('send_private_message', {
        ...messageData,
        receiverId: receiverId
      });
    }

    // Add message to local state
    setMessages(prev => [...prev, {
      ...messageData,
      type: activityId ? 'activity' : 'private',
      isIncoming: false,
      delivered: false
    }]);

    setNewMessage('');
    stopTyping();
  };

  const handleTyping = () => {
    if (!socket) return;

    if (!isTyping) {
      setIsTyping(true);
      
      if (activityId) {
        socket.emit('typing_start', {
          userId: userId,
          userName: userName,
          activityId: activityId
        });
      } else {
        const receiverId = 456; // This should come from props or state
        socket.emit('typing_start', {
          userId: userId,
          userName: userName,
          receiverId: receiverId
        });
      }
    }

    // Reset typing timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    typingTimeoutRef.current = setTimeout(() => {
      stopTyping();
    }, 3000);
  };

  const stopTyping = () => {
    if (!socket || !isTyping) return;

    setIsTyping(false);
    
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    if (activityId) {
      socket.emit('typing_stop', {
        userId: userId,
        userName: userName,
        activityId: activityId
      });
    } else {
      const receiverId = 456; // This should come from props or state
      socket.emit('typing_stop', {
        userId: userId,
        userName: userName,
        receiverId: receiverId
      });
    }
  };

  const markMessageAsRead = (messageId, senderId) => {
    if (!socket) return;

    socket.emit('mark_message_read', {
      messageId: messageId,
      senderId: senderId
    });
  };

  const updateUserStatus = (newStatus) => {
    if (!socket) return;

    setUserStatus(newStatus);
    socket.emit('update_user_status', {
      userId: userId,
      userName: userName,
      status: newStatus
    });
  };

  const showNotification = (title, message, type = 'info') => {
    const notification = {
      id: Date.now(),
      title,
      message,
      type,
      timestamp: new Date().toISOString()
    };

    setNotifications(prev => [...prev, notification]);

    // Auto-remove notification after 5 seconds
    setTimeout(() => {
      setNotifications(prev => prev.filter(n => n.id !== notification.id));
    }, 5000);

    // Show browser notification if permission granted
    if (Notification.permission === 'granted') {
      new Notification(title, {
        body: message,
        icon: '/favicon.ico'
      });
    }
  };

  const dismissNotification = (notificationId) => {
    setNotifications(prev => prev.filter(n => n.id !== notificationId));
  };

  return (
    <div className="chat-container">
      {/* Status Bar */}
      <div className="status-bar">
        <div className="user-status">
          <span>Status: </span>
          <select value={userStatus} onChange={(e) => updateUserStatus(e.target.value)}>
            <option value="ONLINE">Online</option>
            <option value="AWAY">Away</option>
            <option value="BUSY">Busy</option>
            <option value="OFFLINE">Offline</option>
          </select>
        </div>
        
        <div className="online-users">
          <span>Online: {onlineUsers.length}</span>
          {onlineUsers.map(user => (
            <span key={user.userId} className="online-user">
              {user.userName} ({user.status})
            </span>
          ))}
        </div>
      </div>

      {/* Notifications */}
      <div className="notifications">
        {notifications.map(notification => (
          <div 
            key={notification.id} 
            className={`notification notification-${notification.type}`}
            onClick={() => dismissNotification(notification.id)}
          >
            <strong>{notification.title}</strong>
            <p>{notification.message}</p>
            <small>{new Date(notification.timestamp).toLocaleTimeString()}</small>
          </div>
        ))}
      </div>

      {/* Messages */}
      <div className="messages-container">
        {messages.map((message, index) => (
          <div 
            key={index} 
            className={`message ${message.isIncoming ? 'incoming' : 'outgoing'} ${message.type}`}
            onClick={() => message.isIncoming && markMessageAsRead(message.messageId, message.senderId)}
          >
            <div className="message-header">
              <strong>{message.senderName}</strong>
              <span className="timestamp">
                {new Date(message.timestamp).toLocaleTimeString()}
              </span>
              {message.type === 'announcement' && <span className="announcement-badge">📢</span>}
            </div>
            
            <div className="message-content">{message.content}</div>
            
            <div className="message-status">
              {!message.isIncoming && (
                <>
                  {message.delivered && <span className="delivered">✓</span>}
                  {message.read && <span className="read">✓✓</span>}
                </>
              )}
            </div>
          </div>
        ))}
        
        {/* Typing indicators */}
        {typingUsers.length > 0 && (
          <div className="typing-indicators">
            {typingUsers.map(user => (
              <div key={user.userId} className="typing-indicator">
                {user.userName} is typing...
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Message Input */}
      <div className="message-input-container">
        <input
          ref={messageInputRef}
          type="text"
          value={newMessage}
          onChange={(e) => {
            setNewMessage(e.target.value);
            handleTyping();
          }}
          onKeyPress={(e) => {
            if (e.key === 'Enter') {
              sendMessage();
            }
          }}
          placeholder={activityId ? "Type a message to the activity..." : "Type a private message..."}
          className="message-input"
        />
        <button onClick={sendMessage} className="send-button">
          Send
        </button>
      </div>
    </div>
  );
};

export default ChatComponent; 
