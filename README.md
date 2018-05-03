# akka-http-circe-streaming-sample

Akka HTTP allows [Source Streaming](https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html#json-streaming), i.e. indefinite, back-pressured streaming over an HTTP hop.

This repository is a sandbox for showing how that can be used, with `akka-http-circe` (one of the subprojects of [akka-http-json](https://github.com/hseeberger/akka-http-json)) as the marshalling/unmarshalling toolkit.

**Problem to solve:**

Uni-directional push-based connection of two microservices, with each other.

In addition, re-connecting a connection transparently (up to a provided timeout); allowing e.g. version upgrades of the producing service without breaking the data stream at the client end. This also needs some offset mechanism, to allow consumption to continue from the same spot earlier reached.

## Requirements

- `sbt`

## Getting started

### Running tests

```
$ sbt test
```

Tests should pass, or be ignored.

### Manual testing

You'll need multiple terminal windows. In the first one, let's start a server:

```
$ sbt run
[2] sandbox.TestRunSource
...
... Service running at http://localhost:8083
```

Open http://localhost:8083/data and there should be a 1.. stream of JSON objects.

You can also see the server logging the data it has pushed.

Now, let's open a client (in another terminal):

```
$ sbt run 
 [1] sandbox.TestRunSink
...
Data(1)
Data(2)
Data(3)
Data(4)
Data(5)
Data(6)
Data(7)
Data(8)
Data(9)
Data(10)
... 
```

You can open multiple clients in different terminals. 

#### Back pressure

Have a look at the server side to see how the back pressure works.

First, stop any consumption by ctrl-s.

If the client has shown e.g. 50 values, the server still goes on creating more (up to ~ 6000..7000), but stops there (buffers full, no back pressure).

Press ctrl-q on the client, to let it cause demand. The server should start creating more values, again, always trying to keep ahead of the consumption (with me, the second stop happened at 12899).


## References

- Akka HTTP >
  - [Source Streaming](https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html#json-streaming) (Documentation)
  - JSON Support > [Consuming JSON Streaming style APIs](https://doc.akka.io/docs/akka-http/current/common/json-support.html#consuming-json-streaming-style-apis)
