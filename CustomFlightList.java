package com.airline.DataStructure;

import com.airline.model.FlightData; // Assuming FlightData is in com.ars.model
import java.lang.IndexOutOfBoundsException;

/**
 * A custom ArrayList implementation using a basic array.
 * This class contains the logic for dynamic resizing.
 */
public class CustomFlightList {
    private FlightData[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public CustomFlightList() {
        this.elements = new FlightData[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public void add(FlightData flight) {
        if (size == elements.length) {
            int newCapacity = elements.length * 2;
            FlightData[] newArray = new FlightData[newCapacity];
            for (int i = 0; i < elements.length; i++) {
                newArray[i] = elements[i];
            }
            elements = newArray;
        }
        elements[size] = flight;
        size++;
    }

    public FlightData get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return elements[index];
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }
}