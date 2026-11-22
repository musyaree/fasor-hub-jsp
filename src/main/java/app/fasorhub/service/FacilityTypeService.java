package app.fasorhub.service;

import app.fasorhub.dao.FacilityTypeDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.util.DataAccessException;
import app.fasorhub.util.Tx;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

// Adding renaming and removing the facility types a manager can pick from
public final class FacilityTypeService {
    public static final int MAX_NAME = 30;

    private static final Pattern ALLOWED = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} -]*");

    private FacilityTypeService() {
    }

    // Trims the name and turns any run of spaces into one space so two spellings cannot differ only by spacing
    static String clean(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    public static long create(String name) throws ValidationException {
        String clean = validate(name);

        try {
            Long id = Tx.run(c -> FacilityTypeDao.nameTaken(c, clean, 0) ? null : FacilityTypeDao.insert(c, clean));

            if (id == null) {
                throw new ValidationException("p42");
            }

            return id;
        } catch (DataAccessException e) {
            throw duplicateOrRethrow(e);
        }
    }

    public static void rename(long id, String name) throws ValidationException {
        String clean = validate(name);

        try {
            String failure = Tx.run(c -> {
                if (FacilityTypeDao.lockById(c, id).isEmpty()) {
                    return "p01";
                }

                if (FacilityTypeDao.nameTaken(c, clean, id)) {
                    return "p42";
                }

                FacilityTypeDao.rename(c, id, clean);

                return null;
            });

            if (failure != null) {
                throw new ValidationException(failure);
            }
        } catch (DataAccessException e) {
            throw duplicateOrRethrow(e);
        }
    }

    // A type that any facility still uses cannot be removed
    public static void delete(long id) throws ValidationException {
        String failure = Tx.run(c -> {
            if (FacilityTypeDao.lockById(c, id).isEmpty()) {
                return "p01";
            }

            if (FacilityTypeDao.countFacilities(c, id) > 0) {
                return "p43";
            }

            FacilityTypeDao.delete(c, id);

            return null;
        });

        if (failure != null) {
            throw new ValidationException(failure);
        }
    }

    private static String validate(String name) throws ValidationException {
        String clean = clean(name);

        if (clean.isEmpty() || clean.length() > MAX_NAME || !ALLOWED.matcher(clean).matches()) {
            throw new ValidationException("p41");
        }

        return clean;
    }

    // Two requests adding the same name at once are stopped by the unique index
    private static ValidationException duplicateOrRethrow(DataAccessException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
            return new ValidationException("p42");
        }

        throw e;
    }
}
