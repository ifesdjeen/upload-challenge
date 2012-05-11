# Uploadchallenge

A little application that allows you to upload files and download them afterwards.

# History

First implementation attempts was with (noir)[https://github.com/ibdknox/noir], but because they do not allow using
wrap-multipart-params in a (Ring)[https://github.com/mmcgrana/ring] way, I wrote it all using Ring.

# Pitfalls / known issues

  * Right now TMP files are removed whenever JVM is shut down.
  * Store is a hash within an atom based on filename as a key, so obviously if 2 people decide to upload 1 file it's not going to work. Server has to generate and return a unique item ID upon upload, but that makes sense only in cases when we have real database persistency.
  * Upload status is fetched via iframe, and XHR upload progress is not employed. Better thought would of course be to use XHR progress when possible and fallback to iframe in cases it's not accessible.
  * There's no indication on when description was attached, for example. Best way of course would be to use X-Message-Info HTTP headers for that, but it requires quite a significant amount of work.
  * Content-types for the downloads are not determined. All downloads go with 'application/force-download' content-type for now.

## Usage

With (Leiningen 2)[https://github.com/technomancy/leiningen/wiki/Upgrading][-preview3]:

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

When deployed, use run & initscript, that handles things via start-stop-daemon.

```
./initscript.sh start # {stop}
```

## License

Copyright © 2012 Alex P

Distributed under the Eclipse Public License, the same as Clojure.
