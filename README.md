
Boot Stack Server
----

Server side toolchain for stack-editor.

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/cirru/boot-stack-server.svg)](https://clojars.org/cirru/boot-stack-server)

```clojure
[cirru/boot-stack-server "0.1.28"]
```

Also several testing dependencies here:

```clojure
[andare                    "0.4.0"]
[cumulo/shallow-diff       "0.1.1"]
```

Copy `server.cljs` and run with Lumo:

```bash
export boot_deps=`boot show -c`
lumo -Kc $boot_deps:src/ -i server.cljs
```

### License

MIT
