package org.ardverk.collection;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.junit.Test;

public class ByteArrayKeyAnalyzerTest {

    private static final int SIZE = 100000;
    
    @Test
    public void testKeys() {
        PatriciaTrie<byte[], BigInteger> trie 
            = new PatriciaTrie<byte[], BigInteger>(ByteArrayKeyAnalyzer.INSTANCE);
        
        for (int i = 0; i < SIZE; i++) {
            BigInteger value = BigInteger.valueOf(i);
            trie.put(value.toByteArray(), value);
        }
        
        TestCase.assertEquals(SIZE, trie.size());
        
        BigInteger key = BigInteger.valueOf(55);
        BigInteger value = trie.get(key.toByteArray());
        
        TestCase.assertEquals(key, value);
    }
}
