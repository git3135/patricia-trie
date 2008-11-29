package org.ardverk.collection;

public abstract class AbstractKeyAnalyzer<K> implements KeyAnalyzer<K> {
    
    private static final long serialVersionUID = 384115794654304353L;

    protected static final int[] createIntBitMask(int bitCount) {
        int[] bits = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            bits[i] = 1 << (bitCount - i - 1);
        }
        return bits;
    }
}
