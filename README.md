
Boot Stack Server
----

Server side toolchain for stack-editor.

### Usage

```clojure
[cirru/boot-stack-server]
```

```clojure
[stack-server.core :refer [stack-editor!]]

(comp
  (stack-editor! :port 7010 :extname ".cljs"))
```

### Develop

```bash
boot start-editor!
```

### License

MIT
