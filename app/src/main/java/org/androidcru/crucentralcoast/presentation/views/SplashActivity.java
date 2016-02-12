package org.androidcru.crucentralcoast.presentation.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.presentation.views.subscriptions.SubscriptionStartupActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity
{
    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_splash);

        this.mSharedPreferences = CruApplication.getSharedPreferences();
        switchToApp();
    }

    public void switchToApp()
    {
        final Intent intent = new Intent(this, MainActivity.class);

        // Determine if the app has launched before.
        if (mSharedPreferences.getBoolean(CruApplication.FIRST_LAUNCH, false))
            intent.setClass(this, MainActivity.class);
        else
            intent.setClass(this, SubscriptionStartupActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final long DELAY_SECONDS = 2l;

        Observable.timer(DELAY_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
    }
}