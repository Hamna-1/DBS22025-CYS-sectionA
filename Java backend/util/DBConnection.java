package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String Url = "jdbc:mysql://localhost:3306/carnival_db";
    private static final String  userName = "root";
    private static final String passWord = "MySQL";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException{
        if (connection == null || connection.isClosed()){
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(Url,userName,passWord);
                System.out.println("Database connected");
            } catch (ClassNotFoundException e){
                System.err.println("MySQL Driver not found");
                e.printStackTrace();
            }
        }
        return connection;
    }
    public static void closeConnection(){
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
                System.out.println("Database connection closed. ");
            }
            }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
}

