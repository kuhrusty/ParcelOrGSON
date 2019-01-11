package com.kuhrusty.parcelorgson;

import android.os.Parcelable;

import java.util.List;

/**
 * I want each class to have one chunk of code for serialization/deserialization
 * regardless of whether it's going to a Parcel or Gson; I don't want two copies
 * of the same code for handling circular references etc.
 */
public interface ParcelOrGson {
    /**
     * Creates & returns a new List containing the Parcelable objects read from
     * the given property.
     */
    <T extends Parcelable> List<T> readList(String property, Class<T> tc);

    /**
     * Adds the Parcelable objects from the given property to the given list,
     * creating & returning it if it's null.
     */
    <T extends Parcelable> List<T> readList(String property, Class<T> tc, List<T> addTo);

    /**
     * Creates & returns a new array containing the Parcelable objects read from
     * the given property.
     */
    <T extends Parcelable> T[] readArray(String property, Class<T> tc);

    <T extends Parcelable> void writeList(String property, List<T> list);

    <T extends Parcelable> void writeListList(String property, List<List<T>> list);
    <T extends Parcelable> List<List<T>> readListList(String property, Class<T> tc, List<List<T>> addTo);

    /**
     * Returns the int value of the given property, or the given default value.
     */
    int readInt(String property, int dflt);
    void writeInt(String property, int value);

    /**
     * Returns the int array value of the given property, or null.
     */
    int[] readInts(String property);
    void writeInts(String property, int[] values);

    /**
     * Returns the long value of the given property, or the given default value.
     */
    long readLong(String property, long dflt);
    void writeLong(String property, long value);

    boolean readBoolean(String property, boolean dflt);
    void writeBoolean(String property, boolean value);

    String readString(String property);
    void writeString(String property, String value);

    List<String> readStrings(String property);
    void writeStrings(String property, List<String> value);

    boolean isParcel();
    boolean isJSON();
    boolean isXML();
}
