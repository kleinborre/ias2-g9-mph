package util;

import db.DatabaseConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordBulkMigrate {
    public static void main(String[] args) throws Exception {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);

            List<String[]> pending = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT userID, passwordHash FROM authentication");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String uid = rs.getString(1);
                    String ph  = rs.getString(2);
                    if (!PasswordUtil.isHash(ph)) {
                        pending.add(new String[]{uid, ph});
                    }
                }
            }

            System.out.println("Legacy passwords to migrate: " + pending.size());
            try (PreparedStatement upd = c.prepareStatement(
                    "UPDATE authentication SET passwordHash=?, failed_attempts=0, locked_until=NULL WHERE userID=?")) {
                for (String[] rec : pending) {
                    String uid = rec[0];
                    String legacy = rec[1];
                    String bcrypt = PasswordUtil.hash(legacy);
                    upd.setString(1, bcrypt);
                    upd.setString(2, uid);
                    upd.addBatch();
                }
                upd.executeBatch();
            }

            c.commit();
            System.out.println("Migration complete.");
        }
    }
}
