-- Table: Roles insert query
INSERT INTO public.roles_master (role_code,landing_url,role_name) VALUES
	 ('ADM','/admin/dashboard','ADMIN user');

-- Table: Email Template insert query.
INSERT INTO
  email_templates_master (
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
-- Payment number generation
-- =============================

-- Tracks per-company, per-period payment sequences (mirrors company_order_sequence)
CREATE TABLE IF NOT EXISTS company_payment_sequence (
  company_id BIGINT,
  period CHAR(6), -- MMYYYY
  last_value BIGINT,
  PRIMARY KEY (company_id, period)
);

-- Returns the next payment number in the format PAY-MMYYYY-SEQ
-- Usage: SELECT generate_payment_number(:companyId);
CREATE OR REPLACE FUNCTION generate_payment_number(p_company_id BIGINT)
RETURNS TEXT AS $$
DECLARE
  v_period TEXT := TO_CHAR(NOW(), 'MMYYYY');
  v_next BIGINT;
BEGIN
  INSERT INTO company_payment_sequence(company_id, period, last_value)
  VALUES (p_company_id, v_period, 1)
  ON CONFLICT (company_id, period)
  DO UPDATE SET last_value = company_payment_sequence.last_value + 1
  RETURNING last_value INTO v_next;

  RETURN 'PAY-' || v_period || '-' || v_next;
END;
$$ LANGUAGE plpgsql;

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
      WHERE o.customer = p_customer_id
        AND COALESCE(o.is_active, TRUE) = TRUE
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
      WHERE p.customer = p_customer_id
        AND COALESCE(p.is_active, TRUE) = TRUE
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
      WHERE o.vendor = p_vendor_id
        AND COALESCE(o.is_active, TRUE) = TRUE
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
      WHERE p.vendor = p_vendor_id
        AND COALESCE(p.is_active, TRUE) = TRUE
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

-- =============================
-- Config definition seed data
-- =============================
INSERT INTO config_definition_master (config_key, default_value, data_type, category, description, is_active, created_dt)
VALUES
  ('sales_order_prefix', 'SO', 'STRING', 'NUMBERING', 'Prefix for sales order numbers', true, NOW()),
  ('purchase_order_prefix', 'PO', 'STRING', 'NUMBERING', 'Prefix for purchase order numbers', true, NOW()),
  ('payment_out_prefix', 'PAY', 'STRING', 'NUMBERING', 'Prefix for outgoing payment numbers (buyer side)', true, NOW()),
  ('payment_in_prefix', 'REC', 'STRING', 'NUMBERING', 'Prefix for incoming payment numbers (seller side)', true, NOW()),
  ('number_format', '{PREFIX}-{MMYYYY}-{SEQ}', 'STRING', 'NUMBERING', 'Format template for number generation', true, NOW()),
  ('seq_padding', '0', 'INTEGER', 'NUMBERING', 'Zero-pad sequence number (0=no padding, 4=0005)', true, NOW())
ON CONFLICT DO NOTHING;

-- =============================
-- Unified company number generation
-- =============================
-- Replaces separate generate_order_number / generate_payment_number with a configurable version.
-- Reads format config from config_definition (defaults) + company_config (overrides).
-- Types: 'SALES_ORDER', 'PURCHASE_ORDER', 'PAYMENT_OUT', 'PAYMENT_IN'

CREATE OR REPLACE FUNCTION generate_company_number(
  p_company_id BIGINT,
  p_number_type TEXT
)
RETURNS TEXT AS $$
DECLARE
  v_period TEXT := TO_CHAR(NOW(), 'MMYYYY');
  v_next BIGINT;
  v_prefix TEXT;
  v_format TEXT;
  v_padding INT;
  v_prefix_key TEXT;
  v_result TEXT;
BEGIN
  v_prefix_key := CASE p_number_type
    WHEN 'SALES_ORDER'    THEN 'sales_order_prefix'
    WHEN 'PURCHASE_ORDER' THEN 'purchase_order_prefix'
    WHEN 'PAYMENT_OUT'    THEN 'payment_out_prefix'
    WHEN 'PAYMENT_IN'     THEN 'payment_in_prefix'
    ELSE NULL
  END;

  IF v_prefix_key IS NULL THEN
    RAISE EXCEPTION 'Unknown number type: %', p_number_type;
  END IF;

  -- Resolve prefix: company override -> platform default
  SELECT COALESCE(
    (SELECT config_value FROM company_config WHERE company_id = p_company_id AND config_key = v_prefix_key),
    (SELECT default_value FROM config_definition_master WHERE config_key = v_prefix_key)
  ) INTO v_prefix;

  -- Resolve format template
  SELECT COALESCE(
    (SELECT config_value FROM company_config WHERE company_id = p_company_id AND config_key = 'number_format'),
    (SELECT default_value FROM config_definition_master WHERE config_key = 'number_format')
  ) INTO v_format;

  -- Resolve sequence padding
  SELECT COALESCE(
    (SELECT config_value::INT FROM company_config WHERE company_id = p_company_id AND config_key = 'seq_padding'),
    (SELECT default_value::INT FROM config_definition_master WHERE config_key = 'seq_padding'),
    0
  ) INTO v_padding;

  -- Atomic sequence increment
  INSERT INTO company_number_sequence(company_id, number_type, period, last_value)
  VALUES (p_company_id, p_number_type, v_period, 1)
  ON CONFLICT (company_id, number_type, period)
  DO UPDATE SET last_value = company_number_sequence.last_value + 1
  RETURNING last_value INTO v_next;

  -- Build result from format template
  v_result := v_format;
  v_result := REPLACE(v_result, '{PREFIX}', COALESCE(v_prefix, ''));
  v_result := REPLACE(v_result, '{YYYY}', TO_CHAR(NOW(), 'YYYY'));
  v_result := REPLACE(v_result, '{YY}', TO_CHAR(NOW(), 'YY'));
  v_result := REPLACE(v_result, '{MM}', TO_CHAR(NOW(), 'MM'));
  v_result := REPLACE(v_result, '{DD}', TO_CHAR(NOW(), 'DD'));
  v_result := REPLACE(v_result, '{MMYYYY}', TO_CHAR(NOW(), 'MMYYYY'));
  v_result := REPLACE(v_result, '{YYYYMM}', TO_CHAR(NOW(), 'YYYYMM'));

  IF v_padding > 0 THEN
    v_result := REPLACE(v_result, '{SEQ}', LPAD(v_next::TEXT, v_padding, '0'));
  ELSE
    v_result := REPLACE(v_result, '{SEQ}', v_next::TEXT);
  END IF;

  RETURN v_result;
END;
$$ LANGUAGE plpgsql;

-- =============================
-- Platform reference numbers
-- =============================
-- Global sequences (not per-company) for platform-wide order/payment identifiers.
CREATE SEQUENCE IF NOT EXISTS platform_order_seq START 1;
CREATE SEQUENCE IF NOT EXISTS platform_payment_seq START 1;

CREATE OR REPLACE FUNCTION generate_platform_order_ref()
RETURNS TEXT AS $$
BEGIN
  RETURN 'CF-ORD-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || nextval('platform_order_seq');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_platform_payment_ref()
RETURNS TEXT AS $$
BEGIN
  RETURN 'CF-PAY-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || nextval('platform_payment_seq');
END;
$$ LANGUAGE plpgsql;
