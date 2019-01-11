package com.kuhrusty.parcelorgson;

/**
 * A Parcelable which can write to a ParcelOrGson.  You want to implement
 * writeTo(ParcelOrGson), and then make your writeToParcel(Parcel, int) look
 * like this:
 *
 * <pre>
 * &#64Override
 * public void writeToParcel(Parcel dest, int flags) {
 *     writeTo(new ParcelWrapper(dest, flags));
 * }
 * </pre>
 */
public interface Parcelable extends android.os.Parcelable {
    //  nuts.  This would've saved having to do the above, but it requires API
    //  level 24, which I don't want to do.
    //default void writeToParcel(Parcel dest, int flags) {
    //    writeTo(new ParcelWrapper(dest, flags));
    //}

    /**
     * Note that, just as when writing to a Parcel, the order in which you
     * write your properties matters!
     */
    void writeTo(ParcelOrGson dest);
}
