CREATE TABLE songs
(id SERIAL PRIMARY KEY,
 title VARCHAR(100) NOT NULL,
 -- TODO: Move to their own tables
 artist VARCHAR(100),
 album VARCHAR(100),
 genre VARCHAR(30),
 track INTEGER NOT NULL,
 file VARCHAR(300) NOT NULL);

---
-- Ensure that this song is truly unique
---
ALTER TABLE songs ADD CONSTRAINT songs_title_artist_album_unique
UNIQUE (title, artist, album);

---
--- Disallow duplicate nulls (https://stackoverflow.com/questions/10468657/postgres-unique-multi-column-index-for-join-table/10468686#10468686)
---

--- artist
CREATE UNIQUE INDEX songs_unique_title_artist
ON songs(title) WHERE artist IS NULL;

--- album
CREATE UNIQUE INDEX songs_unique_title_album
ON songs(title) WHERE album IS NULL;
