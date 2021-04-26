/*
 * The MIT License
 *
 * Copyright 2019 80116.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.acidmanic.easyplug;

import com.acidmanic.delegates.arg1.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author 80116
 */
public class ClassCollection {

    private final HashMap<String, Class> byFullNames;
    private final HashMap<String, Class> bySimpleNames;
    private final HashMap<Object, Class> byCustomeTag;
    private final HashMap<Object, Class> byExternalTag;
    private Function<Object,Class> customeTagProvider = c -> c.getName();

    public Function<Object,Class> getCustomeTagProvider() {
        return customeTagProvider;
    }

    public void setCustomeTagProvider(Function<Object,Class> customeTagProvider) {
        this.customeTagProvider = customeTagProvider;
    }

    public ClassCollection() {
        this.byFullNames = new HashMap<>();

        this.bySimpleNames = new HashMap<>();

        this.byCustomeTag = new HashMap<>();
        
        this.byExternalTag = new HashMap<>();
    }

    public synchronized void clear() {
        this.byCustomeTag.clear();
        this.byFullNames.clear();
        this.bySimpleNames.clear();
        this.byExternalTag.clear();
    }

    public List<Class> getAllClasses() {
        List<Class> ret = new ArrayList<>();

        ret.addAll(byFullNames.values());

        return ret;
    }

    public Class findByFullName(String name) {
        if (this.byFullNames.containsKey(name)) {
            return this.byFullNames.get(name);
        }
        return null;
    }

    public Class findBySimpleName(String name) {
        if (this.bySimpleNames.containsKey(name)) {
            return this.bySimpleNames.get(name);
        }
        return null;
    }

    public synchronized void add(Class type) {
        this.byFullNames.put(type.getName(), type);

        this.bySimpleNames.put(type.getSimpleName(), type);

        this.byCustomeTag.put(this.customeTagProvider.perform(type), type);
    }
    
    public synchronized void add(Class type,Object externalTag){
        this.add(type);
        
        this.byExternalTag.put(externalTag, type);
    }

    public Class findByCustomeTag(Object tag) {
        if (this.byCustomeTag.containsKey(tag)) {
            return this.byCustomeTag.get(tag);
        }
        return null;
    }
    
    public Class findByExternalTag(Object tag) {
        if (this.byExternalTag.containsKey(tag)) {
            return this.byExternalTag.get(tag);
        }
        return null;
    }
    
}
