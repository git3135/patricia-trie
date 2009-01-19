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
import java.util.Map.Entry;

/**
 * A {@link KeyAnalyzer} for {@link BigInteger}s
 * 
 * NOTE: THIS DOES NOT WORK YET!
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
        return key.bitLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBitSet(BigInteger key, int bitIndex, int lengthInBits) {
        return key.testBit(bitIndex + 1);
    }

    private static final ByteArrayKeyAnalyzer FOO = ByteArrayKeyAnalyzer.INSTANCE;
    
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
        
        if (true) {
            byte[] a = key.toByteArray();
            byte[] b = other.toByteArray();
            
            return FOO.bitIndex(a, 0, a.length*8, b, 0, b.length*8);
        }
        
        if (!key.equals(other)) {
            BigInteger xorValue = key.xor(other);
            int bitLength = Math.max(key.bitLength(), other.bitLength());
            
            System.out.println("bitLength=" + bitLength);
            
            int bitIndex = 0;
            
            /*for (int i = 0; i < bitLength; i++) {
                bitIndex = i;//offsetInBits + (bitLength - i - 1);
                if (xorValue.testBit(bitIndex)) {
                    //System.out.println(bitIndex);
                    return bitIndex;
                }
            }*/
            
            for (int i = 0; i < bitLength; i++) {
                bitIndex = i;//offsetInBits + (bitLength - i - 1);
                if (key.testBit(i) != other.testBit(i)) {
                    //System.out.println(bitIndex);
                    return bitIndex;
                }
            }
        }
        
        System.out.println(key + ", " + other);
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
    
    public static void main(String[] args) {
        Trie<BigInteger, BigInteger> trie = new PatriciaTrie<BigInteger, BigInteger>(BigIntegerKeyAnalyzer.INSTANCE);
        
        for (int i = 0; i < 20; i++) {
            trie.put(BigInteger.valueOf(i), 
                    BigInteger.valueOf(i));
        }
        
        System.out.println(trie.size());
        System.out.println(trie);
        
        trie.traverse(new Cursor<BigInteger, BigInteger>() {
            @Override
            public Decision select(
                    Entry<? extends BigInteger, ? extends BigInteger> entry) {
                System.out.println(entry.getKey());
                return Decision.CONTINUE;
            }
        });
        
        /*BigInteger value = BigInteger.valueOf(0x91);
        System.out.println(Long.toBinaryString(value.longValue()));
        
        int bitLength = value.bitLength();
        for (int i = 0; i < bitLength; i++) {
            int bitIndex = bitLength - i - 1;
            boolean foo = value.testBit(bitIndex);
            System.out.println(i + ") " + foo);
        }*/
        
        for (BigInteger key : trie.keySet()) {
            System.out.println("+ " + key);
        }
    }
}
