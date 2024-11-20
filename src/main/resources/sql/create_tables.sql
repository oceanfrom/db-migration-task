CREATE TABLE IF NOT EXISTS applied_migrations (
                                                  id SERIAL PRIMARY KEY,
                                                  migration_name VARCHAR(255) UNIQUE NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rollbacked_on TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
    );


CREATE TABLE IF NOT EXISTS migration_history (
                                                 id SERIAL PRIMARY KEY,
                                                 migration_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT
    );

CREATE TABLE IF NOT EXISTS migration_lock (
                                              lock_id VARCHAR(255) PRIMARY KEY,
    locked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM migration_lock WHERE lock_id = 'migration_lock') THEN
        INSERT INTO migration_lock (lock_id) VALUES ('migration_lock');
END IF;
END $$;
