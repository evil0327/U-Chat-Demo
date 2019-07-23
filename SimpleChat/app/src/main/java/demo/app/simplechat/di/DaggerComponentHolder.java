package demo.app.simplechat.di;

public class DaggerComponentHolder {
    private static MyDaggerComponent appComponent;

    public static void setAppComponent(MyDaggerComponent component) {
        appComponent = component;
    }

    public static MyDaggerComponent getAppComponent() {
        return appComponent;
    }
}
