--------------------------------------------------
-- Users
--------------------------------------------------

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(email, username, password, is_admin)
VALUES (:email, :username, :password)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET email = :email, password = :password, is_admin = :admin?
WHERE id = :id

-- :name all-users :? :*
-- :doc retrieve all users
SELECT * FROM users
ORDER BY id

-- :name user-by-id :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name user-by-username :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE username = :username

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

--------------------------------------------------
-- Songs
--------------------------------------------------

-- :name create-song<! :<! :1
-- :doc creates a new song record
INSERT INTO songs
(title, artist, album, genre, track, file)
VALUES (:title, :artist, :album, :genre, :track, :file)
RETURNING id

-- :name update-song! :! :n
-- :doc update an existing song record
UPDATE songs
SET title = :title, artist = :artist,
    genre = :genre, track = :track
WHERE id = :id

-- :name all-songs :? :*
-- :doc retrieve all songs
SELECT * FROM songs
ORDER BY id

-- :name song-by-id :? :1
-- :doc retrieve a song given the id
SELECT * FROM songs
WHERE id = :id

-- :name song-exists? :? :1
-- :doc returns song if exists, else returns nil.
SELECT * FROM songs
WHERE title = :title AND
      -- FIXME: find a nicer way of dealing with this
      (artist = :artist OR artist IS NULL) AND
      (album  = :album  OR album  IS NULL)

-- :name delete-song! :! :n
-- :doc delete a song given the id
DELETE FROM songs
WHERE id = :id
