package com.zime.garage.utils

import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.swing.JFileChooser

class Util {
    companion object {

        /**
         * 디렉터리가 있는지 확인하고, 없는 경우 생성합니다.
         * @param dbName : 디렉터리가 있는지 확인할 파일명
         * EXAMPLE: isDirectoryExists("db/sample.db")
         */
        fun isDirectoryExists(dbName: String) {
            val dbFile = File(dbName)
            val parentDir = dbFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
        }

        fun getDatabasePath(dbName: String, isFilePath: Boolean = true): String {
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            val basePath = when {
                osName.contains("mac") -> {
                    val path = Paths.get(System.getProperty("user.home"), "Documents", "19Garage").toFile()
                    if (!path.exists()) {
                        path.mkdirs()
                    }
                    Paths.get(System.getProperty("user.home"), "Documents", "19Garage").toString()
                }
                //C:\Users\zime\AppData\Roaming\19Garage\db\sample.db
                osName.contains("win") -> Paths.get(System.getenv("APPDATA"), "19Garage").toString()
                else -> Paths.get(System.getProperty("user.dir"), "db").toString()
            }

            val path = Paths.get(basePath, dbName).toString()
            println("path] Database Path : $path")
            return if(isFilePath) path
            else path
        }
        fun requestFileAccess() {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Select a Directory"
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

            val returnValue = fileChooser.showOpenDialog(null)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fileChooser.selectedFile
                println("Selected directory: ${selectedFile.absolutePath}")
                // 작업 수행
            } else {
                println("No directory chosen")
            }
        }

    }
}