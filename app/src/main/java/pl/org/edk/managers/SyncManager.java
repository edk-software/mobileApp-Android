package pl.org.edk.managers;

import android.content.Context;

/**
 * Created by pwawrzynek on 2016-02-06.
 */
public class SyncManager {
    // ---------------------------------------
    // Class members
    // ---------------------------------------
    private Context mContext;
    private static SyncManager instance;

    // ---------------------------------------
    // Singleton
    // ---------------------------------------
    private SyncManager(Context context){
        this.mContext = context;
    }

    private static synchronized SyncManager get(Context applicationContext){
        if(instance == null)
            instance = new SyncManager(applicationContext);
        return instance;
    }

    public static synchronized SyncManager getInstance(Context context){
        return get(context.getApplicationContext());
    }

    // ---------------------------------------
    // Public methods
    // ---------------------------------------
    public void SyncReflections(){
        
    }
}
