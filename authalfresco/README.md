# Authentication

The **auth** module offers several abstractions to work with the Alfresco Identity Service and Basic Authentication.

The easiest way to integrate it is by using the abstract activities we have provided.

Simply extend the classes and implement the abstract methods.

```kotlin
class MyLoginActivity() : AuthenticationActivity<MyLoginViewModel>() {
  override val viewModel: MyLoginViewModel by viewModels

  override fun onCredentials(credentials: Credentials) {
    // Called on successful login
  }

  override fun onError(error: String) {
    // Called on login error
  }
}
```

When logging in as a first step you should call `viewModel.checkAuthType(endpoint, authConfig, onResult)` to identify if an Alfresco instance is running at an URL and which authentication method it supports.

### Basic Authentication (Not Recommended)

For basic authentication build your own UI to collect credentials.

On login call `viewModel.basicAuth(username, password)` which will compute an `AuthInterceptor` -compatible state and return it via `onCredentials`.

**Note**: `viewModel.basicAuth` does not provide any crendential validation. It's at this point where you could fetch the user's profile or permissions to complete the authentication process and validate the crendetials.

### SSO Authentication

For SSO, your activity will have to present the user a WebView to log in to. On success the session information will be returned to the app.

To trigger the process call `viewModel.pkceLogin()` which will prepare the auth service and then present the SSO WebView to the user:
```kotlin
class MyLoginActivity() : AuthenticationActivity<MyLoginViewModel>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener { viewModel.pkceLogin(endpoint, authConfig) }
    }
    ...
}
```
Upon successful login the server will callback with your token via `onCredentials`. If any issue occurred it will trigger `onError` or call `onPkceAuthCancelled` if the user cancelled the process.

To ensure a token is returned you'll need to declare the callback uri used in the process in your **AndroidManifest.xml**:
```
<activity android:name="com.alfresco.auth.pkce.RedirectUriReceiverActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="aims"
            android:path="/auth"
            android:scheme="androidacsapp" />
    </intent-filter>
</activity>
```
This **needs to match** your configuration in the Identity Service and the one provided in your **AuthConfig**

### After Authentication

Because authentication is an independent step we **strongly** recommend you do extra validation after receiving **onCredentials** before letting the user into the app.

Things you could check at this time, could be profile information or permissions, which would require calling the Alfresco service and thus verifying your authentication session is working correctly.

### Logging out

While normally you could just destroy the persisted credentials, in case of SSO the session needs to be invalidated or the user will log back in without a credentials prompt.

First, extend `EndSessionActivity` and provide the credentials you were previously given.

```kotlin
class MyLogoutViewModel(context: Context, authType: AuthType?, authState: String, authConfig: AuthConfig)
  : EndSessionViewModel(context, authType, authState, authConfig) {
    ...
}

class MyLogoutActivity() : EndSessionActivity<MyLogoutViewModel>() {
    override val viewModel: MyLogoutViewModel by viewModels
}
```

Then, to logout, call `startActivityForResult`:

```kotlin
fun logout() {
    val intent = Intent(this, LogoutActivity::class.java)
    startActivityForResult(intent, REQUEST_CODE)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE)
        if (resultCode == Activity.RESULT_OK) {
            // success
        } else {
            // failure
        }
    } else {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```

### Authentication Interceptor

During the lifetime of a session it may become invalid.

While using SSO the refresh token may expire or a remote actor may invalidate the user's session in the Identity Service.

Even during basic authentication this may happen if the password gets changed.

To help with this we provide the `AuthInterceptor` class that works with [OkHttp](https://square.github.io/okhttp/).

Simply create it, attach it to your OkHttp session and register your listener to get notified for changes in the authentication state.

``` kotlin
val authInterceptor = AuthInterceptor(
    context,
    username,
    authType,
    authState,
    authConfig
)

val okHttpClient: OkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(authInterceptor)
    .build()
```

Creation takes the same parameters we provide via the `onCredentials()` callback.

When a request fails due to authentication problems we call back with `onAuthFailure()`.

In case of SSO this class also takes care of automatic token refresh, and also calls back with `onAuthStateChange()` so you can persist the new session information.

**Note: all** callbacks from the `authInterceptor` are being called on the OkHttp I/O thread. Before proceeding with UI work please transfer your work to the main thread using something like `runOnUiThread()`

### Re-authenticating

As mentioned before a session may become invalid for various reasons.

When getting `onAuthFailure()` we recommend you prompt the user that they'll have to relogin and reuse the same `MyLoginActivity` created above to collect the user's credentials.

For basic authentication it's up to you to figure out the re-authentication flow.

For SSO we already provide a special re-authentication flow.

To trigger it call `viewModel.pkceLogin()` but this time also provide the existing `authState`. During the process you can query `viewModel.isRelogin` if you need to figure out if the user is doing re-authentication.

```kotlin
class MyLoginActivity() : AuthenticationActivity<MyLoginViewModel>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reLoginButton.setOnClickListener {
            viewModel.pkceLogin(endpoint, authConfig, authState)
        }
    }
    ...
}
```

On success the new session will be returned again via `onCredentials()` and after updating the stored credentials you can let the user resume their activities.
