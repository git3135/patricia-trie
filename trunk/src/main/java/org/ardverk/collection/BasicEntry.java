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

import java.io.Serializable;
import java.util.Map;

/**
 * 
 */
class BasicEntry<K, V> implements Map.Entry<K, V>, Serializable {
    
    private static final long serialVersionUID = -944364551314110330L;

    K key;
    
    V value;
    
    private final int hashCode;
    
    public BasicEntry(K key) {
        this.key = key;
        
        this.hashCode = (key != null ? key.hashCode() : 0);
    }
    
    public BasicEntry(K key, V value) {
        this.key = key;
        this.value = value;
        
        this.hashCode = (key != null ? key.hashCode() : 0)
                ^ (value != null ? value.hashCode() : 0);
    }
    
    K setKey(K key) {
        K previous = this.key;
        this.key = key;
        return previous;
    }
    
    V setKeyValue(K key, V value) {
        setKey(key);
        return setValue(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public K getKey() {
        return key;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public V getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V setValue(V value) {
        V previous = this.value;
        this.value = value;
        return previous;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Map.Entry)) {
            return false;
        }
        
        Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
        Object k1 = getKey();
        Object k2 = e.getKey();
        
        if (k1 == k2 || (k1 != null && k1.equals(k2))) {
            Object v1 = getValue();
            Object v2 = e.getValue();
            if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return key + "=" + value;
    }
}
