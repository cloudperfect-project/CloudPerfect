package eu.artist.migration.pt.results;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import eu.artist.migration.pt.results.IniReader;

public class DbConnection {
	private static volatile Connection connection = null;
	
	private static Connection createConnection() throws SQLException {
		IniReader reader = IniReader.getInstance();
		String connectionString = "jdbc:mysql://" + reader.getDbHost() +":" + reader.getDbPort() + "/" + reader.getDbName() +
				"?" + "user=" + reader.getDbUser() + "&password=" + reader.getDbPass();
		Connection connection = DriverManager.getConnection(connectionString);
		return connection;
	}
	
	public static Connection getConnection() throws SQLException {
		if (connection == null) {
			synchronized (DbConnection.class) {
				// Double check
				if (connection == null) {
					connection = createConnection();
				}
			}
		}
		return connection;
	}
}
