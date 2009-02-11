package org.ardverk.collection;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

public class ByteArrayKeyAnalyzerTest {

    private static final int SIZE = 260;
    
    /*@Test
    public void testKeys() {
        PatriciaTrie<byte[], BigInteger> trie 
            = new PatriciaTrie<byte[], BigInteger>(ByteArrayKeyAnalyzer.INSTANCE);
        
        Map<byte[], BigInteger> map = new TreeMap<byte[], BigInteger>(
                ByteArrayKeyAnalyzer.INSTANCE);
        
        for (int i = 0; i < SIZE; i++) {
            BigInteger value = BigInteger.valueOf(i);
            
            trie.put(value.toByteArray(), value);
            map.put(value.toByteArray(), value);
        }
        
        TestCase.assertEquals(SIZE, trie.size());
        TestCase.assertEquals(SIZE, map.size());
        
        for (Map.Entry<byte[], BigInteger> entry : map.entrySet()) {
            byte[] key = entry.getKey();
            BigInteger expected = entry.getValue();
            BigInteger value = trie.get(key);
            
            TestCase.assertEquals(expected, value);
        }
    }
    
    @Test
    public void testSize() {
        PatriciaTrie<byte[], BigInteger> trie 
            = new PatriciaTrie<byte[], BigInteger>(ByteArrayKeyAnalyzer.INSTANCE);
        
        for (int i = 0; i < SIZE; i++) {
            BigInteger value = BigInteger.valueOf(i);
            
            trie.put(value.toByteArray(), value);
        }
        
        TestCase.assertEquals(SIZE, trie.size());
        
        int count = 0;
        for (Object item : trie.entrySet()) {
            ++count;
        }
        
        TestCase.assertEquals(SIZE, count);
    }*/
    
    @Test
    public void testKeys2() {
        PatriciaTrie<byte[], BigInteger> trie 
            = new PatriciaTrie<byte[], BigInteger>(ByteArrayKeyAnalyzer.INSTANCE);
        
        System.out.println("--- BEGIN ---");
        for (int i = 0; i < 257; i++) {
        //for (int i = SIZE-1; i >= 0; i--) {
            BigInteger value = BigInteger.valueOf(i);
            trie.put(value.toByteArray(), value);
            System.out.println("-");
        }
        
        System.out.println(trie);
        System.out.println(trie.select(BigInteger.valueOf(256).toByteArray()));
        
        TestCase.assertEquals(SIZE, trie.size());
        
        int expected = 0;
        for (byte[] key : trie.keySet()) {
            TestCase.assertEquals(BigInteger.valueOf(expected), new BigInteger(1, key));
            ++expected;
        }
    }
    
    public static void main(String[] args) {
        BigInteger lookup = BigInteger.valueOf(256);
        
        TreeMap<BigInteger, BigInteger> map = new TreeMap<BigInteger, BigInteger>();
        for (int i = 1; i < 256; i++) {
            BigInteger key = BigInteger.valueOf(i);
            map.put(key.xor(lookup), key);
        }
        
        System.out.println(lookup.toString(2));
        System.out.println(map.firstKey().toString(2));
        System.out.println(map.firstKey().xor(lookup).toString(2));
        
        System.out.println(BigInteger.valueOf(255).xor(lookup).toString(2));
        
        System.out.println(map.firstEntry());
    }
}
