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

    fun isPhotoCached(uri: String, qualityFactor: Int): Boolean {
        val files = directory.listFiles()!!
        val filename = buildFullFileName(uri, qualityFactor)
        return files.any { it.nameWithoutExtension == filename }
    }

    fun getPhotoFromCache(uri: String, qualityFactor: Int): Bitmap? {
        val files = directory.listFiles()!!
        val filename = buildFullFileName(uri, qualityFactor)
        val photo = files.find { it.nameWithoutExtension == filename } ?: return null
        return getBitmapOutOfFile(photo)
    }

    fun savePhotoToCache(uri: String, bitmap: Bitmap, qualityFactor: Int) {
        if (isPhotoCached(uri, qualityFactor)) {
            return
        }
        ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            val photo = File(buildAbsoluteFileName(uri, qualityFactor))
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

    private fun buildFullFileName(uri: String, qualityFactor: Int): String = "${uri}${qualityFactor}"

    private fun buildAbsoluteFileName(uri: String, qualityFactor: Int): String =
        "${directory.absolutePath}/${buildFullFileName(uri, qualityFactor)}"

    companion object {
        const val TAG = "Photo Cache Manager"
    }
}