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
        dropLegacyFacilities();
        AppInitializer.createSchema();
        Tx.runVoid(c -> {
            Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 0");
            for (String table : new String[] {"booking_time_slot", "bookings", "time_slots", "facilities", "facility_types", "users"}) {
                Jdbc.update(c, "TRUNCATE TABLE " + table);
            }
            Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 1");
            return null;
        });
        if (seed) {
            Seeder.seedIfEmpty();
        }
    }

    // A test database made before facility types existed still has the old facilities table with a type column
    private static void dropLegacyFacilities() {
        Tx.runVoid(c -> {
            long legacy = Jdbc.queryOne(c, "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() "
                    + "AND table_name = 'facilities' AND column_name = 'type'", r -> r.getLong(1)).orElse(0L);

            if (legacy > 0) {
                Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 0");
                Jdbc.update(c, "DROP TABLE facilities");
                Jdbc.update(c, "SET FOREIGN_KEY_CHECKS = 1");
            }

            return null;
        });
    }

    static long count(String sql, Object... params) {
        return Tx.run(c -> Jdbc.queryOne(c, sql, r -> r.getLong(1), params).orElse(0L));
    }
}
