# 开发文档

## 工程结构

| 目录 | 说明 |
|---|---|
| `otpinput` | Kuikly DSL 组件库 |
| `otpinput-compose` | Compose DSL 组件库 |
| `shared` | Demo 页面 |
| `androidApp` / `iosApp` / `ohosApp` / `h5App` | 各平台宿主应用，Android、iOS 和 H5 宿主包含短信验证码填充的属性处理器（接入方可选） |

两个组件库互不依赖，输入规则（`OtpCode`）在两个模块中分别实现。
两边遵循相同的输入行为契约，并通过对应的测试用例保持功能一致；内部实现可以独立演进。

Android、iOS 和 H5 使用 `settings.gradle.kts` 和 `build.gradle.kts`；
HarmonyOS 使用 `settings.ohos.gradle.kts` 和 `build.ohos.gradle.kts`。
Kotlin、Kuikly 和组件版本统一在 `gradle.properties` 中配置：

| 属性 | 说明 |
|---|---|
| `MAVEN_VERSION` | 组件版本 |
| `KOTLIN_VERSION` / `KUIKLY_VERSION` | Android、iOS 和 H5 使用的 Kotlin 与 Kuikly 版本 |
| `OHOS_KOTLIN_VERSION` / `KUIKLY_OHOS_VERSION` | HarmonyOS 使用的 Kotlin 与 Kuikly 版本 |

## Demo 页面

默认入口是 `otp_input_demo_home`，首页可跳转到：

- `otp_input_demo`：Kuikly DSL；
- `otp_input_compose_demo`：Compose DSL。

两个示例页功能相同：切换位数、字符集、样式、对齐方式、遮挡和可编辑状态，
模拟粘贴整条短信，输入页面提示的验证码校验通过，其他内容校验失败。

各平台使用相同页面名。H5 可通过 `?page_name=otp_input_demo` 或
`?page_name=otp_input_compose_demo` 直接打开对应示例页面。

## 测试

输入规则和 Compose 状态的单元测试：

```bash
./gradlew :otpinput:testDebugUnitTest :otpinput-compose:testDebugUnitTest --no-daemon
```

## 构建

Android 和 H5：

```bash
./gradlew :androidApp:assembleDebug --no-daemon
./gradlew :h5App:jsBrowserProductionWebpack --no-daemon
```

iOS 需要在 macOS 上先生成共享 framework，再运行 CocoaPods：

```bash
./gradlew :shared:generateDummyFramework
cd iosApp
pod install
```

HarmonyOS 需要 DevEco Studio 提供的 OHOS SDK、`ohpm` 和 `hvigorw`。
先编译 `shared` 的鸿蒙动态库，并把动态库和头文件拷贝到 `ohosApp`（这两个文件不提交到仓库）。
链接需要 OHOS SDK，DevEco Studio 不在默认位置时需设置 `OHOS_SDK_HOME` 或 `DEVECO_STUDIO_HOME`：

```bash
./gradlew -c settings.ohos.gradle.kts :shared:linkSharedDebugSharedOhosArm64 --no-daemon
mkdir -p ohosApp/entry/libs/arm64-v8a ohosApp/entry/src/main/cpp/thirdparty/biz_entry
cp shared/build/bin/ohosArm64/sharedDebugShared/libshared.so ohosApp/entry/libs/arm64-v8a/
cp shared/build/bin/ohosArm64/sharedDebugShared/libshared_api.h ohosApp/entry/src/main/cpp/thirdparty/biz_entry/
```

再构建 HAP：

```bash
cd ohosApp
ohpm install --all --strict_ssl true
hvigorw assembleApp --mode project -p product=default -p buildMode=debug --no-daemon
```

## 发布

产物版本号为 `{MAVEN_VERSION}-{Kotlin 版本}`，与 Kuikly-contrib 组件库一致：

| 平台 | 版本号示例 |
|---|---|
| Android / iOS / H5 | `0.2.0-2.1.21` |
| HarmonyOS | `0.2.0-2.0.21-KBA-010` |

通过 JitPack 发布时，JitPack 以 tag 作为 `VERSION`，因此每个组件版本需要打两个 tag，
分别对应上表的两个版本号。tag 包含 `-KBA-` 时 `jitpack.yml` 走鸿蒙构建，否则走 Android / iOS / H5 构建。
iOS klib 依赖 `kotlin.native.enableKlibsCrossCompilation=true` 在 Linux 上交叉编译。

本地发布到 Maven Local：

```bash
./gradlew :otpinput:publishToMavenLocal :otpinput-compose:publishToMavenLocal --no-daemon
./gradlew -c settings.ohos.gradle.kts publishOhosToMavenLocal --no-daemon
```
