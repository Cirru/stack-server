
Boot Stack Server
----

Server side toolchain for stack-editor.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-stack-server.svg)](https://clojars.org/cirru/boot-stack-server)

```clojure
[cirru/boot-stack-server "0.1.15"]
```

```clojure
[stack-server.core :refer [start-stack-editor! transform-stack]]

(comp
  (start-stack-editor! :port 7010 :extname ".cljs" :filename "stack-sepal.ir"))

(comp
  (transform-stack :extname ".cljs" :filename "stack-sepal.ir"))
```

### Develop

```bash
boot start-editor!
```

### License

MIT
