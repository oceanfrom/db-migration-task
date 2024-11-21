--changeset test:1
CREATE TABLE test_rollback (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(50)
);

--changeset test:2
INSERT INTO test_rollback (name) VALUES ('Test 1'), ('Test 2');

-- -- --changeset test:3
-- INSERT INTO test_rollback (id, name) VALUES (NULL, NULL);
