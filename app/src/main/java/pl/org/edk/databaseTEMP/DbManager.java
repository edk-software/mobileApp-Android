package pl.org.edk.databaseTEMP;

import android.content.Context;
import pl.org.edk.databaseTEMP.servicesTEMP.*;

/**
 * Created by pwawrzynek on 2015-12-15.
 */
public class DbManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private static DbManager _instance;
    private Context mContext;

    private boolean initialized = false;

    private DbHelper dbClient;

    /* NOTE: Put all services' entities here */
    private ReflectionService reflectionService;
    private RouteService routeService;
    private TerritoryService territoryService;

    // ---------------------------------------
    // Singleton
    // ---------------------------------------
    private DbManager(Context context){
        this.mContext = context;
    }

    private static synchronized DbManager get(Context applicationContext){
        if(_instance == null){
            _instance = new DbManager(applicationContext);
            _instance.Init();
        }
        return _instance;
    }

    public static synchronized DbManager getInstance(Context context) {
        return get(context.getApplicationContext());
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
    // Public methods
    // ---------------------------------------
    public void Init(){
        if(initialized)
            return;

        // initialize the main client and pass it to all future db services
        this.dbClient = new DbHelper(mContext);
        DbServiceBase.Init(this.dbClient);

        /* NOTE: initialize all services here */
        reflectionService = new ReflectionService();
        routeService = new RouteService();
        territoryService = new TerritoryService();

        initialized = true;
    }

    /**
     * Removed the database. It will be recreated before the first usage
     */
    public void Reset(){
        dbClient.delete(mContext);
    }
}
