package org.androidcru.crucentralcoast.presentation.views.ridesharing.myrides;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Passenger;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.PassengerProvider;
import org.androidcru.crucentralcoast.data.providers.RideProvider;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.MyRidesDriverVM;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by main on 2/27/2016.
 */
public class MyRidesInfoActivity extends AppCompatActivity {

    public final static String DATE_FORMATTER = "EEEE MMMM ee,";
    public final static String TIME_FORMATTER = "h:mm a";

    private LinearLayoutManager layoutManager;
//    private Observer<List<Passenger>> passengerSubscriber;
//    private ArrayList<Passenger> passengers;
    private Ride ride;

    //Injected Views
    @Bind(R.id.event_list) RecyclerView eventList;
    @Bind(R.id.event_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.eventName) EditText eventName;
    @Bind(R.id.ride_time) EditText rideTime;
    @Bind(R.id.departureLoc) EditText departureLoc;

    private void setupUI() {
        //TODO: query for event to access event name and image
        eventName.setText(ride.eventId);
        rideTime.setText(ride.time.format(DateTimeFormatter.ofPattern(DATE_FORMATTER))
                + " " + ride.time.format(DateTimeFormatter.ofPattern(TIME_FORMATTER)));
        departureLoc.setText(ride.location.toString());
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//    {
//        super.onCreateView(inflater, container, savedInstanceState);
//        return inflater.inflate(R.layout.fragment_events, container, false);
//    }

//    @Override
//    public void onCreate(View view, @Nullable Bundle savedInstanceState)
//    {
//        super.onViewCreated(view, savedInstanceState);
//
//        //Let ButterKnife find all injected views and bind them to member variables
//        ButterKnife.bind(this, view);
//
//        //Enables actions in the Activity Toolbar (top-right buttons)
//        //setHasOptionsMenu(true);
//
//        //LayoutManager for RecyclerView
//        layoutManager = new LinearLayoutManager(getActivity());
//        eventList.setLayoutManager(layoutManager);
//
//        //Adapter for RecyclerView
//        MyRidesDriverAdapter rideSharingAdapter = new MyRidesDriverAdapter(new ArrayList<>(), layoutManager);
//        eventList.setAdapter(rideSharingAdapter);
//        eventList.setHasFixedSize(true);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Let ButterKnife find all injected views and bind them to member variables
        ButterKnife.bind(this);
        ride = CruApplication.gson.fromJson(getIntent().getExtras().getString("ride"), Ride.class);
        setupUI();

        //Enables actions in the Activity Toolbar (top-right buttons)
        //setHasOptionsMenu(true);

        //LayoutManager for RecyclerView
        layoutManager = new LinearLayoutManager(this);
        eventList.setLayoutManager(layoutManager);

        //Adapter for RecyclerView
        MyRidesInfoAdapter rideSharingAdapter = new MyRidesInfoAdapter(this, ride.passengers);
        eventList.setAdapter(rideSharingAdapter);
        eventList.setHasFixedSize(true);
    }

//    public void setPassengers(List<Passenger> passengerList)
//    {
//        passengers.clear();
//        rx.Observable.from(passengerList)
//                .subscribeOn(Schedulers.immediate())
//                .subscribe(passengers::add);
//
//        eventList.setAdapter(new MyRidesInfoAdapter(getContext(), passengers));
//        swipeRefreshLayout.setRefreshing(false);
//    }
}
