package com.inumaki.chouten.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun ensureAppFolder() {
    val fileManager = NSFileManager.defaultManager

    val documentsUrl = fileManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    ).first() as NSURL

    val folderUrl = documentsUrl.URLByAppendingPathComponent("repositories")

    val path = folderUrl?.path ?: return

    if (!fileManager.fileExistsAtPath(path)) {
        fileManager.createDirectoryAtURL(
            url = folderUrl,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
}