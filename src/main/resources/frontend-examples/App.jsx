import React, { useState, useEffect } from 'react';
import ChatComponent from './ChatComponent';
import './ChatComponent.css';

function App() {
  const [user, setUser] = useState(null);
  const [selectedActivity, setSelectedActivity] = useState(null);
  const [chatMode, setChatMode] = useState('activity'); // 'activity' or 'private'

  // Mock user data - replace with actual authentication
  useEffect(() => {
    // Simulate getting user data from authentication
    setUser({
      id: 123,
      name: "John Doe",
      email: "john@example.com"
    });
  }, []);

  // Mock activities - replace with actual API call
  const activities = [
    { id: 1, name: "Team Building Workshop", status: "active" },
    { id: 2, name: "Code Review Session", status: "active" },
    { id: 3, name: "Project Planning Meeting", status: "upcoming" }
  ];

  const requestNotificationPermission = async () => {
    if (Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      console.log('Notification permission:', permission);
    }
  };

  useEffect(() => {
    requestNotificationPermission();
  }, []);

  if (!user) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Activity Communication System</h1>
        <div className="user-info">
          Welcome, {user.name}
        </div>
      </header>

      <div className="app-content">
        {/* Sidebar */}
        <div className="sidebar">
          <div className="chat-mode-selector">
            <button 
              className={chatMode === 'activity' ? 'active' : ''}
              onClick={() => setChatMode('activity')}
            >
              Activity Chats
            </button>
            <button 
              className={chatMode === 'private' ? 'active' : ''}
              onClick={() => setChatMode('private')}
            >
              Private Messages
            </button>
          </div>

          {chatMode === 'activity' && (
            <div className="activities-list">
              <h3>Your Activities</h3>
              {activities.map(activity => (
                <div 
                  key={activity.id} 
                  className={`activity-item ${selectedActivity?.id === activity.id ? 'selected' : ''}`}
                  onClick={() => setSelectedActivity(activity)}
                >
                  <div className="activity-name">{activity.name}</div>
                  <div className="activity-status">{activity.status}</div>
                </div>
              ))}
            </div>
          )}

          {chatMode === 'private' && (
            <div className="private-chats">
              <h3>Private Messages</h3>
              <div className="chat-info">
                Select activity chat mode or implement private chat user selection here.
              </div>
            </div>
          )}
        </div>

        {/* Main Chat Area */}
        <div className="main-chat">
          {selectedActivity && chatMode === 'activity' ? (
            <div>
              <div className="chat-header">
                <h2>{selectedActivity.name}</h2>
                <span className="activity-status-badge">{selectedActivity.status}</span>
              </div>
              <ChatComponent 
                userId={user.id}
                userName={user.name}
                activityId={selectedActivity.id}
              />
            </div>
          ) : chatMode === 'private' ? (
            <div>
              <div className="chat-header">
                <h2>Private Messages</h2>
              </div>
              <ChatComponent 
                userId={user.id}
                userName={user.name}
                activityId={null} // null for private messages
              />
            </div>
          ) : (
            <div className="welcome-message">
              <h2>Welcome to Activity Communication</h2>
              <p>Select an activity from the sidebar to start chatting with participants.</p>
              
              <div className="feature-list">
                <h3>Features Available:</h3>
                <ul>
                  <li>📱 Real-time messaging</li>
                  <li>🔔 Live notifications</li>
                  <li>✍️ Typing indicators</li>
                  <li>📢 Activity announcements</li>
                  <li>👥 User presence status</li>
                  <li>✅ Message delivery confirmations</li>
                  <li>💬 Private messaging</li>
                  <li>🏃‍♂️ Activity reminders</li>
                </ul>
              </div>

              <div className="event-guide">
                <h3>Available Socket Events:</h3>
                <ul>
                  <li><strong>send_private_message</strong> - Send private messages</li>
                  <li><strong>send_activity_message</strong> - Send activity group messages</li>
                  <li><strong>send_activity_announcement</strong> - Send announcements (organizers only)</li>
                  <li><strong>typing_start/stop</strong> - Show typing indicators</li>
                  <li><strong>update_user_status</strong> - Update online status</li>
                  <li><strong>join/leave_activity_chat</strong> - Join activity chat rooms</li>
                  <li><strong>mark_message_read</strong> - Mark messages as read</li>
                  <li><strong>user_heartbeat</strong> - Maintain connection presence</li>
                </ul>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App; 
