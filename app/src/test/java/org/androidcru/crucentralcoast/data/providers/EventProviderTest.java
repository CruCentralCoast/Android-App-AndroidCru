package org.androidcru.crucentralcoast.data.providers;

import com.google.gson.reflect.TypeToken;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.mocking.ResourcesUtil;
import org.androidcru.crucentralcoast.mocking.ServerTest;
import org.junit.Test;

import java.util.ArrayList;

import okhttp3.mockwebserver.MockResponse;
import rx.observers.TestObserver;

public class EventProviderTest extends ServerTest
{
    @Test
    public void testEvents()
    {
        String eventsJson = ResourcesUtil.getResourceAsString("server/events.json");
        server.enqueue(new MockResponse().setBody(eventsJson));
        ArrayList<CruEvent> events = CruApplication.gson.fromJson(eventsJson, new TypeToken<ArrayList<CruEvent>>(){}.getType());

        TestObserver<ArrayList<CruEvent>> observer = new TestObserver<>();
        ArrayList<ArrayList<CruEvent>> wrapper = new ArrayList<>();
        wrapper.add(events);
        EventProvider.requestEvents().toBlocking().subscribe(observer);
        observer.assertReceivedOnNext(wrapper);
    }
}