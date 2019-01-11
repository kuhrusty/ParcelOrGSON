package com.kuhrusty.mockparcel;

import android.os.Parcel;
import android.os.Parcelable;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Originally copied from here: https://gist.github.com/Sloy/d59a36e6c51214d0b131
 *
 * <p>Added: readInt(), writeInt(), createIntArray(), writeIntArray(),
 *           readParcelable(), writeParcelable(), readParcelableArray(),
 *           writeParcelableArray(), dataPosition()</p>
 *
 * <p>Also added stupid logging stuff.</p>
 *
 * <p><b>If your reads are failing</b> and you set log to System.err, and it
 * looks like the issue is that one of your fields isn't being written, that may
 * mean you're calling some Parcel method which this doesn't override below
 * (like writeFloatArray() or something), then
 * </p>
 */
public class MockParcel {

    /**
     * If not null, reads & writes will be logged here.
     */
    public static PrintStream log = null;

    /**
     * Returns a new mocked Parcel.
     */
    public static Parcel obtain() {
        return new MockParcel().getMockedParcel();
    }

    /**
     * Returns a copy of the given object which has been written to & then read
     * from a Parcel.
     *
     * @param in must not be null.
     * @param creator the Creator for the given class; must not be null.
     * @param <T>
     * @return
     */
    public static <T extends Parcelable> T parcel(T in, Parcelable.Creator<T> creator) {
        Parcel tp = new MockParcel().getMockedParcel();
        in.writeToParcel(tp, 0);
        int endPos = tp.dataPosition();
        tp.setDataPosition(0);
        T rv = creator.createFromParcel(tp);
        //  confirm that our reads got us to the same position as our writes.
        assertEquals(endPos, tp.dataPosition());
        return rv;
    }

    Parcel mockedParcel;
    int position;
    List<Object> objects;
    private int depth = 0;  //  used for logging

    public Parcel getMockedParcel() {
        return mockedParcel;
    }

    /**
     * If message is null, we'll treat this as about-to-do-a-read, and print the
     * value of the object at our current position.
     */
    private void log(String message) {
        if (log == null) return;
        for (int ii = 0; ii < depth; ++ii) log.print("  ");
        log.print(position);
        log.print(" ");
        if (message == null) {
            Object got = objects.get(position);
            message = "read " + ((got != null) ?
                    got.getClass().getName() + " " + got : "null");
        }
        log.println(message);
    }

    public MockParcel() {
        log("NEW MOCK PARCEL");
        //  We want CALLS_REAL_METHODS here so that, if a "real" Parcel
        //  method is called on Parcel--which, during non-instrumented-tests,
        //  is a bogus stubbed-out version--we'll get an error instead of the
        //  mock version silently doing nothing.
        //didn't work; the real methods get called when setting up our Answers!?
        mockedParcel = mock(Parcel.class);//, Mockito.CALLS_REAL_METHODS);
        objects = new ArrayList<>();
        setupMock();
    }

    private void setupMock() {
        setupWrites();
        setupReads();
        setupOthers();
    }

    private void setupWrites() {
        Answer<Void> writeValueAnswer = new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                Object parameter = invocation.getArguments()[0];
                log("write " + ((parameter == null) ? "null" :
                        (parameter.getClass().getName() + " value " + parameter)));
                objects.add(position++, parameter);
                return null;
            }
        };
        Answer<Void> writeListAnswer = new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                List list = (List)invocation.getArguments()[0];
                log("writeList " + ((list == null) ? "null" : ("" + list.size())));
                if (list == null) {
                    objects.add(position++, -1);
                } else {
                    objects.add(position++, list.size());
                    ++depth;
                    for (int ii = 0; ii < list.size(); ++ii) {
                        log("[" + ii + "] " + list.get(ii));
                        objects.add(position++, list.get(ii));
                    }
                    --depth;
                }
                return null;
            }
        };
        Answer<Void> writeIntArrayAnswer = new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                int[] ta = (int[])invocation.getArguments()[0];
                log("writeIntArray " + ((ta == null) ? "null" : ("" + ta.length)));
                if (ta == null) {
                    objects.add(position++, -1);
                } else {
                    objects.add(position++, ta.length);
                    ++depth;
                    for (int ii = 0; ii < ta.length; ++ii) {
                        log("[" + ii + "] " + ta[ii]);
                        objects.add(position++, ta[ii]);
                    }
                    --depth;
                }
                return null;
            }
        };
        Answer<Void> writeParcelableAnswer = new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                android.os.Parcelable tp = (android.os.Parcelable) invocation.getArguments()[0];
                //  write the object, or null, to indicate whether the object
                //  itself is null.
                if (tp == null) {
                    log("writeParcelable null");
                    objects.add(position++, null);
                } else {
                    log("writeParcelable " + tp.getClass().getName() + " value " + tp);
                    objects.add(position++, tp.getClass().getName());
                    ++depth;
                    tp.writeToParcel(mockedParcel, (Integer)(invocation.getArguments()[1]));
                    --depth;
                }
                return null;
            }
        };
        Answer<Void> writeParcelableArrayAnswer = new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                android.os.Parcelable[] tpa = (android.os.Parcelable[]) invocation.getArguments()[0];
                log("writeParcelableArray " + ((tpa == null) ? "null" : (tpa.length + " " + tpa.getClass())));
                if (tpa == null) {
                    objects.add(position++, -1);
                    return null;
                }
                int flags = (Integer)(invocation.getArguments()[1]);
                objects.add(position++, tpa.length);
                ++depth;
                for (int ii = 0; ii < tpa.length; ++ii) {
                    if (tpa[ii] == null) {
                        log("[" + ii + "] is null");
                        objects.add(position++, null);
                    } else {
                        log("[" + ii + "] " + tpa[ii].getClass());
                        objects.add(position++, tpa[ii].getClass().getName());
                        ++depth;
                        tpa[ii].writeToParcel(mockedParcel, flags);
                        --depth;
                    }
                }
                --depth;
                return null;
            }
        };
        doAnswer(writeValueAnswer).when(mockedParcel).writeInt(anyInt());
        doAnswer(writeIntArrayAnswer).when(mockedParcel).writeIntArray(any(int[].class));
        doAnswer(writeValueAnswer).when(mockedParcel).writeLong(anyLong());
        doAnswer(writeValueAnswer).when(mockedParcel).writeString(anyString());
        doAnswer(writeListAnswer).when(mockedParcel).writeStringList(any(List.class));
        doAnswer(writeParcelableAnswer).when(mockedParcel).writeParcelable(any(Parcelable.class), anyInt());
        doAnswer(writeParcelableArrayAnswer).when(mockedParcel).writeParcelableArray(any(Parcelable[].class), anyInt());
    }

    private void setupReads() {
        when(mockedParcel.readInt()).thenAnswer(new Answer<Integer>() {
            @Override public Integer answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                return (Integer) objects.get(position++);
            }
        });
//different way of attempting the same thing, only without calling the base
//readInt(); didn't work.
//        doReturn(new Answer<Integer>() {
//            @Override public Integer answer(InvocationOnMock invocation) throws Throwable {
//                return (Integer) objects.get(position++);
//            }
//        }).when(mockedParcel).readInt();
        when(mockedParcel.readLong()).thenAnswer(new Answer<Long>() {
            @Override public Long answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                return (Long) objects.get(position++);
            }
        });
        when(mockedParcel.readString()).thenAnswer(new Answer<String>() {
            @Override public String answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                return (String) objects.get(position++);
            }
        });
        when(mockedParcel.createStringArrayList()).thenAnswer(new Answer<ArrayList<String>>() {
            @Override public ArrayList<String> answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                int size = (Integer) objects.get(position++);
                if (size == -1) return null;
                ArrayList rv = new ArrayList(size);
                ++depth;
                for (int ii = 0; ii < size; ++ii) {
                    log(null);
                    rv.add((String)(objects.get(position++)));
                }
                --depth;
                return rv;
            }
        });
        when(mockedParcel.createIntArray()).thenAnswer(new Answer<int[]>() {
            @Override public int[] answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                int size = (Integer) objects.get(position++);
                if (size == -1) return null;
                int[] rv = new int[size];
                ++depth;
                for (int ii = 0; ii < size; ++ii) {
                    log(null);
                    rv[ii] = (Integer)(objects.get(position++));
                }
                --depth;
                return rv;
            }
        });
        when(mockedParcel.readParcelable(any(ClassLoader.class))).thenAnswer(new Answer<Parcelable>() {
            @Override public Parcelable answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                String className = (String) objects.get(position++);
                if (className == null) {
                    return null;  //  it was a null object
                }
                ClassLoader cl = invocation.getArgument(0);
                Class tc = cl.loadClass(className);
                Object creatorWeHope = tc.getField("CREATOR").get(null);
                if (creatorWeHope == null) {
                    throw new RuntimeException("class " + tc + ": failed to find CREATOR");
                }
                ++depth;
                Parcelable rv = (Parcelable) ((Parcelable.Creator)creatorWeHope).createFromParcel(mockedParcel);
                --depth;
                return rv;
            }
        });
        when(mockedParcel.readParcelableArray(any(ClassLoader.class))).thenAnswer(new Answer<Parcelable[]>() {
            @Override public Parcelable[] answer(InvocationOnMock invocation) throws Throwable {
                log(null);
                int size = (Integer) objects.get(position++);
                if (size == -1) return null;
                Parcelable[] rv = new Parcelable[size];

                ClassLoader cl = invocation.getArgument(0);
                ++depth;
                for (int ii = 0; ii < size; ++ii) {
                    log(null);
                    String className = (String) objects.get(position++);
                    if (className != null) {
                        Class tc = cl.loadClass(className);
                        Object creatorWeHope = tc.getField("CREATOR").get(null);
                        if (creatorWeHope == null) {
                            throw new RuntimeException("class " + tc + ": failed to find CREATOR");
                        }
                        ++depth;
                        rv[ii] = (Parcelable) ((Parcelable.Creator)creatorWeHope).createFromParcel(mockedParcel);
                        --depth;
                    }
                }
                --depth;
                return rv;
            }
        });
    }

    private void setupOthers() {
        when(mockedParcel.dataPosition()).thenAnswer(new Answer<Integer>() {
            @Override public Integer answer(InvocationOnMock invocation) throws Throwable {
                return Integer.valueOf(position);
            }
        });

        doAnswer(new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) throws Throwable {
                position = ((Integer) invocation.getArguments()[0]);
                return null;
            }
        }).when(mockedParcel).setDataPosition(anyInt());
    }
}
