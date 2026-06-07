package bankcrm.model;

public class FAQItem {

    public enum FAQCategory {
        ACCOUNT, CARD, LOAN, TRANSACTION, GENERAL, SECURITY
    }

    private String id;
    private FAQCategory category;
    private String question;
    private String answer;
    private int helpfulCount;
    private boolean active;
    
    public FAQItem() {
        this.id = "";
        this.category = FAQCategory.GENERAL;
        this.question = "";
        this.answer = "";
        this.helpfulCount = 0;
        this.active = true;
    }

    public FAQItem(String id, FAQCategory category, String question, String answer) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.helpfulCount = 0;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public FAQCategory getCategory() {
        return category;
    }

    public void setCategory(FAQCategory category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void incrementHelpful() {
        this.helpfulCount++;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}