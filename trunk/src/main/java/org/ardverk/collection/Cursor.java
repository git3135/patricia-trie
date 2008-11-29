package org.ardverk.collection;

import java.util.Map;

/**
 * 
 */
public interface Cursor<K, V> {
    
    /**
     *
     */
    public static enum SelectStatus {
        EXIT, CONTINUE, REMOVE, REMOVE_AND_EXIT;
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
    public SelectStatus select(Map.Entry<? extends K, ? extends V> entry);
}