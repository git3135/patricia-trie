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
 * A {@link KeyAnalyzer} for {@link byte[]}s
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
        
        throw new UnsupportedOperationException("Not implemented yet");
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
        
        throw new UnsupportedOperationException("Not implemented yet");
    }
}