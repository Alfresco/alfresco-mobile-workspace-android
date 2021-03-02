# <img title="Alfresco" alt='Alfresco' src='docs/logo.svg' height="32px" /> Mobile Workspace Android

## What Is Alfresco Mobile Workspace?
Alfresco Mobile Workspace enables users to work away from their workstation without compromising on the way content is accessed. Keep productivity high by transporting technical documents into the field without having to worry about a data connection.

-   View your content on mobile with support for all major document types such as Microsoft Word, Excel and PowerPoint, as well as, large format rendering of JPEG and PNG images + many, many more

-   Manage and view content offline

-   View libraries, recent/shared and manage favorites from your mobile device

-   Dark mode ready to support health and wellbeing at work

-   Seamless experience across all platforms

## Where can I find it ?

You can make use of the app by installing it from the Google Play.

<a href='https://play.google.com/store/apps/details?id=com.alfresco.content.app&hl=en&gl=US'>
    <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='240px'/>
</a>

Alternatively you can build your own version of the app locally.

## How to build locally

Clone this repository and `./gradlew assembleDebug` or just open the project in Android Studio and run the `app` target.

## Development

We'd love to accept your patches and contributions to this project.

## Code reviews

All external submissions require formal review. We use GitHub pull requests for this purpose. Consult [GitHub Help] for more
information on using pull requests.

[GitHub Help]: https://help.github.com/articles/about-pull-requests/

### Before submitting a pull request

We like running a consistent coding style so please run `./gradlew spotlessApply`.

If you're modifying dependencies also run `./gradlew dependencyUpdates -Pstable` to check for newer dependency versions.