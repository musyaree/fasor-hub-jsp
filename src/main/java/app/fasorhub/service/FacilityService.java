package app.fasorhub.service;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.SlotDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.dao.FacilityTypeDao;
import app.fasorhub.util.Clock;
import app.fasorhub.util.Tx;

// Adding editing and deleting facilities with the same validation for add and edit
public final class FacilityService {
    public static final int MAX_NAME = 50;
    public static final int MAX_DESCRIPTION = 200;

    private FacilityService() {
    }

    private record Input(String name, long typeId, String description) {
    }

    private record Created(long id, String failure) {
    }

    // Adds a facility and its sessions in one transaction
    public static long create(String name, Long typeId, String description) throws ValidationException {
        Input input = validate(name, typeId, description);

        Created created = Tx.run(c -> {
            if (FacilityTypeDao.lockShared(c, input.typeId()).isEmpty()) {
                return new Created(0, "p39");
            }

            long id = FacilityDao.insert(c, input.name(), input.typeId(), input.description());
            ScheduleService.generateSlots(c, id, Clock.today(), ScheduleService.SLOT_DAYS);

            return new Created(id, null);
        });

        if (created.failure() != null) {
            throw new ValidationException(created.failure());
        }

        return created.id();
    }

    // Changes the details only and leaves the sessions alone
    public static void update(long id, String name, Long typeId, String description) throws ValidationException {
        Input input = validate(name, typeId, description);

        String failure = Tx.run(c -> {
            if (FacilityDao.lockById(c, id).isEmpty()) {
                return "p01";
            }

            if (FacilityTypeDao.lockShared(c, input.typeId()).isEmpty()) {
                return "p39";
            }

            FacilityDao.update(c, id, input.name(), input.typeId(), input.description());

            return null;
        });

        if (failure != null) {
            throw new ValidationException(failure);
        }
    }

    // Deletes the facility and its sessions unless any booking ever used one of them
    public static void delete(long id) throws ValidationException {
        String failure = Tx.run(c -> {
            if (FacilityDao.lockById(c, id).isEmpty()) {
                return "p01";
            }

            SlotDao.lockIdsByFacilityOrderedById(c, id);

            if (SlotDao.hasBookings(c, id)) {
                return "p37";
            }

            SlotDao.deleteByFacility(c, id);
            FacilityDao.delete(c, id);

            return null;
        });

        if (failure != null) {
            throw new ValidationException(failure);
        }
    }

    private static Input validate(String name, Long typeId, String description) throws ValidationException {
        String cleanName = name == null ? "" : name.trim();

        if (cleanName.isEmpty() || cleanName.length() > MAX_NAME) {
            throw new ValidationException("p38");
        }

        if (typeId == null) {
            throw new ValidationException("p39");
        }

        String cleanDescription = description == null ? null : description.trim();

        if (cleanDescription != null && cleanDescription.isEmpty()) {
            cleanDescription = null;
        }

        if (cleanDescription != null && cleanDescription.length() > MAX_DESCRIPTION) {
            throw new ValidationException("p40");
        }

        return new Input(cleanName, typeId, cleanDescription);
    }
}
