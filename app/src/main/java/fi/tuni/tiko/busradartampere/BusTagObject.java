package fi.tuni.tiko.busradartampere;

/**
 * Object-class for markers to store as Tag
 *
 * @author Mikko Mustasaari
 * @version 2019.0422
 * @since 1.0
 */

public class BusTagObject {

    /**
     * Stores url for route information
     */

    private String routeURL;

    /**
     * Contructor for object
     * @param routeURL an url containing route information
     */

    public BusTagObject(String routeURL) {
        this.routeURL = routeURL;
    }

    /**
     * Getter for route url
     * @return routeUrl
     */

    public String getRouteURL() {
        return this.routeURL;
    }
}