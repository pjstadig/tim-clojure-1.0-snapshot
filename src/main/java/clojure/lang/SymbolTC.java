/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 11:42:47 AM */

package clojure.lang;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class SymbolTC extends AFn implements Comparable, Named, Serializable {
	// these must be interned strings!
	final String ns;

	final String name;

	final int hash;

	public String toString() {
		if (ns != null)
			return ns + "/" + name;
		return name;
	}

	public String getNamespace() {
		return ns;
	}

	public String getName() {
		return name;
	}

	static public SymbolTC intern(String ns, String name) {
		return new SymbolTC(ns == null ? null : ns.intern(), name.intern());
	}

	static public SymbolTC intern(String nsname) {
		int i = nsname.lastIndexOf('/');
		if (i == -1)
			return new SymbolTC(null, nsname.intern());
		else
			return new SymbolTC(nsname.substring(0, i).intern(), nsname
					.substring(i + 1).intern());
	}

	static public SymbolTC create(String name_interned) {
		return new SymbolTC(null, name_interned);
	}

	static public SymbolTC create(String ns_interned, String name_interned) {
		return new SymbolTC(ns_interned, name_interned);
	}

	private SymbolTC(String ns_interned, String name_interned) {
		this.name = name_interned;
		this.ns = ns_interned;
		this.hash = Util.hashCombine(name.hashCode(), Util.hash(ns));
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Symbol))
			return false;

		Symbol symbol = (Symbol) o;

		// identity compares intended, names are interned
		return name.equals(symbol.name)
				&& ((ns == null && ns == null) || (ns != null && ns
						.equals(symbol.ns)));
	}

	public int hashCode() {
		return hash;
	}

	public Obj withMeta(IPersistentMap meta) {
		return new SymbolTC(meta, ns, name);
	}

	private SymbolTC(IPersistentMap meta, String ns, String name) {
		super(meta);
		this.name = name;
		this.ns = ns;
		this.hash = Util.hashCombine(name.hashCode(), Util.hash(ns));
	}

	public int compareTo(Object o) {
		Symbol s = (Symbol) o;
		if (this.equals(o))
			return 0;
		if (this.ns == null && s.ns != null)
			return -1;
		if (this.ns != null) {
			if (s.ns == null)
				return 1;
			int nsc = this.ns.compareTo(s.ns);
			if (nsc != 0)
				return nsc;
		}
		return this.name.compareTo(s.name);
	}

	private Object readResolve() throws ObjectStreamException {
		return intern(ns, name);
	}

	public Object invoke(Object obj) throws Exception {
		return RT.get(obj, this);
	}

	public Object invoke(Object obj, Object notFound) throws Exception {
		return RT.get(obj, this, notFound);
	}

}
