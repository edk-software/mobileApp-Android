package pl.org.edk.EdkMobile.Entities;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Pawel on 2015-02-25.
 */
public class CrossStation implements Parcelable {
    private int stationNumber;
    private String title;
    private String displayName;
    private String description;
    private String reflections;

    private double latitude;
    private double longitude;
    private boolean isValid;

    private boolean isAchieved;
    private double distanceDone;
    private double distanceLeft;

    // Constructors ================
    public CrossStation(int stationNumber, String title, String displayName, String description, double latitude, double longitude,
                        boolean isValid, double distanceDone, double distanceLeft) {
        this.stationNumber = stationNumber;
        this.title = title;
        this.displayName = displayName;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isValid = isValid;
        this.isAchieved = false;
        this.distanceDone = distanceDone;
        this.distanceLeft = distanceLeft;
    }

    public CrossStation(int stationNumber, String title, String displayName, String description, double latitude, double longitude,
                        double distanceDone, double distanceLeft){
        this(stationNumber, title, displayName, description, latitude, longitude, true, distanceDone, distanceLeft);
    }

    private CrossStation(Parcel in){
        stationNumber = in.readInt();
        title = in.readString();
        displayName = in.readString();
        description = in.readString();
        reflections = in.readString();

        latitude = in.readDouble();
        longitude = in.readDouble();
        isValid = in.readByte() != 0;
        isAchieved = in.readByte() != 0;
        distanceDone = in.readDouble();
        distanceLeft = in.readDouble();
    }
    // =============================

    // Getters ================
    public int getStationNumber() {
        return stationNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getReflections() {
        return reflections;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isAchieved() {
        return isAchieved;
    }

    public double getDistanceDone() {
        return distanceDone;
    }

    public double getDistanceLeft() {
        return distanceLeft;
    }
    // =======================

    // Setters ===============
    public void setAchieved(boolean isReached) {
        this.isAchieved = isReached;
    }
    // =======================

    // Helper methods ========
    public Intent toGpsIntent() {
        String intentContent = "google.navigation:q=" + latitude + "," + longitude + "&mode=w";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentContent));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
    // =======================

    public static final Creator<CrossStation> CREATOR = new Creator<CrossStation>(){
        public CrossStation createFromParcel(Parcel in){
            return new CrossStation(in);
        }
        public CrossStation[] newArray(int size){
            return new CrossStation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stationNumber);
        dest.writeString(this.title);
        dest.writeString(this.displayName);
        dest.writeString(this.description);
        dest.writeString(this.reflections);

        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeByte((byte) (isValid ? 1 : 0));
        dest.writeByte((byte) (isAchieved ? 1 : 0));
        dest.writeDouble(distanceDone);
        dest.writeDouble(distanceLeft);
    }
}