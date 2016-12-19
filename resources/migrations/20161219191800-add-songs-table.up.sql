CREATE TABLE songs
(id SERIAL PRIMARY KEY,
 title VARCHAR(100),
 -- TODO: Move to their own tables
 artist VARCHAR(100),
 album VARCHAR(100),
 genre VARCHAR(30),
 track INTEGER NOT NULL,
 file VARCHAR(300) NOT NULL);
