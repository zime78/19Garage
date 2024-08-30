package base

import java.sql.Connection

open class DBBaseModel() {

//    private var connection: Connection? = null
//
//    object DBBaseModel {
//        val dbName = "db/sample.db"
//    }
//
//    init {
//        connection = DriverManager.getConnection("jdbc:sqlite:$dbName")
//        createTable()
//    }

//    private fun createTable() {
//        connection?.createStatement()?.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT NOT NULL)")
//    }
//
//    fun insertUser(name: String) {
//        val statement = connection?.prepareStatement("INSERT INTO users(name) VALUES (?)")
//        statement?.setString(1, name)
//        statement?.executeUpdate()
//    }
//
//    fun getAllUsers(): List<String> {
//        val users = mutableListOf<String>()
//        val resultSet: ResultSet? = connection?.createStatement()?.executeQuery("SELECT * FROM users")
//        while (resultSet?.next() == true) {
//            users.add(resultSet.getString("name"))
//        }
//        return users
//    }

}