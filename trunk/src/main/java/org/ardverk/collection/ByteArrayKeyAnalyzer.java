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
public class ByteArrayKeyAnalyzer implements KeyAnalyzer<byte[]> {
    
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
        
        int index = key.length - (int)(bitIndex / LENGTH) - 1;
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
        
        boolean allNull = true;
        int length = Math.max(key.length, other.length);
        
        for (int i = 0; i < length; i++) {
            byte keyValue = (i < key.length ? key[key.length - i - 1] : 0);
            byte otherValue = (i < other.length ? other[other.length - i - 1] : 0);
            
            if (keyValue != otherValue) {
                int xorValue = keyValue ^ otherValue;
                allNull = false;
                
                for (int j = 0; j < Byte.SIZE; j++) {
                    if ((xorValue & mask(j)) != 0) {
                        return (i * Byte.SIZE) + j;
                    }
                }
            }
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
        if (o1 == null) {
            return (o2 == null) ? 0 : -1;
        } else if (o2 == null) {
            return (o1 == null) ? 0 : 1;
        }
        
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