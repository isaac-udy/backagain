An `expect`/`actual` Storage class with a platform-specific backing store:

```kotlin
// commonMain
expect class AuthCredentialStorage() {
    val authCredentials: StateFlow<AuthCredentials?>
    fun setAuthCredentials(authCredentials: AuthCredentials?)
}

// wasmJsMain
actual class AuthCredentialStorage actual constructor() {
    // Browser implementation over window.localStorage
}
```
