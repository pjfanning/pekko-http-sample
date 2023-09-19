# pekko-http-sample

Basic sample that serves a HTTPS at https://localhost:8443/hello

* The sample uses a server cert from the provided keystore.jks file. This key and keystore should not be used for any other purpose.

Start the server with `sbt run`.

Test with:

```
curl -vk https://localhost:8443/hello
```

The `-k` code option means that curl will not validate the server cert (the sample cert in keystore.jks).

The `application.conf` enables HTTP/2 support.


