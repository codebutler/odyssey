plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))

    implementation(deps.libs.androidx.appcompat.leanbackPreference)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okHttp3Logging)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxJava2)

    kapt(deps.libs.dagger.compiler)
}

android {
    resourcePrefix("webdav_")
}
