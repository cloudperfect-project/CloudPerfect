package eu.artist.migration.pt.cotroller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
	private static volatile Connection connection = null;
	
	private static Connection createConnection(String dbConfFile) throws SQLException {
		IniReader reader = new IniReader(dbConfFile);
		String connectionString = "jdbc:mysql://" + reader.getDbHost() +":" + reader.getDbPort() + "/" + reader.getDbName() +
				"?" + "user=" + reader.getDbUser() + "&password=" + reader.getDbPass();
		Connection connection = DriverManager.getConnection(connectionString);
		return connection;
	}
	
	public static Connection getConnection(String dbConfFile) throws SQLException {
		if (connection == null) {
			synchronized (DbConnection.class) {
				// Double check
				if (connection == null) {
					connection = createConnection(dbConfFile);
				}
			}
		}
		return connection;
	}
}
