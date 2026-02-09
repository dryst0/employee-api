CREATE TABLE IF NOT EXISTS employee (
    uuid UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    employee_type VARCHAR(50) NOT NULL DEFAULT 'WORKER'
);
