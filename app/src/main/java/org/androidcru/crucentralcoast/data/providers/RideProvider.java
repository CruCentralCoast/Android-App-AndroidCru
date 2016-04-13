package org.androidcru.crucentralcoast.data.providers;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.data.models.Passenger;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.models.queries.Query;
import org.androidcru.crucentralcoast.data.providers.util.RxComposeUtil;
import org.androidcru.crucentralcoast.data.providers.util.RxLoggingUtil;
import org.androidcru.crucentralcoast.data.services.CruApiService;
import org.androidcru.crucentralcoast.presentation.views.base.SubscriptionsHolder;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public final class RideProvider
{
    private static CruApiService mCruService = ApiProvider.getService();



    public static void requestRides(SubscriptionsHolder holder, Observer<List<Ride>> observer)
    {
        Subscription s = requestRides()
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    public static void requestMyRidesDriver(SubscriptionsHolder holder, Observer<List<Ride>> observer, String gcmId)
    {
        Subscription s = requestRides()
                .compose(RxComposeUtil.ui())
                .flatMap(rides -> Observable.from(rides))
                .filter(ride -> {
                    return ride.gcmID.equals(gcmId);
                })
                .toList()
                .subscribe(observer);
        holder.addSubscription(s);
    }

    public static void requestMyRidesPassenger(SubscriptionsHolder holder, Observer<List<Ride>> observer, String gcmId)
    {
        Subscription s = requestRides()
                .compose(RxComposeUtil.ui())
                .flatMap(rides -> Observable.from(rides))
                .filter(ride -> {
                    boolean status = false;
                    for (Passenger p : ride.passengers)
                    {
                        if (p.gcm_id != null && p.gcm_id.equals(CruApplication.getGCMID()))
                        {
                            status = true;
                        }
                    }
                    return status;
                })
                .toList()
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<List<Ride>> requestRides()
    {
        return mCruService.getRides()
                .compose(RxComposeUtil.network())
                .flatMap(rides -> {
                    Timber.d("Rides found");
                    return Observable.from(rides);
                })
                .map(ride -> {
                    PassengerProvider.getPassengers(ride.passengerIds)
                            .compose(RxLoggingUtil.log("PASSENGERS"))
                            .map(passengers -> ride.passengers = passengers)
                            .toBlocking()
                            .subscribe();
                    return ride;
                })
                .map(ride -> {
                    EventProvider.requestCruEventByID(ride.eventId)
                            .compose(RxLoggingUtil.log("EVENTS"))
                            .map(theEvent -> ride.event = theEvent)
                            .toBlocking()
                            .subscribe();
                    ;
                    return ride;
                })
                .toList();
    }



    public static void searchRides(SubscriptionsHolder holder, Observer<List<Ride>> observer, Query query)
    {
        Subscription s = searchRides(query)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<List<Ride>> searchRides(Query query)
    {
        return mCruService.searchRides(query)
                .compose(RxComposeUtil.network());
    }



    public static void createRide(SubscriptionsHolder holder, Observer<Ride> observer, Ride ride)
    {
        Subscription s = createRide(ride)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Ride> createRide(Ride ride)
    {
        return mCruService.postRide(ride)
                .compose(RxComposeUtil.network());
    }



    public static void updateRide(SubscriptionsHolder holder, Observer<Ride> observer, Ride ride)
    {
        Subscription s = updateRide(ride)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Ride> updateRide(Ride ride)
    {
        return mCruService.updateRide(ride)
                .compose(RxComposeUtil.network());
    }



    public static void addPassengerToRide(SubscriptionsHolder holder, Observer<Void> observer, String rideId, String passengerId)
    {
        Subscription s = addPassengerToRide(rideId, passengerId)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Void> addPassengerToRide(String rideId, String passengerId)
    {
        return mCruService.addPassenger(rideId, passengerId)
                .compose(RxComposeUtil.network());
    }



    public static void dropPassengerFromRide(SubscriptionsHolder holder, Observer<Void> observer, String rideId, String passengerId)
    {
        Subscription s = dropPassengerFromRide(rideId, passengerId)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Void> dropPassengerFromRide(String rideId, String passengerId)
    {
        return mCruService.dropPassenger(rideId, passengerId)
                .compose(RxComposeUtil.network());
    }


    public static void dropRide(SubscriptionsHolder holder, Observer<Void> observer, String rideId)
    {
        Subscription s = dropRide(rideId)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Void> dropRide(String rideId)
    {
        return mCruService.dropRide(rideId)
                .compose(RxComposeUtil.network());
    }


    public static void requestRideByID(SubscriptionsHolder holder, Observer<Ride> observer, String id)
    {
        Subscription s = requestRideByID(id)
                .compose(RxComposeUtil.ui())
                .subscribe(observer);
        holder.addSubscription(s);
    }

    protected static Observable<Ride> requestRideByID(String id)
    {
        return mCruService.findSingleRide(id)
                .compose(RxComposeUtil.network())
                .flatMap(rides -> {
                    Timber.d("Rides found");
                    return Observable.from(rides);
                })
                .map(ride -> {
                    PassengerProvider.getPassengers(ride.passengerIds)
                            .subscribeOn(Schedulers.io())
                            .map(passengers -> ride.passengers = passengers)
                            .toBlocking()
                            .subscribe();
                    return ride;
                });

    }
}
