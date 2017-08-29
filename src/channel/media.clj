(ns channel.media
  (:import
   (org.jaudiotagger.audio AudioFileIO)
   (org.jaudiotagger.audio.exceptions)
   (org.jaudiotagger.tag FieldKey Tag)))


(defn parse-unsigned-int
  "Parse a string in to an integer, but only if it represents an
  unsigned number. Returns `nil` on failure."
  [track-number]
  (try
    (Integer/parseUnsignedInt track-number)
    (catch NumberFormatException e
      nil)))


(defn make-metadata
  "Returns a metadata map, containing metadata about a media file, using a
  `audio-tag`."
  [^Tag audio-tag]
  {:title        (.getFirst audio-tag FieldKey/TITLE)
   :album        (.getFirst audio-tag FieldKey/ALBUM)
   :artist       (.getFirst audio-tag FieldKey/ARTIST)
   :genre        (.getFirst audio-tag FieldKey/GENRE)
   :year         (.getFirst audio-tag FieldKey/YEAR)
   :track        (parse-unsigned-int
                  (.getFirst audio-tag FieldKey/TRACK))
   :total-tracks (parse-unsigned-int
                  (.getFirst audio-tag FieldKey/TRACK_TOTAL))})


(defn parse-media-file
  "Attempts to parse `file` and return its metadata. If parsing fails, an
  exception is thrown. Supports MP3, MP4, M4A, MP4P, OGG, FLAC, WMA and WAV.

  Note that the extension of the file is significant, so even if the file contains
  correct media data it will not be parsed if it has an invalid extension (E.g. tmp or txt)."
  [file]
  (try
    (let [audio-file (AudioFileIO/read file)]
      (if-let [media-tag (.getTag audio-file)]
        (make-metadata media-tag)
        (throw (ex-info
                "Audio file is missing metadata tags"
                {:type :missing-metadata}))))
    (catch org.jaudiotagger.audio.exceptions.CannotReadException e
      (throw (ex-info "Failed to read audio file" {:type :cannot-read} e)))
    (catch org.jaudiotagger.audio.exceptions.InvalidAudioFrameException e
      (throw (ex-info
              "Audio file contains invalid audio frame"
              {:type :invalid-audio-frame} e)))
    (catch org.jaudiotagger.tag.TagException e
      (throw (ex-info "Audio file tag is invalid" {:type :invalid-tag} e)))))
