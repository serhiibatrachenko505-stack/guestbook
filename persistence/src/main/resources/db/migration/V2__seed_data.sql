-- V2: тестові початкові дані

INSERT INTO books (title, author, pub_year) VALUES ('Кобзар', 'Тарас Шевченко', 1840);
INSERT INTO books (title, author, pub_year) VALUES ('Тіні забутих предків', 'Михайло Коцюбинський', 1911);
INSERT INTO books (title, author, pub_year) VALUES ('Лісова пісня', 'Леся Українка', 1911);
INSERT INTO books (title, author, pub_year) VALUES ('Місто', 'Валер''ян Підмогильний', 1928);
INSERT INTO books (title, author, pub_year) VALUES ('Тигролови', 'Іван Багряний', 1944);
INSERT INTO books (title, author, pub_year) VALUES ('Clean Code', 'Robert C. Martin', 2008);
INSERT INTO books (title, author, pub_year) VALUES ('Effective Java', 'Joshua Bloch', 2018);
INSERT INTO books (title, author, pub_year) VALUES ('Clean Architecture', 'Robert C. Martin', 2017);
INSERT INTO books (title, author, pub_year) VALUES ('Domain-Driven Design', 'Eric Evans', 2003);
INSERT INTO books (title, author, pub_year) VALUES ('Refactoring', 'Martin Fowler', 2018);
INSERT INTO books (title, author, pub_year) VALUES ('Head First Java', 'Kathy Sierra', 2005);
INSERT INTO books (title, author, pub_year) VALUES ('Java Concurrency in Practice', 'Brian Goetz', 2006);

-- паролі зберігаються у форматі Spring Security: {noop} = без шифрування (навчальний приклад)
INSERT INTO users (username, password, role) VALUES ('admin',   '{noop}admin',    'ADMIN');
INSERT INTO users (username, password, role) VALUES ('Олена',   '{noop}password', 'USER');
INSERT INTO users (username, password, role) VALUES ('Андрій',  '{noop}password', 'USER');
INSERT INTO users (username, password, role) VALUES ('Student', '{noop}password', 'USER');

-- #1 свіжий відгук, #2 — створений 2 доби тому (для перевірки правила 24 годин)
INSERT INTO comments (book_id, user_id, author, text)
    VALUES (1, 2, 'Олена', 'Класика, яку варто перечитувати');
INSERT INTO comments (book_id, user_id, author, text, created_at)
    VALUES (1, 3, 'Андрій', 'Цей відгук уже не можна видалити', DATEADD('DAY', -2, CURRENT_TIMESTAMP));
INSERT INTO comments (book_id, user_id, author, text, created_at)
    VALUES (6, 2, 'Олена', 'Must-read для кожного розробника', DATEADD('DAY', -5, CURRENT_TIMESTAMP));
INSERT INTO comments (book_id, user_id, author, text, created_at)
    VALUES (7, 2, 'Олена', 'Найкраща книга про Java', DATEADD('DAY', -3, CURRENT_TIMESTAMP));
INSERT INTO comments (book_id, user_id, author, text, created_at)
    VALUES (3, 3, 'Андрій', 'Перечитую щоліта', DATEADD('DAY', -10, CURRENT_TIMESTAMP));
INSERT INTO comments (book_id, user_id, author, text, created_at)
    VALUES (9, 4, 'Student', 'Складно, але корисно', DATEADD('HOUR', -6, CURRENT_TIMESTAMP));
