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

-- - To Generate Sequence number for each order  
CREATE TABLE company_order_sequence (
  company_id BIGINT,
  period CHAR(6), -- MMYYYY
  last_value BIGINT,
  PRIMARY KEY (company_id, period)
);


CREATE OR REPLACE FUNCTION generate_order_number(p_company_id BIGINT)
RETURNS TEXT AS $$
DECLARE
  v_period TEXT := TO_CHAR(NOW(), 'MMYYYY');
  v_next BIGINT;
BEGIN
  INSERT INTO company_order_sequence(company_id, period, last_value)
  VALUES (p_company_id, v_period, 1)
  ON CONFLICT (company_id, period)
  DO UPDATE SET last_value = company_order_sequence.last_value + 1
  RETURNING last_value INTO v_next;

  RETURN 'ORD-' || v_period || '-' || v_next;
END;
$$ LANGUAGE plpgsql;

-- SELECT generate_order_number(:companyId);


CREATE VIEW v_user_company_assets AS
SELECT
    ucm.user_id,
    ucm.company_id,
    c.customers,
    v.vendors,
    i.items
FROM user_comp_map ucm
LEFT JOIN (
    SELECT comp_id, ARRAY_AGG(customer_id) AS customers
    FROM customers
    GROUP BY comp_id
) c ON c.comp_id = ucm.company_id
LEFT JOIN (
    SELECT comp_id, ARRAY_AGG(vendor_id) AS vendors
    FROM vendors
    GROUP BY comp_id
) v ON v.comp_id = ucm.company_id
LEFT JOIN (
    SELECT comp_id, ARRAY_AGG(item_id) AS items
    FROM items
    GROUP BY comp_id
) i ON i.comp_id = ucm.company_id;

-- Subscription master data
INSERT INTO subscription_plans (plan_code, name, billing_period, price, is_active, created_dt, modified_dt)
SELECT 'FREE', 'Free Plan', 'MONTHLY', 0.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'FREE');

INSERT INTO subscription_plans (plan_code, name, billing_period, price, is_active, created_dt, modified_dt)
SELECT 'STARTER', 'Starter Plan', 'MONTHLY', 999.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'STARTER');

INSERT INTO subscription_plans (plan_code, name, billing_period, price, is_active, created_dt, modified_dt)
SELECT 'PRO', 'Pro Plan', 'MONTHLY', 2499.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'PRO');

INSERT INTO subscription_plans (plan_code, name, billing_period, price, is_active, created_dt, modified_dt)
SELECT 'ENTERPRISE', 'Enterprise Plan', 'YEARLY', 24999.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE plan_code = 'ENTERPRISE');

INSERT INTO features (feature_code, name, description, is_active, created_dt, modified_dt)
SELECT 'OCR_PAYMENT_PROOF', 'OCR Payment Proof', 'Upload and OCR parse payment proof documents', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM features WHERE feature_code = 'OCR_PAYMENT_PROOF');

INSERT INTO features (feature_code, name, description, is_active, created_dt, modified_dt)
SELECT 'ORDER_EXPORT', 'Order Export', 'Export order data for reporting and analysis', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM features WHERE feature_code = 'ORDER_EXPORT');

INSERT INTO features (feature_code, name, description, is_active, created_dt, modified_dt)
SELECT 'ADV_REPORTS', 'Advanced Reports', 'Access advanced analytics and report dashboards', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM features WHERE feature_code = 'ADV_REPORTS');

-- STARTER -> OCR_PAYMENT_PROOF
INSERT INTO plan_features (plan_id, feature_id, limit_value, created_dt)
SELECT sp.plan_id, f.feature_id, NULL, NOW()
FROM subscription_plans sp
JOIN features f ON f.feature_code = 'OCR_PAYMENT_PROOF'
WHERE sp.plan_code = 'STARTER'
AND NOT EXISTS (
    SELECT 1
    FROM plan_features pf
    WHERE pf.plan_id = sp.plan_id
      AND pf.feature_id = f.feature_id
);

-- PRO -> OCR_PAYMENT_PROOF, ORDER_EXPORT, ADV_REPORTS
INSERT INTO plan_features (plan_id, feature_id, limit_value, created_dt)
SELECT sp.plan_id, f.feature_id, NULL, NOW()
FROM subscription_plans sp
JOIN features f ON f.feature_code IN ('OCR_PAYMENT_PROOF', 'ORDER_EXPORT', 'ADV_REPORTS')
WHERE sp.plan_code = 'PRO'
AND NOT EXISTS (
    SELECT 1
    FROM plan_features pf
    WHERE pf.plan_id = sp.plan_id
      AND pf.feature_id = f.feature_id
);

-- ENTERPRISE -> OCR_PAYMENT_PROOF, ORDER_EXPORT, ADV_REPORTS
INSERT INTO plan_features (plan_id, feature_id, limit_value, created_dt)
SELECT sp.plan_id, f.feature_id, NULL, NOW()
FROM subscription_plans sp
JOIN features f ON f.feature_code IN ('OCR_PAYMENT_PROOF', 'ORDER_EXPORT', 'ADV_REPORTS')
WHERE sp.plan_code = 'ENTERPRISE'
AND NOT EXISTS (
    SELECT 1
    FROM plan_features pf
    WHERE pf.plan_id = sp.plan_id
      AND pf.feature_id = f.feature_id
);
