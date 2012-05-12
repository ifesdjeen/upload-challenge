# Uploadchallenge

A little application that allows you to upload files and download them afterwards.

# History

As regards problem definition, I realize importance of running things on 80 port, but it's strongly discouraged to
run main app on 80 port, as it requires root. Servers like nginx run master on 80 port, but have several workers that
later proxy traffic to web servers or handle requests themselves.

As Nginx performs buffering (even though it's configurable), for experiment to be clear I have decided to run app
on the server on a different port (8080), but so that I could comply with the problem definition and not make excuses
and explain why I haven't launched it directly on 80, I've used the following iptables rule:

```
iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
```

First implementation attempts was with [noir](https://github.com/ibdknox/noir), but because they do not allow using
wrap-multipart-params in a [Ring](https://github.com/mmcgrana/ring) way, I wrote it all using Ring.

Suggested solution: As not all the browsers have an ability to track upload progress, we'll create a hook that
starts up in browser right after upload and queries backend for the upload status every X seconds.

There are 2 ways of going further: first one is to [inherit from InputStream](https://github.com/ifesdjeen/upload-challenge/blob/master/src/uploadchallenge/file_processor.clj#L66), which is used
internally by Ring, output stream results to the File and save amount of read bytes along the way.
Another way would be to use ApacheCommons FileUpload, which allows tracking progress on a stream.
File upload data (and descriptions, later on) are stored in [Atom hash](https://github.com/ifesdjeen/upload-challenge/blob/master/src/uploadchallenge/file_processor.clj#L9).

A very basic routing is implemented. As we need to display multiple pages, we have to know which function on
a backend should handle it. So we add a set of rules and some related handler as a set of rules

```clojure
;; will match all the files with .js extension under /javascripts/ dir
(routing/add-route :get \"/javascripts/(.*).js\" js)

;; matches an exact path
(routing/add-route :post \"/blobs\" index)
```

Internally, whenever routing happens, we're matching route by passing current current request method and uri,
for example:

```clojure
(def x (atom 0))
(add-route :get \"/\" (fn []
                      (reset! x 20)))
(match-route :get \"/\") ;; will run reset x atom to 20
(match-route :get \"/bazinga\") ;; will not match


(def x (atom 0))
(add-route :get \"/javascripts/(.*).js\" (fn []
                                      (reset! x 20)))

(match-route :get \"/javascripts/jquery.js\") ;; will reset x atom to 20
(match-route :get \"/YABADABASCRIPTS/jquery.js\") ;; will not match
```


# Pitfalls / known issues

  * Right now TMP files are removed whenever JVM is shut down.
  * Files with special (e.q. German characters) chars and spaces are not handled correctly, that is also related to the problem mentioned in the next point
  * Store is a hash within an atom based on filename as a key, so obviously if 2 people decide to upload 1 file it's not going to work. Server has to generate and return a unique item ID upon upload, but that makes sense only in cases when we have real database persistency.
  * Upload status is fetched via iframe, and XHR upload progress is not employed. Better thought would of course be to use XHR progress when possible and fallback to iframe in cases it's not accessible.
  * There's no indication on when description was attached, for example. Best way of course would be to use X-Message-Info HTTP headers for that, but it requires quite a significant amount of work.
  * Content-types for the downloads are not determined. All downloads go with 'application/force-download' content-type for now.

## Usage

With [Leiningen 2](https://github.com/technomancy/leiningen):

    lein2 deps
    lein2 test
    lein2 run

In order to generate ~100MB test file for upload, use

```
dd if=/dev/zero of=test.img bs=1024 count=0 seek=$[1024*100]
```
For ~1GB

```
dd if=/dev/zero of=test.img bs=1024 count=0 seek=$[1024*1000]
```

When deployed, use run & initscript, that handles things via start-stop-daemon. On local machine even 100MB complete in no time.

```
./initscript.sh start # {stop}
```

Please, do not try uploading files more than 400Mb to the server, as a) if you run on 80 port, you have to be root b) app is hidden
behind nginx server with

```
client_max_body_size 400M;
```




## License

Copyright © 2012 Alex P

Distributed under the Eclipse Public License, the same as Clojure.
