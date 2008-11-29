package org.ardverk.collection;


public class IntegerKeyAnalyzer implements KeyAnalyzer<Integer> {
    
    private static final long serialVersionUID = 4928508653722068982L;

    public static int[] createIntBitMask(final int bitCount) {
        int[] bits = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            bits[i] = 1 << (bitCount - i - 1);
        }
        return bits;
    }

    private static final int[] BITS = createIntBitMask(32);

    public int length(Integer key) {
        return 32;
    }

    public boolean isBitSet(Integer key, int keyLength, int bitIndex) {
        return (key & BITS[bitIndex]) != 0;
    }

    public int bitIndex(Integer key,   int keyOff, int keyLength,
                        Integer found, int foundOff, int foundKeyLength) {
        if (found == null)
            found = 0;
        
        if(keyOff != 0 || foundOff != 0)
            throw new IllegalArgumentException("offsets must be 0 for fixed-size keys");

        boolean allNull = true;
        
        int length = Math.max(keyLength, foundKeyLength);
        
        for (int i = 0; i < length; i++) {
            int a = key & BITS[i];
            int b = found & BITS[i];

            if (allNull && a != 0) {
                allNull = false;
            }

            if (a != b) {
                return i;
            }
        }

        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }

    public int bitsPerElement() {
        return 1;
    }
    
    public boolean isPrefix(Integer prefix, int offset, int length, Integer key) {
        int addr1 = prefix;
        int addr2 = key;
        addr1 = addr1 << offset;
        
        int mask = 0;
        for(int i = 0; i < length; i++) {
            mask |= (0x1 << i);
        }
        
        addr1 &= mask;
        addr2 &= mask;
        
        return addr1 == addr2;
    }
}