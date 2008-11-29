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
 * 
 */
public class CharSequenceKeyAnalyzer extends AbstractKeyAnalyzer<CharSequence> {
    
    private static final long serialVersionUID = -7032449491269434877L;
    
    private static final int[] BITS = createIntBitMask(16);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CharSequence> getKeyClass() {
        return CharSequence.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length(CharSequence key) {
        return (key != null ? key.length() * 16 : 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int bitIndex(CharSequence key,   int keyOff, int keyLength,
                        CharSequence found, int foundOff, int foundKeyLength) {
        boolean allNull = true;
        
        if (keyOff % 16 != 0 || foundOff % 16 != 0 
                || keyLength % 16 != 0 || foundKeyLength % 16 != 0) {
            throw new IllegalArgumentException("offsets & lengths must be at character boundaries");
        }
        
        int off1 = keyOff / 16;
        int off2 = foundOff / 16;
        int len1 = keyLength / 16 + off1;
        int len2 = foundKeyLength / 16 + off2;
        int length = Math.max(len1, len2);
        
        // Look at each character, and if they're different
        // then figure out which bit makes the difference
        // and return it.
        char k = 0, f = 0;
        for(int i = 0; i < length; i++) {
            int kOff = i + off1;
            int fOff = i + off2;
            
            if (kOff >= len1) {
                k = 0;
            } else {
                k = key.charAt(kOff);
            }
            
            if (found == null || fOff >= len2) {
                f = 0;
            } else {
                f = found.charAt(fOff);
            }
            
            if (k != f) {
               int x = k ^ f;
               return i * 16 + (Integer.numberOfLeadingZeros(x) - 16);
            }
            
            if (k != 0) {
                allNull = false;
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
    public boolean isBitSet(CharSequence key, int keyLength, int bitIndex) {
        if (key == null || bitIndex >= keyLength) {
            return false;
        }
        
        int index = bitIndex / BITS.length;
        int bit = bitIndex - index * BITS.length;
        return (key.charAt(index) & BITS[bit]) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(CharSequence o1, CharSequence o2) {
        return o1.toString().compareTo(o2.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bitsPerElement() {
        return 16;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefix(CharSequence prefix, int offset, 
            int length, CharSequence key) {
        if (offset % 16 != 0 || length % 16 != 0) {
            throw new IllegalArgumentException("Cannot determine prefix outside of character boundaries");
        }
    
        String s1 = prefix.subSequence(offset / 16, length / 16).toString();
        String s2 = key.toString();
        return s2.startsWith(s1);
    }
}