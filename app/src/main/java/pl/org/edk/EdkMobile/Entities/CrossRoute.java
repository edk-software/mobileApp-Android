package pl.org.edk.EdkMobile.Entities;

import java.util.List;

/**
 * Created by Pawel on 2015-03-07.
 */
public class CrossRoute {
    private String location;
    private String createDate;
    private String contactNumber;
    private List<CrossStation> stations;

    public CrossRoute(String location, String createDate, List<CrossStation> stations) {
        this.location = location;
        this.createDate = createDate;
        this.stations = stations;
    }

    public String getLocation() {
        return location;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public List<CrossStation> getStations() {
        return stations;
    }
}
