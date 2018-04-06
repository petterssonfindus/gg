/*
 * Created on 15.01.2018
 */
package data;

import java.sql.SQLException;
import java.sql.*;
import java.util.Properties;

/**
 * @author XK02200
 */
public class ConnectionFactory
{
    private static java.sql.Connection curConnection = null;    
    private static Properties connectionProps;
    
    private static final String userName = "root";
    private static final String password = "";
    private static final String host = "localhost";
    private static final String port = "3306";
    private static final String dbName = "kurse";

    public static Connection getConnection()
    {
		if (curConnection == null)
        	
    	connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        	
        {
            Driver treiber;
            try
            {
//            	java.sql.Driver d=new com.mysql.jdbc.Driver();
                treiber = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
                String serverURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                curConnection = (Connection) treiber.connect(serverURL, connectionProps);
            } catch (InstantiationException e)
            {
            	System.out.println("InstantiationException: ConnectionFactory.getConnection");
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
            	System.out.println("IllegalAccessException: ConnectionFactory.getConnection");
                e.printStackTrace();
            } catch (ClassNotFoundException e)
            {
            	System.out.println("ClassNotFoundException: ConnectionFactory.getConnection");
                e.printStackTrace();
            } catch (SQLException e)
            {
            	System.out.println("Sieht aus, wie wenn die Datenbank nicht läuft");
                e.printStackTrace();
            }
        }
        return curConnection;
    }
}
