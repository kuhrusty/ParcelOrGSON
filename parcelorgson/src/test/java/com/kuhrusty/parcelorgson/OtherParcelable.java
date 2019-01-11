package com.kuhrusty.parcelorgson;


import android.os.Parcel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An example Parcelable for use in unit tests.
 */
public class OtherParcelable implements Parcelable {
    //  package because we fondle these directly in WrapperTest.
    List<SomeParcelable> listOfParcelables;
    List<List<SomeParcelable>> listOfListsOfParcelables;
    boolean someBoolean = true;
    boolean anotherBoolean = false;
    int someInt;
    int anotherInt;
    int aThirdInt;
    String someString;

    public OtherParcelable(boolean b1, boolean b2, int i1, int i2, int i3, String s1) {
        listOfParcelables = new ArrayList<SomeParcelable>();
        listOfListsOfParcelables = new LinkedList<List<SomeParcelable>>();
        someBoolean = b1;
        anotherBoolean = b2;
        someInt = i1;
        anotherInt = i2;
        aThirdInt = i3;
        someString = s1;
    }

    protected OtherParcelable(ParcelOrGson in) {
        listOfParcelables = in.readList("list1", SomeParcelable.class);
        listOfListsOfParcelables = new LinkedList<List<SomeParcelable>>();
        in.readListList("list3", SomeParcelable.class, listOfListsOfParcelables);
        someBoolean = in.readBoolean("b1", false);
        anotherBoolean = in.readBoolean("b2", false);
        someInt = in.readInt("i1", 123);
        anotherInt = in.readInt("i2", 456);
        aThirdInt = in.readInt("i3", 789);
        someString = in.readString("s1");
    }
    @Override
    public void writeTo(ParcelOrGson dest) {
        dest.writeList("list1", listOfParcelables);
        dest.writeListList("list3", listOfListsOfParcelables);
        dest.writeBoolean("b1", someBoolean);
        dest.writeBoolean("b2", anotherBoolean);
        dest.writeInt("i1", someInt);
        dest.writeInt("i2", anotherInt);
        dest.writeInt("i3", aThirdInt);
        dest.writeString("s1", someString);
    }

    public void addAll(List<SomeParcelable> stuff) {
        listOfParcelables.addAll(stuff);
    }
    public void addList(List<SomeParcelable> stuff) {
        listOfListsOfParcelables.add(stuff);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeTo(new ParcelWrapper(dest, flags));
    }
    @Override
    public int describeContents() {
        return 0;
    }
    public static final CreatorSD<OtherParcelable> CREATOR = new CreatorSD<OtherParcelable>() {
        @Override
        public OtherParcelable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new OtherParcelable(new GsonWrapper(json.getAsJsonObject(), context));
        }
        @Override
        public OtherParcelable createFromParcel(Parcel in) {
            return new OtherParcelable(new ParcelWrapper(in));
        }
        @Override
        public OtherParcelable[] newArray(int size) {
            return new OtherParcelable[size];
        }
    };
}
