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
 * A {@link KeyAnalyzer} for {@link Character}s
 */
public class CharacterKeyAnalyzer implements KeyAnalyzer<Character> {
    
    private static final long serialVersionUID = 3928565962744720753L;
    
    /**
     * A singleton instance of the {@link CharacterKeyAnalyzer}.
     */
    public static final CharacterKeyAnalyzer INSTANCE = new CharacterKeyAnalyzer();
    
    /**
     * The length of a {@link Character} in bits
     */
    public static final int LENGTH = Character.SIZE;
    
    /**
     * A bit mask where the first bit is 1 and the others are zero
     */
    private static final int MSB = 0x8000;
    
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
    public int lengthInBits(Character key) {
        return LENGTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(Character key, int bitIndex, int lengthInBits) {
        return (key & mask(bitIndex)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitIndex(Character key, int offsetInBits, int lengthInBits, 
            Character other, int otherOffsetInBits, int otherLengthInBits) {
        
        if (offsetInBits != 0 || otherOffsetInBits != 0) {
            throw new IllegalArgumentException("offsetInBits=" + offsetInBits 
                    + ", otherOffsetInBits=" + otherOffsetInBits);
        }
        
        int keyValue = key.charValue();
        if (keyValue == 0) {
            return NULL_BIT_KEY;
        }
        
        if (other == null) {
            other = Character.MIN_VALUE;
        }
        
        int otherValue = (other != null ? other.charValue() : Character.MIN_VALUE);
        
        if (keyValue != otherValue) {
            int xorValue = keyValue ^ otherValue;
            for (int i = 0; i < LENGTH; i++) {
                if ((xorValue & mask(i)) != 0) {
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
    public int compare(Character o1, Character o2) {
        return o1.compareTo(o2);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefix(Character prefix, int offsetInBits, 
            int lengthInBits, Character key) {
        
        int addr1 = (prefix << offsetInBits);
        int addr2 = key;
        
        int mask = 0;
        for(int i = 0; i < lengthInBits; i++) {
            mask |= (0x1 << i);
        }
        
        return (addr1 & mask) == (addr2 & mask);
    }
}