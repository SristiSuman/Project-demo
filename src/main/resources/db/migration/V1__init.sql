-- V1__init.sql
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'loan_type') THEN
    CREATE TYPE loan_type AS ENUM ('PERSONAL','HOME','AUTO','BUSINESS');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'loan_status') THEN
    CREATE TYPE loan_status AS ENUM (
      'APPLIED',
      'APPROVED_BY_SYSTEM','REJECTED_BY_SYSTEM','UNDER_REVIEW',
      'APPROVED_BY_AGENT','REJECTED_BY_AGENT'
    );
  END IF;
END $$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS customers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  phone TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS agents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  phone TEXT UNIQUE,
  manager_id UUID REFERENCES agents(id) ON DELETE SET NULL,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS loans (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  loan_public_id TEXT NOT NULL UNIQUE,
  customer_id UUID NOT NULL REFERENCES customers(id),
  loan_amount NUMERIC(14,2) NOT NULL CHECK (loan_amount > 0),
  loan_type loan_type NOT NULL,
  application_status loan_status NOT NULL DEFAULT 'APPLIED',
  assigned_agent_id UUID REFERENCES agents(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(application_status);
CREATE INDEX IF NOT EXISTS idx_loans_customer ON loans(customer_id);

CREATE TABLE IF NOT EXISTS loan_status_history (
  id BIGSERIAL PRIMARY KEY,
  loan_id UUID NOT NULL REFERENCES loans(id),
  old_status loan_status,
  new_status loan_status NOT NULL,
  changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  changed_by TEXT
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGSERIAL PRIMARY KEY,
  channel TEXT NOT NULL,
  recipient TEXT NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
