CREATE TABLE employee (
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    employee_type VARCHAR(50) NOT NULL DEFAULT 'WORKER'
);
