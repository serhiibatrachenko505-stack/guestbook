insert into books(title, author, pub_year) values ('Кобзар', 'Тарас Шевченко', 1840);
insert into books(title, author, pub_year) values ('Тіні забутих предків', 'Михайло Коцюбинський', 1911);
insert into books(title, author, pub_year) values ('Лісова пісня', 'Леся Українка', 1911);
insert into books(title, author, pub_year) values ('Місто', 'Валер''ян Підмогильний', 1928);
insert into books(title, author, pub_year) values ('Тигролови', 'Іван Багряний', 1944);
insert into books(title, author, pub_year) values ('Clean Code', 'Robert C. Martin', 2008);
insert into books(title, author, pub_year) values ('Effective Java', 'Joshua Bloch', 2018);
insert into books(title, author, pub_year) values ('Clean Architecture', 'Robert C. Martin', 2017);
insert into books(title, author, pub_year) values ('Domain-Driven Design', 'Eric Evans', 2003);
insert into books(title, author, pub_year) values ('Refactoring', 'Martin Fowler', 2018);
insert into books(title, author, pub_year) values ('Head First Java', 'Kathy Sierra', 2005);
insert into books(title, author, pub_year) values ('Java Concurrency in Practice', 'Brian Goetz', 2006);

insert into comments(book_id, author, text) values (1, 'Олена', 'Класика, яку варто перечитувати');
-- старий відгук (2 доби тому) — для демонстрації правила 24 годин (DELETE -> 409)
insert into comments(book_id, author, text, created_at)
    values (1, 'Андрій', 'Цей відгук уже не можна видалити', dateadd('DAY', -2, current_timestamp));
