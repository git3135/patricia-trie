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
 * A {@link KeyAnalyzer} for {@link BigInteger}s
 */
class BigIntegerKeyAnalyzer implements KeyAnalyzer<BigInteger> {
    
    private static final long serialVersionUID = 7123669849156062477L;
    
    /**
     * A singleton instance of {@link BigIntegerKeyAnalyzer}
     */
    public static final BigIntegerKeyAnalyzer INSTANCE = new BigIntegerKeyAnalyzer();

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitsPerElement() {
        return 1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int lengthInBits(BigInteger key) {
        return key.bitCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(BigInteger key, int bitIndex, int lengthInBits) {
        return key.testBit(bitIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitIndex(BigInteger key, int offsetInBits, int lengthInBits, 
            BigInteger other, int otherOffsetInBits, int otherLengthInBits) {
        
        if (offsetInBits != 0 || otherOffsetInBits != 0) {
            throw new IllegalArgumentException("offsetInBits=" + offsetInBits 
                    + ", otherOffsetInBits=" + otherOffsetInBits);
        }
        
        if (key.equals(BigInteger.ZERO)) {
            return NULL_BIT_KEY;
        }

        if (other == null) {
            other = BigInteger.ZERO;
        }
        
        if (!key.equals(other)) {
            BigInteger xorValue = key.xor(other);
            int bitCount = xorValue.bitCount();
            
            for (int i = 0; i < bitCount; i++) {
                if (xorValue.testBit(i)) {
                    return i;
                }
            }
        }
        
        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(BigInteger o1, BigInteger o2) {
        return o1.compareTo(o2);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefix(BigInteger prefix, int offsetInBits, 
            int lengthInBits, BigInteger key) {
        
        BigInteger mask = BigInteger.ZERO;
        BigInteger value1 = prefix.shiftLeft(offsetInBits);
        BigInteger value2 = key;
        
        for (int i = 0; i < lengthInBits; i++) {
            mask = mask.setBit(i);
        }
        
        return (value1.and(mask)).equals((value2.and(mask)));
    }
}
