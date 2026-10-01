> [!NOTE]
> **This file is generated. Do not edit it directly.**
> Generated from the `@Describe` annotations in `src/main/kotlin/architecture/rules/serverservices/` and the `*.examples.md` files beside them.
> Regenerate with `./gradlew :platform:common:architecture:updateArchitectureDocumentation`.

# [Server Services](../src/main/kotlin/architecture/rules/serverservices/ServerServices.kt)

`feature.[name].server.services` defines the contract between client and server, and the
server's entry points. The contract lives in `:api`, so both the client and server see it; the
implementation lives in `:server` under the same package name.

Client and server talk plain HTTP and WebSockets with JSON bodies. An
[Api contract](#api-contract) names the routes and nests the `@Serializable` request and
response types; a [Routes](#routes) class installs those paths into Ktor's routing.
A [Job](#job) is the other kind of entry point: work the server starts on a timer rather than
in answer to a request. The work an entry point triggers is not declared here — it is stated as
[`server.domain` interfaces](serverdomain.md#domain-interface) and done by
[UseCases](serverdomain.md#use-case).

On the client, [Repositories](clientdata.md#repository) (in `client.data`) call the contract's
paths with a Ktor `HttpClient`. On the server, a Routes class composes domain interfaces;
persistence sits behind them, in [`server.data`](serverdata.md), which this layer never imports.

## Cross-layer dependencies

Within a feature, the layer dependency rules are:

* The `client.domain` and `server.domain` layers import feature roots only.
* `server.services` may depend on [`server.domain`](serverdomain.md) — and never on
  [`server.data`](serverdata.md) (`ServerServices.noDataImports`).
* `server.data` may depend on `server.domain` — and never on `server.services`
  (`ServerData.noServiceImports`).
* `client.data` may depend on `client.domain` and on this layer's contracts, so Repositories
  can call the server (`ClientData.clientServerDependencyRestriction`).
* `client.ui` may depend on `client.domain` only; server calls go through
  [Repositories](clientdata.md#repository), which provide
  [domain interfaces](clientdomain.md#domain-interface) for the UI to consume.
* Nothing depends on `client.ui`.

The client and server meet only at the contract this layer declares in `:api`. Cross-feature
use of another feature's services goes through `:api` as well: `ServerServices.crossFeatureViaApi`,
in the [rules](#rules) below.

An Api contract exists for clients to call, not for server features to compose. One server
feature reaches another through the capability the owner publishes — a
[`server.domain` interface](serverdomain.md#domain-interface) whose file resides in `:api` —
which is the same channel the domain layers use.

## Sub-packages

Any sub-package of the layer is an ordinary subsystem under
`ProjectRules.subsystemVisibility`: it sees its own package, its direct children, and its
ancestors up to the layer root — never a sibling.

## Persistence

Routes reach persistence only through
[`server.domain` interfaces](serverdomain.md#domain-interface) provided by
[Repositories](serverdata.md#repository); the Postgres conventions and codegen pipeline are
documented on [`server.data`](serverdata.md).

##### Constructs

* [Api Contract](#api-contract)
* [Routes](#routes)
* [Job](#job)

##### Rules

* The `server.services` layer must never import `server.data`
    * **Why:** `server.domain` sits between services and persistence and imports neither: services consume domain interfaces, and Repositories provide them. A Routes class that reaches a table directly has skipped the layer where the contract should have been stated, so nothing else can reuse that access, and nothing names what the service actually needed.  `ServerData.noServiceImports` is the other half. Together they make storage a thing that *satisfies* a stated need rather than a thing services reach through.
    * **Note:** Tested over imports of persistence, wherever the imported file sits: reaching a table is the same act whatever the package holding it is called.
* The `server.services` layer must not import client code
    * **Why:** The client and server meet at the Api contract and nowhere else.
* The `services` layer may depend on another feature's `services` only via that feature's `:api` module
    * **Enforced by:** `ModuleRules.clientApiOnly`, `ModuleRules.serverApiOnly`, `ModuleRules.crossFeatureCodeViaApi`
* A `server.services` package imports this layer only through its own package, its direct child subsystems, and its ancestors up to the layer root
    * **Enforced by:** `ProjectRules.subsystemVisibility`
* A `server.services` subsystem package imports `server.domain` only through the matching `server.domain` subsystem package, that package's direct children, and their ancestors
    * **Note:** A file at the layer root — a Routes class — is unconstrained by the matching-subsystem rule and sees the whole of `server.domain`.
    * **Enforced by:** `ProjectRules.subsystemMirrorsDomain`

---

## [Api Contract](../src/main/kotlin/architecture/rules/serverservices/ApiContract.kt)

The client-server contract, declared in `:api` so both sides compile against it: an `object`
named `[Name]Api` that nests each HTTP route as a Ktor `@Resource` class and each request and
response body as a `@Serializable` class. A WebSocket route, which Ktor's type-safe routing
does not cover, is a `const val` path. The server installs the routes in a
[Routes](#routes) class (`post<WallApi.Messages> { … }`); the client calls them from a
[Repository](clientdata.md#repository) with the same classes
(`httpClient.post(WallApi.Messages()) { … }`).

##### Requirements

* An Api Contract resides in `feature..server.services..`
* An Api Contract is an `object`
* An Api Contract is named `[Name]Api`
* An Api Contract resides in `feature.[name].server.services` itself, not in a sub-package

##### Rules

* An Api contract must live in `feature.[name].server.services` of the `:api` module
* An Api contract's properties must be `const val` paths starting with `/api/` or `/ws/`
    * **Why:** A path is the one string both sides must agree on byte for byte. Declared once in the shared contract — as a `@Resource` class, or a constant where a route cannot be one — a typo is a compile error on whichever side misspells it, and a feature's whole URL surface reads in one place.
* Every class and object nested in an Api contract must be `@Serializable`
    * **Why:** The nested types are what crosses the wire, encoded on one side and decoded on the other by the same generated serializer. A nested helper that is not a wire type belongs beside the code that uses it, not in the contract.

##### Guidance

* A failed request must answer with a non-2xx status and an `ApiError` body; a response type only models success
    * **Note:** The server throws `ApiException`, which the StatusPages plugin turns into the status and body; the client's `HttpClient` turns a non-2xx response back into an `ApiException`.

##### Examples

An Api contract in `:api`: routes as `@Resource` classes, bodies as `@Serializable` classes, both nested in one object:

```kotlin
// feature.wall.server.services.WallApi.kt (:api)
object WallApi {
    @Serializable
    @Resource("/api/wall/messages")
    class Messages

    @Serializable
    @Resource("/api/wall/messages/{id}/hide")
    class Hide(val id: String)

    @Serializable
    data class PostMessageRequest(val text: String)
}
```

The server and the client name the same classes:

```kotlin
// feature.wall.server.services.WallRoutes.kt (:server)
post<WallApi.Messages> {
    val request = call.receive<WallApi.PostMessageRequest>()
    call.respond(postWallMessage(request.text))
}

// feature.wall.client.data.WallRepository.kt (:client)
httpClient.post(WallApi.Messages()) {
    contentType(ContentType.Application.Json)
    setBody(WallApi.PostMessageRequest(text))
}.body<WallMessage>()
```

---

## [Routes](../src/main/kotlin/architecture/rules/serverservices/Routes.kt)

The server side of an [Api contract](#api-contract): a class that implements
`platform.server.http.RouteHandler` and installs the contract's routes into Ktor's routing.
It lives in `feature.[name].server.services` of `:server`, the same package as the contract,
and answers each request by composing the feature's
[`server.domain` interfaces](serverdomain.md#domain-interface).

The server finds every `RouteHandler` through Koin, so a new Routes class is live once its
feature's Dependency Module binds it (`FeatureRules.DependencyModule.routeHandlerBinding`).

##### Requirements

* A Routes resides in `feature..server.services..`
* A Routes is named `[Name]Routes`
* A Routes resides in `feature.[name].server.services` itself, beside the contract, not in a sub-package
* A Routes is declared in a `:server` module

##### Rules

* A Routes class must be `internal`
* A Routes class must implement `platform.server.http.RouteHandler`
    * **Why:** The construct classifies on name and package alone. A `[Name]Routes` that does not implement `RouteHandler` is never installed, so its routes answer 404 with nothing to say why.
* A Routes class must not inject persistence: neither a Repository nor a StorageClass
    * **Why:** A Routes class answers a request by composing the feature's `server.domain` interfaces; persistence sits on the far side of those interfaces, where `ServerServices.noDataImports` keeps it. A Repository is the wiring that provides those interfaces, not a thing to hold — injecting it, or the StorageClass under it, states the table the handler wants instead of the contract it needs, so nothing else can reuse that access and nothing names what the request actually required. It is the same rule that keeps ViewModels off client Repositories.
    * **Note:** Tested on the primary constructor: a parameter whose type is named `[Name]Repository`, `[Name]Storage`, or `[Name]Store`, or whose type resolves into a persistence package.
    * **Note:** This is the constructor-shaped half of `ServerServices.noDataImports`, which measures the same coupling over imports.
* A Routes class must not depend on the `ui` package
    * **Why:** Routes run on the server and have no Compose runtime: a UI import here would either fail to compile in `:server` or mean a UI type is being treated as data. A shape shared with the UI belongs in the feature's `:api` module.

##### Guidance

* A Routes class may inject its feature's `server.domain` interfaces, other features' `server.domain` interfaces published to `:api`, and platform types

---

## [Job](../src/main/kotlin/architecture/rules/serverservices/Job.kt)

An entry point the server triggers itself, on a timer: a class that implements
`platform.server.http.BackgroundJob`. Like [Routes](#routes), it lives in
`feature.[name].server.services` of `:server` and does its work by calling the feature's
[`server.domain` interfaces](serverdomain.md#domain-interface).

The server starts every `BackgroundJob` bound in Koin when it starts.

##### Requirements

* A Job resides in `feature..server.services..`
* A Job is named `[Name]Job`
* A Job resides in `feature.[name].server.services` itself, not in a sub-package
* A Job is declared in a `:server` module

##### Rules

* A Job must be `internal`
* A Job must implement `platform.server.http.BackgroundJob`
    * **Why:** A `[Name]Job` that does not implement `BackgroundJob` is never started.

##### Guidance

* A Job may inject only domain interfaces and platform types; persistence stays behind the domain, as it does for Routes
    * **Note:** `ServerServices.noDataImports` holds the persistence half of this for every class in the layer.
