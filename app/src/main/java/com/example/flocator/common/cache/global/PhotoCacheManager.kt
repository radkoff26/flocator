package com.example.flocator.common.cache.global

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoCacheManager @Inject constructor(@ApplicationContext context: Context) {
    private val directory = File(context.externalCacheDir!!.absolutePath + "/media").apply {
        if (!exists()) {
            this.mkdir()
        }
    }

    fun isPhotoCached(uri: String): Boolean {
        val files = directory.listFiles()!!
        return files.any { it.nameWithoutExtension == uri }
    }

    fun getPhotoFromCache(uri: String): Bitmap? {
        val files = directory.listFiles()!!
        val photo = files.find { it.nameWithoutExtension == uri } ?: return null
        return getBitmapOutOfFile(photo)
    }

    fun savePhotoToCache(uri: String, bitmap: Bitmap) {
        if (isPhotoCached(uri)) {
            return
        }
        ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            val photo = File(directory.absolutePath + "/${uri}")
            photo.writeBytes(it.toByteArray())
            photo.createNewFile()
        }
    }

    private fun getBitmapOutOfFile(file: File): Bitmap {
        return FileInputStream(file).use {
            val bytes = it.readBytes()
            val out = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            out
        }
    }

    companion object {
        const val TAG = "Photo Cache Manager"
    }
}