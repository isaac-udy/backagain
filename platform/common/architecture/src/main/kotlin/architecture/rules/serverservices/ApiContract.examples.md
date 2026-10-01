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
