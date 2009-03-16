package org.ardverk.collection;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

public class ByteArrayKeyAnalyzerTest {

    private static final int SIZE = 20000;
    
    @Test
    public void testKeys() {
        PatriciaTrie<byte[], BigInteger> trie
            = new PatriciaTrie<byte[], BigInteger>(
                    ByteArrayKeyAnalyzer.INSTANCE);
        
        Map<byte[], BigInteger> map 
            = new TreeMap<byte[], BigInteger>(
                    ByteArrayKeyAnalyzer.INSTANCE);
        
        for (int i = 0; i < SIZE; i++) {
            BigInteger value = BigInteger.valueOf(i);

            trie.put(value.toByteArray(), value);
            map.put(value.toByteArray(), value);
        }
        
        for (byte[] key : map.keySet()) {
            BigInteger expected = new BigInteger(1, key);
            BigInteger value = trie.get(key);
            
            TestCase.assertEquals(expected, value);
        }
    }
}
