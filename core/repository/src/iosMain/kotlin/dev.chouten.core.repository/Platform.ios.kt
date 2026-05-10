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
            memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                data.writeToURL(
                    url = fileUrl,
                    options = NSDataWritingAtomic,
                    error = error.ptr
                )
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun read(path: String): ByteArray? {
        val fm = NSFileManager.defaultManager
        val documentsUrl = fm.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL

        val fileUrl = documentsUrl.URLByAppendingPathComponent(path) ?: return null
        val filePath = fileUrl.path ?: return null

        if (!fm.fileExistsAtPath(filePath)) return null

        val data = NSData.dataWithContentsOfFile(filePath) ?: return null
        val length = data.length.toInt()
        if (length == 0) return ByteArray(0)

        val byteArray = ByteArray(length)
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return byteArray
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun resolvedPath(path: String): String? {
        val fm = NSFileManager.defaultManager
        val documentsUrl = fm.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL
        return documentsUrl.URLByAppendingPathComponent(path)?.path
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun delete(path: String) {
        val resolved = resolvedPath(path) ?: return
        val manager = NSFileManager.defaultManager
        if (manager.fileExistsAtPath(resolved)) {
            manager.removeItemAtPath(resolved, error = null)
        }
    }

    actual suspend fun exists(path: String): Boolean {
        val resolved = resolvedPath(path) ?: return false
        return NSFileManager.defaultManager.fileExistsAtPath(resolved)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun createDirectories(path: String) {
        val resolved = resolvedPath(path) ?: return
        NSFileManager.defaultManager.createDirectoryAtPath(
            resolved,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun move(from: String, to: String) {
        val resolvedFrom = resolvedPath(from) ?: return
        val resolvedTo = resolvedPath(to) ?: return
        val manager = NSFileManager.defaultManager
        manager.createDirectoryAtPath(
            resolvedTo.substringBeforeLast("/"),
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        if (manager.fileExistsAtPath(resolvedTo)) {
            manager.removeItemAtPath(resolvedTo, error = null)
        }
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val success = manager.moveItemAtPath(
                srcPath = resolvedFrom,
                toPath = resolvedTo,
                error = error.ptr
            )
            if (!success) {
                error.value?.let { throw IllegalStateException(it.localizedDescription) }
            }
        }
    }
}