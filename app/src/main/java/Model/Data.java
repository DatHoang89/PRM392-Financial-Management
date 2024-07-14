package Model;
public class Data {
    private String id;
    private String type;
    private String note;
    private String date;
    private int amount;

    // Default constructor required for calls to DataSnapshot.getValue(Data.class)
    public Data() {}

    public Data( int amount,String type, String note,String id, String date) {
        this.type = type;
        this.note = note;
        this.date = date;
        this.amount = amount;
        this.id=id;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getDate() {
        return date;
    }

    public int getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }
}