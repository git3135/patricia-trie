package org.ardverk.collection;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;

abstract class AbstractPatriciaTrie<K, V> extends AbstractMap<K, V> 
        implements Trie<K, V>, Serializable {
    
    private static final long serialVersionUID = 5155253417231339498L;

    protected final KeyAnalyzer<? super K> keyAnalyzer;
    
    /** Constructs a new PatriciaTrie using the given keyAnalyzer. */
    public AbstractPatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer) {
        if (keyAnalyzer == null) {
            throw new NullPointerException("keyAnalyzer");
        }
        
        this.keyAnalyzer = keyAnalyzer;
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
    
    /** Gets the key as a 'K'. */
    protected final K asKey(Object key) {
        return (K)key;
    }
    
    /** Returns true if bitIndex is a valid index */
    protected static boolean isValidBitIndex(int bitIndex) {
        return 0 <= bitIndex && bitIndex <= Integer.MAX_VALUE;
    }
    
    /** Returns true if bitIndex is a NULL_BIT_KEY */
    protected static boolean isNullBitKey(int bitIndex) {
        return bitIndex == KeyAnalyzer.NULL_BIT_KEY;
    }
    
    /** Returns true if bitIndex is a EQUAL_BIT_KEY */
    protected static boolean isEqualBitKey(int bitIndex) {
        return bitIndex == KeyAnalyzer.EQUAL_BIT_KEY;
    }
    
    /**
     * Returns the length of the given key in bits
     */
    protected int lengthInBits(K key) {
        if (key == null) {
            return 0;
        }
        
        return keyAnalyzer.lengthInBits(key);
    }
    
    protected int bitsPerElement() {
        return keyAnalyzer.bitsPerElement();
    }
    
    /**
     * Returns whether or not the given bit on the 
     * key is set, or false if the key is null
     */
    protected boolean isBitSet(K key, int lengthInBits, int bitIndex) {
        if (key == null) { // root's might be null!
            return false;
        }
        return keyAnalyzer.isBitSet(key, lengthInBits, bitIndex);
    }
    
    /**
     * Utility method for calling
     * keyAnalyzer.bitIndex(key, 0, length(key), foundKey, 0, length(foundKey))
     */
    protected int bitIndex(K key, K foundKey) {
        return keyAnalyzer.bitIndex(key, 0, lengthInBits(key), foundKey, 0, lengthInBits(foundKey));
    }
    
    /**
     * An utility method to compare values.
     */
    protected static boolean compareValues(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
}
