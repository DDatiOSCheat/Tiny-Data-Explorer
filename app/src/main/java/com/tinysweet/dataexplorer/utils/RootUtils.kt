package com.tinysweet.dataexplorer.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RootUtils - Tiện ích xử lý các thao tác root shell
 * Sử dụng libsu để thực thi lệnh root
 *
 * Shell is initialized in App.kt with FLAG_MOUNT_MASTER
 * so it can access /data/data on Android 11+ with Magisk.
 */
object RootUtils {

    /**
     * Kiểm tra thiết bị đã root chưa
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Thực thi lệnh root và trả về kết quả
     */
    suspend fun executeCommand(command: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd(command).exec()
            result.out
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Thực thi nhiều lệnh root
     */
    suspend fun executeCommands(commands: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val job = Shell.cmd(*commands.toTypedArray()).exec()
            job.isSuccess
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Đọc nội dung file bằng root
     */
    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("cat '$path'").exec().out.joinToString("\n")
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Ghi nội dung vào file bằng root.
     * Sử dụng base64 để tránh vấn đề escape.
     */
    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Write via a heredoc to handle special characters safely
            val escaped = content.replace("\\", "\\\\").replace("'", "'\"'\"'")
            Shell.cmd("echo '$escaped' > '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Liệt kê thư mục bằng root
     * Sử dụng ls -la và parse đầu ra phù hợp với Android toybox
     */
    suspend fun listDirectory(path: String): List<FileInfo> = withContext(Dispatchers.IO) {
        try {
            val output = Shell.cmd("ls -la '$path'").exec().out
            parseLsOutput(output)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Parse kết quả ls -la
     *
     * Android toybox ls -la output format (8 fields):
     *   drwxrwx--x  4 u0_a150 u0_a150  4096 2024-01-15 10:30 dirname
     *   -rw-rw-rw-  1 u0_a150 u0_a150  1234 2024-01-15 10:30 filename
     *   lrwxrwxrwx  1 root    root       12 2024-01-15 10:30 link -> target
     *
     * Traditional Linux ls -la (9 fields - month day time/year):
     *   drwxr-xr-x  4 root root 4096 Jan 15 10:30 dirname
     *
     * We handle both by checking field count.
     */
    private fun parseLsOutput(output: List<String>): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        for (line in output) {
            if (line.startsWith("total") || line.isBlank()) continue

            val parts = line.trim().split(Regex("\\s+"))
            if (parts.size < 7) continue

            val permissions = parts[0]
            val isDirectory = permissions.startsWith("d")

            // Determine name based on field count
            // Android toybox: perm links owner group size date time name...
            // Linux: perm links owner group size month day time/year name...
            val name: String
            val sizeStr: String

            if (parts.size >= 8) {
                // Could be Android (8+ fields) or Linux (9+ fields)
                // Check if parts[5] looks like a date (contains '-') → Android format
                if (parts[5].contains("-")) {
                    // Android toybox format: perm links owner group size YYYY-MM-DD HH:MM name...
                    sizeStr = parts[4]
                    name = parts.drop(7).joinToString(" ")
                } else {
                    // Traditional Linux format: perm links owner group size Mon DD time name...
                    sizeStr = parts[4]
                    name = if (parts.size >= 9) parts.drop(8).joinToString(" ") else parts.last()
                }
            } else {
                // Fallback: just take size and name from what we have
                sizeStr = parts.getOrElse(4) { "0" }
                name = parts.last()
            }

            // Skip . and ..
            val cleanName = name.split(" -> ").first().trim()
            if (cleanName == "." || cleanName == "..") continue
            if (cleanName.isBlank()) continue

            val size = if (isDirectory) 0L else sizeStr.toLongOrNull() ?: 0L

            files.add(
                FileInfo(
                    name = cleanName,
                    isDirectory = isDirectory,
                    size = size,
                    permissions = permissions
                )
            )
        }
        return files
    }

    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("rm -rf '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun copyFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("cp -r '$source' '$destination'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun moveFile(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("mv '$source' '$destination'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun createDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("mkdir -p '$path'").exec().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun zipDirectory(sourceDir: String, outputZip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Use tar+gzip as fallback since zip might not be available on all devices
            val result = Shell.cmd("cd '$sourceDir/..' && tar czf '$outputZip' '${sourceDir.substringAfterLast("/")}'").exec()
            if (!result.isSuccess) {
                // Try zip as alternative
                Shell.cmd("cd '$sourceDir/..' && zip -r '$outputZip' '${sourceDir.substringAfterLast("/")}'").exec().isSuccess
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unzipFile(zipFile: String, destinationDir: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Try tar first (for .tar.gz), then unzip
            val result = Shell.cmd("tar xzf '$zipFile' -C '$destinationDir'").exec()
            if (!result.isSuccess) {
                Shell.cmd("unzip -o '$zipFile' -d '$destinationDir'").exec().isSuccess
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getDirectorySize(path: String): String = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("du -sh '$path'").exec().out.firstOrNull() ?: "Unknown"
            // du output: "4.0K\t/path" - extract just the size
            result.split(Regex("\\s+")).firstOrNull() ?: "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    /**
     * Check if a path exists
     */
    suspend fun pathExists(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("[ -e '$path' ] && echo 'yes' || echo 'no'").exec().out.firstOrNull()?.trim() == "yes"
        } catch (_: Exception) {
            false
        }
    }

    fun closeShell() {
        // No-op: libsu handles shell lifecycle internally.
    }
}
