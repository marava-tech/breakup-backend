# Email Setup for OTP Authentication

This application uses Gmail SMTP to send OTP emails for user authentication. Follow these steps to configure your Gmail account:

## 1. Enable 2-Factor Authentication

1. Go to your Google Account settings
2. Navigate to Security
3. Enable 2-Step Verification

## 2. Generate App Password

1. Go to your Google Account settings
2. Navigate to Security
3. Under "2-Step Verification", click on "App passwords"
4. Select "Mail" as the app and "Other" as the device
5. Generate the app password
6. Copy the 16-character password

## 3. Configure Environment Variables

Set the following environment variables:

```bash
export GMAIL_USERNAME=your-email@gmail.com
export GMAIL_APP_PASSWORD=your-16-character-app-password
export JWT_SECRET=your-super-secret-jwt-key
```

Or create a `.env` file in your project root:

```env
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-16-character-app-password
JWT_SECRET=your-super-secret-jwt-key
```

## 4. Update application.yml (Optional)

You can also directly update the `application.yml` file with your credentials:

```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-16-character-app-password
```

## 5. Test the Configuration

Start the application and test the OTP functionality:

1. Send OTP for registration:
```bash
curl -X POST http://localhost:8080/api/auth/send-otp-registration \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

2. Check your email for the OTP

## Troubleshooting

- **Authentication failed**: Make sure you're using the app password, not your regular Gmail password
- **Connection timeout**: Check your internet connection and firewall settings
- **Invalid credentials**: Double-check your GMAIL_USERNAME and GMAIL_APP_PASSWORD

## Security Notes

- Never commit your actual email credentials to version control
- Use environment variables or external configuration management
- Regularly rotate your app passwords
- Consider using a dedicated email service for production applications 