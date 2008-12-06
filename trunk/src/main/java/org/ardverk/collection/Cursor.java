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

/**
 * A {@link Cursor} can be used to traverse a {@link Trie}, visit each node 
 * step by step and make {@link Decision}s on each step how to continue with 
 * traversing the {@link Trie}.
 */
public interface Cursor<K, V> {
    
    /**
     * The {@link Decision} tells the {@link Cursor} what to do on each step 
     * while traversing the {@link Trie}.
     * 
     * NOTE: Not all operations that work with a {@link Cursor} support all 
     * {@link Decision} types
     */
    public static enum Decision {
        
        /**
         * Exit the traverse operation
         */
        EXIT, 
        
        /**
         * Continue with the traverse operation
         */
        CONTINUE, 
        
        /**
         * Remove the previously returned element
         * from the {@link Trie} and continue
         */
        REMOVE, 
        
        /**
         * Remove the previously returned element
         * from the {@link Trie} and exit from the
         * traverse operation
         */
        REMOVE_AND_EXIT;
    }
    
    /**
     * Notification that the Trie is currently looking at the given entry.
     * Return <code>EXIT</code> to finish the Trie operation, 
     * <code>CONTINUE</code> to look at the next entry, <code>REMOVE</code>
     * to remove the entry and continue iterating, or
     * <code>REMOVE_AND_EXIT</code> to remove the entry and stop iterating. 
     * Not all operations support <code>REMOVE</code>.
     * 
     */
    public Decision select(Map.Entry<? extends K, ? extends V> entry);
}