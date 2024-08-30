package utils

import java.io.File

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
    }
}