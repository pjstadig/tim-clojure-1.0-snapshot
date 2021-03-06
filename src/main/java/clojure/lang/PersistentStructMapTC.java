/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Dec 16, 2007 */

package clojure.lang;

import java.util.Iterator;
import java.util.Map;

public class PersistentStructMapTC extends APersistentMap {

	final PersistentStructMap.Def def;

	final Object[] vals;

	final IPersistentMap ext;

	static public PersistentStructMap.Def createSlotMap(ISeq keys) {
		if (keys == null)
			throw new IllegalArgumentException("Must supply keys");
		PersistentHashMap ret = PersistentHashMap.EMPTY;
		int i = 0;
		for (ISeq s = keys; s != null; s = s.next(), i++) {
			ret = (PersistentHashMap) ret.assoc(s.first(), i);
		}
		return new PersistentStructMap.Def(keys, ret);
	}

	static public PersistentStructMap create(PersistentStructMap.Def def,
			ISeq keyvals) {
		Object[] vals = new Object[def.keyslots.count()];
		IPersistentMap ext = PersistentHashMap.EMPTY;
		for (; keyvals != null; keyvals = keyvals.next().next()) {
			if (keyvals.next() == null)
				throw new IllegalArgumentException(String.format(
						"No value supplied for key: %s", keyvals.first()));
			Object k = keyvals.first();
			Object v = RT.second(keyvals);
			Map.Entry e = def.keyslots.entryAt(k);
			if (e != null)
				vals[(Integer) e.getValue()] = v;
			else
				ext = ext.assoc(k, v);
		}
		return new PersistentStructMap(null, def, vals, ext);
	}

	static public PersistentStructMap construct(PersistentStructMap.Def def,
			ISeq valseq) {
		Object[] vals = new Object[def.keyslots.count()];
		IPersistentMap ext = PersistentHashMap.EMPTY;
		for (int i = 0; i < vals.length && valseq != null; valseq = valseq
				.next(), i++) {
			vals[i] = valseq.first();
		}
		if (valseq != null)
			throw new IllegalArgumentException(
					"Too many arguments to struct constructor");
		return new PersistentStructMap(null, def, vals, ext);
	}

	static public IFn getAccessor(final PersistentStructMap.Def def, Object key) {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null) {
			final int i = (Integer) e.getValue();
			return new AFn() {
				public Object invoke(Object arg1) throws Exception {
					PersistentStructMap m = (PersistentStructMap) arg1;
					if (m.def != def)
						throw new Exception("Accessor/struct mismatch");
					return m.vals[i];
				}
			};
		}
		throw new IllegalArgumentException("Not a key of struct");
	}

	protected PersistentStructMapTC(IPersistentMap meta,
			PersistentStructMap.Def def, Object[] vals, IPersistentMap ext) {
		super(meta);
		this.ext = ext;
		this.def = def;
		this.vals = vals;
	}

	/**
	 * Returns a new instance of PersistentStructMap using the given parameters.
	 * This function is used instead of the PersistentStructMap constructor by
	 * all methods that return a new PersistentStructMap. This is done so as to
	 * allow subclasses to return instances of their class from all
	 * PersistentStructMap methods.
	 */
	protected PersistentStructMap makeNew(IPersistentMap meta,
			PersistentStructMap.Def def, Object[] vals, IPersistentMap ext) {
		return new PersistentStructMap(meta, def, vals, ext);
	}

	public Obj withMeta(IPersistentMap meta) {
		if (meta == _meta)
			return this;
		return makeNew(meta, def, vals, ext);
	}

	public boolean containsKey(Object key) {
		return def.keyslots.containsKey(key) || ext.containsKey(key);
	}

	public IMapEntry entryAt(Object key) {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null) {
			return new MapEntry(e.getKey(), vals[(Integer) e.getValue()]);
		}
		return ext.entryAt(key);
	}

	public IPersistentMap assoc(Object key, Object val) {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null) {
			int i = (Integer) e.getValue();
			Object[] newVals = new Object[vals.length];
			System.arraycopy(vals, 0, newVals, 0, vals.length);
			newVals[i] = val;
			return makeNew(_meta, def, newVals, ext);
		}
		return makeNew(_meta, def, vals, ext.assoc(key, val));
	}

	public Object valAt(Object key) {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null) {
			return vals[(Integer) e.getValue()];
		}
		return ext.valAt(key);
	}

	public Object valAt(Object key, Object notFound) {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null) {
			return vals[(Integer) e.getValue()];
		}
		return ext.valAt(key, notFound);
	}

	public IPersistentMap assocEx(Object key, Object val) throws Exception {
		if (containsKey(key))
			throw new Exception("Key already present");
		return assoc(key, val);
	}

	public IPersistentMap without(Object key) throws Exception {
		Map.Entry e = def.keyslots.entryAt(key);
		if (e != null)
			throw new Exception("Can't remove struct key");
		IPersistentMap newExt = ext.without(key);
		if (newExt == ext)
			return this;
		return makeNew(_meta, def, vals, newExt);
	}

	public Iterator iterator() {
		return new SeqIterator(seq());
	}

	public int count() {
		return vals.length + RT.count(ext);
	}

	public ISeq seq() {
		return new PersistentStructMap.Seq(null, def.keys, vals, 0, ext);
	}

	public IPersistentCollection empty() {
		return construct(def, null);
	}
}
