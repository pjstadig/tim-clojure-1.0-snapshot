/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Aug 21, 2007 */

package clojure.lang;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// todo: possibly extend URLClassLoader?

public class DynamicClassLoaderTC extends URLClassLoader {
	HashMap<Integer, Object[]> constantVals = new HashMap<Integer, Object[]>();

	static ConcurrentHashMap<String, byte[]> map = new ConcurrentHashMap<String, byte[]>();

	static final URL[] EMPTY_URLS = new URL[] {};

	public DynamicClassLoaderTC() {
		// pseudo test in lieu of hasContextClassLoader()
		super(
				EMPTY_URLS,
				(Thread.currentThread().getContextClassLoader() == null || Thread
						.currentThread().getContextClassLoader() == ClassLoader
						.getSystemClassLoader()) ? Compiler.class
						.getClassLoader() : Thread.currentThread()
						.getContextClassLoader());
		// super(EMPTY_URLS,Compiler.class.getClassLoader());
	}

	public DynamicClassLoaderTC(ClassLoader parent) {
		super(EMPTY_URLS, parent);
	}

	public Class defineClass(String name, byte[] bytes) {
		addBytecode(name, bytes);
		return defineClass(name, bytes, 0, bytes.length);
	}

	public void addBytecode(String className, byte[] bytes) {
		if (!map.containsKey(className))
			map.put(className, bytes);
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] bytes = map.get(name);
		if (bytes != null)
			return defineClass(name, bytes, 0, bytes.length);
		return super.findClass(name);
		// throw new ClassNotFoundException(name);
	}

	public void registerConstants(int id, Object[] val) {
		constantVals.put(id, val);
	}

	public Object[] getConstants(int id) {
		return constantVals.get(id);
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

}
