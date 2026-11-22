package app.fasorhub;

import app.fasorhub.exception.ValidationException;
import app.fasorhub.service.FacilityService;
import app.fasorhub.service.FacilityTypeService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityTypeServiceTest {

    @BeforeEach
    void setUp() {
        TestDb.fresh(true);
    }

    private static long typeCount() {
        return TestDb.count("SELECT COUNT(*) FROM facility_types");
    }

    private static long idOf(String name) {
        return TestDb.count("SELECT id FROM facility_types WHERE name = ?", name);
    }

    private static String keyOf(ValidationException e) {
        return e.getMessageKey();
    }

    // FAC-07
    @Test
    void theSeededTypesExist() {
        assertEquals(4, typeCount());
        assertTrue(idOf("Futsal") > 0);
    }

    // FAC-07
    @Test
    void createAddsATypeWithTrimmedAndSingleSpacedName() throws Exception {
        long id = FacilityTypeService.create("  Table   tennis ");

        assertEquals(5, typeCount());
        assertEquals(id, idOf("Table tennis"));
    }

    // FAC-07
    @Test
    void renameChangesTheNameOfFacilitiesThatUseTheType() throws Exception {
        long futsal = idOf("Futsal");

        FacilityTypeService.rename(futsal, "Indoor futsal");

        assertEquals(futsal, idOf("Indoor futsal"));
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM facilities f JOIN facility_types t ON t.id = f.type_id WHERE t.name = 'Indoor futsal'"));
    }

    // FAC-07
    @Test
    void aTypeCanBeRenamedWithOnlyADifferentCase() throws Exception {
        long tennis = idOf("Tennis");

        FacilityTypeService.rename(tennis, "TENNIS");

        assertEquals(4, typeCount());
        assertEquals(1, TestDb.count("SELECT COUNT(*) FROM facility_types WHERE BINARY name = 'TENNIS'"));
    }

    // FAC-07
    @Test
    void theLimitAndLettersAreAccepted() throws Exception {
        FacilityTypeService.create("x".repeat(30));
        FacilityTypeService.create("Sepak takraw 2-on-2");
        FacilityTypeService.create("Bola voli pantai");

        assertEquals(7, typeCount());
    }

    // FAC-08
    @Test
    void invalidNamesAreRejectedAndNothingIsSaved() {
        for (String bad : new String[] {null, "", "   ", "x".repeat(31), "Foot@ball", "-Futsal", "Futsal!", "Net<script>"}) {
            assertEquals("p41", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.create(bad))), String.valueOf(bad));
        }

        assertEquals("p41", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.rename(idOf("Futsal"), "  "))));
        assertEquals(4, typeCount());
    }

    // FAC-08
    @Test
    void aDuplicateIgnoringCaseAndSpacingIsRejected() throws Exception {
        FacilityTypeService.create("Table tennis");

        assertEquals("p42", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.create("table tennis"))));
        assertEquals("p42", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.create("TABLE   TENNIS"))));
        assertEquals("p42", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.create("futsal"))));
        assertEquals("p42", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.rename(idOf("Tennis"), "Futsal"))));
        assertEquals(5, typeCount());
    }

    // FAC-08
    @Test
    void renamingATypeThatNoLongerExistsGivesP01() {
        assertEquals("p01", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.rename(999_999L, "Squash"))));
    }

    // FAC-09
    @Test
    void aTypeUsedByAFacilityCannotBeRemoved() {
        assertEquals("p43", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.delete(idOf("Futsal")))));
        assertEquals(4, typeCount());
    }

    // FAC-09
    @Test
    void anUnusedTypeCanBeRemovedAndThenCannotBeChosen() throws Exception {
        long squash = FacilityTypeService.create("Squash");

        FacilityTypeService.delete(squash);

        assertEquals(4, typeCount());
        assertEquals("p39", keyOf(assertThrows(ValidationException.class, () -> FacilityService.create("Court", squash, null))));
    }

    // FAC-09
    @Test
    void aTypeBecomesRemovableOnceItsLastFacilityIsGone() throws Exception {
        long id = FacilityTypeService.create("Squash");
        long facility = FacilityService.create("Squash court", id, null);

        assertEquals("p43", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.delete(id))));

        FacilityService.delete(facility);
        FacilityTypeService.delete(id);

        assertEquals(4, typeCount());
    }

    // FAC-09
    @Test
    void removingATypeThatNoLongerExistsGivesP01() {
        assertEquals("p01", keyOf(assertThrows(ValidationException.class, () -> FacilityTypeService.delete(999_999L))));
    }

    // FAC-08
    @Test
    void sameNameAddedAtTheSameTimeIsSavedOnlyOnce() throws Exception {
        int callers = 8;
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> results = new ArrayList<>();

        for (int i = 0; i < callers; i++) {
            results.add(pool.submit(() -> {
                start.await();

                try {
                    FacilityTypeService.create("Volleyball");

                    return "saved";
                } catch (ValidationException e) {
                    return e.getMessageKey();
                }
            }));
        }

        start.countDown();

        int saved = 0;
        int duplicates = 0;

        for (Future<String> result : results) {
            String outcome = result.get(10, TimeUnit.SECONDS);

            if ("saved".equals(outcome)) {
                saved++;
            } else if ("p42".equals(outcome)) {
                duplicates++;
            } else {
                fail("Unexpected outcome " + outcome);
            }
        }

        pool.shutdown();

        assertEquals(1, saved);
        assertEquals(callers - 1, duplicates);
        assertEquals(5, typeCount());
    }
}
