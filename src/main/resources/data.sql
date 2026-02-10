INSERT INTO employee (uuid, first_name, last_name, employee_type)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Juan', 'dela Cruz', 'WORKER'),
       ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Maria', 'Santos', 'MANAGER'),
       ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Pedro', 'Reyes', 'FINANCE_MANAGER')
ON CONFLICT (uuid) DO NOTHING;
