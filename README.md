# Activity Management Backend

## Email Configuration Setup

To fix email authentication issues, follow these steps:

### 1. Generate Gmail App Password

1. Go to your Google Account settings
2. Enable 2-Factor Authentication if not already enabled
3. Go to **Security** → **2-Step Verification** → **App passwords**
4. Select **Mail** as the app and **Other** as the device
5. Generate the 16-character app password

### 2. Set Environment Variables

Create a `.env` file in the project root or set these environment variables:

```bash
# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-character-app-password
```

### 3. Alternative: Set in IDE/System Environment

**For IntelliJ IDEA:**
- Go to Run Configuration → Environment Variables
- Add: `MAIL_USERNAME=your-email@gmail.com`
- Add: `MAIL_PASSWORD=your-app-password`

**For Command Line:**
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
mvn spring-boot:run
```

### 4. Configuration Changes Made

- Changed from port 465 (SSL) to port 587 (STARTTLS) for better compatibility
- Added proper STARTTLS configuration
- Used environment variables for sensitive data
- Added proper timeouts and error handling

### 5. Common Issues and Solutions

**Issue: "Authentication failed"**
- Make sure you're using an App Password, not your regular Gmail password
- Verify 2FA is enabled on your Google account

**Issue: "Connection timeout"**
- Check your firewall settings
- Verify port 587 is not blocked

**Issue: "STARTTLS failed"**
- Ensure `starttls.enable=true` and `starttls.required=true` are set

<a href="https://dbdiagram.io/d/ttcs-678df7306b7fa355c36580a7">Diagram</a>
