package com.kuhrusty.parcelorgson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * A wrapper around Parcelable.Creator and Gson deserialization.
 */
public abstract class CreatorSD<T extends Parcelable> implements android.os.Parcelable.Creator<T>, JsonSerializer<T>, JsonDeserializer<T> {
    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        GsonWrapper gw = new GsonWrapper(context);
        src.writeTo(gw);
        return gw.getJsonObject();
    }
}
