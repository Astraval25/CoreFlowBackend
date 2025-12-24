-- Table: Roles insert query
INSERT INTO public.roles (role_code,landing_url,role_name) VALUES
	 ('ADM','/admin/dashboard','ADMIN user');

-- Table: Email Template insert query.
INSERT INTO
  email_templates (
    name,
    from_email,
    to_email,
    subject,
    body_html,
    body_text,
    description,
    type,
    is_active,
    created_by,
    created_dt,
    modified_by,
    modified_dt
  )
VALUES
  (
    'OTP Verification',
    null,
    null,
    'Your Verification Code',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Verify Your Account</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px; }
        .container { max-width: 600px; margin: auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        .header { background: #007bff; color: white; padding: 30px; text-align: center; }
        .content { padding: 30px; text-align: center; }
        .otp-code { font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #007bff; margin: 20px 0; }
        .footer { background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Account Verification</h1>
        </div>
        <div class="content">
            <p>Hello,</p>
            <p>Use the following one-time password (OTP) to verify your account:</p>
            <div class="otp-code">{{otp}}</div>
            <p>This code is valid for the next 10 minutes.</p>
            <p>If you didn''t request this, please ignore this email.</p>
        </div>
        <div class="footer">
            <p>&copy; 2025 Your Company. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
    'Hello,

Use the following one-time password (OTP) to verify your account:

{{otp}}

This code is valid for the next 10 minutes.

If you did not request this, please ignore this email.

Best regards,
CoreFlow Team',
    'Email template used for sending OTP during login, registration, or password reset verification.',
    'TRANSACTIONAL',
    true,
    0,
    NOW (),
    1,
    NOW ()
  );