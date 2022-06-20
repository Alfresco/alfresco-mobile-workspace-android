package com.alfresco.auth.pkce

import android.app.Activity
import android.os.Bundle
import net.openid.appauth.AuthorizationManagementActivity
import net.openid.appauth.AuthorizationService

/**
 * Activity that receives the redirect Uri sent by the OpenID endpoint. It forwards the data
 * received as part of this redirect to [AuthorizationManagementActivity], which
 * destroys the browser tab before returning the result to the completion
 * [android.app.PendingIntent]
 * provided to [AuthorizationService.performAuthorizationRequest].
 *
 * App developers using this library must override the `appAuthRedirectScheme`
 * property in their `build.gradle` to specify the custom scheme that will be used for
 * the OAuth2 redirect. If custom scheme redirect cannot be used with the identity provider
 * you are integrating with, then a custom intent filter should be defined in your
 * application manifest instead. For example, to handle
 * `myapp://aims/auth`:
 *
 * ```xml
 * <intent-filter>
 * <action android:name="android.intent.action.VIEW"></action>
 * <category android:name="android.intent.category.DEFAULT"></category>
 * <category android:name="android.intent.category.BROWSABLE"></category>
 * <data android:scheme="myapp" android:host="aims" android:path="/auth" />
</intent-filter> *
 * ```
 */
class RedirectUriReceiverActivity : Activity() {

    override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)

        // while this does not appear to be achieving much, handling the redirect in this way
        // ensures that we can remove the browser tab from the back stack. See the documentation
        // on AuthorizationManagementActivity for more details.
        startActivity(
            AuthorizationManagementActivity.createResponseHandlingIntent(
                this, intent.data
            )
        )
        finish()
    }
}
