package pl.org.edk.database;

import android.content.Context;
import pl.org.edk.database.Services.DbServiceBase;
import pl.org.edk.database.Services.ReflectionService;
import pl.org.edk.database.Services.RouteService;
import pl.org.edk.database.Services.TerritoryService;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class DbManager {
    // ---------------------------------------
    // Class variables
    // ---------------------------------------
    private boolean initialized = false;

    private DbHelper dbClient;

    /* NOTE: Put all services' entities here */
    private ReflectionService reflectionService;
    private RouteService routeService;
    private TerritoryService territoryService;

    // ---------------------------------------
    // Constructors
    // ---------------------------------------
    private DbManager(){}

    private static DbManager _instance;
    public static DbManager getInstance(){
        if(_instance == null)
            _instance = new DbManager();
        return _instance;
    }

    // ---------------------------------------
    // Getters and setters
    // ---------------------------------------
    public ReflectionService getReflectionService() {
        return reflectionService;
    }

    public RouteService getRouteService() {
        return routeService;
    }

    public TerritoryService getTerritoryService() {
        return territoryService;
    }

    // ---------------------------------------
    // Methods
    // ---------------------------------------
    public void Init(Context context){
        if(initialized)
            return;

        // Initialize the main client and pass it to all future db services
        this.dbClient = new DbHelper(context);
        DbServiceBase.Init(this.dbClient);

        /* NOTE: Initialize all services here */
        reflectionService = new ReflectionService();
        routeService = new RouteService();
        territoryService = new TerritoryService();

        initialized = true;
    }

    /**
     * Removed the database. It will be recreated before the first usage
     */
    public void Reset(Context context){
        dbClient.delete(context);
    }
}
