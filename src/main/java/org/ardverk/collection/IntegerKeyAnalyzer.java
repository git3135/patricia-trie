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


public class IntegerKeyAnalyzer extends AbstractKeyAnalyzer<Integer> {
    
    private static final long serialVersionUID = 4928508653722068982L;
    
    private static final int[] BITS = createIntBitMask(32);

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
    public int length(Integer key) {
        return 32;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(Integer key, int keyLength, int bitIndex) {
        return (key & BITS[bitIndex]) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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