/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

import constants.ServerConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DatabaseConnection {
    private static final HashMap<Integer, DatabaseConnection.ConWrapper> connections = new HashMap();
    private static boolean propsInited = false;
    private static long connectionTimeOut = 2 * 60 * 1000; // 2 minutes
    public static final int RETURN_GENERATED_KEYS = 1;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        Thread cThread = Thread.currentThread();
        int threadID = (int) cThread.getId();
        DatabaseConnection.ConWrapper ret = connections.get(threadID);
        if (ret == null) {
            Connection retCon = connectToDB();
            ret = new DatabaseConnection.ConWrapper(retCon);
            ret.id = threadID;
            connections.put(threadID, ret);
        }
        return ret.getConnection();
    }

    private static long getWaitTimeout(Connection con) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'wait_timeout'");
            if (rs.next()) {
                return Math.max(1000, rs.getInt(2) * 1000 - 1000);
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            return -1;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException ex1) {
                        }
                    }
                }
            }
        }
    }

    private static Connection connectToDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");    // touch the MySQL driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Connection con = DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
            if (!propsInited) {
                long timeout = getWaitTimeout(con);
                if (timeout == -1) {
                } else {
                    connectionTimeOut = timeout;
                }
                propsInited = true;
            }
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ConWrapper {
        private long lastAccessTime = 0;
        private Connection connection;
        private int id;

        public ConWrapper(Connection con) {
            this.connection = con;
        }

        public Connection getConnection() {
            if (expiredConnection()) {
                try { // Assume that the connection is stale
                    connection.close();
                } catch (Throwable err) {
                    // Who cares
                }
                this.connection = connectToDB();
            }

            lastAccessTime = System.currentTimeMillis(); // Record Access
            return this.connection;
        }

        /**
         * Returns whether this connection has expired
         * @return
         */
        public boolean expiredConnection() {
            if (lastAccessTime == 0) {
                return false;
            }
            try {
                return System.currentTimeMillis() - lastAccessTime >= connectionTimeOut || connection.isClosed();
            } catch (Throwable ex) {
                return true;
            }
        }
    }

    public static void closeAll() throws SQLException {
        for (DatabaseConnection.ConWrapper con : connections.values()) {
            con.connection.close();
        }
        connections.clear();
    }
}