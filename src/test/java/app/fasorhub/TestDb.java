package app.fasorhub;

import app.fasorhub.util.AppInitializer;
import app.fasorhub.util.Db;
import app.fasorhub.util.Jdbc;
import app.fasorhub.util.Seeder;
import app.fasorhub.util.Tx;

final class TestDb {
    private TestDb() {
    }

    static void fresh(boolean seed) {
        System.setProperty("DB_NAME", "fasor_hub_test");
        Db.init();
        AppInitializer.createSchema();
        Tx.runVoid(c -> {
            Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 0");
            for (String table : new String[] {"booking_time_slot", "bookings", "time_slots", "facilities", "users"}) {
                Jdbc.update(c, "TRUNCATE TABLE " + table);
            }
            Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 1");
            return null;
        });
        if (seed) {
            Seeder.seedIfEmpty();
        }
    }

    static long count(String sql, Object... params) {
        return Tx.run(c -> Jdbc.queryOne(c, sql, r -> r.getLong(1), params).orElse(0L));
    }
}
