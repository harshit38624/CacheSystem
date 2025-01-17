# CacheSystem
# Cache System with LRU and LFU Strategies

This project implements a customizable cache system in Java, featuring two caching strategies:

1. **LRU (Least Recently Used)**
2. **LFU (Least Frequently Used)**

The implementation demonstrates the use of the Strategy Design Pattern, allowing easy integration of new caching strategies in the future.

## Features
- **LRU Cache Strategy**: Evicts the least recently accessed item when the cache reaches its capacity.
- **LFU Cache Strategy**: Evicts the least frequently accessed item, and resolves ties using the least recently used item.
- **Concurrency Support**: Uses thread-safe data structures (e.g., `ConcurrentHashMap`).
- **Doubly Linked List**: Used for efficient insertion and removal of cache items in O(1) time.

---

## Table of Contents
1. [Requirements](#requirements)
2. [Usage](#usage)
3. [Code Overview](#code-overview)
4. [Test Cases](#test-cases)
5. [Future Enhancements](#future-enhancements)

---

## Requirements
- **Java 8 or higher**

---

## Usage
### Running the Program
1. Clone the repository or copy the provided source files.
2. Compile the code:
   ```sh
   javac -d . *.java
   ```
3. Run the program:
   ```sh
   java project.Main
   ```
4. View the output in the console.

### Expected Output
```plaintext
Testing LRU Cache Strategy:
One
null
Three
Four
null
Five

Testing LFU Cache Strategy:
One
null
Three
Four
null
Three
Five
```

---

## Code Overview
### 1. **CacheStrategy Interface**
Defines the basic operations of a cache:
- `get(K key)` - Retrieves the value associated with the key.
- `put(K key, V value)` - Inserts or updates a key-value pair.
- `evict()` - Removes an item based on the strategy.

### 2. **LRUCacheStrategy**
- Maintains the order of access using a `DoublyLinkedList`.
- Evicts the least recently used key when capacity is exceeded.

### 3. **LFUCacheStrategy**
- Tracks the frequency of access for each key using a `Map`.
- Uses a `DoublyLinkedList` for resolving ties based on recency.

### 4. **DoublyLinkedList**
A helper class used in both LRU and LFU strategies for efficient item management.

### 5. **CacheSystem**
A generic wrapper for the cache that accepts any implementation of `CacheStrategy`.

### 6. **Main Class**
Demonstrates the functionality of the cache system with test cases for both LRU and LFU strategies.

---

## Test Cases
The `Main` class includes test cases for both LRU and LFU strategies. Below are some examples:

### LRU Cache Test
```java
CacheSystem<Integer, String> lruCache = new CacheSystem<>(new LRUCacheStrategy<>(3));
lruCache.put(1, "One");
lruCache.put(2, "Two");
lruCache.put(3, "Three");
System.out.println(lruCache.get(1)); // Expected: "One"
lruCache.put(4, "Four"); // Evicts key 2
System.out.println(lruCache.get(2)); // Expected: null
```

### LFU Cache Test
```java
CacheSystem<Integer, String> lfuCache = new CacheSystem<>(new LFUCacheStrategy<>(3));
lfuCache.put(1, "One");
lfuCache.put(2, "Two");
lfuCache.put(3, "Three");
System.out.println(lfuCache.get(1)); // Expected: "One"
lfuCache.put(4, "Four"); // Evicts key 2
System.out.println(lfuCache.get(2)); // Expected: null
```

---

## Future Enhancements
1. **Additional Cache Strategies**: Add more strategies, such as MRU (Most Recently Used) or FIFO (First In, First Out).
2. **Persistent Storage**: Store evicted items in a database or file for future retrieval.
3. **Configurable Eviction Policies**: Allow users to specify custom eviction logic.
4. **Metrics and Logging**: Add statistics (e.g., hit/miss ratio) and detailed logs for better debugging and performance monitoring.

---

## License
This project is licensed under the MIT License.

