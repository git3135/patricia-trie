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

public abstract class AbstractKeyAnalyzer<K> implements KeyAnalyzer<K> {
    
    private static final long serialVersionUID = 384115794654304353L;

    protected static final int[] createIntBitMask(int bitCount) {
        int[] bits = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            bits[i] = 1 << (bitCount - i - 1);
        }
        return bits;
    }
}
