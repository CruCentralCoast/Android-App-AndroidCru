package org.androidcru.crucentralcoast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;

import com.anupcowkur.reservoir.Reservoir;
import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import org.aaronhe.threetengson.ThreeTenGsonAdapter;
import org.androidcru.crucentralcoast.data.converters.DirectionConverter;
import org.androidcru.crucentralcoast.data.converters.ResourceTypeConverter;
import org.androidcru.crucentralcoast.data.models.Resource;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.notifications.RegistrationIntentService;

import okhttp3.OkHttpClient;

public class CruApplication extends MultiDexApplication
{


    public static Gson gson;
    public static OkHttpClient okHttpClient;
    private static Context context;
    private static SharedPreferences sharedPreferences;

    public static Context getContext()
    {
        return context;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;

        Logger.init().setLogLevel(LogLevel.values()[BuildConfig.LOG_LEVEL]);

        Stetho.initializeWithDefaults(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        FacebookSdk.sdkInitialize(context);

        setupDiskCache();
        setupGson();
        setupOkHttpClient();

        if (checkPlayServices())
        {
            // Start IntentService to register this application with GCM.
            Intent service = new Intent(this, RegistrationIntentService.class);
            startService(service);
        }
    }

    private void setupOkHttpClient()
    {
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }

    /**
     * Important because it's essential that SharedPreferences doesn't get overwritten
     * @return
     */
    public static SharedPreferences getSharedPreferences()
    {
        return sharedPreferences;
    }


    /**
     * Determines if the current device is enabled for google play services and if not, will prompt
     * the user to enable it.
     *
     * @return true if enabled, otherwise false
     */
    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        sharedPreferences.edit().putInt(AppConstants.PLAY_SERVICES, resultCode).apply();
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void setupDiskCache()
    {
        try
        {
            Reservoir.init(this, 10240, gson); //in bytes
        } catch (Exception e)
        {
            Logger.e(e, "Not enough space for disk cache!");
        }
    }

    private void setupGson()
    {
        GsonBuilder builder = new GsonBuilder();
        builder = ThreeTenGsonAdapter.registerAll(builder);
        builder.registerTypeAdapter(Ride.Direction.class, new DirectionConverter());
        builder.registerTypeAdapter(Resource.ResourceType.class, new ResourceTypeConverter());
        gson = builder.create();
    }

    public static void saveGCMID(String key)
    {
        CruApplication.getSharedPreferences().edit().putString(context.getString(R.string.gcm_registration_id), key).apply();
    }

    //TODO apparently these can change.
    public static String getGCMID()
    {
        return CruApplication.getSharedPreferences().getString(context.getString(R.string.gcm_registration_id), "");
    }

}
