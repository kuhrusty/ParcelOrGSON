# ParcelOrGSON

This is a somewhat dumb wrapper intended to let the same code
serialize to/from both a Parcel and Gson.  In general, **you don't
need this,** because in general, default Gson serialization works
for you, so the only serialization code you write is to/from a
Parcel.

However, if you find yourself doing "something weird" while
reading/writing your Parcelable (like storing some deserialization
state in a ThreadLocal variable), and you don't want to duplicate
the same weirdness in a Gson deserializer...

### 1. Implement Parcelable... no, the *other* Parcelable

    import com.kuhrusty.parcelorgson.Parcelable;
    
    public class YourClass implements Parcelable {

### 2. Add Parcelable stuff

Instead of the normal CREATOR, you're going to use CreatorSD,
which extends Creator, and also implements JsonSerializer and
JsonDeserializer.  Where the usual CREATOR has this:

    @Override
    public YourClass createFromParcel(Parcel in) {
        return new YourClass(in);
    }

You're going to have this:

    @Override
    public YourClass createFromParcel(Parcel in) {
        return new YourClass(new ParcelWrapper(in));
    }

You're also going to have a **deserialize** method which calls
the same YourClass constructor:

    @Override
    public YourClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new YourClass(new GsonWrapper(json.getAsJsonObject(), context));
    }

So, all together, your CREATOR looks like this:

    public static final CreatorSD<YourClass> CREATOR = new CreatorSD<YourClass>() {
        @Override
        public YourClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new YourClass(new GsonWrapper(json.getAsJsonObject(), context));
        }
        @Override
        public YourCLass createFromParcel(Parcel in) {
            return new YourClass(new ParcelWrapper(in));
        }
        @Override
        public YourClass[] newArray(int size) {
            return new YourClass[size];
        }
    };

You're also going to add a writeToParcel method which looks the
same in every one of your ParcelOrGson classes.  (I would've
preferred to do this as a **default** method in
com.kuhrusty.parcelorgson.Parcelable, but it didn't work out.)

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeTo(new ParcelWrapper(dest, flags));
    }

### 3. Add your actual serialization/deserialization

Your serialization is going to be done in a new writeTo()
method; both Parcelable and Gson serialization calls are going
to go through here.

    @Override
    public void writeTo(ParcelOrGson dest) {
        dest.writeList("things", things);
        dest.writeList("otherThings", otherThings);
        dest.writeInt("aNumberIGuess", importantNumber);
    }

Deserialization will be done in the constructor which is
called by both Parcelable and Gson deserialization:

    //  suppose this context is needed when deserializing
    //  YourOtherClass objects.
    static ThreadLocal<YourClass> currentlyDeserializing = new ThreadLocal<YourClass>();

    public YourClass(ParcelOrGson in) {
        try {
            currentlyDeserializing.set(this);
            things = in.readList("things", YourOtherClass.class);
            otherThings = in.readList("otherThings", MoreStuff.class);
            importantNumber = in.readInt("aNumberIGuess", -1);
        } finally {
            currentlyDeserializing.remove();
        }
    }

**Note** that, just as in normal Parcelable serialization, *the
order matters!*  Those property names are used by GsonWrapper
but ignored by ParcelWrapper, which expects you to read elements
in the same order you wrote them.

### 4. Register your Gson type adapter

In order for the Gson stuff to work, you'll need to register
your serializers/deserializers (your CREATOR instances) with
GsonBuilder!  So, something like this:

    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(YourClass.class, YourClass.CREATOR);
    gb.registerTypeAdapter(YourOtherClass.class, YourOtherClass.CREATOR);
    ...
    Gson gson = gb.create();

## MockParcel

This also contains `MockParcel`, originally copied from [here](https://gist.github.com/Sloy/d59a36e6c51214d0b131), and then added to/screwed with to meet my needs.
