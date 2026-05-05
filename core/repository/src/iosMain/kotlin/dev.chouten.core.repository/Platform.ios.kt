package dev.chouten.core.repository

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.*
import kotlinx.cinterop.*
import platform.posix.memcpy

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong();

actual object FileStore {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun write(path: String, bytes: ByteArray) {
        val fileManager = NSFileManager.defaultManager

        val documentsUrl = fileManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        ).first() as NSURL

        val fileUrl = documentsUrl.URLByAppendingPathComponent(path)!!

        val dirUrl = fileUrl.URLByDeletingLastPathComponent()!!

        fileManager.createDirectoryAtURL(
            dirUrl,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        bytes.usePinned { pinned ->
            val data = NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )

            data.writeToURL(fileUrl, atomically = true)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun read(path: String): ByteArray? {
        val fm = NSFileManager.defaultManager

        val documentsUrl = fm.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL

        val fileUrl = documentsUrl
            .URLByAppendingPathComponent(path)

        val path = fileUrl?.path ?: return null

        if (!fm.fileExistsAtPath(path)) return null

        val data = NSData.dataWithContentsOfFile(path) ?: return null

        val length = data.length.toInt()
        val byteArray = ByteArray(length)

        memScoped {
            val buffer = byteArray.refTo(0)
            memcpy(buffer, data.bytes, data.length)
        }

        return byteArray
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun delete(path: String) {
        val manager = NSFileManager.defaultManager
        if (manager.fileExistsAtPath(path)) {
            manager.removeItemAtPath(path, error = null)
        }
    }

    actual suspend fun exists(path: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun createDirectories(path: String) {
        NSFileManager.defaultManager.createDirectoryAtPath(
            path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun move(from: String, to: String) {
        val manager = NSFileManager.defaultManager

        manager.createDirectoryAtPath(
            to.substringBeforeLast("/"),
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        manager.moveItemAtPath(from, to, error = null)
    }
}