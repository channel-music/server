CREATE TABLE users
(id       SERIAL       NOT NULL PRIMARY KEY,
 username VARCHAR(30)  UNIQUE NOT NULL,
 email    VARCHAR(30),
 password VARCHAR(100) NOT NULL,
 admin    BOOLEAN      NOT NULL DEFAULT FALSE);

---
--- username is always queried
---
CREATE INDEX users_username_index ON users (username);
