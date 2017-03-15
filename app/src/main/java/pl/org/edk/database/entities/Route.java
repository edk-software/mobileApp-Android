package pl.org.edk.database.entities;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import pl.org.edk.util.NumConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class Route extends DbEntityBase {
    // ---------------------------------------
    // Constant variables
    // ---------------------------------------
    public static final String TABLE_NAME = "Route";
    public static final String COLUMN_NAME_AREA_ID = "AreaID";
    public static final String COLUMN_NAME_RELEASE_DATE = "ReleaseDate";
    public static final String COLUMN_NAME_DISPLAY_NAME = "Name";
    public static final String COLUMN_NAME_KML_DATA_PATH = "KmlDataPath";
    public static final String COLUMN_NAME_IS_CURRENT = "IsCurrent";

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    public Route() {
        descriptions = new ArrayList<>();
        stations = new ArrayList<>();
    }

    public Route(int areaId, String releaseDate, String name) {
        this();
        this.areaId = areaId;
        this.releaseDate = releaseDate;
        this.name = name;
    }

    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private long areaId;
    private String releaseDate;
    private String name;
    private String kmlDataPath;
    @SerializedName("currentYear")
    private boolean isCurrent;
    // External tables
    private ArrayList<RouteDesc> descriptions;
    private ArrayList<Station> stations;
    // Additional variables
    private transient String kmlData;

    // ---------------------------------------
    // Static methods
    // ---------------------------------------
    public static String getCreateEntries() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA +
                COLUMN_NAME_SERVER_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_AREA_ID + INTEGER_TYPE + COMMA +
                COLUMN_NAME_RELEASE_DATE + TEXT_TYPE + COMMA +
                COLUMN_NAME_DISPLAY_NAME + TEXT_TYPE + COMMA +
                COLUMN_NAME_KML_DATA_PATH + TEXT_TYPE + COMMA +
                COLUMN_NAME_IS_CURRENT + INTEGER_TYPE + ");";
    }

    public static String getDeleteEntries() {
        return "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static String[] getFullProjection() {
        String[] projection = {
                _ID,
                COLUMN_NAME_SERVER_ID,
                COLUMN_NAME_AREA_ID,
                COLUMN_NAME_RELEASE_DATE,
                COLUMN_NAME_DISPLAY_NAME,
                COLUMN_NAME_KML_DATA_PATH,
                COLUMN_NAME_IS_CURRENT
        };
        return projection;
    }

    public static String[] getUpdateTwo() {
        String[] queries = new String[2];
        queries[0] = "ALTER TABLE " + Route.TABLE_NAME + " ADD " +
                Route.COLUMN_NAME_IS_CURRENT + DbEntityBase.INTEGER_TYPE;
        queries[1] = "UPDATE " + Route.TABLE_NAME + " SET " +
                Route.COLUMN_NAME_IS_CURRENT + "=0;";
        return queries;
    }

    // ---------------------------------------
    // Base class methods
    // ---------------------------------------

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME_SERVER_ID, serverID);
        values.put(COLUMN_NAME_AREA_ID, areaId);
        values.put(COLUMN_NAME_RELEASE_DATE, releaseDate);
        values.put(COLUMN_NAME_DISPLAY_NAME, name);
        values.put(COLUMN_NAME_KML_DATA_PATH, kmlDataPath);
        values.put(COLUMN_NAME_IS_CURRENT, isCurrent ? 1 : 0);

        return values;
    }

    @Override
    public boolean readFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            this.serverID = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_SERVER_ID));
            this.areaId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_AREA_ID));
            this.releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_RELEASE_DATE));
            this.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DISPLAY_NAME));
            this.kmlDataPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_KML_DATA_PATH));
            this.isCurrent = (cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NAME_IS_CURRENT)) == 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    // ---------------------------------------
    // Getters and setters
    // ---------------------------------------
    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public Date getReleaseDate() {
        return NumConverter.stringToDate(this.releaseDate);
    }

    public void setReleaseDate(Date date) {
        this.releaseDate = NumConverter.dateToString(date);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKmlDataPath() {
        return kmlDataPath;
    }

    public void setKmlDataPath(String kmlDataPath) {
        this.kmlDataPath = kmlDataPath;
        this.kmlData = null;
    }

    public ArrayList<RouteDesc> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<RouteDesc> descriptions) {
        this.descriptions = descriptions;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public void setStations(ArrayList<Station> stations) {
        this.stations = stations;
    }

    public String getKmlData() {
        if (kmlData == null && kmlDataPath != null) {
            kmlData = readKml(kmlDataPath);
        }
        return kmlData;
    }

    public boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public boolean isDownloaded() {
        return getKmlData() != null;
    }

    // ---------------------------------------
    // Private methods
    // ---------------------------------------
    private String readKml(String path) {
        File kmlFile = new File(path);
        if (!kmlFile.exists()) {
            return null;
        }

        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(kmlFile));

            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            return text.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
