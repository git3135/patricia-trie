/*
 * Copyright 2005-2008 Roger Kapsi, Sam Berlin
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
 * A {@link KeyAnalyzer} for {@link Integer}s
 */
public class IntegerKeyAnalyzer implements KeyAnalyzer<Integer> {
    
    private static final long serialVersionUID = 4928508653722068982L;
    
    public static final IntegerKeyAnalyzer INSTANCE = new IntegerKeyAnalyzer();
    
    public static final int LENGTH = 32;
    
    private static final int MSB = 0x80000000;
    
    /**
     * 
     */
    private static int mask(int bit) {
        return MSB >>> bit;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Integer> getKeyClass() {
        return Integer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lengthInBits(Integer key) {
        return LENGTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(Integer key, int bitIndex, int lengthInBits) {
        return (key & mask(bitIndex)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitIndex(Integer key, int offset, int lengthInBits, 
            Integer other, int otherOffset, int otherLengthInBits) {
        
        if (other == null) {
            other = 0;
        }
        
        if (offset != 0 || otherOffset != 0) {
            throw new IllegalArgumentException("offset=" + offset 
                    + ", otherOffset=" + otherOffset);
        }
        
        if (lengthInBits != LENGTH || otherLengthInBits != LENGTH) {
            throw new IllegalArgumentException("lengthInBits=" + lengthInBits 
                    + ", otherLengthInBits=" + otherLengthInBits);
        }
        
        boolean allNull = true;
        
        for (int i = 0; i < LENGTH; i++) {
            int mask = mask(i);
            
            int a = key & mask;
            int b = other & mask;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }

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
    public boolean isPrefix(Integer prefix, int offset, 
            int lengthInBits, Integer key) {
        
        int addr1 = prefix;
        int addr2 = key;
        addr1 = addr1 << offset;
        
        int mask = 0;
        for (int i = 0; i < lengthInBits; i++) {
            mask |= (0x1 << i);
        }
        
        addr1 &= mask;
        addr2 &= mask;
        
        return addr1 == addr2;
    }
}