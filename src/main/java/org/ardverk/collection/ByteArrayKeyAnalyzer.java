/*
 * Copyright 2005-2009 Roger Kapsi
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


/**
 * A {@link KeyAnalyzer} for byte[]s
 */
class ByteArrayKeyAnalyzer implements KeyAnalyzer<byte[]> {
    
    private static final long serialVersionUID = 7382825097492285877L;

    /**
     * A singleton instance of {@link ByteArrayKeyAnalyzer}
     */
    public static final ByteArrayKeyAnalyzer INSTANCE = new ByteArrayKeyAnalyzer();
    
    /**
     * The length of an {@link Byte} in bits
     */
    public static final int LENGTH = Byte.SIZE;
    
    /**
     * A bit mask where the first bit is 1 and the others are zero
     */
    private static final int MSB = 0x80;
    
    private static final byte[] NULL = new byte[0];
    
    /**
     * Returns a bit mask where the given bit is set
     */
    private static int mask(int bit) {
        return MSB >>> bit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitsPerElement() {
        return LENGTH;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int lengthInBits(byte[] key) {
        return key.length * bitsPerElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(byte[] key, int bitIndex, int lengthInBits) {
        if (key == null || bitIndex >= lengthInBits) {
            return false;
        }
        
        int index = (int)(bitIndex / LENGTH);
        int bit = (int)(bitIndex % LENGTH);
        
        return (key[index] & mask(bit)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitIndex(byte[] key, int offsetInBits, int lengthInBits, 
            byte[] other, int otherOffsetInBits, int otherLengthInBits) {
        
        if (other == null) {
            other = NULL;
        }
        
        int beginIndex1 = (int)(offsetInBits / LENGTH);
        int beginIndex2 = (int)(otherOffsetInBits / LENGTH);
        
        int endIndex1 = beginIndex1 + lengthInBits / LENGTH;
        int endIndex2 = beginIndex2 + lengthInBits / LENGTH;
        
        if (beginIndex1 < 0 || endIndex1 >= key.length 
                || endIndex1 < beginIndex1 || beginIndex2 < 0 
                || endIndex2 >= other.length || endIndex2 < beginIndex2) {
            throw new IndexOutOfBoundsException("key.length=" + key.length 
                    + ", offsetInBits=" + offsetInBits 
                    + ", lengthInBits=" + lengthInBits
                    + ", other.length=" + other.length
                    + ", otherOffsetInBits=" + otherOffsetInBits
                    + ", otherLengthInBits=" + otherLengthInBits);
        }
        
        boolean allNull = true;
        while (offsetInBits < lengthInBits) {
            if (isBitSet(key, offsetInBits, lengthInBits)) {
                allNull = false;
                
                if (!isBitSet(other, otherOffsetInBits, otherLengthInBits)) {
                    return offsetInBits;
                }
            }
            
            ++offsetInBits;
            ++otherOffsetInBits;
        }
        
        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }
        
        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(byte[] o1, byte[] o2) {
        if (o1.length != o2.length) {
            return o1.length - o2.length;
        }
        
        for (int i = 0; i < o1.length; i++) {
            int diff = (o1[i] & 0xFF) - (o2[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefix(byte[] prefix, int offsetInBits, 
            int lengthInBits, byte[] key) {
        
        int bitIndex = 0;
        int keyLength = lengthInBits(key);
        
        while (offsetInBits < lengthInBits) {
            if (isBitSet(prefix, offsetInBits, lengthInBits) 
                    != isBitSet(key, bitIndex, keyLength)) {
                return false;
            }
            
            ++bitIndex;
            ++offsetInBits;
        }
        
        return true;
    }
}