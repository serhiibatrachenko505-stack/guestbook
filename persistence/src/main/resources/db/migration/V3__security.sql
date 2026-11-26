-- V3: дані для автентифікації — email, підтвердження акаунта

ALTER TABLE users ADD COLUMN email VARCHAR(128);
ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE users ADD COLUMN confirmation_code VARCHAR(64);

ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

-- тестові акаунти з V2: email і підтверджений статус
UPDATE users SET email = 'admin@library.local'   WHERE username = 'admin';
UPDATE users SET email = 'olena@library.local'   WHERE username = 'Олена';
UPDATE users SET email = 'andriy@library.local'  WHERE username = 'Андрій';
UPDATE users SET email = 'student@library.local' WHERE username = 'Student';

-- Паролі тестових акаунтів поки що у форматі {noop}.
-- Під час першого входу Spring Security (DelegatingPasswordEncoder + UserDetailsPasswordService)
-- автоматично перехешує їх у BCrypt: {bcrypt}$2a$10$...
-- admin / admin, Student / password, Олена / password, Андрій / password
