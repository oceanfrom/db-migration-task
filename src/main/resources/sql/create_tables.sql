CREATE TABLE IF NOT EXISTS applied_migrations (
id SERIAL PRIMARY KEY,                                                  migration_name VARCHAR(255) UNIQUE NOT NULL,
applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
rollbacked_on TIMESTAMP,
status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS migration_lock (
lock_id VARCHAR(255) PRIMARY KEY,
locked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
released_at TIMESTAMP
);

