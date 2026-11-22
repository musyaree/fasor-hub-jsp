package app.fasorhub.web;

import app.fasorhub.dao.FacilityDao;
import app.fasorhub.dao.FacilityTypeDao;
import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Facility;
import app.fasorhub.model.FacilityRow;
import app.fasorhub.model.FacilityType;
import app.fasorhub.model.FacilityTypeRow;
import app.fasorhub.service.FacilityService;
import app.fasorhub.service.FacilityTypeService;
import app.fasorhub.util.Tx;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({"/manage-facilities", "/manage-facilities/save", "/manage-facilities/delete", "/manage-facilities/types/save",
        "/manage-facilities/types/delete"})
public class FacilityServlet extends BaseServlet {
    private static final String PAGE = "/manage-facilities";
    private static final String TYPES_PAGE = "/manage-facilities?types";

    private static final String FORM = "form";
    private static final String DELETE = "delete";
    private static final String TYPES = "types";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long editId = paramLong(req, "edit");
        Long deleteId = paramLong(req, "delete");
        Long editTypeId = paramLong(req, "editType");

        String dialog = null;

        if (req.getParameter("add") != null) {
            dialog = FORM;
        } else if (editId != null) {
            Facility editing = Tx.run(c -> FacilityDao.find(c, editId).orElse(null));

            if (editing != null) {
                req.setAttribute("formId", editing.getId());
                req.setAttribute("formName", editing.getName());
                req.setAttribute("formType", editing.getType().getId());
                req.setAttribute("formDescription", editing.getDescription());
                dialog = FORM;
            }
        } else if (deleteId != null) {
            dialog = DELETE;
        } else if (req.getParameter("types") != null || editTypeId != null) {
            dialog = TYPES;

            FacilityType renaming = editTypeId == null ? null : Tx.run(c -> FacilityTypeDao.find(c, editTypeId).orElse(null));

            if (renaming != null) {
                req.setAttribute("typeId", renaming.getId());
                req.setAttribute("typeName", renaming.getName());
            }
        }

        show(req, resp, dialog, deleteId);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if (path.endsWith("/types/save")) {
            saveType(req, resp);
        } else if (path.endsWith("/types/delete")) {
            deleteType(req, resp);
        } else if (path.endsWith("/delete")) {
            delete(req, resp);
        } else {
            save(req, resp);
        }
    }

    private void save(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = paramLong(req, "id");
        String name = param(req, "name");
        Long typeId = paramLong(req, "type");
        String description = param(req, "description");

        try {
            if (id == null) {
                FacilityService.create(name, typeId, description);
                flashSuccess(req, msg("p34"));
            } else {
                FacilityService.update(id, name, typeId, description);
                flashSuccess(req, msg("p35"));
            }

            redirect(req, resp, PAGE);
        } catch (ValidationException e) {
            if ("p01".equals(e.getMessageKey())) {
                flashWarning(req, msg("p01"));
                redirect(req, resp, PAGE);
                return;
            }

            req.setAttribute("errorKey", e.getMessageKey());
            req.setAttribute("formId", id);
            req.setAttribute("formName", name);
            req.setAttribute("formType", typeId);
            req.setAttribute("formDescription", description);
            show(req, resp, FORM, null);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = paramLong(req, "id");

        if (id != null) {
            try {
                FacilityService.delete(id);
                flashSuccess(req, msg("p36"));
            } catch (ValidationException e) {
                flashError(req, msg(e.getMessageKey()));
            }
        }

        redirect(req, resp, PAGE);
    }

    private void saveType(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = paramLong(req, "id");
        String name = param(req, "name");

        try {
            if (id == null) {
                FacilityTypeService.create(name);
                flashSuccess(req, msg("p44"));
            } else {
                FacilityTypeService.rename(id, name);
                flashSuccess(req, msg("p45"));
            }

            redirect(req, resp, TYPES_PAGE);
        } catch (ValidationException e) {
            if ("p01".equals(e.getMessageKey())) {
                flashWarning(req, msg("p01"));
                redirect(req, resp, TYPES_PAGE);
                return;
            }

            req.setAttribute("typeErrorKey", e.getMessageKey());
            req.setAttribute("typeId", id);
            req.setAttribute("typeName", name);
            show(req, resp, TYPES, null);
        }
    }

    private void deleteType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = paramLong(req, "id");

        if (id != null) {
            try {
                FacilityTypeService.delete(id);
                flashSuccess(req, msg("p46"));
            } catch (ValidationException e) {
                flashError(req, msg(e.getMessageKey()));
            }
        }

        redirect(req, resp, TYPES_PAGE);
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, String dialog, Long deleteId) throws ServletException, IOException {
        List<FacilityRow> facilities = Tx.run(FacilityDao::findAllWithCounts);
        List<FacilityTypeRow> types = Tx.run(FacilityTypeDao::findAllWithCounts);

        FacilityRow toDelete = facilities.stream().filter(f -> deleteId != null && f.getFacility().getId() == deleteId).findFirst().orElse(null);

        req.setAttribute("facilities", facilities);
        req.setAttribute("types", types);
        req.setAttribute("dialog", DELETE.equals(dialog) && toDelete == null ? null : dialog);
        req.setAttribute("toDelete", toDelete);

        render(req, resp, "manage-facilities.jsp");
    }
}
