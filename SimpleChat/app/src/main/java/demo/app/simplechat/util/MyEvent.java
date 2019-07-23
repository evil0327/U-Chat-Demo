package demo.app.simplechat.util;

public class MyEvent {
    public static final String NOTIFY_USER_CHANGE = "notify_user_change";

    private String event;

    public MyEvent(String event){
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
