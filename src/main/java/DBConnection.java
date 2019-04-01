import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by gwjense on 3/8/19.
 */
public class DBConnection {


        private Connection dbConnection;


        public Connection getDBConnection() throws ClassNotFoundException, SQLException {
            Class.forName("com.mysql.jdbc.Driver");


            dbConnection = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U041WH",
                    "U041WH", "53688147352");
            return dbConnection;
        }

        public int executeQuery(String query) throws ClassNotFoundException, SQLException {
            return dbConnection.createStatement().executeUpdate(query);
        }

    }


