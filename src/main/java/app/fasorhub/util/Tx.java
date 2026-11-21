package app.fasorhub.util;

import java.sql.Connection;
import java.sql.SQLException;

// Runs work in one transaction that commits when it finishes and rolls back when it fails
public final class Tx {
    private Tx() {
    }

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    public static <T> T run(Work<T> work) {
        try (Connection connection = Db.get()) {
            connection.setAutoCommit(false);

            try {
                T result = work.run(connection);
                connection.commit();

                return result;
            } catch (SQLException e) {
                connection.rollback();
                throw new DataAccessException(e.getMessage(), e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public static void runVoid(Work<Void> work) {
        run(work);
    }
}
