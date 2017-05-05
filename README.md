
Stack Editor Server
----

...for https://github.com/Cirru/stack-editor

> The npm package is `stack-editor` despite the repo is `stack-server`.

### Usage

```bash
stack-server
```

or

```bash
se
```

configure it with:

```bash
op=watch port=7010 extension=.cljs out=src stack-editor stack-sepal.ir
```

`op` for operation: `watch` or `compile`.

`stack-sepal.ir` is the default filename.

### License

MIT
