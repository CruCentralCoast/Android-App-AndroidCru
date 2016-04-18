package org.androidcru.crucentralcoast.data.models;

import com.google.gson.Gson;

import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.mocking.ResourcesUtil;
import org.json.JSONException;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.junit.Assert.assertThat;

public class CruEventTest
{
    private Gson gson = CruApplication.setupGson();

    @Test
    public void testSerialization() throws JSONException
    {
        String result = ResourcesUtil.getResourceAsString("model/event.json");

        CruEvent event = gson.fromJson(result, CruEvent.class);
        assertThat(result, jsonEquals(gson.toJson(event)));
    }

}
