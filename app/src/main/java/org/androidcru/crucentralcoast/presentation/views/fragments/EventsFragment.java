package org.androidcru.crucentralcoast.presentation.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.providers.CruEventsProvider;
import org.androidcru.crucentralcoast.presentation.modelviews.CruEventMV;
import org.androidcru.crucentralcoast.presentation.views.adapters.events.EventsAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventsFragment extends Fragment
{
    //Injected Views
    @Bind(R.id.event_list) RecyclerView mEventList;
    private ArrayList<CruEventMV> mCruEventMVs;
    //View elements
    private LinearLayoutManager mLayoutManager;

    private Subscriber<ArrayList<CruEvent>> mEventSubscriber;
    private Subscriber<Pair<String, Long>> mOnCalendarWrittenSubscriber;

    private SharedPreferences mSharedPreferences;
    public EventsFragment()
    {
        mCruEventMVs = new ArrayList<>();
        mEventSubscriber = new Subscriber<ArrayList<CruEvent>>()
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

        mOnCalendarWrittenSubscriber = new Subscriber<Pair<String, Long>>()
        {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(Pair<String, Long> eventInfo)
            {
                if(eventInfo.second > -1)
                {
                    Toast.makeText(getActivity(), "EventID: " + Long.toString(eventInfo.second) + "added to default calendar",
                            Toast.LENGTH_LONG).show();
                    mSharedPreferences.edit().putLong(eventInfo.first, eventInfo.second).commit();
                }
                else
                {
                    mSharedPreferences.edit().remove(eventInfo.first).commit();
                }

                Observable.from(mCruEventMVs)
                    .filter(cruEventMV -> cruEventMV.mCruEvent.mId.equals(eventInfo.first))
                    .subscribeOn(Schedulers.immediate())
                    .subscribe(cruEventMV -> {
                        cruEventMV.mAddedToCalendar = mSharedPreferences.contains(cruEventMV.mCruEvent.mId);
                        cruEventMV.mLocalEventId = mSharedPreferences.getLong(cruEventMV.mCruEvent.mId, -1);
                    });
                mEventList.getAdapter().notifyDataSetChanged();
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

        mSharedPreferences = getActivity().getSharedPreferences(CruApplication.retrievePackageName(), Context.MODE_PRIVATE);

        //Enables actions in the Activity Toolbar (top-right buttons)
        setHasOptionsMenu(true);

        //LayoutManager for RecyclerView
        mLayoutManager = new LinearLayoutManager(getActivity());
        mEventList.setLayoutManager(mLayoutManager);

        //Adapter for RecyclerView
        EventsAdapter mEventAdapter = new EventsAdapter(getActivity(), new ArrayList<>(), mLayoutManager, mOnCalendarWrittenSubscriber);
        mEventList.setAdapter(mEventAdapter);
        mEventList.setHasFixedSize(true);

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
                .subscribe(mEventSubscriber);
    }

    private void getCruEvents()
    {
        CruEventsProvider.getInstance().requestEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mEventSubscriber);
    }


    /**
     * Updates the UI to reflect the Events in events
     * @param cruEvents List of new Events the UI should adhere to
     */
    public void setEvents(ArrayList<CruEvent> cruEvents)
    {
        mCruEventMVs.clear();
        rx.Observable.from(cruEvents)
                .map(cruEvent -> new CruEventMV(cruEvent, false,
                        mSharedPreferences.contains(cruEvent.mId),
                        mSharedPreferences.getLong(cruEvent.mId, -1)))
                .subscribeOn(Schedulers.immediate())
                .subscribe(mCruEventMVs::add);

        mEventList.setAdapter(new EventsAdapter(getActivity(), mCruEventMVs, mLayoutManager, mOnCalendarWrittenSubscriber));
    }
}
