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
    
    public static final CharacterKeyAnalyzer INSTANCE = new CharacterKeyAnalyzer();
    
    public static final int LENGTH = 16;
    
    private static final int MSB = 0x8000;
    
    private static int mask(int bit) {
        return MSB >>> bit;
    }
    
    @Override
    public int lengthInBits(Character key) {
        return LENGTH;
    }

    @Override
    public boolean isBitSet(Character key, int bitIndex, int lengthInBits) {
        return (key & mask(bitIndex)) != 0;
    }

    @Override
    public int bitIndex(Character key, int offset, int lengthInBits, 
            Character other, int otherOffset, int otherLengthInBits) {
        
        if (other == null) {
            other = Character.MIN_VALUE;
        }
        
        if (offset != 0 || otherOffset != 0) {
            throw new IllegalArgumentException("offset=" + offset 
                    + ", otherOffset=" + otherOffset);
        }
        
        int length = Math.min(lengthInBits, otherLengthInBits);
        
        boolean allNull = true;
        for (int i = 0; i < length; i++) {
            
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

    @Override
    public int compare(Character o1, Character o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int bitsPerElement() {
        return 1;
    }
    
    @Override
    public boolean isPrefix(Character prefix, int offset, int length, Character key) {
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

    @Override
    public Class<Character> getKeyClass() {
        return Character.class;
    }
}