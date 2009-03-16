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

import java.math.BigInteger;


/**
 * A {@link KeyAnalyzer} for byte[]s
 */
public class ByteArrayKeyAnalyzer implements KeyAnalyzer<byte[]> {
    
    private static final long serialVersionUID = 7382825097492285877L;

    /**
     * A singleton instance of {@link ByteArrayKeyAnalyzer}
     */
    public static final ByteArrayKeyAnalyzer INSTANCE 
        = new ByteArrayKeyAnalyzer(Integer.MAX_VALUE);
    
    /**
     * The length of an {@link Byte} in bits
     */
    public static final int LENGTH = Byte.SIZE;
    
    /**
     * A bit mask where the first bit is 1 and the others are zero
     */
    private static final int MSB = 0x80;
    
    private static final byte[] NULL = new byte[0];
    
    private final int maxLengthInBits;
    //private final int maxLengthInBits = Integer.MAX_VALUE;
    
    public ByteArrayKeyAnalyzer(int maxLengthInBits) {
        if (maxLengthInBits < 0) {
            throw new IllegalArgumentException(
                    "maxLengthInBits=" + maxLengthInBits);
        }
        
        this.maxLengthInBits = maxLengthInBits;
    }
    
    /**
     * Returns a bit mask where the given bit is set
     */
    private static int mask(int bit) {
        return MSB >>> bit;
    }

    public int getMaxLengthInBits() {
        return maxLengthInBits;
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
        if (key == null) {     
            return false;
        }
        
        int bla = maxLengthInBits - lengthInBits;
        int foo = bitIndex - bla;
        
        if (foo >= lengthInBits || foo < 0) {
            return false;
        }
        
        int index = (int)(foo / LENGTH);
        int bit = (int)(foo % LENGTH);
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
        int length = Math.max(lengthInBits, otherLengthInBits);
        int prefix = maxLengthInBits - length;
        
        if (prefix < 0) {
            return KeyAnalyzer.OUT_OF_BOUNDS_BIT_KEY;
        }
        
        for (int i = 0; i < length; i++) {
            int index = prefix + (offsetInBits + i);
            boolean value = isBitSet(key, index, lengthInBits);
                
            if (value) {
                allNull = false;
            }
            
            int otherIndex = prefix + (otherOffsetInBits + i);
            boolean otherValue = isBitSet(other, otherIndex, otherLengthInBits);
            
            if (value != otherValue) {
                return index;
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
    
    public static void main(String[] args) {
        ByteArrayKeyAnalyzer keyAnalyzer = ByteArrayKeyAnalyzer.INSTANCE;
        
        byte[] key = BigInteger.valueOf(256).toByteArray();
        byte[] other = new byte[] { 1 };
        
        int bitIndex = keyAnalyzer.bitIndex(
                key, 0, keyAnalyzer.lengthInBits(key), 
                other, 0, keyAnalyzer.lengthInBits(other));
        
        System.out.println(bitIndex);
    }
}