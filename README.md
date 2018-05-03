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

```
$ sbt test:run
```

Open http://localhost:8083 and there should be a 1.. stream of JSON objects


## References

- Akka HTTP >
  - [Source Streaming](https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html#json-streaming) (Documentation)
  - JSON Support > [Consuming JSON Streaming style APIs](https://doc.akka.io/docs/akka-http/current/common/json-support.html#consuming-json-streaming-style-apis)
