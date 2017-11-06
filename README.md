
Stack Editor Server
----

...for https://github.com/Cirru/stack-editor

> The npm package is `stack-editor` despite the repo is `stack-server`.

### Usage

```bash
npm i -g stack-editor
stack-editor
se # for short
```

configure it with:

```bash
op=watch port=7010 extension=.cljs out=src stack-editor ir.edn
```

`op` for operation: `watch` or `compile`.

`ir.edn` is the default filename.

### Library

[![Clojars Project](https://img.shields.io/clojars/v/cirru/stack-server.svg)](https://clojars.org/cirru/stack-server)

```edn
[cirru/stack-server "0.3.0"]
```

```clojure
(stack-server.analyze/generate file-info)
; file-info {:ns [] :defs {} :proc []}
```

### Develop

```bash
yarn
yarn compile
./bin/index.js
```

Release:

```bash
yarn build
```

### License

MIT
