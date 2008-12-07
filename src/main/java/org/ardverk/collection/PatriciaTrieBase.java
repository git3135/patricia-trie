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

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import org.ardverk.collection.Cursor.Decision;

/**
 * This is just an abstract base class for {@link PatriciaTrie}. It's
 * purpose is to reduce the size of the actual class.
 */
abstract class PatriciaTrieBase<K, V> extends AbstractMap<K, V> 
        implements Trie<K, V>, Serializable {
    
    private static final long serialVersionUID = 5155253417231339498L;

    /** The root element of the Trie. */
    final TrieEntry<K, V> root = new TrieEntry<K, V>(null, null, -1);
    
    /** The current size (total number of elements) of the Trie. */
    private int size = 0;
    
    /** The number of times this has been modified (to fail-fast the iterators). */
    transient int modCount = 0;
    
    protected final KeyAnalyzer<? super K> keyAnalyzer;
    
    /** 
     * Constructs a new {@link PatriciaTrieBase} using the 
     * given {@link KeyAnalyzer} 
     */
    public PatriciaTrieBase(KeyAnalyzer<? super K> keyAnalyzer) {
        if (keyAnalyzer == null) {
            throw new NullPointerException("keyAnalyzer");
        }
        
        this.keyAnalyzer = keyAnalyzer;
    }
    
    /**
     * Constructs a new {@link PatriciaTrieBase} using the 
     * given {@link KeyAnalyzer} and initializes the {@link Trie}
     * with the values from the provided {@link Map}.
     */
    public PatriciaTrieBase(KeyAnalyzer<? super K> keyAnalyzer, 
            Map<? extends K, ? extends V> m) {
        this(keyAnalyzer);
        
        if (m == null) {
            throw new NullPointerException("m");
        }
        
        putAll(m);
    }
    
    /**
     * Returns the {@link KeyAnalyzer} that constructed the {@link Trie}.
     */
    public KeyAnalyzer<? super K> getKeyAnalyzer() {
        return keyAnalyzer;
    }

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
    public void clear() {
        root.key = null;
        root.bitIndex = -1;
        root.value = null;
        
        root.parent = null;
        root.left = root;
        root.right = null;
        root.predecessor = root;
        
        size = 0;
        incrementModCount();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }
   
    /**
     * A helper method to increment the {@link Trie} size
     * and the modification counter.
     */
    private void incrementSize() {
        size++;
        incrementModCount();
    }
    
    /**
     * A helper method to decrement the {@link Trie} size
     * and increment the modification counter.
     */
    private void decrementSize() {
        size--;
        incrementModCount();
    }
    
    /**
     * A helper method to increment the modification counter.
     */
    private void incrementModCount() {
        modCount++;
    }
    
    /**
     * Adds a new <key, value> pair to the Trie and if a pair already
     * exists it will be replaced. In the latter case it will return
     * the old value.
     */
    @Override
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        
        int lengthInBits = lengthInBits(key);
        
        // The only place to store a key with a length
        // of zero bits is the root node
        if (lengthInBits == 0) {
            if (root.isEmpty()) {
                incrementSize();
            } else {
                incrementModCount();
            }
            return root.setKeyValue(key, value);
        }
        
        TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (key.equals(found.key)) {
            if (found.isEmpty()) { // <- must be the root
                incrementSize();
            } else {
                incrementModCount();
            }
            return found.setKeyValue(key, value);
        }
        
        int bitIndex = bitIndex(key, found.key);
        if (isValidBitIndex(bitIndex)) { // in 99.999...9% the case
            /* NEW KEY+VALUE TUPLE */
            TrieEntry<K, V> t = new TrieEntry<K, V>(key, value, bitIndex);
            addEntry(t, lengthInBits);
            incrementSize();
            return null;
        } else if (isNullBitKey(bitIndex)) {
            // A bits of the Key are zero. The only place to
            // store such a Key is the root Node!
            
            /* NULL BIT KEY */
            if (root.isEmpty()) {
                incrementSize();
            } else {
                incrementModCount();
            }
            return root.setKeyValue(key, value);
            
        } else if (isEqualBitKey(bitIndex)) {
            // This is a very special and rare case.
            
            /* REPLACE OLD KEY+VALUE */
            if (found != root) {
                incrementModCount();
                return found.setKeyValue(key, value);
            }
        }
        
        throw new IndexOutOfBoundsException("Failed to put: " + key + " -> " + value + ", " + bitIndex);
    }
    
    /**
     * Adds the given {@link TrieEntry} to the {@link Trie}
     */
    TrieEntry<K, V> addEntry(TrieEntry<K, V> toAdd, int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while(true) {
            if (current.bitIndex >= toAdd.bitIndex 
                    || current.bitIndex <= path.bitIndex) {
                toAdd.predecessor = toAdd;
                
                if (!isBitSet(toAdd.key, toAdd.bitIndex, lengthInBits)) {
                    toAdd.left = toAdd;
                    toAdd.right = current;
                } else {
                    toAdd.left = current;
                    toAdd.right = toAdd;
                }
               
                toAdd.parent = path;
                if (current.bitIndex >= toAdd.bitIndex) {
                    current.parent = toAdd;
                }
                
                // if we inserted an uplink, set the predecessor on it
                if (current.bitIndex <= path.bitIndex) {
                    current.predecessor = toAdd;
                }
         
                if (path == root || !isBitSet(toAdd.key, path.bitIndex, lengthInBits)) {
                    path.left = toAdd;
                } else {
                    path.right = toAdd;
                }
                
                return toAdd;
            }
                
            path = current;
            
            if (!isBitSet(toAdd.key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object k) {
        TrieEntry<K, V> entry = getEntry(k);
        return entry != null ? entry.getValue() : null;
    }

    /**
     * Returns the entry associated with the specified key in the
     * PatriciaTrie.  Returns null if the map contains no mapping
     * for this key.
     * 
     * This may throw ClassCastException if the object is not of type K.
     */
    TrieEntry<K,V> getEntry(Object k) {
        K key = castKey(k);
        if (key == null) {
            return null;
        }
        
        int lengthInBits = lengthInBits(key);
        TrieEntry<K,V> entry = getNearestEntryForKey(key, lengthInBits);
        return !entry.isEmpty() && key.equals(entry.key) ? entry : null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public K firstKey() {
        return firstEntry().getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K lastKey() {
        TrieEntry<K, V> entry = lastEntry();
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Map.Entry<K, V> select(K key) {
        int lengthInBits = lengthInBits(key);
        Reference<Map.Entry<K, V>> reference 
            = new Reference<Map.Entry<K,V>>();
        if (!selectR(root.left, -1, key, lengthInBits, reference)) {
            return reference.get();
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public K selectKey(K key) {
        Map.Entry<K, V> entry = select(key);
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }
    
    /**
     * {@inheritDoc}
     */
    public V selectValue(K key) {
        Map.Entry<K, V> entry = select(key);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    public Map.Entry<K,V> select(K key, Cursor<? super K, ? super V> cursor) {
        int lengthInBits = lengthInBits(key);
        Reference<Map.Entry<K, V>> reference 
            = new Reference<Map.Entry<K,V>>();
        selectR(root.left, -1, key, lengthInBits, cursor, reference);
        return reference.get();
    }

    /**
     * This is equivalent to the other selectR() method but without
     * its overhead because we're selecting only one best matching
     * Entry from the Trie.
     */
    private boolean selectR(TrieEntry<K, V> h, int bitIndex, 
            final K key, final int lengthInBits, 
            final Reference<Map.Entry<K, V>> reference) {
        
        if (h.bitIndex <= bitIndex) {
            // If we hit the root Node and it is empty
            // we have to look for an alternative best
            // matching node.
            if (!h.isEmpty()) {
                reference.set(h);
                return false;
            }
            return true;
        }

        if (!isBitSet(key, h.bitIndex, lengthInBits)) {
            if (selectR(h.left, h.bitIndex, key, lengthInBits, reference)) {
                return selectR(h.right, h.bitIndex, key, lengthInBits, reference);
            }
        } else {
            if (selectR(h.right, h.bitIndex, key, lengthInBits, reference)) {
                return selectR(h.left, h.bitIndex, key, lengthInBits, reference);
            }
        }
        return false;
    }
    
    /**
     * 
     */
    private boolean selectR(TrieEntry<K,V> h, int bitIndex, 
            final K key, 
            final int lengthInBits,
            final Cursor<? super K, ? super V> cursor,
            final Reference<Map.Entry<K, V>> reference) {

        if (h.bitIndex <= bitIndex) {
            if (!h.isEmpty()) {
                Decision decision = cursor.select(h);
                switch(decision) {
                    case REMOVE:
                        throw new UnsupportedOperationException("cannot remove during select");
                    case EXIT:
                        reference.set(h);
                        return false; // exit
                    case REMOVE_AND_EXIT:
                        TrieEntry<K, V> entry = new TrieEntry<K, V>(h.getKey(), h.getValue(), -1);
                        reference.set(entry);
                        removeEntry(h);
                        return false;
                    case CONTINUE:
                        // fall through.
                }
            }
            return true; // continue
        }

        if (!isBitSet(key, h.bitIndex, lengthInBits)) {
            if (selectR(h.left, h.bitIndex, key, lengthInBits, cursor, reference)) {
                return selectR(h.right, h.bitIndex, key, lengthInBits, cursor, reference);
            }
        } else {
            if (selectR(h.right, h.bitIndex, key, lengthInBits, cursor, reference)) {
                return selectR(h.left, h.bitIndex, key, lengthInBits, cursor, reference);
            }
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map.Entry<K, V> traverse(Cursor<? super K, ? super V> cursor) {
        TrieEntry<K, V> entry = nextEntry(null);
        while (entry != null) {
            TrieEntry<K, V> current = entry;
            
            Decision decision = cursor.select(current);
            entry = nextEntry(current);
            
            switch(decision) {
                case EXIT:
                    return current;
                case REMOVE:
                    removeEntry(current);
                    break; // out of switch, stay in while loop
                case REMOVE_AND_EXIT:
                    Map.Entry<K, V> value = new TrieEntry<K, V>(
                            current.getKey(), current.getValue(), -1);
                    removeEntry(current);
                    return value;
                case CONTINUE: // do nothing.
            }
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getPrefixedBy(K key) {
        return getPrefixedByBits(key, 0, lengthInBits(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getPrefixedBy(K key, int length) {
        return getPrefixedByBits(key, 0, length * bitsPerElement());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getPrefixedBy(K key, int offset, int length) {
        return getPrefixedByBits(key, offset * bitsPerElement(), length * bitsPerElement());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getPrefixedByBits(K key, int lengthInBits) {
        return getPrefixedByBits(key, 0, lengthInBits);
    }
    
    /**
     * Returns a view of this map, with entries containing only those that
     * are prefixed by a value whose bits matches the bits between 'offset'
     * and 'length' in the given key.
     * 
     * The view that this returns is optimized to have a very efficient
     * Iterator.  The firstKey, lastKey & size methods must iterate
     * over all possible values in order to determine the results.  This
     * information is cached until the Patricia tree changes.  All other
     * methods (except Iterator) must compare the given key to the prefix
     * to ensure that it is within the range of the view.  The Iterator's
     * remove method must also relocate the subtree that contains the
     * prefixes if the entry holding the subtree is removed or changes.
     * Changing the subtree takes O(K) time.
     * 
     * @param key
     * @param offsetInBits
     * @param lengthInBits
     */
    abstract SortedMap<K, V> getPrefixedByBits(K key, int offsetInBits, int lengthInBits);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object k) {
        if (k == null) {
            return false;
        }
        
        K key = castKey(k);
        int lengthInBits = lengthInBits(key);
        TrieEntry<?, ?> entry = getNearestEntryForKey(key, lengthInBits);
        return !entry.isEmpty() && key.equals(entry.key);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws ClassCastException
     */
    @Override
    public V remove(Object k) {
        if (k == null) {
            return null;
        }
        
        K key = castKey(k);
        int lengthInBits = lengthInBits(key);        
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            if (current.bitIndex <= path.bitIndex) {
                if (!current.isEmpty() && key.equals(current.key)) {
                    return removeEntry(current);
                } else {
                    return null;
                }
            }
            
            path = current;
            
            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }
    
    
    
    /**
     * Returns the nearest entry for a given key.  This is useful
     * for finding knowing if a given key exists (and finding the value
     * for it), or for inserting the key.
     * 
     * The actual get implementation. This is very similar to
     * selectR but with the exception that it might return the
     * root Entry even if it's empty.
     */
    TrieEntry<K, V> getNearestEntryForKey(K key, int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while(true) {
            if (current.bitIndex <= path.bitIndex) {
                return current;
            }
            
            path = current;
            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }
    
    /**
     * Removes a single entry from the Trie.
     * 
     * If we found a Key (Entry h) then figure out if it's
     * an internal (hard to remove) or external Entry (easy 
     * to remove)
     */
    V removeEntry(TrieEntry<K, V> h) {
        if (h != root) {
            if (h.isInternalNode()) {
                removeInternalEntry(h);
            } else {
                removeExternalEntry(h);
            }
        }
        
        decrementSize();
        return h.setKeyValue(null, null);
    }
    
    /**
     * Removes an external entry from the Trie.
     * 
     * If it's an external Entry then just remove it.
     * This is very easy and straight forward.
     */
    private void removeExternalEntry(TrieEntry<K, V> h) {
        if (h == root) {
            throw new IllegalArgumentException("Cannot delete root Entry!");
        } else if (!h.isExternalNode()) {
            throw new IllegalArgumentException(h + " is not an external Entry!");
        } 
        
        TrieEntry<K, V> parent = h.parent;
        TrieEntry<K, V> child = (h.left == h) ? h.right : h.left;
        
        if (parent.left == h) {
            parent.left = child;
        } else {
            parent.right = child;
        }
        
        // either the parent is changing, or the predecessor is changing.
        if (child.bitIndex > parent.bitIndex) {
            child.parent = parent;
        } else {
            child.predecessor = parent;
        }
        
    }
    
    /**
     * Removes an internal entry from the Trie.
     * 
     * If it's an internal Entry then "good luck" with understanding
     * this code. The Idea is essentially that Entry p takes Entry h's
     * place in the trie which requires some re-wiring.
     */
    private void removeInternalEntry(TrieEntry<K, V> h) {
        if (h == root) {
            throw new IllegalArgumentException("Cannot delete root Entry!");
        } else if (!h.isInternalNode()) {
            throw new IllegalArgumentException(h + " is not an internal Entry!");
        } 
        
        TrieEntry<K, V> p = h.predecessor;
        
        // Set P's bitIndex
        p.bitIndex = h.bitIndex;
        
        // Fix P's parent, predecessor and child Nodes
        {
            TrieEntry<K, V> parent = p.parent;
            TrieEntry<K, V> child = (p.left == h) ? p.right : p.left;
            
            // if it was looping to itself previously,
            // it will now be pointed from it's parent
            // (if we aren't removing it's parent --
            //  in that case, it remains looping to itself).
            // otherwise, it will continue to have the same
            // predecessor.
            if (p.predecessor == p && p.parent != h) {
                p.predecessor = p.parent;
            }
            
            if (parent.left == p) {
                parent.left = child;
            } else {
                parent.right = child;
            }
            
            if (child.bitIndex > parent.bitIndex) {
                child.parent = parent;
            }
        };
        
        // Fix H's parent and child Nodes
        {         
            // If H is a parent of its left and right child 
            // then change them to P
            if (h.left.parent == h) {
                h.left.parent = p;
            }
            
            if (h.right.parent == h) {
                h.right.parent = p;
            }
            
            // Change H's parent
            if (h.parent.left == h) {
                h.parent.left = p;
            } else {
                h.parent.right = p;
            }
        };
        
        // Copy the remaining fields from H to P
        //p.bitIndex = h.bitIndex;
        p.parent = h.parent;
        p.left = h.left;
        p.right = h.right;
        
        // Make sure that if h was pointing to any uplinks,
        // p now points to them.
        if (isValidUplink(p.left, p)) {
            p.left.predecessor = p;
        }
        
        if (isValidUplink(p.right, p)) {
            p.right.predecessor = p;
        }   
    }
    
    /**
     * Returns the node lexicographically before the given node (or null if none).
     * 
     * This follows four simple branches:
     *  - If the uplink that returned us was a right uplink:
     *      - If predecessor's left is a valid uplink from predecessor, return it.
     *      - Else, follow the right path from the predecessor's left.
     *  - If the uplink that returned us was a left uplink:
     *      - Loop back through parents until we encounter a node where 
     *        node != node.parent.left.
     *          - If node.parent.left is uplink from node.parent:
     *              - If node.parent.left is not root, return it.
     *              - If it is root & root isEmpty, return null.
     *              - If it is root & root !isEmpty, return root.
     *          - If node.parent.left is not uplink from node.parent:
     *              - Follow right path for first right child from node.parent.left   
     * 
     * @param start
     */
    TrieEntry<K, V> previousEntry(TrieEntry<K, V> start) {
        if (start.predecessor == null) {
            throw new IllegalArgumentException("must have come from somewhere!");
        }
        
        if (start.predecessor.right == start) {
            if (isValidUplink(start.predecessor.left, start.predecessor)) {
                return start.predecessor.left;
            } else {
                return followRight(start.predecessor.left);
            }
        } else {
            TrieEntry<K, V> node = start.predecessor;
            while (node.parent != null && node == node.parent.left) {
                node = node.parent;
            }
            
            if (node.parent == null) { // can be null if we're looking up root.
                return null;
            }
            
            if (isValidUplink(node.parent.left, node.parent)) {
                if (node.parent.left == root) {
                    if (root.isEmpty()) {
                        return null;
                    } else {
                        return root;
                    }
                    
                } else {
                    return node.parent.left;
                }
            } else {
                return followRight(node.parent.left);
            }
        }
    }
    
    /**
     * Returns the entry lexicographically after the given entry.
     * If the given entry is null, returns the first node.
     */
    TrieEntry<K, V> nextEntry(TrieEntry<K, V> node) {
        if (node == null) {
            return firstEntry();
        } else {
            return nextEntryImpl(node.predecessor, node, null);
        }
    }
    
    /**
     * Returns the entry lexicographically after the given entry.
     * If the given entry is null, returns the first node.
     * 
     * This will traverse only within the subtree.  If the given node
     * is not within the subtree, this will have undefined results.
     */
    TrieEntry<K, V> nextEntryInSubtree(TrieEntry<K, V> node, 
            TrieEntry<K, V> parentOfSubtree) {
        if (node == null) {
            return firstEntry();
        } else {
            return nextEntryImpl(node.predecessor, node, parentOfSubtree);
        }
    }
    
    /**
     * Scans for the next node, starting at the specified point, and using 'previous'
     * as a hint that the last node we returned was 'previous' (so we know not to return
     * it again).  If 'tree' is non-null, this will limit the search to the given tree.
     * 
     * The basic premise is that each iteration can follow the following steps:
     * 
     * 1) Scan all the way to the left.
     *   a) If we already started from this node last time, proceed to Step 2.
     *   b) If a valid uplink is found, use it.
     *   c) If the result is an empty node (root not set), break the scan.
     *   d) If we already returned the left node, break the scan.
     *   
     * 2) Check the right.
     *   a) If we already returned the right node, proceed to Step 3.
     *   b) If it is a valid uplink, use it.
     *   c) Do Step 1 from the right node.
     *   
     * 3) Back up through the parents until we encounter find a parent
     *    that we're not the right child of.
     *    
     * 4) If there's no right child of that parent, the iteration is finished.
     *    Otherwise continue to Step 5.
     * 
     * 5) Check to see if the right child is a valid uplink.
     *    a) If we already returned that child, proceed to Step 6.
     *       Otherwise, use it.
     *    
     * 6) If the right child of the parent is the parent itself, we've
     *    already found & returned the end of the Trie, so exit.
     *    
     * 7) Do Step 1 on the parent's right child.
     */
    private TrieEntry<K, V> nextEntryImpl(TrieEntry<K, V> start, TrieEntry<K, V> previous, TrieEntry<K, V> tree) {
        TrieEntry<K, V> current = start;

        // Only look at the left if this was a recursive or
        // the first check, otherwise we know we've already looked
        // at the left.
        if (previous == null || start != previous.predecessor) {
            while (!current.left.isEmpty()) {
                // stop traversing if we've already
                // returned the left of this node.
                if (previous == current.left) {
                    break;
                }
                
                if (isValidUplink(current.left, current)) {
                    return current.left;
                }
                
                current = current.left;
            }
        }
        
        // If there's no data at all, exit.
        if (current.isEmpty()) {
            return null;
        }
        
        // If we've already returned the left,
        // and the immediate right is null,
        // there's only one entry in the Trie
        // which is stored at the root.
        //
        //  / ("")   <-- root
        //  \_/  \
        //       null <-- 'current'
        //
        if (current.right == null) {
            return null;
        }
        
        // If nothing valid on the left, try the right.
        if (previous != current.right) {
            // See if it immediately is valid.
            if (isValidUplink(current.right, current)) {
                return current.right;
            }
            
            // Must search on the right's side if it wasn't initially valid.
            return nextEntryImpl(current.right, previous, tree);
        }
        
        // Neither left nor right are valid, find the first parent
        // whose child did not come from the right & traverse it.
        while (current == current.parent.right) {
            // If we're going to traverse to above the subtree, stop.
            if (current == tree) {
                return null;
            }
            
            current = current.parent;
        }

        // If we're on the top of the subtree, we can't go any higher.
        if (current == tree) {
            return null;
        }
        
        // If there's no right, the parent must be root, so we're done.
        if (current.parent.right == null) {
            return null;
        }
        
        // If the parent's right points to itself, we've found one.
        if (previous != current.parent.right 
                && isValidUplink(current.parent.right, current.parent)) {
            return current.parent.right;
        }
        
        // If the parent's right is itself, there can't be any more nodes.
        if (current.parent.right == current.parent) {
            return null;
        }
        
        // We need to traverse down the parent's right's path.
        return nextEntryImpl(current.parent.right, previous, tree);
    }
    
    /**
     * Returns the first entry the Trie is storing.
     * 
     * This is implemented by going always to the left until
     * we encounter a valid uplink. That uplink is the first key.
     */
    TrieEntry<K, V> firstEntry() {
        // if Trie is empty, no first node.
        if (isEmpty()) {
            return null;
        }
        
        return followLeft(root);
    }
    
    /** Goes left through the tree until it finds a valid node. */
    TrieEntry<K, V> followLeft(TrieEntry<K, V> node) {
        while(true) {
            TrieEntry<K, V> child = node.left;
            // if we hit root and it didn't have a node, go right instead.
            if (child.isEmpty()) {
                child = node.right;
            }
            
            if (child.bitIndex <= node.bitIndex) {
                return child;
            }
            
            node = child;
        }
    }
    
    /**
     * Returns the last entry the Trie is storing.
     * <p>
     * This is implemented by going always to the right until
     * we encounter a valid uplink. That uplink is the last key.
     */
    TrieEntry<K, V> lastEntry() {
        return followRight(root.left);
    }
    
    /** Traverses down the right path until it finds an uplink. */
    TrieEntry<K, V> followRight(TrieEntry<K, V> node) {
        // if Trie is empty, no last entry.
        if (node.right == null) {
            return null;
        }
        
        // Go as far right as possible, until we encounter an uplink.
        while (node.right.bitIndex > node.bitIndex) {
            node = node.right;
        }
        
        return node.right;
    }

    /**
     * A utility method to cast keys
     */
    @SuppressWarnings("unchecked")
    final K castKey(Object key) {
        return (K)key;
    }
    
    /**
     * Returns the length of the given key in bits
     * 
     * @see KeyAnalyzer#lengthInBits(Object)
     */
    final int lengthInBits(K key) {
        if (key == null) {
            return 0;
        }
        
        return keyAnalyzer.lengthInBits(key);
    }
    
    /**
     * Returns the number of bits per element in the key
     * 
     * @see KeyAnalyzer#bitsPerElement()
     */
    final int bitsPerElement() {
        return keyAnalyzer.bitsPerElement();
    }
    
    /**
     * Returns whether or not the given bit on the 
     * key is set or false if the key is null.
     * 
     * @see KeyAnalyzer#isBitSet(Object, int, int)
     */
    final boolean isBitSet(K key, int bitIndex, int lengthInBits) {
        if (key == null) { // root's might be null!
            return false;
        }
        return keyAnalyzer.isBitSet(key, bitIndex, lengthInBits);
    }
    
    /**
     * Returns an entry strictly higher than the given key,
     * or null if no such entry exists.
     */
    TrieEntry<K,V> higherEntry(K key) {
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined 
        //       functions to perform the search.)
        int lengthInBits = lengthInBits(key);
        
        if (lengthInBits == 0) {
            if (!root.isEmpty()) {
                // If data in root, and more after -- return it.
                if (size() > 1) {
                    return nextEntry(root);
                } else { // If no more after, no higher entry.
                    return null;
                }
            } else {
                // Root is empty & we want something after empty, return first.
                return firstEntry();
            }
        }
        
        TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (key.equals(found.key)) {
            return nextEntry(found);
        }
        
        int bitIndex = bitIndex(key, found.key);
        if (isValidBitIndex(bitIndex)) {
            TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            TrieEntry<K, V> ceil = nextEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return ceil;
        } else if (isNullBitKey(bitIndex)) {
            if (!root.isEmpty()) {
                return firstEntry();
            } else if (size() > 1) {
                return nextEntry(firstEntry());
            } else {
                return null;
            }
        } else if (isEqualBitKey(bitIndex)) {
            return nextEntry(found);
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }
    
    /**
     * Returns a key-value mapping associated with the least key greater
     * than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K,V> ceilingEntry(K key) {
        // Basically:
        // Follow the steps of adding an entry, but instead...
        //
        // - If we ever encounter a situation where we found an equal
        //   key, we return it immediately.
        //
        // - If we hit an empty root, return the first iterable item.
        //
        // - If we have to add a new item, we temporarily add it,
        //   find the successor to it, then remove the added item.
        //
        // These steps ensure that the returned value is either the
        // entry for the key itself, or the first entry directly after
        // the key.
        
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined 
        //       functions to perform the search.)
        int lengthInBits = lengthInBits(key);
        
        if (lengthInBits == 0) {
            if (!root.isEmpty()) {
                return root;
            } else {
                return firstEntry();
            }
        }
        
        TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (key.equals(found.key)) {
            return found;
        }
        
        int bitIndex = bitIndex(key, found.key);
        if (isValidBitIndex(bitIndex)) {
            TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            TrieEntry<K, V> ceil = nextEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return ceil;
        } else if (isNullBitKey(bitIndex)) {
            if (!root.isEmpty()) {
                return root;
            } else {
                return firstEntry();
            }
        } else if (isEqualBitKey(bitIndex)) {
            return found;
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }
    
    /**
     * Returns a key-value mapping associated with the greatest key
     * strictly less than the given key, or null if there is no such key.
     */
    TrieEntry<K,V> lowerEntry(K key) {
        // Basically:
        // Follow the steps of adding an entry, but instead...
        //
        // - If we ever encounter a situation where we found an equal
        //   key, we return it's previousEntry immediately.
        //
        // - If we hit root (empty or not), return null.
        //
        // - If we have to add a new item, we temporarily add it,
        //   find the previousEntry to it, then remove the added item.
        //
        // These steps ensure that the returned value is always just before
        // the key or null (if there was nothing before it).
        
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined 
        //       functions to perform the search.)
        int lengthInBits = lengthInBits(key);
        
        if (lengthInBits == 0) {
            return null; // there can never be anything before root.
        }
        
        TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (key.equals(found.key)) {
            return previousEntry(found);
        }
        
        int bitIndex = bitIndex(key, found.key);
        if (isValidBitIndex(bitIndex)) {
            TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            TrieEntry<K, V> prior = previousEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return prior;
        } else if (isNullBitKey(bitIndex)) {
            return null;
        } else if (isEqualBitKey(bitIndex)) {
            return previousEntry(found);
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }
    
    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K,V> floorEntry(K key) {        
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined 
        //       functions to perform the search.)
        int lengthInBits = lengthInBits(key);
        
        if (lengthInBits == 0) {
            if (!root.isEmpty()) {
                return root;
            } else {
                return null;
            }
        }
        
        TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (key.equals(found.key)) {
            return found;
        }
        
        int bitIndex = bitIndex(key, found.key);
        if (isValidBitIndex(bitIndex)) {
            TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            TrieEntry<K, V> floor = previousEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return floor;
        } else if (isNullBitKey(bitIndex)) {
            if (!root.isEmpty()) {
                return root;
            } else {
                return null;
            }
        } else if (isEqualBitKey(bitIndex)) {
            return found;
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }
    
    /**
     * Finds the subtree that contains the prefix.
     * 
     * This is very similar to getR but with the difference that
     * we stop the lookup if h.bitIndex > lengthInBits.
     */
    TrieEntry<K, V> subtree(K prefix, int offsetInBits, int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while(true) {
            if (current.bitIndex <= path.bitIndex 
                    || lengthInBits < current.bitIndex) {
                break;
            }
            
            path = current;
            if (!isBitSet(prefix, offsetInBits + current.bitIndex, 
                    offsetInBits + lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }        

        // Make sure the entry is valid for a subtree.
        TrieEntry<K, V> entry = current.isEmpty() ? path : current;
        
        // If entry is root, it can't be empty.
        if (entry.isEmpty()) {
            return null;
        }
        
        int offsetLength = offsetInBits + lengthInBits;
        
        // if root && length of root is less than length of lookup,
        // there's nothing.
        // (this prevents returning the whole subtree if root has an empty
        //  string and we want to lookup things with "\0")
        if (entry == root && lengthInBits(entry.getKey()) < offsetLength) {
            return null;
        }
        
        // Found key's length-th bit differs from our key
        // which means it cannot be the prefix...
        if (isBitSet(prefix, offsetLength, offsetLength) 
                != isBitSet(entry.key, lengthInBits, lengthInBits(entry.key))) {
            return null;
        }
        
        // ... or there are less than 'length' equal bits
        int bitIndex = keyAnalyzer.bitIndex(prefix, offsetInBits, 
                lengthInBits, entry.key, 0, lengthInBits(entry.getKey()));
        
        if (bitIndex >= 0 && bitIndex < lengthInBits) {
            return null;
        }
        
        return entry;
    }
    
    /**
     * Utility method for calling {@link KeyAnalyzer#bitIndex(Object, int, int, Object, int, int)}
     */
    final int bitIndex(K key, K foundKey) {
        return keyAnalyzer.bitIndex(key, 0, lengthInBits(key), 
                foundKey, 0, lengthInBits(foundKey));
    }
    
    /** 
     * Returns true if the given bitIndex is valid. Indices 
     * are considered valid if they're between 0 and 
     * {@link Integer#MAX_VALUE}
     */
    static boolean isValidBitIndex(int bitIndex) {
        return 0 <= bitIndex && bitIndex <= Integer.MAX_VALUE;
    }
    
    /** 
     * Returns true if bitIndex is a {@link KeyAnalyzer#NULL_BIT_KEY} 
     */
    static boolean isNullBitKey(int bitIndex) {
        return bitIndex == KeyAnalyzer.NULL_BIT_KEY;
    }
    
    /** 
     * Returns true if bitIndex is a {@link KeyAnalyzer#EQUAL_BIT_KEY}
     */
    static boolean isEqualBitKey(int bitIndex) {
        return bitIndex == KeyAnalyzer.EQUAL_BIT_KEY;
    }
    
    /** 
     * Returns true if 'next' is a valid uplink coming from 'from'. 
     */
    static boolean isValidUplink(TrieEntry<?, ?> next, TrieEntry<?, ?> from) { 
        return next != null && next.bitIndex <= from.bitIndex && !next.isEmpty();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Trie[").append(size()).append("]={\n");
        for (Map.Entry<K, V> entry : entrySet()) {
            buffer.append("  ").append(entry).append("\n");
        }
        buffer.append("}\n");
        return buffer.toString();
    }
    
    /**
     * A {@link Reference} allows us to return something through a Method's 
     * argument list. An alternative would be to an Array with a length of 
     * one (1) but that leads to compiler warnings. Computationally and memory
     * wise there's no difference (except for the need to load the 
     * {@link Reference} Class but that happens only once).
     */
    static class Reference<E> {
        
        private E item;
        
        public void set(E item) {
            this.item = item;
        }
        
        public E get() {
            return item;
        }
    }
    
    /**
     * A basic implementation of {@link Entry}
     */
    static class SimpleEntry<K, V> implements Map.Entry<K, V>, Serializable {
        
        private static final long serialVersionUID = -944364551314110330L;

        protected K key;
        
        protected V value;
        
        private final int hashCode;
        
        public SimpleEntry(K key) {
            this.key = key;
            
            this.hashCode = (key != null ? key.hashCode() : 0);
        }
        
        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
            
            this.hashCode = (key != null ? key.hashCode() : 0)
                    ^ (value != null ? value.hashCode() : 0);
        }
        
        public boolean compareKey(K other) {
            return TrieUtils.compare(key, other);
        }
        
        public boolean compareValue(V other) {
            return TrieUtils.compare(value, other);
        }
        
        public K setKey(K key) {
            K previous = this.key;
            this.key = key;
            return previous;
        }
        
        public V setKeyValue(K key, V value) {
            setKey(key);
            return setValue(value);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public K getKey() {
            return key;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V setValue(V value) {
            V previous = this.value;
            this.value = value;
            return previous;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Map.Entry)) {
                return false;
            }
            
            Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
    
    /**
     *  A {@link Trie} is a set of {@link TrieEntry} nodes
     */
    static class TrieEntry<K,V> extends SimpleEntry<K, V> {
        
        private static final long serialVersionUID = 4596023148184140013L;
        
        /** The index this entry is comparing. */
        protected int bitIndex;
        
        /** The parent of this entry. */
        protected TrieEntry<K,V> parent;
        
        /** The left child of this entry. */
        protected TrieEntry<K,V> left;
        
        /** The right child of this entry. */
        protected TrieEntry<K,V> right;
        
        /** The entry who uplinks to this entry. */ 
        protected TrieEntry<K,V> predecessor;
        
        public TrieEntry(K key, V value, int bitIndex) {
            super(key, value);
            
            this.bitIndex = bitIndex;
            
            this.parent = null;
            this.left = this;
            this.right = null;
            this.predecessor = this;
        }
        
        /**
         * Whether or not the entry is storing a key.
         * Only the root can potentially be empty, all other
         * nodes must have a key.
         */
        public boolean isEmpty() {
            return key == null;
        }
        
        /** 
         * Neither the left nor right child is a loopback 
         */
        public boolean isInternalNode() {
            return left != this && right != this;
        }
        
        /** 
         * Either the left or right child is a loopback 
         */
        public boolean isExternalNode() {
            return !isInternalNode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            
            if (bitIndex == -1) {
                buffer.append("RootEntry(");
            } else {
                buffer.append("Entry(");
            }
            
            buffer.append("key=").append(getKey()).append(" [").append(bitIndex).append("], ");
            buffer.append("value=").append(getValue()).append(", ");
            //buffer.append("bitIndex=").append(bitIndex).append(", ");
            
            if (parent != null) {
                if (parent.bitIndex == -1) {
                    buffer.append("parent=").append("ROOT");
                } else {
                    buffer.append("parent=").append(parent.getKey()).append(" [").append(parent.bitIndex).append("]");
                }
            } else {
                buffer.append("parent=").append("null");
            }
            buffer.append(", ");
            
            if (left != null) {
                if (left.bitIndex == -1) {
                    buffer.append("left=").append("ROOT");
                } else {
                    buffer.append("left=").append(left.getKey()).append(" [").append(left.bitIndex).append("]");
                }
            } else {
                buffer.append("left=").append("null");
            }
            buffer.append(", ");
            
            if (right != null) {
                if (right.bitIndex == -1) {
                    buffer.append("right=").append("ROOT");
                } else {
                    buffer.append("right=").append(right.getKey()).append(" [").append(right.bitIndex).append("]");
                }
            } else {
                buffer.append("right=").append("null");
            }
            buffer.append(", ");
            
            if (predecessor != null) {
                if(predecessor.bitIndex == -1) {
                    buffer.append("predecessor=").append("ROOT");
                } else {
                    buffer.append("predecessor=").append(predecessor.getKey()).append(" [").append(predecessor.bitIndex).append("]");
                }
            }
            
            buffer.append(")");
            return buffer.toString();
        }
    }
}
