package fi.tuni.tiko.busradartampere;

public class BusTagObject {
    private String routeURL;

    public BusTagObject(String routeURL) {
        this.routeURL = routeURL;
    }

    public String getRouteURL() {
        return this.routeURL;
    }
}
