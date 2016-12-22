CREATE TABLE users
(id SERIAL PRIMARY KEY,
 username VARCHAR(30) UNIQUE NOT NULL,
 email VARCHAR(30),
 password VARCHAR(300) NOT NULL);

---
--- username is always queried
---
CREATE INDEX users_username_index ON users (username);
