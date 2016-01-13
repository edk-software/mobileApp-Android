package pl.org.edk.Database;

import android.content.Context;
import pl.org.edk.Database.Services.DbServiceBase;
import pl.org.edk.Database.Services.ReflectionService;
import pl.org.edk.Database.Services.RouteService;

import java.util.ArrayList;

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
    // Methods
    // ---------------------------------------
    public void Init(Context context){
        // Initialize the main client and pass it to all future db services
        this.dbClient = new DbHelper(context);
        DbServiceBase.Init(this.dbClient);

        /* NOTE: Initialize all services here */
        reflectionService = new ReflectionService();
        routeService = new RouteService();

        initialized = true;
    }
}
