-- Drop tables if they exist
DROP TABLE IF EXISTS earnings;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS pools;

-- Create pools table
CREATE TABLE pools (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    apy DECIMAL(5, 2) NOT NULL,
    protocol VARCHAR(100) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    wallet_address VARCHAR(42) UNIQUE NOT NULL,
    current_pool VARCHAR(50) REFERENCES pools(id),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    from_pool VARCHAR(50) REFERENCES pools(id),
    to_pool VARCHAR(50) REFERENCES pools(id),
    amount DECIMAL(18, 8) NOT NULL,
    fee DECIMAL(18, 8) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create earnings table
CREATE TABLE earnings (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount DECIMAL(18, 8) NOT NULL,
    period VARCHAR(50) NOT NULL, -- e.g., "DAILY", "WEEKLY", "TOTAL"
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for wallet lookup
CREATE INDEX idx_users_wallet ON users(wallet_address);
