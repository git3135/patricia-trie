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

import java.util.Map;
import java.util.SortedMap;

/**
 * Defines the interface for a prefix tree, an ordered tree data structure. For 
 * more information, see <a href="http://en.wikipedia.org/wiki/Trie">Tries</a>.
 * 
 * @author Roger Kapsi
 * @author Sam Berlin
 */
public interface Trie<K, V> extends SortedMap<K, V> {
    
    /**
     * Returns a view of this Trie of all elements that are
     * prefixed by the given key.
     * <p>
     * In a fixed-keysize Trie, this is essentially a 'get' operation.
     * <p>
     * For example, if the Trie contains 'Lime', 'LimeWire', 
     * 'LimeRadio', 'Lax', 'Later', 'Lake', and 'Lovely', then
     * a lookup of 'Lime' would return 'Lime', 'LimeRadio', and 'LimeWire'.
     */
    public SortedMap<K, V> getPrefixedBy(K key);
    
    /**
     * Returns a view of this Trie of all elements that are
     * prefixed by the length of the key.
     * <p>
     * Fixed-keysize Tries will not support this operation
     * (because all keys will be the same length).
     * <p>
     * For example, if the Trie contains 'Lime', 'LimeWire', 
     * 'LimeRadio', 'Lax', 'Later', 'Lake', and 'Lovely', then
     * a lookup of 'LimePlastics' with a length of 4 would
     * return 'Lime', 'LimeRadio', and 'LimeWire'.
     */
    public SortedMap<K, V> getPrefixedBy(K key, int length);
    
    /**
     * Returns a view of this Trie of all elements that are prefixed
     * by the key, starting at the given offset and for the given length.
     * <p>
     * Fixed-keysize Tries will not support this operation
     * (because all keys are the same length).
     * <p>
     * For example, if the Trie contains 'Lime', 'LimeWire', 
     * 'LimeRadio', 'Lax', 'Later', 'Lake', and 'Lovely', then
     * a lookup of 'The Lime Plastics' with an offset of 4 and a 
     * length of 4 would return 'Lime', 'LimeRadio', and 'LimeWire'.
     */
    public SortedMap<K, V> getPrefixedBy(K key, int offset, int length);
    
    /**
     * Returns a view of this Trie of all elements that are prefixed
     * by the number of bits in the given Key.
     * <p>
     * Fixed-keysize Tries can support this operation as a way to do
     * lookups of partial keys.  That is, if the Trie is storing IP
     * addresses, you can lookup all addresses that begin with
     * '192.168' by providing the key '192.168.X.X' and a length of 16
     * would return all addresses that begin with '192.168'.
     */
    public SortedMap<K, V> getPrefixedByBits(K key, int lengthInBits);
    
    /**
     * Returns the value for the entry whose key is closest in a bitwise
     * XOR metric to the given key.  This is NOT lexicographic closeness.
     * For example, given the keys:<br>
     *  D = 1000100 <br>
     *  H = 1001000 <br> 
     *  L = 1001100 <br>
     * <p>
     * If the Trie contained 'H' and 'L', a lookup of 'D' would return 'L',
     * because the XOR distance between D & L is smaller than the XOR distance 
     * between D & H. 
     */
    public Map.Entry<K, V> select(K key);
    
    /**
     * Returns the {@link Entry} for the entry whose key is closest in a 
     * bitwise XOR metric to the given key. This is NOT lexicographic 
     * closeness!
     * 
     * For example, given the keys:<br>
     *  D = 1000100 <br>
     *  H = 1001000 <br> 
     *  L = 1001100 <br>
     * <p>
     * If the {@link Trie} contained 'H' and 'L', a lookup of 'D' would 
     * return 'L', because the XOR distance between D & L is smaller 
     * than the XOR distance between D & H. 
     */
    public K selectKey(K key);
    
    /**
     * Returns the value for the entry whose key is closest in a bitwise
     * XOR metric to the given key. This is NOT lexicographic closeness!
     * 
     * For example, given the keys:<br>
     *  D = 1000100 <br>
     *  H = 1001000 <br> 
     *  L = 1001100 <br>
     * <p>
     * If the {@link Trie} contained 'H' and 'L', a lookup of 'D' would 
     * return 'L', because the XOR distance between D & L is smaller 
     * than the XOR distance between D & H. 
     */
    public V selectValue(K key);
    
    /**
     * Iterates through the Trie, starting with the entry whose bitwise
     * value is closest in an XOR metric to the given key.  After the closest
     * entry is found, the Trie will call select on that entry and continue
     * calling select for each entry (traversing in order of XOR closeness,
     * NOT lexicographically) until the cursor returns 
     * <code>Cursor.SelectStatus.EXIT</code>.<br>
     * The cursor can return <code>Cursor.SelectStatus.CONTINUE</code> to 
     * continue traversing.<br>
     * <code>Cursor.SelectStatus.REMOVE_AND_EXIT</code> is used to remove the current element
     * and stop traversing.
     * <p>
     * Note: The {@link Cursor.Decision#REMOVE} operation is not supported.
     * 
     * @return The entry the cursor returned EXIT on, or null if it continued
     *         till the end.
     */
    public Map.Entry<K,V> select(K key, Cursor<? super K, ? super V> cursor);
    
    /**
     * Traverses the Trie in lexicographical order. <code>Cursor.select</code> 
     * will be called on each entry.<p>
     * The traversal will stop when the cursor returns <code>Cursor.SelectStatus.EXIT</code>.<br>
     * <code>Cursor.SelectStatus.CONTINUE</code> is used to continue traversing.<br>
     * <code>Cursor.SelectStatus.REMOVE</code> is used to remove the element that was 
     * selected and continue traversing.<br>
     * <code>Cursor.SelectStatus.REMOVE_AND_EXIT</code> is used to remove the current element
     * and stop traversing.
     *   
     * @return The entry the cursor returned EXIT on, or null if it continued
     *         till the end.
     */
    public Map.Entry<K,V> traverse(Cursor<? super K, ? super V> cursor);
}

