# Channel

[![Build Status](https://travis-ci.org/kalouantonis/channel.svg?branch=master)](https://travis-ci.org/kalouantonis/channel)

A self-hosted music streaming application.

**WARNING: This project is heavily WIP**

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run
    
## Working with the REPL

To start a REPL run:

    lein repl

Then you can start the ring server, figwheel and even connect to a ClojureScript REPL like this:

    user> (start)    ;; starts the backend server
    user> (start-fw) ;; starts figwheel
    user> (cljs)     ;; connects REPL to the browser

## Testing

To run all server-side tests run:

    lein test
    
To run front-end tests run:

    lein doo
    
## License

Licensed under [Apache License v2](LICENSE).
