package org.ardverk.collection;

/**
 * 
 */
class TrieUtils {

    private TrieUtils() {}
    
    /**
     * 
     */
    static boolean compare(Object a, Object b) {
        return (a == null ? b == null : a.equals(b));
    }
}
