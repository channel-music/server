--------------------------------------------------
-- Users
--------------------------------------------------

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(email, username, password)
VALUES (:email, :username, :password)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET email = :email, password = :password
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

-- :name create-song! :! :n
-- :doc creates a new song record
INSERT INTO songs
(title, artist, album, genre, track, file)
VALUES (:title, :artist, :album, :genre, :track, :file)

-- :name all-songs :? :*
-- :doc retrieve all songs
SELECT * FROM songs
ORDER BY id

-- :name song-by-id :? :1
-- :doc retrieve a song given the id.
SELECT * FROM songs
WHERE id = :id

-- :name delete-song! :! :n
-- :doc delete a song given the id
DELETE FROM songs
WHERE id = :id
