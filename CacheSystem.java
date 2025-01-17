package project;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Strategy Interface
interface CacheStrategy<K, V> {
    V get(K key);
    void put(K key, V value);
    void evict();
}

// LFU (Least Frequently Used) Strategy
class LFUCacheStrategy<K, V> implements CacheStrategy<K, V> {
    private final int capacity;
    private final Map<K, V> values;
    private final Map<K, Integer> frequencies;
    private final Map<Integer, DoublyLinkedList<K>> frequencyList;
    private int minFrequency;

    public LFUCacheStrategy(int capacity) {
        this.capacity = capacity;
        this.values = new ConcurrentHashMap<>();
        this.frequencies = new ConcurrentHashMap<>();
        this.frequencyList = new ConcurrentHashMap<>();
        this.minFrequency = 0;
    }

    @Override
    public V get(K key) {
        if (!values.containsKey(key)) {
            return null;
        }
        updateFrequency(key);
        return values.get(key);
    }

    @Override
    public void put(K key, V value) {
        if (capacity <= 0) return;

        if (values.containsKey(key)) {
            values.put(key, value);
            updateFrequency(key);
            return;
        }

        if (values.size() >= capacity) {
            evict();
        }

        values.put(key, value);
        frequencies.put(key, 1);
        minFrequency = 1;
        frequencyList.computeIfAbsent(1, k -> new DoublyLinkedList<>()).add(key);
    }

    @Override
    public void evict() {
        if (frequencyList.get(minFrequency).isEmpty()) return;

        K evictKey = frequencyList.get(minFrequency).removeLeastRecentlyUsed();
        values.remove(evictKey);
        frequencies.remove(evictKey);

        if (frequencyList.get(minFrequency).isEmpty()) {
            frequencyList.remove(minFrequency);
        }
    }

    private void updateFrequency(K key) {
        int currentFreq = frequencies.get(key);
        int newFreq = currentFreq + 1;
        frequencies.put(key, newFreq);

        // Remove from old frequency list
        DoublyLinkedList<K> currentList = frequencyList.get(currentFreq);
        currentList.remove(key);
        if (currentList.isEmpty() && currentFreq == minFrequency) {
            frequencyList.remove(currentFreq);
            minFrequency++;
        }

        // Add to new frequency list
        frequencyList.computeIfAbsent(newFreq, k -> new DoublyLinkedList<>()).add(key);
    }
}

// LRU (Least Recently Used) Strategy
class LRUCacheStrategy<K, V> implements CacheStrategy<K, V> {
    private final int capacity;
    private final Map<K, V> values;
    private final Map<K, DoublyLinkedListNode<K>> nodeMap;
    private final DoublyLinkedList<K> orderList;

    public LRUCacheStrategy(int capacity) {
        this.capacity = capacity;
        this.values = new ConcurrentHashMap<>();
        this.nodeMap = new ConcurrentHashMap<>();
        this.orderList = new DoublyLinkedList<>();
    }

    @Override
    public V get(K key) {
        if (!values.containsKey(key)) {
            return null;
        }
        moveToFront(key);
        return values.get(key);
    }

    @Override
    public void put(K key, V value) {
        if (capacity <= 0) return;

        if (values.containsKey(key)) {
            values.put(key, value);
            moveToFront(key);
            return;
        }

        if (values.size() >= capacity) {
            evict();
        }

        values.put(key, value);
        DoublyLinkedListNode<K> newNode = new DoublyLinkedListNode<>(key);
        nodeMap.put(key, newNode);
        orderList.addFirst(newNode);
    }

    @Override
    public void evict() {
        K evictKey = orderList.removeLast();
        values.remove(evictKey);
        nodeMap.remove(evictKey);
    }

    private void moveToFront(K key) {
        DoublyLinkedListNode<K> node = nodeMap.get(key);
        orderList.remove(node);
        orderList.addFirst(node);
    }
}

// CacheSystem using Strategy Pattern
public class CacheSystem<K, V> {
    private final CacheStrategy<K, V> cacheStrategy;

    public CacheSystem(CacheStrategy<K, V> cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public V get(K key) {
        return cacheStrategy.get(key);
    }

    public void put(K key, V value) {
        cacheStrategy.put(key, value);
    }

    public void evict() {
        cacheStrategy.evict();
    }
}

// Doubly Linked List Node
class DoublyLinkedListNode<K> {
    K key;
    DoublyLinkedListNode<K> prev;
    DoublyLinkedListNode<K> next;

    public DoublyLinkedListNode(K key) {
        this.key = key;
    }
}

// Doubly Linked List for LRU and LFU
class DoublyLinkedList<K> {
    private final DoublyLinkedListNode<K> head;
    private final DoublyLinkedListNode<K> tail;

    public DoublyLinkedList() {
        this.head = new DoublyLinkedListNode<>(null);
        this.tail = new DoublyLinkedListNode<>(null);
        head.next = tail;
        tail.prev = head;
    }

    public void addFirst(DoublyLinkedListNode<K> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public void add(K key) {
        DoublyLinkedListNode<K> newNode = new DoublyLinkedListNode<>(key);
        addFirst(newNode);
    }

    public K removeLast() {
        if (head.next == tail) return null;
        DoublyLinkedListNode<K> lastNode = tail.prev;
        remove(lastNode);
        return lastNode.key;
    }

    public void remove(DoublyLinkedListNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public void remove(K key) {
        DoublyLinkedListNode<K> node = findNode(key);
        if (node != null) remove(node);
    }

    private DoublyLinkedListNode<K> findNode(K key) {
        DoublyLinkedListNode<K> current = head.next;
        while (current != tail) {
            if (current.key.equals(key)) return current;
            current = current.next;
        }
        return null;
    }

    public boolean isEmpty() {
        return head.next == tail;
    }

    public K removeLeastRecentlyUsed() {
        return removeLast();
    }
}

class Main {
    public static void main(String[] args) {
        // Test LRU Cache Strategy
        System.out.println("Testing LRU Cache Strategy:");
        CacheSystem<Integer, String> lruCache = new CacheSystem<>(new LRUCacheStrategy<>(3));

        lruCache.put(1, "One");
        lruCache.put(2, "Two");
        lruCache.put(3, "Three");

        System.out.println(lruCache.get(1)); // Expected: "One"
        lruCache.put(4, "Four");            // Evicts key 2 (least recently used)
        System.out.println(lruCache.get(2)); // Expected: null
        System.out.println(lruCache.get(3)); // Expected: "Three"
        System.out.println(lruCache.get(4)); // Expected: "Four"

        lruCache.put(5, "Five");            // Evicts key 1
        System.out.println(lruCache.get(1)); // Expected: null
        System.out.println(lruCache.get(5)); // Expected: "Five"

        // Test LFU Cache Strategy
        System.out.println("\nTesting LFU Cache Strategy:");
        CacheSystem<Integer, String> lfuCache = new CacheSystem<>(new LFUCacheStrategy<>(3));

        lfuCache.put(1, "One");
        lfuCache.put(2, "Two");
        lfuCache.put(3, "Three");

        System.out.println(lfuCache.get(1)); // Expected: "One" (Frequency of key 1 increase)
        lfuCache.put(4, "Four");            // Evicts key 2 (least frequently used)
        System.out.println(lfuCache.get(2)); // Expected: null
        System.out.println(lfuCache.get(3)); // Expected: "Three"
        System.out.println(lfuCache.get(4)); // Expected: "Four"

        lfuCache.put(5, "Five");            // Evicts key 1 (lowest frequency)
        System.out.println(lfuCache.get(1)); // Expected: null
        System.out.println(lfuCache.get(3)); // Expected: "Three"
        System.out.println(lfuCache.get(5)); // Expected: "Five"
    }
}
