package org.androidcru.crucentralcoast.presentation.views.ridesharing.driversignup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.MapFragment;

import org.androidcru.crucentralcoast.AppConstants;
import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.models.CruUser;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.RideProvider;
import org.androidcru.crucentralcoast.presentation.customviews.CruSupportPlaceAutocompleteFragment;
import org.androidcru.crucentralcoast.presentation.util.DrawableUtil;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.DriverSignupEditingVM;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.DriverSignupVM;
import org.androidcru.crucentralcoast.presentation.views.base.BaseAppCompatActivity;
import org.androidcru.crucentralcoast.util.SharedPreferencesUtil;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.observers.Observers;
import timber.log.Timber;

public class DriverSignupActivity extends BaseAppCompatActivity
{
    private DriverSignupVM driverSignupVM;

    @BindView(R.id.fab) FloatingActionButton fab;
    private CruSupportPlaceAutocompleteFragment autocompleteFragment;

    @BindView(com.google.android.gms.R.id.place_autocomplete_search_input) EditText searchInput;
    private MapFragment mapFragment;

    private CruEvent event;
    private Observer<CruUser> userObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_form);
        //get event from bundle
        Bundle bundle = getIntent().getExtras();
        event = (CruEvent)Parcels.unwrap(bundle.getParcelable(AppConstants.EVENT_KEY));
        if(bundle == null || event == null)
        {
            Timber.e("DriverSignupActivity requires that you pass an event");
            Timber.e("Finishing activity...");
            finish();
            return;
        }

        unbinder = ButterKnife.bind(this);

        setupFab();

        autocompleteFragment = (CruSupportPlaceAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

        String rideId = bundle.getString(AppConstants.RIDE_KEY, "");

        userObserver = Observers.create(cruUser -> {
//            if(cruUser)
//            {
//
//            }
        });

        if (!rideId.isEmpty())
            requestRides(rideId);
        else
            bindNewRideVM(null);
    }

    @OnClick(R.id.autocomplete_layout)
    public void onAutocompleteTouched(View v)
    {
        if(getCurrentFocus() != null)
            getCurrentFocus().clearFocus();
        searchInput.callOnClick();
    }

    //fill in fields that only the DriverSignupActivity has access to but DriverSignupVM doesn't
    private Ride completeRide(Ride r)
    {
        r.gcmID = SharedPreferencesUtil.getGCMID();
        r.eventId = event.id;
        return r;
    }

    private void createDriver()
    {
        RideProvider.createRide(Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void updateDriver()
    {
        RideProvider.updateRide(Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void setupPlacesAutocomplete()
    {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setHint(getString(R.string.autocomplete_hint_driver));
        autocompleteFragment.setOnPlaceSelectedListener(driverSignupVM.createPlaceSelectionListener());
    }

    private void requestRides(String rideId)
    {
        RideProvider.requestRideByID(this,
                Observers.create(
                        ride -> {
                            bindNewRideVM(ride);
                        }
                ), rideId);
    }

    private void bindNewRideVM(Ride r) {
        //new ride
        if (r == null)
            driverSignupVM = new DriverSignupVM(this, getFragmentManager(), event.id, event.startDate, event.endDate);
        //editing an existing ride
        else
            driverSignupVM = new DriverSignupEditingVM(this, getFragmentManager(), r, event.endDate);
        mapFragment.getMapAsync(driverSignupVM.onMapReady());
        setupPlacesAutocomplete();
    }

    private void setupFab()
    {
        fab.setImageDrawable(DrawableUtil.getTintedDrawable(this, R.drawable.ic_check_grey600, android.R.color.white));
        fab.setOnClickListener(v -> {

            validateNumber();

            //if fields are valid, update shared preferences and the Ride
            if(driverSignupVM.validator.validate() && autocompleteFragment.validate())
            {
                SharedPreferencesUtil.writeBasicInfo(driverSignupVM.nameField.getText().toString(), null, driverSignupVM.phoneField.getText().toString());

                if(driverSignupVM instanceof DriverSignupEditingVM)
                    updateDriver();
                else
                    createDriver();

                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void validateNumber()
    {

    }
}
