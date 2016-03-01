package org.androidcru.crucentralcoast.data.converters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.androidcru.crucentralcoast.data.models.Resource;
import org.androidcru.crucentralcoast.data.models.Ride;

import java.lang.reflect.Type;


public class ResourceTypeConverter  implements JsonDeserializer<Resource.ResourceType>, JsonSerializer<Resource.ResourceType>
{
    @Override
    public Resource.ResourceType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        switch(json.getAsString())
        {
            case "article":
                return Resource.ResourceType.ARTICLE;
            case "video":
                return Resource.ResourceType.VIDEO;
            case "audio":
                return Resource.ResourceType.AUDIO;
            default:
                throw new JsonParseException("ResourceType Serialize error: Unexpected value: " + json.getAsString());
        }
    }

    @Override
    public JsonElement serialize(Resource.ResourceType src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
