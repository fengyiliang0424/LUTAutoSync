# LUT Auto Sync

这是 LUT Auto Sync 的 Android 项目。即使电脑没有安装 Android Studio，也可以让 GitHub Actions 在云端构建测试 APK。

## 如何通过 GitHub Actions 构建 APK

### 1. 创建 GitHub Repository

1. 登录 [GitHub](https://github.com/)。
2. 点击页面右上角的 `+`，选择 `New repository`。
3. Repository name 可填写 `LUTAutoSync`。
4. 选择 `Public` 或 `Private`。
5. 不要勾选自动创建 README、`.gitignore` 或 License。
6. 点击 `Create repository`。

### 2. 上传项目

进入刚创建的 Repository，点击 `uploading an existing file`，把本项目目录中的全部文件和文件夹上传。

请确认 `.github`、`app`、`gradle` 这些文件夹都已上传。`.github` 在 Windows 中可能显示为隐藏文件夹，不要漏掉它。

如果网页不方便上传文件夹，可以安装 GitHub Desktop，选择 `Add an Existing Repository from your Hard Drive`，然后发布到 GitHub。

### 3. 打开 Actions

进入 Repository 后，点击页面顶部的 `Actions`。第一次使用时，GitHub 可能显示启用提示，点击允许运行 Actions。

每次上传或修改项目后，`Build APK` 都会自动运行。Pull Request 也会触发构建。

### 4. 手动运行 Build APK

1. 在 Actions 页面左侧点击 `Build APK`。
2. 点击右侧的 `Run workflow`。
3. 再点击绿色的 `Run workflow` 按钮。

### 5. 等待构建完成

点击本次运行记录可以查看进度。绿色对勾表示成功，红色叉号表示失败。失败时展开红色步骤即可看到完整错误日志。

首次构建需要下载 Android 和 Gradle 依赖，通常比以后构建更慢。

### 6. 找到 Artifacts

构建成功后，打开这次运行的详情页，滚动到页面底部的 `Artifacts` 区域。

### 7. 下载 APK

点击 `LUTAutoSync-debug-apk` 下载压缩包。

### 8. 解压得到 APK

解压下载的 ZIP 文件，可以看到：

`app-debug.apk`

### 9. 安装到 Android 手机

1. 把 `app-debug.apk` 发送或复制到 Android 12 及以上版本的手机。
2. 在手机文件管理器中点击 APK。
3. 如果系统阻止安装，请按提示允许该文件管理器“安装未知应用”。
4. 再次点击 APK 完成安装。

这是 Debug APK，Android 可能显示安全提示。它仅用于当前项目的测试，不适合直接发布到应用商店。

## 自动构建配置

构建工作流位于 `.github/workflows/build-apk.yml`，使用 Java 17、Gradle 8.7、Android API 35 和 Build Tools 35.0.0，执行 `./gradlew assembleDebug`。构建结果会以 `LUTAutoSync-debug-apk` 的名称保存 14 天。
