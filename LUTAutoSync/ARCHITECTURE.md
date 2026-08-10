# LUT Auto Sync MVP

## 架构

采用单模块 Clean-ish 分层：`ui`（Compose/Material 3） -> `data`（Room/DataStore） -> `service`（Foreground Service + FileObserver） -> `processing`（LUT 解析与图像处理）。服务以 `START_STICKY` 持续运行，监听目录的 `CREATE`/`MOVED_TO`，通过 `(path, md5, lutId)` 唯一索引避免重复处理。

## 目录

`app/src/main/java/com/example/lutautosync/{data,processing,service,ui}`；Manifest 声明 Android 12+、媒体读取、前台数据同步服务与电池优化白名单权限。

## Gradle

AGP 8.5.2、Kotlin 2.0.20、Compose BOM 2024.10、Material 3、Room 2.6.1、DataStore、Coroutines。Room 使用 KSP。

## 数据库

`luts(id,name,path,format,favorite,isDefault)`；`processed_files(id,path,md5,processedAt,lutId)`，`path + md5` 唯一索引，查询最近 20 条与总数。

## 核心类

`SyncForegroundService`：通知、FileObserver、去重与入库。
`LutParser`：`.cube`/基础 3DL 文本解析为 3D 网格。
`ImageProcessor`：图片读写、MD5 与 GPU 替换边界。
`AppContainer`：Room 单例。
`MainActivity`：导入 LUT、默认 LUT、服务开关与统计首页。

## MVP 与生产增强

当前代码可以作为 Android Studio 工程导入。为满足 2400 万像素 2 秒目标，应将 `ImageProcessor` 的像素循环替换为 OpenGL ES 3.0：上传 Bitmap 为纹理，使用 `sampler3D` LUT 纹理和 FBO 输出；通过 `ExifInterface` 复制全部 EXIF；使用 MediaStore 的临时 URI 原子替换原文件。正式版还应加入 SAF 目录授权（DCIM/Pictures/自定义）、电池优化设置 Intent、HEIF 编解码、LUT 重命名/删除/收藏、WorkManager 重启兜底，以及处理队列的并发上限。
