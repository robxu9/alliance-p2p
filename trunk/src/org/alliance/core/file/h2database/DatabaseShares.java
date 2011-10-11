package org.alliance.core.file.h2database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Bastvera
 */
public class DatabaseShares {

    private final Connection conn;

    public DatabaseShares(Connection conn) throws SQLException {
        this.conn = conn;
        createTable();
        createIndexes();
    }

    private void createTable() throws SQLException {
        Statement statement = conn.createStatement();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS shares(");
        sql.append("base_path character varying(4096) NOT NULL, ");
        sql.append("sub_path character varying(4096) NOT NULL, ");
        sql.append("filename character varying(4096) NOT NULL, ");
        sql.append("type tinyint NOT NULL, ");
        sql.append("size bigint NOT NULL, ");
        sql.append("root_hash binary NOT NULL, ");
        sql.append("modified bigint NOT NULL, ");
        sql.append("CONSTRAINT fk_sharesbases_base_path FOREIGN KEY (base_path) REFERENCES sharesbases(base_path) ON DELETE CASCADE, ");
        sql.append("CONSTRAINT pk_shares PRIMARY KEY (root_hash));");
        statement.executeUpdate(sql.toString());
    }

    private void createIndexes() throws SQLException {
        Statement statement = conn.createStatement();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE INDEX IF NOT EXISTS idx_shares_base_path ON shares(base_path);");
        sql.append("CREATE INDEX IF NOT EXISTS idx_shares_sub_path ON shares(sub_path);");
        sql.append("CREATE INDEX IF NOT EXISTS idx_shares_filename ON shares(filename);");
        sql.append("CREATE INDEX IF NOT EXISTS idx_shares_type ON shares(type);");
        statement.executeUpdate(sql.toString());
    }

    public boolean addEntry(String basePath, String subPath, String filename, byte type, long size, byte[] rootHash, long modifiedAt) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("INSERT INTO shares(base_path, sub_path, filename, type, size, root_hash, modified) ");
            statement.append("VALUES(?, ?, ?, ?, ?, ?, ?);");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, basePath);
            ps.setString(2, subPath);
            ps.setString(3, filename);
            ps.setByte(4, type);
            ps.setLong(5, size);
            ps.setBytes(6, rootHash);
            ps.setLong(7, modifiedAt);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            //TODO ONLY Detection of duplicate pk exception
            return false;
        }
    }

    public ResultSet getEntry(String basePath, String subPath, String filename) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT * FROM shares WHERE filename=? ");
            statement.append("GROUP BY root_hash HAVING (base_path=? AND sub_path =?);");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, filename);
            ps.setString(2, basePath);
            ps.setString(3, subPath);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getEntry(byte[] rootHash) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT * FROM shares WHERE root_hash=?;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setBytes(1, rootHash);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getEntries(String basePath, String subPath, boolean withSubPaths, int limit) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT * FROM shares WHERE sub_path LIKE ? ");
            statement.append("GROUP BY root_hash HAVING base_path=? ");
            statement.append("LIMIT ?;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            if (withSubPaths) {
                ps.setString(1, subPath + "%");
            } else {
                ps.setString(1, subPath);
            }
            ps.setString(2, basePath);
            ps.setInt(3, limit);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getEntries(String filename, byte type) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT * FROM shares WHERE filename=?");
            if (type != 0) {
                statement.append(" AND TYPE=?");
            }
            statement.append(";");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, filename);
            if (type != 0) {
                ps.setByte(2, type);
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getFilenames(String query, int limit) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT filename FROM shares GROUP BY filename HAVING LOWER(filename) LIKE ? LIMIT ?;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, searchQuery(query));
            ps.setInt(2, limit);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getSubPaths(String basePath) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT sub_path FROM SHARES GROUP BY base_path, sub_path HAVING base_path=? ORDER BY sub_path DESC");
            PreparedStatement ps = conn.prepareStatement(statement.toString());       
            ps.setString(1, basePath);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet contains(String basePath, String subPath, String filename) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT BOOL_OR(base_path=? AND sub_path =?) AS contains FROM shares WHERE (filename=?)");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, basePath);
            ps.setString(2, subPath);
            ps.setString(3, filename);
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getNumberOfShares() {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT Count(*) FROM shares;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResultSet getTotalSizeOfFiles() {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("SELECT SUM(size) FROM shares;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            return ps.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void deleteEntry(byte[] rootHash) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("DELETE FROM shares WHERE root_hash=?;");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setBytes(1, rootHash);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteEntries(String basePath, String subPath, int limit) {
        try {
            StringBuilder statement = new StringBuilder();
            statement.append("DELETE FROM shares WHERE root_hash IN ");
            statement.append("(SELECT root_hash FROM shares WHERE sub_path LIKE ? ");
            statement.append("GROUP BY root_hash HAVING base_path=? LIMIT ?);");
            PreparedStatement ps = conn.prepareStatement(statement.toString());
            ps.setString(1, subPath + "%");
            ps.setString(2, basePath);
            ps.setInt(3, limit);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private String searchQuery(String query) {
    	//System.out.println("Original query: " + query);
    	query = query.toLowerCase() // case-insensitive search
    		.trim().replaceAll("\\s+", " "); // collapse whitespace
        String search = "";
        if (!query.startsWith("*")) {
        	search += "%"; // allow anything before search terms
        }
        int n = query.length();
        boolean escaped = false;
        for (int i = 0; i < n; i++) {
        	char c = query.charAt(i);
        	if (escaped) {
        		if (c == '*') {
        			search += "*"; // \* matches literal *
        		}
        		else if (c == '?') {
        			search += "?"; // \? matches literal ?
        		}
        		else if (c == '\\') {
        			search += "\\"; // \\ matches literal \
        		}
        		else if (c == ' ') {
        			search += " "; // \space  matches literal space
        		}
        		else {
        			search += "\\" + c; // bad escape sequence; treat \ literally
        		}
        		escaped = false;
        	}
        	else {
        		if (c == '*' || c == ' ') {
        			search += "%"; // * and space match any characters
        		}
        		else if (c == '%') {
        			search += "\\%"; // since SQL LIKE uses % instead of *
        		}
        		else if (c == '?') {
        			search += "_"; // ? matches any single character
        		}
        		else if (c == '_') {
        			search += "\\_"; // since SQL LIKE uses _ instead of ?
        		}
        		else if (c == '\\') {
        			escaped = true; // \ escapes the next character
        		}
        	}
        }
        if (escaped) {
        	search += "\\"; // incomplete escape sequence; treat \ literally
        }
        if (!search.endsWith("%")) {
        	search += "%"; // allow anything after search terms
        }
        //System.out.println("Search query: " + search);
        return search;
    }
}
