# backagain

**Browser, backend, and back again**: a conference talk about hosting a Kotlin/Wasm web app and
its Kotlin server on Google Cloud, where the slide deck is the app being talked about.

The presenter drives the deck from a phone. The projector, and everyone in the audience who opens
the page, follows along live — and some slides ask the audience to post a message, vote in a poll,
or react.

| | |
| --- | --- |
| Client | [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) compiled to WebAssembly (`wasmJs`), [Enro](https://github.com/isaac-udy/Enro) for navigation, [Koin](https://insert-koin.io/) for DI |
| Server | [Ktor](https://ktor.io/) on Cloud Run: serves the wasm bundle, a JSON API, and a WebSocket |
| Contract | Routes as Ktor `@Resource` classes and bodies as `@Serializable` classes, in modules both sides compile against |
| Data | Postgres on Cloud SQL; Flyway migrations, [Exposed](https://github.com/JetBrains/Exposed) tables generated from them |
| Deploy | GitHub Actions and Terraform, authenticated to Google by Workload Identity Federation |

## How it fits together

```
 phone (presenter) ──POST /api/presenter/position──┐
                                                    ▼
 browsers ◄──── /ws/live: snapshot, then events ─── Ktor on Cloud Run ──── Cloud SQL
 browsers ─────── POST /api/wall, /api/polls … ───►        │
                                                   Secret Manager
```

Writes are ordinary requests. The WebSocket only ever carries server-to-client frames: a snapshot
of everything when a client connects, then numbered events. A client that sees a gap in the
numbers reconnects and starts from a fresh snapshot.

- [`feature/live`](feature/live): the contract (`api`), the server's routes, storage and live
  hub (`server`), and the client's socket and requests (`client`).
- [`feature/deck`](feature/deck): the slides, the presenter's phone console, and how a screen
  follows, wanders off from, and rejoins the talk.
- [`platform`](platform): the design system, the shared HTTP setup on each side, Postgres, and the
  [architecture rules](platform/common/architecture/README.md) the build checks.
- [`infra`](infra): Terraform and the deploy runbook.

## Running it

```sh
./gradlew :app:client:web:wasmJsBrowserDistribution --no-configuration-cache
./gradlew :app:server:run
```

Open <http://localhost:8080>. The server starts an embedded Postgres, so there is nothing else to
install. Open <http://localhost:8080/present> in a second window and sign in with `presenter` to
drive the deck; `?stage` is the projector's view.

For quicker UI iteration, leave the server running and serve the client from webpack instead,
on <http://localhost:8081>:

```sh
./gradlew :app:client:web:wasmJsBrowserDevelopmentRun --no-configuration-cache
```

## Checking it

```sh
./gradlew check verifyArchitecture --no-configuration-cache
./gradlew :app:server:smokeTestFatJar
```

## Deploying it

See [infra/README.md](infra/README.md).

## License

[PolyForm Noncommercial 1.0.0](LICENSE): read it, learn from it, run it and adapt it for anything
noncommercial. For commercial use, get in touch.

The bundled JetBrains Mono font keeps its own
[SIL Open Font License](platform/client/design/design-system/fonts).
