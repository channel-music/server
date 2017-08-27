-- :name all-songs :? :*
-- :doc Retrieve all songs
SELECT * FROM song ORDER BY id

-- :name song-by-id :? :1
-- :doc Retrieve a song given an ID
SELECT * FROM song WHERE id=:id

-- :name song-exists? :? :1
-- :doc Returns song if exists, else returns nil
SELECT * FROM song
  WHERE title = :title AND
  -- FIXME: find a nicer way of dealing with this
  (artist = :artist OR artist IS NULL) AND
  (album = :album OR album IS NULL)

-- :name create-song! :<! :1
-- :doc Creates a new song record
INSERT INTO song
  (title, artist, album, genre, track, file)
  VALUES (:title, :artist, :album, :genre, :track, :file)
  RETURNING id

-- :name update-song! :! :n
-- :doc Update an existing song
UPDATE song
  SET title = :title, artist = :artist,
      genre = :genre, track  = :track
  WHERE id=:id

-- :name delete-song! :! :n
-- :doc Delete a song given its ID
DELETE FROM song WHERE id=:id
