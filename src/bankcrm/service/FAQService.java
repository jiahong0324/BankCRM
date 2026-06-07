package bankcrm.service;

import bankcrm.model.FAQItem;

import java.util.ArrayList;
import java.util.List;

public class FAQService {

    private static final FAQService instance = new FAQService();

    private FAQService() {}

    public static FAQService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public List<FAQItem> getAll() {
        return ds.getFaqItems();
    }

    public List<FAQItem> search(String keyword, String category) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<FAQItem> result = new ArrayList<>();

        for (FAQItem f : ds.getFaqItems()) {
            if (!f.isActive()) {
                continue;
            }
            if (!category.equals("ALL") && !f.getCategory().name().equals(category)) {
                continue;
            }
            if (!kw.isEmpty()) {
                boolean matchQuestion = f.getQuestion().toLowerCase().contains(kw);
                boolean matchAnswer = f.getAnswer().toLowerCase().contains(kw);
                if (!matchQuestion && !matchAnswer) {
                    continue;
                }
            }
            result.add(f);
        }

        return result;
    }

    public String add(String categoryStr, String question, String answer) {
        if (question == null || question.isBlank()) {
            return "Question is required.";
        }
        if (answer == null || answer.isBlank()) {
            return "Answer is required.";
        }

        FAQItem.FAQCategory cat;
        try {
            cat = FAQItem.FAQCategory.valueOf(categoryStr);
        } catch (IllegalArgumentException e) {
            return "Invalid category.";
        }

        ds.addFaqItem(new FAQItem(ds.nextFaqId(), cat, question.trim(), answer.trim()));
        return null;
    }

    public void markHelpful(String id) {
        FAQItem f = ds.findFaqById(id);
        if (f != null) {
            f.incrementHelpful();
        }
    }

    public boolean remove(String id) {
        return ds.removeFaq(id);
    }
}