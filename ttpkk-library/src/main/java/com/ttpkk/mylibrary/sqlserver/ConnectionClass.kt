package com.ttpkk.mylibrary.sqlserver

import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.ttpkk.library.R
import java.lang.StringBuilder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class ConnectionClass {

    companion object {

        fun openConnection(server: String, port: String, database: String, user: String, password: String, timeout: String): Connection? {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val connection: Connection?
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver")
                val connectionURL = "jdbc:jtds:sqlserver://$server:$port;databaseName=$database;loginTimeout=$timeout;socketTimeout=$timeout"
                connection = DriverManager.getConnection(connectionURL, user, password)
            } catch (ex: SQLException) {
                Log.e("error here 1 : ", ex.message.toString())
                throw ex
            } catch (ex: ClassNotFoundException) {
                Log.e("error here 2 : ", ex.message.toString())
                throw ex
            } catch (ex: Exception) {
                Log.e("error here 3 : ", ex.message.toString())
                throw ex
            }
            return connection
        }

        fun setConnection(connection: Connection, column: String, parameters: ArrayList<ParameterResult>?): PreparedStatement {
            val sql = StringBuilder()
            sql.append("EXEC $column")

            parameters?.forEachIndexed { index, element ->
                sql.append(" @${element.column} = ?")
                if (index != (parameters.size - 1)) {
                    sql.append(",")
                }
            }

            val ps = connection.prepareStatement(sql.toString())

            ps.setEscapeProcessing(true)
            ps.queryTimeout = 10

            parameters?.forEachIndexed { index, element ->
                val i = index + 1
                /*when (val value = element.value) {
                    is String -> ps.setString(i, value.toString())
                    is Int -> ps.setInt(i, value.toInt())
                    is Double -> ps.setDouble(i, value.toDouble())
                    is Boolean -> ps.setBoolean(i, value)
                }*/
                ps.setString(i, element.value.toString())
            }
            return ps
        }

    }
}

data class ParameterResult(
    var column: String,
    var value: Any
)

data class SettingsPref(
    val prefServer: String,
    val prefPort: String,
    val prefDatabase: String,
    val prefUser: String,
    val prefPassword: String,
    val prefTimeout: String
)