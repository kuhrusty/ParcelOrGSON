package com.kuhrusty.parcelorgson;

import android.os.Parcel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * An example Parcelable for use in unit tests.
 */
public class SomeParcelable implements Parcelable {
    private int someInt;
    private String someString;
    private int[] someInts;

    public SomeParcelable(int someInt, String someString, int[] someInts) {
        this.someInt = someInt;
        this.someString = someString;
        this.someInts = someInts;
    }

    protected SomeParcelable(ParcelOrGson in) {
        someInt = in.readInt("foo", -1);
        someString = in.readString("bar");
        someInts = in.readInts("baz");
    }
    @Override
    public void writeTo(ParcelOrGson dest) {
        dest.writeInt("foo", someInt);
        dest.writeString("bar", someString);
        dest.writeInts("baz", someInts);
    }

    public int getSomeInt() {
        return someInt;
    }
    public String getSomeString() {
        return someString;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeTo(new ParcelWrapper(dest, flags));
    }
    @Override
    public int describeContents() {
        return 0;
    }
    public static final CreatorSD<SomeParcelable> CREATOR = new CreatorSD<SomeParcelable>() {
        @Override
        public SomeParcelable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new SomeParcelable(new GsonWrapper(json.getAsJsonObject(), context));
        }
        @Override
        public SomeParcelable createFromParcel(Parcel in) {
            return new SomeParcelable(new ParcelWrapper(in));
        }
        @Override
        public SomeParcelable[] newArray(int size) {
            return new SomeParcelable[size];
        }
    };

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof SomeParcelable)) return false;
        SomeParcelable sap = (SomeParcelable)that;
        if (someInt != sap.someInt) return false;
        if (((someString == null) && (sap.someString != null)) ||
            (!someString.equals(sap.someString))) {
            return false;
        }
        if (((someInts == null) && (sap.someInts != null)) ||
             (someInts != null) && (sap.someInts == null)) {
            return false;
        }
        if (someInts != null) {
            if (someInts.length != sap.someInts.length) return false;
            for (int ii = 0; ii < someInts.length; ++ii) {
                if (someInts[ii] != sap.someInts[ii]) return false;
            }
        }
        return true;
    }
}
