package com.kuhrusty.parcelorgson;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ParcelWrapper implements ParcelOrGson {
    private final Parcel in;
    private final int flags;
    public ParcelWrapper(Parcel in) {
        this.in = in;
        flags = 0;
    }
    public ParcelWrapper(Parcel out, int flags) {
        this.in = out;
        this.flags = flags;
    }

    @Override
    public <T extends Parcelable> List<T> readList(String property, Class<T> tc) {
        Parcelable[] ta = in.readParcelableArray(tc.getClassLoader());
        List<T> rv = new ArrayList<T>(ta.length);
        for (Parcelable tp : ta) rv.add((T)tp);
        return rv;
    }

    @Override
    public <T extends Parcelable> List<T> readList(String property, Class<T> tc, List<T> addTo) {
        Parcelable[] ta = in.readParcelableArray(tc.getClassLoader());
        if (addTo == null) {
            addTo = new ArrayList<T>(ta.length);
        }
        for (Parcelable tp : ta) addTo.add((T)tp);
        return addTo;
    }

    @Override
    public <T extends Parcelable> T[] readArray(String property, Class<T> tc) {
        Parcelable[] ta = in.readParcelableArray(tc.getClassLoader());
        T[] rv = getCreator(tc).newArray(ta.length);
        for (int ii = 0; ii < ta.length; ++ii) rv[ii] = (T)(ta[ii]);
        return rv;
    }

    @Override
    public <T extends Parcelable> void writeList(String property, List<T> list) {
        Parcelable[] ta = new Parcelable[list.size()];
        list.toArray(ta);
        in.writeParcelableArray(ta, flags);
    }

    @Override
    public <T extends Parcelable> void writeListList(String property, List<List<T>> list) {
        if (list == null) {
            in.writeInt(0);
            return;
        }
        in.writeInt(list.size());
        for (int ii = 0; ii < list.size(); ++ii) {
            List<T> tl = list.get(ii);
            Parcelable[] ta = new Parcelable[(tl != null) ? tl.size() : 0];
            tl.toArray(ta);
            in.writeParcelableArray(ta, flags);
        }
    }

    @Override
    public <T extends Parcelable> List<List<T>> readListList(String property, Class<T> tc, List<List<T>> addTo) {
        int expect = in.readInt();
        if (addTo == null) {
            addTo = new ArrayList<List<T>>(expect);
        }
        for (int ii = 0; ii < expect; ++ii) {
            Parcelable[] ta = in.readParcelableArray(tc.getClassLoader());
            List<T> tl = new ArrayList<T>(ta.length);
            for (Parcelable tp : ta) tl.add((T)tp);
            addTo.add(tl);
        }
        return addTo;
    }

    @Override
    public int readInt(String property, int dflt) {
        return in.readInt();
    }
    @Override
    public void writeInt(String property, int value) {
        in.writeInt(value);
    }

    @Override
    public int[] readInts(String property) {
        return in.createIntArray();
    }
    @Override
    public void writeInts(String property, int[] values) {
        in.writeIntArray(values);
    }

    @Override
    public long readLong(String property, long dflt) {
        return in.readLong();
    }
    @Override
    public void writeLong(String property, long value) {
        in.writeLong(value);
    }

    @Override
    public boolean readBoolean(String property, boolean dflt) {
        return (in.readByte() != 0);
    }
    @Override
    public void writeBoolean(String property, boolean value) {
        in.writeByte(value ? (byte)1 : (byte)0);
    }

    @Override
    public String readString(String property) {
        return in.readString();
    }
    @Override
    public void writeString(String property, String value) {
        in.writeString(value);
    }

    @Override
    public List<String> readStrings(String property) {
        return in.createStringArrayList();
    }
    @Override
    public void writeStrings(String property, List<String> values) {
        in.writeStringList(values);
    }

    @Override
    public boolean isParcel() {
        return true;
    }
    @Override
    public boolean isJSON() {
        return false;
    }
    @Override
    public boolean isXML() {
        return false;
    }

    /**
     * Returns a class' CREATOR member.
     */
    static <T extends Parcelable> Parcelable.Creator<T> getCreator(Class<T> tc) {
        try {
            return (Parcelable.Creator<T>)(tc.getDeclaredField("CREATOR").get(null));
        } catch (NoSuchFieldException nsfe) {
            Log.w("ParcelWrapper", "no CREATOR for alleged Parcelable " + tc.toString());
        } catch (IllegalAccessException iae) {
            Log.w("ParcelWrapper", "can't access CREATOR for alleged Parcelable " + tc.toString());
        }
        return null;
    }
}
