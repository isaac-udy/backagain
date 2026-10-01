# Agent guidance

backagain is a Compose for Web (wasmJs) client and a Ktor server, laid out as feature slices
(`:feature:[name]:{api,client,server}`) over `:platform` modules and assembled by `:app`. The
structure follows the [UKPT](https://github.com/isaac-udy/ukpt) template, and its rules are checked
by tests: the generated [architecture README](platform/common/architecture/README.md) is the source
of truth.

## Working with the architecture rules

1. Write new code by copying the nearest existing example in the module you're changing.
2. Run `./gradlew verifyArchitecture`. It takes seconds, and a failure names the rule, says why it
   exists, and lists each violating declaration.
3. Look up only the rule that failed, in
   [`docs/rule-index.md`](platform/common/architecture/docs/rule-index.md).

The rules are defined in `platform/common/architecture/src/main/kotlin/architecture/rules/`; the
README and everything under `platform/common/architecture/docs/` are generated from them with
`./gradlew :platform:common:architecture:updateArchitectureDocumentation`. Never edit the
generated files.

Comments follow [docs/code-comments.md](docs/code-comments.md): a comment says something the code
cannot.

## Building and checking

```sh
./gradlew check verifyArchitecture --no-configuration-cache        # tests, including the wasm browser tests
./gradlew :app:server:smokeTestFatJar                               # boots the deployable jar on a throwaway database
./gradlew :app:client:web:wasmJsBrowserDistribution --no-configuration-cache
```

`compileKotlinWasmJs` only type-checks; bundle the web client to be sure it loads. Every task that
runs webpack needs `--no-configuration-cache`.

If a `fun interface` changes shape (a function becoming `suspend`, say) and the server then fails
at runtime with `AbstractMethodError`, the SAM lambdas implementing it were not recompiled.
Rebuild the affected module with `clean` and `--no-build-cache`.

## Running locally

```sh
./gradlew :app:server:run                                           # :8080, embedded Postgres, presenter password "presenter"
./gradlew :app:client:web:wasmJsBrowserDevelopmentRun --no-configuration-cache   # hot client on :8081, proxied to :8080
```

`:app:server:run` also serves the last production web bundle, if one has been built.
`BACKAGAIN_DEV_DB=ephemeral` gives a throwaway database; `./gradlew :app:server:wipeDevDatabase`
resets the persistent one.
