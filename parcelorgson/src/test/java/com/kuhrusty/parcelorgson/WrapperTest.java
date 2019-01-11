package com.kuhrusty.parcelorgson;

import android.os.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WrapperTest {

    private List<SomeParcelable> list1;
    private List<SomeParcelable> list2;
    private OtherParcelable op1;

    @Before
    public void setup() {
        list1 = new ArrayList<SomeParcelable>();
        list1.add(new SomeParcelable(123, "Abbath Doom Occulta", new int[] { 6, 6, 6 }));
        list1.add(new SomeParcelable(456, "Iscariah", (int[])null));
        list1.add(new SomeParcelable(789, "Horgh", new int[] { 666 }));

        list2 = new ArrayList<SomeParcelable>();
        list2.add(new SomeParcelable(101, "At The Heart of Winter", (int[])null));
        list2.add(new SomeParcelable(234, "Sons of Northern Darkness", (int[])null));

        op1 = new OtherParcelable(true, false, 666, -1, 0, "Immortal");
        op1.addAll(list1);
        op1.addList(list1);
        op1.addList(list2);
        op1.addList(list1);
    }

    @Test
    public void testWriteParcel() {
        Parcel tp = mock(Parcel.class);
        ParcelWrapper pw = new ParcelWrapper(tp, 0);
        assertTrue(pw.isParcel());
        assertFalse(pw.isJSON());

        op1.writeTo(pw);

        android.os.Parcelable[] array1 = new android.os.Parcelable[] {
                new SomeParcelable(123, "Abbath Doom Occulta", new int[] { 6, 6, 6 }),
                new SomeParcelable(456, "Iscariah", (int[])null),
                new SomeParcelable(789, "Horgh", new int[] { 666 })
        };
        android.os.Parcelable[] array2 = new android.os.Parcelable[] {
                new SomeParcelable(101, "At The Heart of Winter", (int[])null),
                new SomeParcelable(234, "Sons of Northern Darkness", (int[])null)
        };

        InOrder inOrder = inOrder(tp);
        inOrder.verify(tp).writeParcelableArray(array1, 0);
        inOrder.verify(tp).writeInt(3);
        inOrder.verify(tp).writeParcelableArray(array1, 0);
        inOrder.verify(tp).writeParcelableArray(array2, 0);
        inOrder.verify(tp).writeParcelableArray(array1, 0);
        inOrder.verify(tp).writeByte((byte)1);
        inOrder.verify(tp).writeByte((byte)0);
        inOrder.verify(tp).writeInt(666);
        inOrder.verify(tp).writeInt(-1);
        inOrder.verify(tp).writeInt(0);
        inOrder.verify(tp).writeString("Immortal");
    }

    @Test
    public void testReadParcel() {
        Parcel tp = mock(Parcel.class);
        ParcelWrapper pw = new ParcelWrapper(tp, 0);
        assertTrue(pw.isParcel());
        assertFalse(pw.isJSON());

        SomeParcelable[] array1 = new SomeParcelable[] {
                list1.get(0), list1.get(1), list1.get(2)
        };
        SomeParcelable[] array2 = new SomeParcelable[] {
                list2.get(0), list2.get(1)
        };
        when(tp.readByte()).thenReturn((byte)1, (byte)0);
        when(tp.readInt()).thenReturn(3, 666, -1, 0);
        when(tp.readParcelableArray(SomeParcelable.class.getClassLoader())).thenReturn(
                array1,
                array1, array2, array1);
        when(tp.readString()).thenReturn("Immortal");

        OtherParcelable op2 = new OtherParcelable(pw);
        check(op1, op2);
    }

    @Test
    public void testReadWriteJSON() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SomeParcelable.class, SomeParcelable.CREATOR)
                .registerTypeAdapter(OtherParcelable.class, OtherParcelable.CREATOR)
                //.setPrettyPrinting()
                .create();
        String json = gson.toJson(op1);
        //System.err.println("got JSON:\n" + json);
        OtherParcelable op2 = gson.fromJson(json, OtherParcelable.class);
        check(op1, op2);
    }

    private static void check(OtherParcelable expect, OtherParcelable got) {
        assertFalse(expect == got);
        assertEquals(expect.someBoolean, got.someBoolean);
        assertEquals(expect.anotherBoolean, got.anotherBoolean);
        assertEquals(expect.someInt, got.someInt);
        assertEquals(expect.anotherInt, got.anotherInt);
        assertEquals(expect.aThirdInt, got.aThirdInt);
        assertEquals(expect.someString, got.someString);
        assertEquals(expect.listOfParcelables.size(), got.listOfParcelables.size());
        for (int ii = 0; ii < expect.listOfParcelables.size(); ++ii) {
            assertEquals(expect.listOfParcelables.get(ii), got.listOfParcelables.get(ii));
        }
        assertEquals(expect.listOfListsOfParcelables.size(), got.listOfListsOfParcelables.size());
        for (int ii = 0; ii < expect.listOfListsOfParcelables.size(); ++ii) {
            assertEquals(expect.listOfListsOfParcelables.get(ii).size(), got.listOfListsOfParcelables.get(ii).size());
            for (int jj = 0; jj < expect.listOfListsOfParcelables.get(ii).size(); ++jj) {
                assertEquals(expect.listOfListsOfParcelables.get(ii).get(jj), got.listOfListsOfParcelables.get(ii).get(jj));
            }
        }
    }
}
