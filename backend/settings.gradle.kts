rootProject.name = "pulse"

include("feed-service")
include("gateway-service")
include("identity-service")
include("messenger-service")
include("music-service")
include("notification-service")
include("space-service")
include("shared")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}