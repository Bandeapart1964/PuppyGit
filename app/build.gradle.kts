plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("kotlin-android-extensions")  //不能和kotlin-parcelize一起启用
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("androidx.room")
//    kotlin("jvm") version "1.9.23"
//    kotlin("plugin.serialization")  == id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")



}
val dbSchemaLocation="$projectDir/schemas"
room {
    schemaDirectory(dbSchemaLocation)
}
android {
    val packageName = "com.catpuppyapp.puppygit.play.pro"

    namespace = packageName  // 这个值和包名有所不同，但是最好把这个值和包名设成一样的 或 让此值在系统中唯一，绝对不要不同或相同包名的app拥有相同namespace！如果包名不同但此值相同，会删除已安装的app！或者冲突，安装失败。（R.string 那个R需要在这个作用域下运行）
    compileSdk = 34

    defaultConfig {
        applicationId = packageName
        //ndk编译的库不能大于minSdk值，否则会不支持
        minSdk = 26 //26，安卓8(Oreo, O)
        targetSdk = 34
        versionCode = 27
        versionName = "1.0.5.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters += listOf("arm64-v8a","x86_64","x86","armeabi-v7a")
        }
        externalNativeBuild {
            cmake {
                //这个 -DANDROID_STL=none 我也不知道什么意思，从其他项目复制来的，可能就是不用 C++ 的STL？不能用new delete之类的关键字？
                arguments+= listOf("-DANDROID_STL=none")
            }
        }
        //这种写法虽看着像ksp代码块，但其实是给ksp方法传了个函数参数
//        ksp{
////            arg("room.schemaLocation", dbSchemaLocation)
//        }
    }
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }
//    sourceSets["main"].jniLibs.srcDir("jniLibs")


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    kotlin {
        jvmToolchain(11)
    }
    buildFeatures {
        compose = true
        buildConfig = true  //不开这个不能用BuildConfig，我是为了用来获取versionName和versionCode
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            //别用 `excludes+=xxxxx` 语法，看着太像拼接字符串，实际上excludes是集合
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
//            //不知道为什么，引入包含 SubscriptionPurchaseV2 的依赖 google-api-services-androidpublisher 后，会报错，排除这两个东西就不报错了，意义不明
//            excludes.add("META-INF/INDEX.LIST")
//            excludes.add("META-INF/DEPENDENCIES")

        }
    }

    ndkVersion = "26.0.10792818"
}

dependencies {
    //billing
//    implementation ("com.android.billingclient:billing-ktx:7.0.0")

    //admob
//    implementation ("com.google.android.gms:play-services-ads:23.1.0")
//    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
//    // implementation("androidx.ads:ads-identifier:1.0.0-alpha05")


    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    //implementation("androidx.appcompat:appcompat-resources:1.6.1")
    //    val work_version = "2.9.0"
//    // Kotlin + coroutines
//    implementation("androidx.work:work-runtime-ktx:$work_version")

    //snowflake id，本来想用的，感觉没必要
//    implementation("de.mkammerer.snowflake-id:snowflake-id:0.0.2")

//    implementation("com.github.kaleidot725:text-editor-compose:0.6.0")

    // room start
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    // room end
    // moshi json lib
    // moshi is weired, idk why so many adapaters etc......
//    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
//    implementation("com.squareup.moshi:moshi:1.15.0")

//    implementation("com.google.accompanist:accompanist-drawablepainter:0.35.1-SNAPSHOT")

    // coil，加载图片用的
//    implementation("io.coil-kt:coil:2.6.0")
//    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
// https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
//    implementation("org.eclipse.jgit:org.eclipse.jgit:v6.6.1.202309021850-r")
// https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit.pgm
//猜测（待测试）：这个好像是jgit命令行的实现，这个包的各种类对应git命令行的各种操作，例如：Clone类对应clone命令，可以让你在命令行
// 像使用git命令一样使用jgit，而这个包是用jgit的各种api实现的，若想了解jgit如何实现git的各种命令，这个包很有参考价值，
// 不过不知道里面的类是否能直接用？比如我想用clone命令，可不可以直接调用这里的Clone类？
//    implementation("org.eclipse.jgit:org.eclipse.jgit.pgm:6.8.0.202311291450-r")
    implementation(files("libs/git24j-1.0.2.20240423.jar"))
    implementation("androidx.navigation:navigation-compose:2.8.2")

    //查询支付状态的api，如果前端取消订单后不久就过期，就不需要这个了，否则需要
//    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
//    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev20240516-2.0.0")


// https://mvnrepository.com/artifact/org.danilopianini/khttp
//    implementation("org.danilopianini:khttp:1.6.2")

//    implementation("androidx.compose.material3:material3-android:1.2.0-beta02")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

//    implementation("androidx.compose.compiler:compiler:1.5.12")
//    implementation("androidx.compose.compiler:compiler-hosted:1.5.12")

    implementation("androidx.compose.material3:material3:1.3.0")
    implementation ("androidx.compose.material:material-icons-extended:1.7.3")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
