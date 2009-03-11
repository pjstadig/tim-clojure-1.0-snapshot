/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jan 23, 2008 */

package clojure.lang;

import java.util.concurrent.ConcurrentHashMap;

public class NamespaceTC extends AReference {
	final public Symbol name;

	IPersistentMap mappings;

	IPersistentMap aliases;

	final static Symbol IN_SYM = Symbol.create("*in*");

	final static Symbol OUT_SYM = Symbol.create("*out*");

	final static Symbol ERR_SYM = Symbol.create("*err*");

	final static ConcurrentHashMap<Symbol, Namespace> namespaces = new ConcurrentHashMap<Symbol, Namespace>();

	public String toString() {
		return name.toString();
	}

	NamespaceTC(Symbol name) {
		super(name.meta());
		this.name = name;
		mappings = RT.DEFAULT_IMPORTS;
		aliases = RT.map();
	}

	public static ISeq all() {
		return RT.seq(namespaces.values());
	}

	public Symbol getName() {
		return name;
	}

	public synchronized IPersistentMap getMappings() {
		return mappings;
	}

	public synchronized Var intern(Symbol sym) {
		if (sym.ns != null) {
			throw new IllegalArgumentException(
					"Can't intern namespace-qualified symbol");
		}
		IPersistentMap map = getMappings();
		Object o;
		Object v = null;
		while ((o = map.valAt(sym)) == null) {
			if (v == null)
				v = new Var((Namespace) (Object) this, sym);
			IPersistentMap newMap = map.assoc(sym, v);
			mappings = newMap;
			map = getMappings();
		}
		if (o instanceof Var && ((Var) o).ns == (Namespace) (Object) this)
			return (Var) o;

		throw new IllegalStateException(sym + " already refers to: " + o
				+ " in namespace: " + name);
	}

	synchronized Object reference(Symbol sym, Object val) {
		if (sym.ns != null) {
			throw new IllegalArgumentException(
					"Can't intern namespace-qualified symbol");
		}
		IPersistentMap map = getMappings();
		Object o;
		while ((o = map.valAt(sym)) == null) {
			IPersistentMap newMap = map.assoc(sym, val);
			mappings = newMap;
			map = getMappings();
		}
		if (o == val)
			return o;

		throw new IllegalStateException(sym + " already refers to: " + o
				+ " in namespace: " + name);
	}

	public synchronized void unmap(Symbol sym) throws Exception {
		if (sym.ns != null) {
			throw new IllegalArgumentException(
					"Can't unintern namespace-qualified symbol");
		}
		IPersistentMap map = getMappings();
		while (map.containsKey(sym)) {
			IPersistentMap newMap = map.without(sym);
			mappings = newMap;
			map = getMappings();
		}
	}

	public Class importClass(Symbol sym, Class c) {
		return (Class) reference(sym, c);
	}

	public Var refer(Symbol sym, Var var) {
		return (Var) reference(sym, var);
	}

	public static Namespace findOrCreate(Symbol name) {
		Namespace ns = namespaces.get(name);
		if (ns != null)
			return ns;
		Namespace newns = new Namespace(name);
		ns = namespaces.putIfAbsent(name, newns);
		return ns == null ? newns : ns;
	}

	public static Namespace remove(Symbol name) {
		if (name.equals(RT.CLOJURE_NS.name))
			throw new IllegalArgumentException(
					"Cannot remove clojure namespace");
		return namespaces.remove(name);
	}

	public static Namespace find(Symbol name) {
		return namespaces.get(name);
	}

	public synchronized Object getMapping(Symbol name) {
		return mappings.valAt(name);
	}

	public synchronized Var findInternedVar(Symbol symbol) {
		Object o = mappings.valAt(symbol);
		if (o != null && o instanceof Var
				&& ((Var) o).ns == (Namespace) (Object) this)
			return (Var) o;
		return null;
	}

	public synchronized IPersistentMap getAliases() {
		return aliases;
	}

	public Namespace lookupAlias(Symbol alias) {
		IPersistentMap map = getAliases();
		return (Namespace) map.valAt(alias);
	}

	public synchronized void addAlias(Symbol alias, Namespace ns) {
		if (alias == null || ns == null)
			throw new NullPointerException("Expecting Symbol + Namespace");
		IPersistentMap map = getAliases();
		while (!map.containsKey(alias)) {
			IPersistentMap newMap = map.assoc(alias, ns);
			aliases = newMap;
			map = getAliases();
		}
		// you can rebind an alias, but only to the initially-aliased namespace.
		if (!map.valAt(alias).equals(ns))
			throw new IllegalStateException("Alias " + alias
					+ " already exists in namespace " + name + ", aliasing "
					+ map.valAt(alias));
	}

	public synchronized void removeAlias(Symbol alias) throws Exception {
		IPersistentMap map = getAliases();
		while (map.containsKey(alias)) {
			IPersistentMap newMap = map.without(alias);
			aliases = newMap;
			map = getAliases();
		}
	}
}
