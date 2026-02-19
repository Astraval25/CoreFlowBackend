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

-- =============================
-- Due amount helper SQL objects
-- =============================

-- Customer due = sales total - sales payments received
-- Includes unallocated/advance payments too.
CREATE OR REPLACE FUNCTION fn_customer_due_amount(p_customer_id BIGINT)
RETURNS DOUBLE PRECISION AS $$
  SELECT
    COALESCE((
      SELECT SUM(COALESCE(o.total_amount, 0.0))
      FROM order_details o
      JOIN customers c ON c.customer_id = o.customer
      WHERE c.customer_id = p_customer_id
        AND COALESCE(o.is_active, TRUE) = TRUE
        AND o.seller_company = c.comp_id
        AND COALESCE(o.order_status, '') NOT IN (
          'QUOTATION',
          'QUOTATION_VIEWED',
          'QUOTATION_ACCEPTED',
          'QUOTATION_DECLINED'
        )
    ), 0.0)
    -
    COALESCE((
      SELECT SUM(COALESCE(p.amount, 0.0))
      FROM payments p
      JOIN customers c ON c.customer_id = p.customer
      WHERE c.customer_id = p_customer_id
        AND COALESCE(p.is_active, TRUE) = TRUE
        AND p.receiver_comp = c.comp_id
        AND COALESCE(p.payment_status, '') <> 'PAYMENT_DECLINED'
    ), 0.0);
$$ LANGUAGE SQL STABLE;

-- Vendor due = purchase total - purchase payments made
CREATE OR REPLACE FUNCTION fn_vendor_due_amount(p_vendor_id BIGINT)
RETURNS DOUBLE PRECISION AS $$
  SELECT
    COALESCE((
      SELECT SUM(COALESCE(o.total_amount, 0.0))
      FROM order_details o
      JOIN vendors v ON v.vendor_id = o.vendor
      WHERE v.vendor_id = p_vendor_id
        AND COALESCE(o.is_active, TRUE) = TRUE
        AND o.buyer_company = v.comp_id
        AND COALESCE(o.order_status, '') NOT IN (
          'QUOTATION',
          'QUOTATION_VIEWED',
          'QUOTATION_ACCEPTED',
          'QUOTATION_DECLINED'
        )
    ), 0.0)
    -
    COALESCE((
      SELECT SUM(COALESCE(p.amount, 0.0))
      FROM payments p
      JOIN vendors v ON v.vendor_id = p.vendor
      WHERE v.vendor_id = p_vendor_id
        AND COALESCE(p.is_active, TRUE) = TRUE
        AND p.sender_comp = v.comp_id
        AND COALESCE(p.payment_status, '') <> 'PAYMENT_DECLINED'
    ), 0.0);
$$ LANGUAGE SQL STABLE;

-- Batch reconciliation helper.
CREATE OR REPLACE FUNCTION refresh_party_due_amounts()
RETURNS VOID AS $$
BEGIN
  UPDATE customers c
  SET due_amount = fn_customer_due_amount(c.customer_id);

  UPDATE vendors v
  SET due_amount = fn_vendor_due_amount(v.vendor_id);
END;
$$ LANGUAGE plpgsql;

-- Optional analytics/reporting object.
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_party_due_summary AS
SELECT
  'CUSTOMER'::TEXT AS party_type,
  c.customer_id AS party_id,
  c.comp_id AS company_id,
  c.display_name AS party_name,
  fn_customer_due_amount(c.customer_id) AS due_amount,
  NOW() AS refreshed_at
FROM customers c
WHERE COALESCE(c.is_active, TRUE) = TRUE
UNION ALL
SELECT
  'VENDOR'::TEXT AS party_type,
  v.vendor_id AS party_id,
  v.comp_id AS company_id,
  v.display_name AS party_name,
  fn_vendor_due_amount(v.vendor_id) AS due_amount,
  NOW() AS refreshed_at
FROM vendors v
WHERE COALESCE(v.is_active, TRUE) = TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_mv_party_due_summary_party
  ON mv_party_due_summary (party_type, party_id);

CREATE INDEX IF NOT EXISTS ix_mv_party_due_summary_company_party
  ON mv_party_due_summary (company_id, party_type);

CREATE OR REPLACE FUNCTION refresh_mv_party_due_summary()
RETURNS VOID AS $$
BEGIN
  REFRESH MATERIALIZED VIEW mv_party_due_summary;
END;
$$ LANGUAGE plpgsql;
