/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package clojure.lang;

import java.util.Iterator;
import java.util.Map;

/**
 * Simple implementation of persistent map on an array <p/> Note that instances
 * of this class are constant values i.e. add/remove etc return new values <p/>
 * Copies array on every change, so only appropriate for _very_small_ maps <p/>
 * null keys and values are ok, but you won't be able to distinguish a null
 * value via valAt - use contains/entryAt
 */

public class PersistentArrayMapTC extends APersistentMap {

	final Object[] array;

	static final int HASHTABLE_THRESHOLD = 16;

	public static final PersistentArrayMap EMPTY = new PersistentArrayMap();

	static public IPersistentMap create(Map other) {
		IPersistentMap ret = EMPTY;
		for (Object o : other.entrySet()) {
			Map.Entry e = (Entry) o;
			ret = ret.assoc(e.getKey(), e.getValue());
		}
		return ret;
	}

	protected PersistentArrayMapTC() {
		this.array = new Object[] {};
	}

	public PersistentArrayMap withMeta(IPersistentMap meta) {
		return new PersistentArrayMap(meta, array);
	}

	PersistentArrayMap create(Object... init) {
		return new PersistentArrayMap(meta(), init);
	}

	IPersistentMap createHT(Object[] init) {
		return PersistentHashMap.create(meta(), init);
	}

	/**
	 * This ctor captures/aliases the passed array, so do not modify later
	 * 
	 * @param init
	 *            {key1,val1,key2,val2,...}
	 */
	public PersistentArrayMapTC(Object[] init) {
		this.array = init;
	}

	public PersistentArrayMapTC(IPersistentMap meta, Object[] init) {
		super(meta);
		this.array = init;
	}

	public int count() {
		return array.length / 2;
	}

	public boolean containsKey(Object key) {
		return indexOf(key) >= 0;
	}

	public IMapEntry entryAt(Object key) {
		int i = indexOf(key);
		if (i >= 0)
			return new MapEntry(array[i], array[i + 1]);
		return null;
	}

	public IPersistentMap assocEx(Object key, Object val) throws Exception {
		int i = indexOf(key);
		Object[] newArray;
		if (i >= 0) {
			throw new Exception("Key already present");
		} else // didn't have key, grow
		{
			if (array.length > HASHTABLE_THRESHOLD)
				return createHT(array).assocEx(key, val);
			newArray = new Object[array.length + 2];
			if (array.length > 0)
				System.arraycopy(array, 0, newArray, 2, array.length);
			newArray[0] = key;
			newArray[1] = val;
		}
		return create(newArray);
	}

	public IPersistentMap assoc(Object key, Object val) {
		int i = indexOf(key);
		Object[] newArray;
		if (i >= 0) // already have key, same-sized replacement
		{
			if (utilSame(array[i + 1], val)) // no change, no op
				return this;
			newArray = new Object[array.length];
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[i + 1] = val;
		} else // didn't have key, grow
		{
			if (array.length > HASHTABLE_THRESHOLD)
				return createHT(array).assoc(key, val);
			newArray = new Object[array.length + 2];
			if (array.length > 0)
				System.arraycopy(array, 0, newArray, 2, array.length);
			newArray[0] = key;
			newArray[1] = val;
		}
		return create(newArray);
	}

	public IPersistentMap without(Object key) {
		int i = indexOf(key);
		if (i >= 0) // have key, will remove
		{
			int newlen = array.length - 2;
			if (newlen == 0)
				return empty();
			Object[] newArray = new Object[newlen];
			for (int s = 0, d = 0; s < array.length; s += 2) {
				if (!equalKey(array[s], key)) // skip removal key
				{
					newArray[d] = array[s];
					newArray[d + 1] = array[s + 1];
					d += 2;
				}
			}
			return create(newArray);
		}
		// don't have key, no op
		return this;
	}

	public IPersistentMap empty() {
		return (IPersistentMap) EMPTY.withMeta(meta());
	}

	final public Object valAt(Object key, Object notFound) {
		int i = indexOf(key);
		if (i >= 0)
			return array[i + 1];
		return notFound;
	}

	public Object valAt(Object key) {
		return valAt(key, null);
	}

	public int capacity() {
		return count();
	}

	private int indexOf(Object key) {
		for (int i = 0; i < array.length; i += 2) {
			if (equalKey(array[i], key))
				return i;
		}
		return -1;
	}

	boolean equalKey(Object k1, Object k2) {
		if (k1 == null)
			return k2 == null;
		return k1.equals(k2);
	}

	public Iterator iterator() {
		return new PersistentArrayMap.Iter(array);
	}

	public ISeq seq() {
		if (array.length > 0)
			return new PersistentArrayMap.Seq(array, 0);
		return null;
	}

	private boolean utilSame(Object o1, Object o2) {
		try {
			return ((Boolean) Reflector.invokeStaticMethod(Util.class, "same",
					new Object[] { o1, o2 })).booleanValue();
		} catch (Exception e) {
			throw new RuntimeException("Could not invoke 'same' method of Util", e);
		}
	}
}
