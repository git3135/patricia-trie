/*
 * Copyright 2005-2008 Roger Kapsi, Sam Berlin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

/**
 * <h3>PATRICIA Trie</h3>
 *  
 * <i>Practical Algorithm to Retrieve Information Coded in Alphanumeric</i>
 * 
 * <p>
 * A PATRICIA Trie is a compressed Trie. Instead of storing all data at the
 * edges of the Trie (and having empty internal nodes), PATRICIA stores data
 * in every node. This allows for very efficient traversal, insert, delete,
 * predecessor, successor, prefix, range, and 'select' operations. All operations
 * are performed at worst in O(K) time, where K is the number of bits in the
 * largest item in the tree. In practice, operations actually take O(A(K))
 * time, where A(K) is the average number of bits of all items in the tree.
 * 
 * <p>
 * Most importantly, PATRICIA requires very few comparisons to keys while
 * doing any operation. While performing a lookup, each comparison (at most 
 * K of them, described above) will perform a single bit comparison against 
 * the given key, instead of comparing the entire key to another key.
 * <p>
 * The Trie can return operations in lexicographical order using the 'traverse',
 * 'prefix', 'submap', or 'iterator' methods. The Trie can also scan for items
 * that are 'bitwise' (using an XOR metric) by the 'select' method. Bitwise
 * closeness is determined by the {@link KeyAnalyzer} returning true or false 
 * for a bit being set or not in a given key.
 * <p>
 * This PATRICIA Trie supports both variable length & fixed length keys.
 * Some methods, such as {@link #getPrefixedBy(Object)} are suited only to 
 * variable length keys, whereas {@link #getPrefixedByBits(Object, int)} is 
 * suited to fixed-size keys.
 * <p>
 * Any methods here that take an {@link Object} argument may throw a 
 * {@link ClassCastException} if the method is expecting an instance of K 
 * and it isn't K.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Radix_tree">Radix Tree</a>
 * @see <a href="http://www.csse.monash.edu.au/~lloyd/tildeAlgDS/Tree/PATRICIA">PATRICIA</a>
 * @see <a href="http://www.imperialviolet.org/binary/critbit.pdf">Crit-Bit Tree</a>
 * 
 * @author Roger Kapsi
 * @author Sam Berlin
 */
public class PatriciaTrie<K, V> extends PatriciaTrieBase<K, V> {
    
    private static final long serialVersionUID = 6473154301823684262L;

    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested.  The views are
     * stateless, so there's no reason to create more than one of each.
     */
    private transient volatile Set<K>               keySet = null;
    private transient volatile Collection<V>        values = null;
    private transient volatile Set<Map.Entry<K,V>>  entrySet = null;
    
    
    /** 
     * {@inheritDoc}
     */
    public PatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer) {
        super(keyAnalyzer);
    }
    
    /**
     * {@inheritDoc}
     */
    public PatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer, 
            Map<? extends K, ? extends V> m) {
        super(keyAnalyzer, m);
    }
    
    /**
     * {@inheritDoc}
     */
    SortedMap<K, V> getPrefixedByBits(K key, int offsetInBits, int lengthInBits) {
        
        int offsetLength = offsetInBits + lengthInBits;
        if (offsetLength > lengthInBits(key)) {
            throw new IllegalArgumentException(offsetInBits + " + " 
                    + lengthInBits + " > " + lengthInBits(key));
        }
        
        if (offsetLength == 0) {
            return this;
        }
        
        return new PrefixRangeMap(key, offsetInBits, lengthInBits);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }
    
    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }
    
    /**
     * Returns a collection view of the values contained in this map. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return new RangeEntryMap(null, toKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new RangeEntryMap(fromKey, toKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return new RangeEntryMap(fromKey, null);
    } 
    
    /**
     * This is a entry set view of the {@link Trie} as returned 
     * by {@link Map#entrySet()}
     */
    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            
            TrieEntry<K,V> candidate = getEntry(((Map.Entry<?, ?>)o).getKey());
            return candidate != null && candidate.equals(o);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object o) {
            int size = size();
            PatriciaTrie.this.remove(o);
            return size != size();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return PatriciaTrie.this.size();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            PatriciaTrie.this.clear();
        }
        
        /**
         * An {@link Iterator} that returns {@link Entry} Objects
         */
        private class EntryIterator extends TrieIterator<Map.Entry<K,V>> {
            @Override
            public Map.Entry<K,V> next() {
                return nextEntry();
            }
        }
    }
    
    /**
     * This is a key set view of the {@link Trie} as returned 
     * by {@link Map#keySet()}
     */
    private class KeySet extends AbstractSet<K> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return PatriciaTrie.this.size();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object o) {
            int size = size();
            PatriciaTrie.this.remove(o);
            return size != size();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            PatriciaTrie.this.clear();
        }
        
        /**
         * An {@link Iterator} that returns Key Objects
         */
        private class KeyIterator extends TrieIterator<K> {
            @Override
            public K next() {
                return nextEntry().getKey();
            }
        }
    }
    
    /**
     * This is a value view of the {@link Trie} as returned 
     * by {@link Map#values()}
     */
    private class Values extends AbstractCollection<V> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return PatriciaTrie.this.size();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            PatriciaTrie.this.clear();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object o) {
            for (Iterator<V> it = iterator(); it.hasNext(); ) {
                V value = it.next();
                if (TrieUtils.compare(value, o)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
        
        /**
         * An {@link Iterator} that returns Value Objects
         */
        private class ValueIterator extends TrieIterator<V> {
            @Override
            public V next() {
                return nextEntry().getValue();
            }
        }
    }
    
    /** 
     * An iterator for the entries. 
     */
    private abstract class TrieIterator<E> implements Iterator<E> {
        
        /**
         * For fast-fail
         */
        protected int expectedModCount = PatriciaTrie.this.modCount;
        
        protected TrieEntry<K, V> next; // the next node to return
        protected TrieEntry<K, V> current; // the current entry we're on
        
        /**
         * Starts iteration from the root
         */
        protected TrieIterator() {
            next = PatriciaTrie.this.nextEntry(null);
        }
        
        /**
         * Starts iteration at the given entry
         */
        protected TrieIterator(TrieEntry<K, V> firstEntry) {
            next = firstEntry;
        }
        
        /**
         * Returns the next {@link TrieEntry}
         */
        protected TrieEntry<K,V> nextEntry() { 
            if (expectedModCount != PatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }
            
            TrieEntry<K,V> e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }
            
            next = findNext(e);
            current = e;
            return e;
        }
        
        /**
         * @see PatriciaTrie#nextEntry(TrieEntry)
         */
        protected TrieEntry<K, V> findNext(TrieEntry<K, V> prior) {
            return PatriciaTrie.this.nextEntry(prior);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return next != null;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            
            if (expectedModCount != PatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }
            
            TrieEntry<K, V> node = current;
            current = null;
            PatriciaTrie.this.removeEntry(node);
            
            expectedModCount = PatriciaTrie.this.modCount;
        }
    }
    
    /**
     *
     */
    private abstract class RangeMap extends AbstractMap<K, V> 
            implements SortedMap<K, V> {

        private transient Set<Map.Entry<K, V>> entrySet;

        /**
        * 
        */
        protected abstract Set<Map.Entry<K, V>> createEntrySet();

        protected abstract K getFromKey();
        
        protected abstract boolean isFromInclusive();
        
        protected abstract K getToKey();
        
        protected abstract boolean isToInclusive();
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Comparator<? super K> comparator() {
            return keyAnalyzer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsKey(Object key) {
            if (!inRange(castKey(key))) {
                return false;
            }

            return PatriciaTrie.this.containsKey(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V remove(Object key) {
            if (!inRange(castKey(key))) {
                return null;
            }

            return PatriciaTrie.this.remove(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V get(Object key) {
            if (!inRange(castKey(key))) {
                return null;
            }

            return PatriciaTrie.this.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V put(K key, V value) {
            if (!inRange(key)) {
                throw new IllegalArgumentException("key out of range");
            }

            return PatriciaTrie.this.put(key, value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            if (entrySet == null) {
                entrySet = createEntrySet();
            }
            return entrySet;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            if (!inRange2(fromKey)) {
                throw new IllegalArgumentException("fromKey out of range");
            }

            if (!inRange2(toKey)) {
                throw new IllegalArgumentException("toKey out of range");
            }

            return createRangeMap(fromKey, isFromInclusive(), toKey,
                    isToInclusive());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SortedMap<K, V> headMap(K toKey) {
            if (!inRange2(toKey)) {
                throw new IllegalArgumentException("toKey out of range");
            }

            return createRangeMap(getFromKey(), isFromInclusive(), toKey,
                    isToInclusive());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SortedMap<K, V> tailMap(K fromKey) {
            if (!inRange2(fromKey)) {
                throw new IllegalArgumentException("fromKey out of range");
            }

            return createRangeMap(fromKey, isFromInclusive(), getToKey(),
                    isToInclusive());
        }

        /**
         * 
         */
        protected boolean inRange(K key) {

            K fromKey = getFromKey();
            K toKey = getToKey();

            return (fromKey == null || inFromRange(key, false))
                    && (toKey == null || inToRange(key, false));
        }

        /**
         * This form allows the high endpoint (as well as all legit keys)
         */
        protected boolean inRange2(K key) {

            K fromKey = getFromKey();
            K toKey = getToKey();

            return (fromKey == null || inFromRange(key, false))
                    && (toKey == null || inToRange(key, true));
        }

        /**
         * 
         */
        protected boolean inFromRange(K key, boolean forceInclusive) {

            K fromKey = getFromKey();
            boolean fromInclusive = isFromInclusive();

            int ret = keyAnalyzer.compare(key, fromKey);
            if (fromInclusive || forceInclusive) {
                return ret >= 0;
            } else {
                return ret > 0;
            }
        }

        /**
         * 
         */
        protected boolean inToRange(K key, boolean forceInclusive) {

            K toKey = getToKey();
            boolean toInclusive = isToInclusive();

            int ret = keyAnalyzer.compare(key, toKey);
            if (toInclusive || forceInclusive) {
                return ret <= 0;
            } else {
                return ret < 0;
            }
        }

        /**
         * 
         */
        protected abstract SortedMap<K, V> createRangeMap(K fromKey,
                boolean fromInclusive, K toKey, boolean toInclusive);
    }
   
   /**
    * 
    */
   private class RangeEntryMap extends RangeMap {
       
       /** The key to start from, null if the beginning. */
       protected final K fromKey;
       
       /** The key to end at, null if till the end. */
       protected final K toKey;
       
       /** Whether or not the 'from' is inclusive. */
       protected final boolean fromInclusive;
       
       /** Whether or not the 'to' is inclusive. */
       protected final boolean toInclusive;
       
       /**
        * Creates a {@link RangeEntryMap} with the fromKey included and
        * the toKey excluded from the range
        */
       protected RangeEntryMap(K fromKey, K toKey) {
           this(fromKey, true, toKey, false);
       }
       
       /**
        * 
        */
       protected RangeEntryMap(K fromKey, boolean fromInclusive, 
               K toKey, boolean toInclusive) {
           
           if (fromKey == null && toKey == null) {
               throw new IllegalArgumentException("must have a from or to!");
           }
           
           if (fromKey != null && toKey != null 
                   && keyAnalyzer.compare(fromKey, toKey) > 0) {
               throw new IllegalArgumentException("fromKey > toKey");
           }
           
           this.fromKey = fromKey;
           this.fromInclusive = fromInclusive;
           this.toKey = toKey;
           this.toInclusive = toInclusive;
       }
       
       /**
        * {@inheritDoc}
        */
       @Override
       public K firstKey() {
           Map.Entry<K,V> e = null;
           if (fromKey == null) {
               e = firstEntry();
           } else {
               if (fromInclusive) {
                   e = ceilingEntry(fromKey);
               } else {
                   e = higherEntry(fromKey);
               }
           }
           
           K first = e != null ? e.getKey() : null;
           if (e == null || toKey != null && !inToRange(first, false)) {
               throw new NoSuchElementException();
           }
           return first;
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public K lastKey() {
           Map.Entry<K,V> e;
           if (toKey == null) {
               e = lastEntry();
           } else {
               if (toInclusive) {
                   e = floorEntry(toKey);
               } else {
                   e = lowerEntry(toKey);
               }
           }
           
           K last = e != null ? e.getKey() : null;
           if (e == null || fromKey != null && !inFromRange(last, false)) {
               throw new NoSuchElementException();
           }
           return last;
       }
       
       /**
        * {@inheritDoc}
        */
       @Override
       protected Set<Entry<K, V>> createEntrySet() {
           return new RangeEntrySet(this);
       }
       
       /**
        * {@inheritDoc}
        */
       @Override
       public K getFromKey() {
           return fromKey;
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public K getToKey() {
           return toKey;
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public boolean isFromInclusive() {
           return fromInclusive;
       }

       /**
        * {@inheritDoc}
        */
       @Override
       public boolean isToInclusive() {
           return toInclusive;
       }

       /**
        * {@inheritDoc}
        */
       @Override
       protected SortedMap<K, V> createRangeMap(K fromKey, boolean fromInclusive,
               K toKey, boolean toInclusive) {
           return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
       }
   }
   
   /**
    * 
    */
    private class RangeEntrySet extends AbstractSet<Map.Entry<K, V>> {

        private final RangeMap rangeMap;

        private transient int size = -1;

        private transient int expectedModCount;

        /**
         * 
         */
        public RangeEntrySet(RangeMap rangeMap) {
            if (rangeMap == null) {
                throw new NullPointerException("rangeMap");
            }

            this.rangeMap = rangeMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            K fromKey = rangeMap.getFromKey();
            K toKey = rangeMap.getToKey();

            TrieEntry<K, V> first = null;
            if (fromKey == null) {
                first = firstEntry();
            } else {
                first = ceilingEntry(fromKey);
            }

            TrieEntry<K, V> last = null;
            if (toKey != null) {
                last = ceilingEntry(toKey);
            }

            return new RangeEntrySetIterator(first, last);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            if (size == -1 || expectedModCount != PatriciaTrie.this.modCount) {
                size = 0;

                for (Iterator<?> it = iterator(); it.hasNext(); it.next()) {
                    ++size;
                }

                expectedModCount = PatriciaTrie.this.modCount;
            }
            return size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return !iterator().hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            if (!rangeMap.inRange(key)) {
                return false;
            }

            TrieEntry<K, V> node = getEntry(key);
            return node != null && TrieUtils.compare(
                    node.getValue(), entry.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            if (!rangeMap.inRange(key)) {
                return false;
            }

            TrieEntry<K, V> node = getEntry(key);
            if (node != null && TrieUtils.compare(
                    node.getValue(), entry.getValue())) {
                removeEntry(node);
                return true;
            }
            return false;
        }
        
        /** 
         * An iterator for submaps. 
         */
        private class RangeEntrySetIterator extends TrieIterator<Map.Entry<K,V>> {
            
            private final K excludedKey;

            /**
             * 
             */
            private RangeEntrySetIterator(
                    TrieEntry<K,V> first, 
                    TrieEntry<K,V> last) {
                super(first);
                
                this.excludedKey = (last != null ? last.getKey() : null);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return next != null && !TrieUtils.compare(next.key, excludedKey);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Map.Entry<K,V> next() {
                if (next == null || TrieUtils.compare(next.key, excludedKey)) {
                    throw new NoSuchElementException();
                }
                
                return nextEntry();
            }
        }
    }   
   
    /** 
     * A submap used for prefix views over the Trie. 
     */
    private class PrefixRangeMap extends RangeMap {
        
        private final K prefix;
        
        private final int offsetInBits;
        
        private final int lengthInBits;
        
        private K fromKey = null;
        
        private K toKey = null;
        
        private transient int expectedModCount = 0;
        
        private int size = -1;
        
        /**
         * 
         */
        private PrefixRangeMap(K prefix, int offsetInBits, int lengthInBits) {
            this.prefix = prefix;
            this.offsetInBits = offsetInBits;
            this.lengthInBits = lengthInBits;
        }
        
        /**
         * 
         */
        private int fixup() {
            // The trie has changed since we last
            // found our toKey / fromKey
            if (size == - 1 || PatriciaTrie.this.modCount != expectedModCount) {
                Iterator<Map.Entry<K, V>> it = entrySet().iterator();
                size = 0;
                
                Map.Entry<K, V> entry = null;
                if (it.hasNext()) {
                    entry = it.next();
                    size = 1;
                }
                
                fromKey = entry == null ? null : entry.getKey();
                if (fromKey != null) {
                    TrieEntry<K, V> prior = previousEntry((TrieEntry<K, V>)entry);
                    fromKey = prior == null ? null : prior.getKey();
                }
                
                toKey = fromKey;
                
                while (it.hasNext()) {
                    ++size;
                    entry = it.next();
                }
                
                toKey = entry == null ? null : entry.getKey();
                
                if (toKey != null) {
                    entry = nextEntry((TrieEntry<K, V>)entry);
                    toKey = entry == null ? null : entry.getKey();
                }
                
                expectedModCount = PatriciaTrie.this.modCount;
            }
            
            return size;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public K firstKey() {
            fixup();
            
            Map.Entry<K,V> e = null;
            if (fromKey == null) {
                e = firstEntry();
            } else {
                e = higherEntry(fromKey);
            }
            
            K first = e != null ? e.getKey() : null;
            if (e == null || !keyAnalyzer.isPrefix(prefix, 
                    offsetInBits, lengthInBits, first)) {
                throw new NoSuchElementException();
            }
            
            return first;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K lastKey() {
            fixup();
            
            Map.Entry<K,V> e = null;
            if (toKey == null) {
                e = lastEntry();
            } else {
                e = lowerEntry(toKey);
            }
            
            K last = e != null ? e.getKey() : null;
            if (e == null || !keyAnalyzer.isPrefix(prefix, 
                    offsetInBits, lengthInBits, last)) {
                throw new NoSuchElementException();
            }
            
            return last;
        }
        
        /**
         * 
         */
        protected boolean inRange(K key) {
            return keyAnalyzer.isPrefix(prefix, offsetInBits, lengthInBits, key);
        }

        /**
         * 
         */
        protected boolean inRange2(K key) {
            return keyAnalyzer.isPrefix(prefix, offsetInBits, lengthInBits, key);
        }
        
        /**
         * 
         */
        protected boolean inToRange(K key, boolean forceInclusive) {
            return keyAnalyzer.isPrefix(prefix, offsetInBits, lengthInBits, key);
        }
        
        /**
         * 
         */
        protected boolean inFromRange(K key, boolean forceInclusive) {
            return keyAnalyzer.isPrefix(prefix, offsetInBits, lengthInBits, key);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected Set<Map.Entry<K, V>> createEntrySet() {
            return new PrefixRangeEntrySet(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K getFromKey() {
            return fromKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K getToKey() {
            return toKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFromInclusive() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isToInclusive() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected SortedMap<K, V> createRangeMap(
                K fromKey, boolean fromInclusive,
                K toKey, boolean toInclusive) {
            return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
        }
    }
    
    /**
     * 
     */
    private class PrefixRangeEntrySet extends RangeEntrySet {
        
        private final PrefixRangeMap prefixRangeMap;
        
        private TrieEntry<K, V> prefixStart;
        
        private int expectedModCount = 0;
        
        public PrefixRangeEntrySet(PrefixRangeMap prefixRangeMap) {
            super(prefixRangeMap);
            this.prefixRangeMap = prefixRangeMap;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return prefixRangeMap.fixup();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Map.Entry<K,V>> iterator() {
            if (PatriciaTrie.this.modCount != expectedModCount) {
                prefixStart = subtree(prefixRangeMap.prefix, prefixRangeMap.offsetInBits, prefixRangeMap.lengthInBits);
                expectedModCount = PatriciaTrie.this.modCount;
            }
            
            if (prefixStart == null) {
                Set<Map.Entry<K,V>> empty = Collections.emptySet();
                return empty.iterator();
            } else if (prefixRangeMap.lengthInBits >= prefixStart.bitIndex) {
                return new SingletonIterator(prefixStart);
            } else {
                return new PrefixEntryIterator(prefixStart, prefixRangeMap.prefix, prefixRangeMap.offsetInBits, prefixRangeMap.lengthInBits);
            }
        }
        
        /** 
         * An iterator that stores a single TrieEntry. 
         */
        private class SingletonIterator implements Iterator<Map.Entry<K, V>> {
            
            private final TrieEntry<K, V> entry;
            
            private int hit = 0;
            
            public SingletonIterator(TrieEntry<K, V> entry) {
                this.entry = entry;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return hit == 0;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Map.Entry<K, V> next() {
                if (hit != 0) {
                    throw new NoSuchElementException();
                }
                
                hit++;
                return entry;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                if (hit != 1) {
                    throw new IllegalStateException();
                }
                
                hit++;
                PatriciaTrie.this.removeEntry(entry);
            }
        }
        
        /** 
         * An iterator for iterating over a prefix search. 
         */
        private class PrefixEntryIterator extends TrieIterator<Map.Entry<K, V>> {
            // values to reset the subtree if we remove it.
            protected final K prefix; 
            protected final int offset;
            protected final int lengthInBits;
            protected boolean lastOne;
            
            protected TrieEntry<K, V> subtree; // the subtree to search within
            
            /**
             * Starts iteration at the given entry & search only 
             * within the given subtree.
             */
            PrefixEntryIterator(TrieEntry<K, V> startScan, K prefix, 
                    int offset, int lengthInBits) {
                subtree = startScan;
                next = PatriciaTrie.this.followLeft(startScan);
                this.prefix = prefix;
                this.offset = offset;
                this.lengthInBits = lengthInBits;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Map.Entry<K,V> next() {
                Map.Entry<K, V> entry = nextEntry();
                if (lastOne) {
                    next = null;
                }
                return entry;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected TrieEntry<K, V> findNext(TrieEntry<K, V> prior) {
                return PatriciaTrie.this.nextEntryInSubtree(prior, subtree);
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                // If the current entry we're removing is the subtree
                // then we need to find a new subtree parent.
                boolean needsFixing = false;
                int bitIdx = subtree.bitIndex;
                if (current == subtree) {
                    needsFixing = true;
                }
                
                super.remove();
                
                // If the subtree changed its bitIndex or we
                // removed the old subtree, get a new one.
                if (bitIdx != subtree.bitIndex || needsFixing) {
                    subtree = subtree(prefix, offset, lengthInBits);
                }
                
                // If the subtree's bitIndex is less than the
                // length of our prefix, it's the last item
                // in the prefix tree.
                if (lengthInBits >= subtree.bitIndex) {
                    lastOne = true;
                }
            }
        }
    }
}
