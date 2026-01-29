package com.airline.DataStructure;

/**
 * A custom HashMap implementation using an array of linked lists (buckets).
 * This demonstrates the logic of hashing and collision handling.
 */
public class CustomAircraftCache {

    // Inner class to represent a key-value pair in a linked list
    private static class Entry {
        int key; // Aircraft ID
        String value; // Model Name
        Entry next; // Pointer to the next entry in the same bucket

        Entry(int key, String value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private Entry[] buckets;
    private static final int INITIAL_CAPACITY = 16; // Should be a power of 2

    public CustomAircraftCache() {
        this.buckets = new Entry[INITIAL_CAPACITY];
    }

    /**
     * LOGICAL CODE: Adds a key-value pair to the cache.
     */
    public void put(int key, String value) {
        int bucketIndex = key % buckets.length; // Simple hash function
        Entry existingEntry = buckets[bucketIndex];

        if (existingEntry == null) {
            // If the bucket is empty, create a new entry and place it there.
            buckets[bucketIndex] = new Entry(key, value);
        } else {
            // If the bucket is not empty (a collision occurred), traverse the linked list.
            Entry current = existingEntry;
            while (current.next != null) {
                // If a key already exists, update its value.
                if (current.key == key) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }
            // Add the new entry to the end of the linked list in this bucket.
            current.next = new Entry(key, value);
        }
    }

    /**
     * LOGICAL CODE: Retrieves a value based on its key.
     */
    public String get(int key) {
        int bucketIndex = key % buckets.length;
        Entry current = buckets[bucketIndex];

        // Traverse the linked list in the correct bucket
        while (current != null) {
            if (current.key == key) {
                return current.value; // Key found
            }
            current = current.next;
        }
        return null; // Key not found
    }

    /**
     * LOGICAL CODE: Checks if a key exists in the cache.
     */
    public boolean containsKey(int key) {
        int bucketIndex = key % buckets.length;
        Entry current = buckets[bucketIndex];
        while (current != null) {
            if (current.key == key) {
                return true; // Key found
            }
            current = current.next;
        }
        return false; // Key not found
    }
}