package com.kuhrusty.parcelorgson;

import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class GsonWrapper implements ParcelOrGson {
    private final JsonObject in;
    private final JsonSerializationContext sctx;
    private final JsonDeserializationContext dctx;

    /**
     * For reading from the given JsonObject.
     */
    public GsonWrapper(JsonObject in, JsonDeserializationContext context) {
        this.in = in;
        this.dctx = context;
        sctx = null;
    }

    /**
     * For writing to a new JsonObject, which you can get from getJsonObject().
     */
    public GsonWrapper(JsonSerializationContext context) {
        this.in = new JsonObject();
        dctx = null;
        sctx = context;
    }

    @Override
    public <T extends Parcelable> List<T> readList(String property, Class<T> tc) {
        JsonArray ta = in.getAsJsonArray(property);
        List<T> rv = new ArrayList<T>(ta.size());
        for (JsonElement tp : ta) rv.add((T)(dctx.deserialize(tp, tc)));
        return rv;
    }

    @Override
    public <T extends Parcelable> List<T> readList(String property, Class<T> tc, List<T> addTo) {
        JsonArray ta = in.getAsJsonArray(property);
        if (addTo == null) addTo = new ArrayList<T>(ta.size());
        for (JsonElement tp : ta) addTo.add((T)(dctx.deserialize(tp, tc)));
        return addTo;
    }

    @Override
    public <T extends Parcelable> T[] readArray(String property, Class<T> tc) {
        JsonArray ta = in.getAsJsonArray(property);
        T[] rv = ParcelWrapper.getCreator(tc).newArray(ta.size());
        for (int ii = 0; ii < rv.length; ++ii) rv[ii] = (T)(dctx.deserialize(ta.get(ii), tc));
        return rv;
    }

    @Override
    public <T extends Parcelable> void writeList(String property, List<T> list) {
        in.add(property, sctx.serialize(list));
    }

    @Override
    public <T extends Parcelable> void writeListList(String property, List<List<T>> list) {
        if ((list == null) || (list.size() == 0)) return;
        JsonArray ta = new JsonArray();
        for(List<T> tl : list) {
            ta.add(sctx.serialize(tl));
        }
        in.add(property, ta);
    }

    @Override
    public <T extends Parcelable> List<List<T>> readListList(String property, Class<T> tc, List<List<T>> addTo) {
        JsonArray ta = in.getAsJsonArray(property);
        if ((ta == null) || (ta.size() == 0)) return addTo;
        if (addTo == null) {
            addTo = new ArrayList<List<T>>(ta.size());
        }
        for (JsonElement tp : ta) {
            JsonArray ta2 = tp.getAsJsonArray();
            List<T> tl = null;
            if (ta2 != null) {
                tl = new ArrayList<T>(ta2.size());
                for (JsonElement tp2 : ta2) tl.add((T)(dctx.deserialize(tp2, tc)));
            }
            addTo.add(tl);
        }
        return addTo;
    }

    @Override
    public int readInt(String property, int dflt) {
        JsonElement te = in.get(property);
        return (te != null) ? te.getAsInt() : dflt;
    }
    @Override
    public void writeInt(String property, int value) {
        in.addProperty(property, value);
    }

    @Override
    public int[] readInts(String property) {
        JsonArray ta = in.getAsJsonArray(property);
        if (ta == null) return null;
        int[] rv = new int[ta.size()];
        int idx = 0;
        for (JsonElement tp : ta) rv[idx++] = dctx.deserialize(tp, Integer.class);
        return rv;
    }
    @Override
    public void writeInts(String property, int[] values) {
        if (values != null) in.add(property, sctx.serialize(values));
    }

    @Override
    public long readLong(String property, long dflt) {
        JsonElement te = in.get(property);
        return (te != null) ? te.getAsLong() : dflt;
    }
    @Override
    public void writeLong(String property, long value) {
        in.addProperty(property, value);
    }

    @Override
    public boolean readBoolean(String property, boolean dflt) {
        JsonElement te = in.get(property);
        return (te != null) ? te.getAsBoolean() : dflt;
    }

    @Override
    public void writeBoolean(String property, boolean value) {
        in.addProperty(property, value);
    }

    @Override
    public String readString(String property) {
        JsonElement te = in.get(property);
        return (te != null) ? te.getAsString() : null;
    }
    @Override
    public void writeString(String property, String value) {
        in.addProperty(property, value);
    }

    @Override
    public List<String> readStrings(String property) {
        JsonArray ta = in.getAsJsonArray(property);
        List<String> rv = new ArrayList<String>(ta.size());
        for (JsonElement tp : ta) rv.add(dctx.deserialize(tp, String.class).toString());
        return rv;
    }
    @Override
    public void writeStrings(String property, List<String> values) {
        in.add(property, sctx.serialize(values));
    }

    /**
     * Returns false.
     */
    @Override
    public boolean isParcel() {
        return false;
    }
    /**
     * Returns true.
     */
    @Override
    public boolean isJSON() {
        return true;
    }
    /**
     * Returns false.
     */
    @Override
    public boolean isXML() {
        return false;
    }

    public JsonObject getJsonObject() {
        return in;
    }
}
