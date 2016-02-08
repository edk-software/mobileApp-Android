package pl.org.edk.database.services;

import android.content.ContentValues;
import android.database.Cursor;
import pl.org.edk.database.entities.Area;
import pl.org.edk.database.entities.Territory;

import java.util.ArrayList;

/**
 * Created by Admin on 2016-01-28.
 */
public class TerritoryService extends DbServiceBase {
    // ---------------------------------------
    // Insert
    // ---------------------------------------
    public boolean InsertTerritoryWithAreas(Territory territory){
        // Insert the territory
        ContentValues territoryValues = territory .getContentValues();
        long territoryId = dbWrite().insert(Territory.TABLE_NAME, null, territoryValues);
        if(territoryId <= 0)
            return false;
        territory.setId(territoryId);

        for(Area area : territory.getAreas()){
            InsertAreaForTerritory(area, territoryId);
        }
        return true;
    }

    public boolean InsertAreaForTerritory(Area area, long territoryId){
        // Make sure they are linked
        area.setTerritoryId(territoryId);

        // Make the insert
        ContentValues areaValues = area.getContentValues();
        long areaId = dbWrite().insert(Area.TABLE_NAME, null, areaValues);
        if(areaId <= 0)
            return false;

        area.setId(areaId);
        return true;
    }

    // ---------------------------------------
    // Get
    // ---------------------------------------
    /**
     * Gets a list of all Territories, without any additional info.
     * @return
     */
    public ArrayList<Territory> GetTerritories(){
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
    public ArrayList<Territory> GetTerritoriesWithAreas(){
        ArrayList<Territory> territories = GetTerritories();
        for (Territory territory : territories){
            ArrayList<Area> areas = GetAreasForTerritory(territory.getId());
            territory.setAreas(areas);
        }
        return territories;
    }

    public ArrayList<Area> GetAreasForTerritory(long territoryId){
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
}
