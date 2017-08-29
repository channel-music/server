CREATE TABLE song
(id     SERIAL       NOT NULL PRIMARY KEY,
 title  VARCHAR(100) NOT NULL,
 -- TODO: Move to its own table
 artist VARCHAR(100),
 album  VARCHAR(100),
 genre  VARCHAR(30),
 track  SMALLINT,
 file   VARCHAR(300) NOT NULL);


---
--- Ensure that this song is truly unique
---

ALTER TABLE song ADD CONSTRAINT song_title_artist_album_unique
UNIQUE (title, artist, album);


---
--- Disallow duplicate nulls (https://stackoverflow.com/questions/10468657/postgres-unique-multi-column-index-for-join-table/10468686)
---

--- artist
CREATE UNIQUE INDEX song_unique_title_artist
ON song(title) WHERE artist IS NULL;

-- album
CREATE UNIQUE INDEX song_unique_title_album
ON song(title) WHERE album IS NULL;
