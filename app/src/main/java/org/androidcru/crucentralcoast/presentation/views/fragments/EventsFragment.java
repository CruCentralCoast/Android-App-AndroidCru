package org.androidcru.crucentralcoast.presentation.views.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.providers.CruEventsProvider;
import org.androidcru.crucentralcoast.presentation.views.adapters.EventsAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class EventsFragment extends Fragment
{
    //Injected Views
    @Bind(R.id.event_list) RecyclerView mEventList;
    @Bind(R.id.event_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    //View elements
    private LinearLayoutManager mLayoutManager;

    private Subscriber<ArrayList<CruEvent>> subscriber;

    public EventsFragment()
    {
        subscriber = new Subscriber<ArrayList<CruEvent>>()
        {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e)
            {
                Logger.e(e, "CruEvents failed to retrieve.");
            }

            @Override
            public void onNext(ArrayList<CruEvent> cruEvents)
            {
                setEvents(cruEvents);
            }
        };
    }

    /**
     * Invoked early on from the Android framework during rendering.
     * @param inflater Object used to inflate new views, provided by Android
     * @param container Parent view to inflate in, provided by Android
     * @param savedInstanceState State of the application if it is being refreshed, given to Android by dev
     * @return inflated View
     */
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    /**
     * Invoked after onCreateView() and deals with binding view references after the
     * view has already been inflated.
     * @param view Inflated View created by onCreateView()
     * @param savedInstanceState State of the application if it is being refreshed, given to Android by dev
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Let ButterKnife find all injected views and bind them to member variables
        ButterKnife.bind(this, view);

        //Enables actions in the Activity Toolbar (top-right buttons)
        setHasOptionsMenu(true);

        //LayoutManager for RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        mEventList.setLayoutManager(mLayoutManager);

        //Adapter for RecyclerView
        EventsAdapter mEventAdapter = new EventsAdapter(new ArrayList<>(), mLayoutManager);
        mEventList.setAdapter(mEventAdapter);
        mEventList.setHasFixedSize(true);

        //Set up SwipeRefreshLayout
        mSwipeRefreshLayout.setColorSchemeColors(R.color.cruDarkBlue, R.color.cruGold, R.color.cruOrange);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceUpdate();
            }
        });

        getCruEvents();


    }

    /**
     * Inovoked by the Android framework if setHasOptionsMenu() is called
     * @param menu Reference to Menu, provided by Android
     * @param inflater Inflater object, provided by Android
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_events, menu);
    }

    /**
     * Click listener for when actions in the Toolbar are clicked
     * @param item Item clicked, provided by Android
     * @return True to consume the touch event or false to allow Android to handle it
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        switch(itemId)
        {
            case R.id.action_refresh:
                forceUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void forceUpdate()
    {
        CruEventsProvider.getInstance().forceUpdate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void getCruEvents()
    {
        CruEventsProvider.getInstance().requestEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    /**
     * Updates the UI to reflect the Events in events
     * @param cruEvents List of new Events the UI should adhere to
     */
    public void setEvents(ArrayList<CruEvent> cruEvents)
    {
        mEventList.setAdapter(new EventsAdapter(cruEvents, mLayoutManager));
    }
}
