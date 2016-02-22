package pl.org.edk.database.services;

import android.database.Cursor;
import pl.org.edk.database.entities.Area;
import pl.org.edk.database.entities.DbEntityBase;
import pl.org.edk.database.entities.Territory;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * Created by pwawrzynek on 2016-01-28.
 */
public class TerritoryService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean insertTerritoryWithAreas(Territory territory){
        // Insert the territory
        long territoryId = executeQueryInsert(territory);
        if(territoryId <= 0)
            return false;
        territory.setId(territoryId);

        // Add areas
        if(territory.getAreas() != null) {
            for (Area area : territory.getAreas()) {
                area.setTerritoryId(territoryId);
                insertArea(area);
            }
        }
        return true;
    }

    public boolean insertArea(Area area){
        if(area.getTerritoryId() <= 0){
            throw new InvalidParameterException("Specified Area is not linked with any Territory!");
        }

        // Make the insert
        long areaId = executeQueryInsert(area);
        if(areaId <= 0)
            return false;

        area.setId(areaId);
        return true;
    }

    // ---------------------------------------
    // Update
    // ---------------------------------------

    /**
     * Updates the specified Territory in the database based on it's ServerId.
     * Al of the linked Areas are updated as well. If any of the entities doesn't exist in the DB, it's inserted.
     * @param territory Territory to be updated (its serverId has to be a positive integer)
     * @return Update result
     */
    public boolean updateTerritoryWithAreasByServerId(Territory territory){
        if(territory.getServerID() <= 0){
            throw new InvalidParameterException("Specified Territory ServerId is not a positive value!");
        }

        Territory previous = getTerritoryByServerId(territory.getServerID());
        if(previous == null){
            return insertTerritoryWithAreas(territory);
        }

        territory.setId(previous.getId());
        if(executeQueryUpdate(territory) > 0){
            for(Area area : territory.getAreas()){
                updateAreaByServerId(area);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean updateAreaByServerId(Area area){
        if(area.getTerritoryId() <= 0){
            throw new InvalidParameterException("Specified Area is not linked with any Territory!");
        }
        if(area.getServerID() <= 0){
            throw new InvalidParameterException("Specified Area ServerId is not a positive value!");
        }

        Area previous = getAreaByServerId(area.getServerID());
        if(previous == null) {
            return insertArea(area);
        }

        area.setId(previous.getId());
        return executeQueryUpdate(area) > 0;
    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    public Territory getTerritory(long territoryId){
        Cursor cursor = executeQueryWhere(Territory.TABLE_NAME, Territory.getFullProjection(),
                Territory._ID, String.valueOf(territoryId));

        // Nothing found
        if(cursor.getCount() == 0) {
            return null;
        }

        // Sth found
        cursor.moveToFirst();
        Territory territory = new Territory();
        if(!territory.readFromCursor(cursor)) {
            return null;
        }

        // Fetch it's areas
        territory.setAreas(getAreasForTerritory(territory.getId()));
        return territory;
    }

    public Territory getTerritoryByServerId(long serverId){
        Cursor cursor = executeQueryWhere(Territory.TABLE_NAME, Territory.getFullProjection(),
                DbEntityBase.COLUMN_NAME_SERVER_ID, String.valueOf(serverId));

        // Nothing found
        if(cursor.getCount() == 0) {
            return null;
        }

        // Sth found
        cursor.moveToFirst();
        Territory nextTerritory = new Territory();
        if(nextTerritory.readFromCursor(cursor)) {
            return nextTerritory;
        }
        else {
            return null;
        }
    }

    /**
     * Gets a list of all Territories, without any additional info.
     * @return
     */
    public ArrayList<Territory> getTerritories(){
        Cursor cursor = executeQueryGetAll(Territory.TABLE_NAME, Territory.getFullProjection());

        ArrayList<Territory> territories = new ArrayList<Territory>();
        for(int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            Territory nextTerritory = new Territory();
            if(nextTerritory.readFromCursor(cursor))
                territories.add(nextTerritory);
        }
        return territories;
    }

    /**
     * Gets a list of all Territories with their Areas.
     * @return
     */
    public ArrayList<Territory> getTerritoriesWithAreas(){
        ArrayList<Territory> territories = getTerritories();
        for (Territory territory : territories){
            ArrayList<Area> areas = getAreasForTerritory(territory.getId());
            territory.setAreas(areas);
        }
        return territories;
    }

    public ArrayList<Area> getAreasForTerritory(long territoryId){
        Cursor cursor = executeQueryWhere(Area.TABLE_NAME, Area.getFullProjection(),
                Area.COLUMN_NAME_TERRITORY_ID, String.valueOf(territoryId));

        ArrayList<Area> areas = new ArrayList<>();
        for (int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            Area nextArea = new Area();
            if(nextArea.readFromCursor(cursor))
                areas.add(nextArea);
        }
        return areas;
    }

    public Area getArea(long areaId){
        Cursor cursor = executeQueryWhere(Area.TABLE_NAME, Area.getFullProjection(),
                DbEntityBase._ID, String.valueOf(areaId));

        // Nothing found
        if(cursor.getCount() == 0) {
            return null;
        }

        // Sth found
        cursor.moveToFirst();
        Area nextArea = new Area();
        if(nextArea.readFromCursor(cursor)) {
            return nextArea;
        }
        else {
            return null;
        }
    }

    public Area getAreaByServerId(long serverId) {
        Cursor cursor = executeQueryWhere(Area.TABLE_NAME, Area.getFullProjection(),
                DbEntityBase.COLUMN_NAME_SERVER_ID, String.valueOf(serverId));

        // Nothing found
        if(cursor.getCount() == 0) {
            return null;
        }

        // Sth found
        cursor.moveToFirst();
        Area nextArea = new Area();
        if(nextArea.readFromCursor(cursor)) {
            return nextArea;
        }
        else {
            return null;
        }
    }
}
