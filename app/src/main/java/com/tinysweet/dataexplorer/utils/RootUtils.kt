package com.tinysweet.dataexplorer.utils

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * RootUtils - Tiện ích xử lý các thao tác root shell
 * Sử dụng libsu để thực thi lệnh root
 */
object RootUtils {

    private var rootShell: Shell? = null

    /**
     * Kiểm tra thiết bị đã root chưa
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (rootShell == null || rootShell?.isAlive == false) {
                rootShell = Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .build("su")
            }
            rootShell?.isAlive == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Thực thi lệnh root và trả về kết quả
     */
    suspend fun executeCommand(command: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, command)
            result?.out ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Thực thi nhiều lệnh root
     */
    suspend fun executeCommands(commands: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = rootShell?.newJob()?.add(*commands.toTypedArray())?.exec()
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Đọc nội dung file bằng root
     */
    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        try {
            ShellUtils.fastCmd(rootShell, "cat '$path'").out.joinToString("\n")
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Ghi nội dung vào file bằng root
     */
    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val escapedContent = content.replace("'", "'\"'\"'")
            val result = ShellUtils.fastCmdResult(rootShell, "echo '$escapedContent' > '$path'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Liệt kê thư mục bằng root
     */
    suspend fun listDirectory(path: String): List<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmd(rootShell, "ls -la '$path'").out
            parseLsOutput(result)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parse kết quả ls -la
     */
    private fun parseLsOutput(output: List<String>): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        for (line in output) {
            if (line.startsWith("total") || line.isEmpty()) continue
            val parts = line.trim().split(Regex("\\s+"), limit = 9)
            if (parts.size >= 9) {
                val permissions = parts[0]
                val isDirectory = permissions.startsWith("d")
                val name = parts[8]
                val size = if (isDirectory) 0L else parts[4].toLongOrNull() ?: 0L
                files.add(
                    FileInfo(
                        name = name,
                        isDirectory = isDirectory,
                        size = size,
                        permissions = permissions
                    )
                )
            }
        }
        return files
    }

    /**
     * Xóa file/thư mục
     */
    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "rm -rf '$path'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Copy file
     */
    suspend fun copyFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "cp -r '$source' '$destination'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Di chuyển file
     */
    suspend fun moveFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "mv '$source' '$destination'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Tạo thư mục
     */
    suspend fun createDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "mkdir -p '$path'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Nén thư mục thành zip
     */
    suspend fun zipDirectory(sourceDir: String, outputZip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "cd '${sourceDir.substringBeforeLast("/")}' && zip -r '$outputZip' '${sourceDir.substringAfterLast("/")}'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Giải nén zip
     */
    suspend fun unzipFile(zipFile: String, destinationDir: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = ShellUtils.fastCmdResult(rootShell, "unzip -o '$zipFile' -d '$destinationDir'")
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lấy dung lượng thư mục
     */
    suspend fun getDirectorySize(path: String): String = withContext(Dispatchers.IO) {
        try {
            ShellUtils.fastCmd(rootShell, "du -sh '$path' | cut -f1").out.firstOrNull() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Đóng root shell
     */
    fun closeShell() {
        rootShell?.close()
        rootShell = null
    }
}