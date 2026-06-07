package bankcrm.service;

import bankcrm.model.CannedResponse;

import java.util.ArrayList;
import java.util.List;

public class CannedResponseService {

    private static final CannedResponseService instance = new CannedResponseService();

    private CannedResponseService() {}

    public static CannedResponseService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public List<CannedResponse> getAll() {
        return ds.getCannedResponses();
    }

    public List<CannedResponse> getByCategory(String category) {
        List<CannedResponse> result = new ArrayList<>();
        for (CannedResponse cr : ds.getCannedResponses()) {
            if (cr.isActive() && (cr.getCategory().equals("ALL") || cr.getCategory().equals(category))) {
                result.add(cr);
            }
        }
        return result;
    }

    public String add(String title, String body, String category) {
        if (title == null || title.isBlank()) {
            return "Title is required.";
        }
        if (body == null || body.isBlank()) {
            return "Body is required.";
        }

        String cat = (category == null || category.isBlank()) ? "GENERAL" : category;
        ds.addCannedResponse(new CannedResponse(ds.nextCannedId(), title.trim(), body.trim(), cat));
        return null;
    }

    public boolean remove(String id) {
        return ds.removeCannedResponse(id);
    }
}