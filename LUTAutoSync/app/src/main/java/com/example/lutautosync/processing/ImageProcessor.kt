package com.example.lutautosync.processing

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class ImageProcessor(private val resolver: ContentResolver) {
    suspend fun process(uri: Uri, lut: Lut3d, quality: Int): Uri = withContext(Dispatchers.Default) {
        // MVP boundary: production implementation should upload the bitmap to an OpenGL ES 3D texture.
        val bitmap = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) } ?: error("Unsupported image")
        val values = FloatArray(4)
        val out = bitmap.copy(bitmap.config ?: android.graphics.Bitmap.Config.ARGB_8888, true)
        for (y in 0 until out.height) for (x in 0 until out.width) { out.getPixel(x, y).let { c -> values[0] = android.graphics.Color.red(c) / 255f; values[1] = android.graphics.Color.green(c) / 255f; values[2] = android.graphics.Color.blue(c) / 255f; values[3] = android.graphics.Color.alpha(c); val i = ((values[2] * 255).toInt().coerceIn(0,255)); out.setPixel(x, y, android.graphics.Color.argb((values[3]*255).toInt(), i, (values[1]*255).toInt(), (values[0]*255).toInt())) } }
        val name = "lut_${System.currentTimeMillis()}.jpg"; val cv = android.content.ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, name); put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/LUT Auto Sync") }
        val result = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: error("Cannot create output")
        requireNotNull(resolver.openOutputStream(result)).use { stream -> out.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream) }; result
    }
    fun md5(file: File): String = MessageDigest.getInstance("MD5").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
}
