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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Mani Moayedi
 */
public class PluginProfile {

    private File pluginsDirectory;
    private List<File> allLibraries;
    private ClassCollection allPluggedClasses;

    public PluginProfile(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
        loadPlugins();
    }

    public PluginProfile(String pluginsDirectory) {
        this(new File(pluginsDirectory));
    }

    public final void loadPlugins() {

        loadAllLibraries();

        loadAllClasses();

    }

    private void loadAllClasses() {
        this.allPluggedClasses = new ClassCollection();

        this.allLibraries.forEach(jar -> loadAllClasses(jar, this.allPluggedClasses));
    }

    private void loadAllLibraries() {

        this.allLibraries = new ArrayList<>();

        loadLibraries(this.pluginsDirectory, this.allLibraries);
    }

    private void loadLibraries(File file, List<File> ret) {

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (File f : files) {
                loadLibraries(f, ret);
            }

        } else {
            if (file.getName().toLowerCase().endsWith(".jar")) {
                ret.add(file);
            }
        }
    }

    private void loadAllClasses(File jar, ClassCollection allPluggedClasses) {

        List<String> classNames = loadClassNames(jar);

        ClassLoader appl = new Object() {}.getClass().getClassLoader();

        try {

            URL jarUrl = jar.toURI().toURL();

            URLClassLoader loader = new URLClassLoader(
                    new URL[]{jarUrl}, appl);

            for (String className : classNames) {
                try {

                    Class c = loader.loadClass(className);

                    allPluggedClasses.add(c);
                } catch (Exception e) {
                }

            }
        } catch (Exception ex) {
        }
    }

    private List<String> loadClassNames(File jar) {
        List<String> classNames = new ArrayList<String>();
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(jar));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // This ZipEntry represents a class. Now, what class does it represent?
                    String className = entry.getName().replace('/', '.'); // including ".class"
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
        } catch (Exception e) {
        }
        return classNames;
    }

    public Class byFullName(String name) throws ClassNotFoundException {
        Class ret = this.allPluggedClasses.findByFullName(name);

        if (ret == null) {
            throw new ClassNotFoundException("No class with full name: " + name
                    + " found to be plugged in.");
        }

        return ret;
    }

    public Class bySimpleName(String name) throws ClassNotFoundException {
        Class ret = this.allPluggedClasses.findBySimpleName(name);

        if (ret == null) {
            throw new ClassNotFoundException("No class with simple name: " + name
                    + " found to be plugged in.");
        }

        return ret;
    }

    /**
     * *
     *
     * This method first looks for full name match, if it doesn't find any, it
     * will search for simple name match. If you use this method, there is a
     * chance that the result is different from your expectation. for example
     * when there are two classes which one's full name (default package) is
     * match with other's simple name.
     *
     * @param name
     * @return
     */
    public Class byName(String name) throws ClassNotFoundException {
        Class type = this.allPluggedClasses.findByFullName(name);

        if (type == null) {
            type = this.allPluggedClasses.findBySimpleName(name);
        }

        if (type == null) {
            throw new ClassNotFoundException("No class with name: " + name
                    + " found to be plugged in.");
        }

        return type;
    }

    public <T> T makeObject(String className) throws Exception {

        Class type = byName(className);

        return (T) type.newInstance();
    }

    public <T> T makeObject(String className, Object... arguments) throws Exception {

        Class type = byName(className);

        Class[] parameterTypes = getTypes(arguments);

        Constructor c = type.getConstructor(parameterTypes);

        return (T) c.newInstance(arguments);
    }

    private Class[] getTypes(Object[] objects) {
        Class[] ret = new Class[objects.length];

        for (int i = 0; i < objects.length; i++) {
            if (ret[i] != null) {
                ret[i] = objects[i].getClass();
            } else {
                ret[i] = null;
            }
        }
        return ret;
    }

    public List<Class> allClasses() {
        return this.allPluggedClasses.getAllClasses();
    }

}
