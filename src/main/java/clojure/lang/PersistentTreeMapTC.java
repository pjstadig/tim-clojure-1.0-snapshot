/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich May 20, 2006 */

package clojure.lang;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * Persistent Red Black Tree Note that instances of this class are constant
 * values i.e. add/remove etc return new values <p/> See Okasaki, Kahrs, Larsen
 * et al
 */

public class PersistentTreeMapTC extends APersistentMap implements Reversible,
		Sorted {

	public final Comparator comp;

	public final PersistentTreeMap.Node tree;

	public final int _count;

	final static public PersistentTreeMap EMPTY = new PersistentTreeMap();

	static public IPersistentMap create(Map other) {
		IPersistentMap ret = EMPTY;
		for (Object o : other.entrySet()) {
			Map.Entry e = (Entry) o;
			ret = ret.assoc(e.getKey(), e.getValue());
		}
		return ret;
	}

	public PersistentTreeMapTC() {
		this(RT.DEFAULT_COMPARATOR);
	}

	public PersistentTreeMap withMeta(IPersistentMap meta) {
		return new PersistentTreeMap(meta, comp,
				(PersistentTreeMap.Node) (Object) tree, _count);
	}

	private PersistentTreeMapTC(Comparator comp) {
		this(null, comp);
	}

	public PersistentTreeMapTC(IPersistentMap meta, Comparator comp) {
		super(meta);
		this.comp = comp;
		tree = null;
		_count = 0;
	}

	PersistentTreeMapTC(IPersistentMap meta, Comparator comp,
			PersistentTreeMap.Node tree, int _count) {
		super(meta);
		this.comp = comp;
		this.tree = tree;
		this._count = _count;
	}

	static public PersistentTreeMap create(ISeq items) {
		IPersistentMap ret = EMPTY;
		for (; items != null; items = items.next().next()) {
			if (items.next() == null)
				throw new IllegalArgumentException(String.format(
						"No value supplied for key: %s", items.first()));
			ret = ret.assoc(items.first(), RT.second(items));
		}
		return (PersistentTreeMap) ret;
	}

	static public PersistentTreeMap create(Comparator comp, ISeq items) {
		IPersistentMap ret = new PersistentTreeMapTC(comp);
		for (; items != null; items = items.next().next()) {
			if (items.next() == null)
				throw new IllegalArgumentException(String.format(
						"No value supplied for key: %s", items.first()));
			ret = ret.assoc(items.first(), RT.second(items));
		}
		return (PersistentTreeMap) ret;
	}

	public boolean containsKey(Object key) {
		return entryAt(key) != null;
	}

	public PersistentTreeMap assocEx(Object key, Object val) throws Exception {
		Box found = new Box(null);
		PersistentTreeMap.Node t = add(tree, key, val, found);
		if (t == null) // null == already contains key
		{
			throw new Exception("Key already present");
		}
		return (PersistentTreeMap) (Object) new PersistentTreeMapTC(comp, t
				.blacken(), _count + 1, meta());
	}

	public PersistentTreeMap assoc(Object key, Object val) {
		Box found = new Box(null);
		PersistentTreeMap.Node t = add(tree, key, val, found);
		if (t == null) // null == already contains key
		{
			PersistentTreeMap.Node foundNode = (PersistentTreeMap.Node) found.val;
			if (utilSame(foundNode.val(), val)) // note only get same
				// collection on identity of
				// val, not equals()
				return (PersistentTreeMap) (Object) this;
			return new PersistentTreeMap(comp,
					(PersistentTreeMap.Node) (Object) replace(tree, key, val),
					_count, meta());
		}
		return new PersistentTreeMap(comp, (PersistentTreeMap.Node) (Object) t
				.blacken(), _count + 1, meta());
	}

	public PersistentTreeMap without(Object key) {
		Box found = new Box(null);
		PersistentTreeMap.Node t = remove(tree, key, found);
		if (t == null) {
			if (found.val == null)// null == doesn't contain key
				return (PersistentTreeMap) (Object) this;
			// empty
			return new PersistentTreeMap(meta(), comp);
		}
		return new PersistentTreeMap(comp, (PersistentTreeMap.Node) (Object) t
				.blacken(), _count - 1, meta());
	}

	public ISeq seq() {
		if (_count > 0)
			return PersistentTreeMap.Seq.create(tree, true, _count);
		return null;
	}

	public IPersistentCollection empty() {
		return EMPTY.withMeta(meta());
	}

	public ISeq rseq() throws Exception {
		if (_count > 0)
			return PersistentTreeMap.Seq.create(tree, false, _count);
		return null;
	}

	public Comparator comparator() {
		return comp;
	}

	public Object entryKey(Object entry) {
		return ((IMapEntry) entry).key();
	}

	public ISeq seq(boolean ascending) {
		if (_count > 0)
			return PersistentTreeMap.Seq.create(tree, ascending, _count);
		return null;
	}

	public ISeq seqFrom(Object key, boolean ascending) {
		if (_count > 0) {
			ISeq stack = null;
			PersistentTreeMap.Node t = tree;
			while (t != null) {
				int c = doCompare(key, t.key);
				if (c == 0) {
					stack = RT.cons(t, stack);
					return new PersistentTreeMap.Seq(stack, ascending);
				} else if (ascending) {
					if (c < 0) {
						stack = RT.cons(t, stack);
						t = t.left();
					} else
						t = t.right();
				} else {
					if (c > 0) {
						stack = RT.cons(t, stack);
						t = t.right();
					} else
						t = t.left();
				}
			}
			if (stack != null)
				return new PersistentTreeMap.Seq(stack, ascending);
		}
		return null;
	}

	public PersistentTreeMap.NodeIterator iterator() {
		return new PersistentTreeMap.NodeIterator(tree, true);
	}

	public PersistentTreeMap.NodeIterator reverseIterator() {
		return new PersistentTreeMap.NodeIterator(tree, false);
	}

	public Iterator keys() {
		return keys(iterator());
	}

	public Iterator vals() {
		return vals(iterator());
	}

	public Iterator keys(PersistentTreeMap.NodeIterator it) {
		return new PersistentTreeMap.KeyIterator(it);
	}

	public Iterator vals(PersistentTreeMap.NodeIterator it) {
		return new PersistentTreeMap.ValIterator(it);
	}

	public Object minKey() {
		PersistentTreeMap.Node t = min();
		return t != null ? t.key : null;
	}

	public PersistentTreeMap.Node min() {
		PersistentTreeMap.Node t = tree;
		if (t != null) {
			while (t.left() != null)
				t = t.left();
		}
		return t;
	}

	public Object maxKey() {
		PersistentTreeMap.Node t = max();
		return t != null ? t.key : null;
	}

	public PersistentTreeMap.Node max() {
		PersistentTreeMap.Node t = tree;
		if (t != null) {
			while (t.right() != null)
				t = t.right();
		}
		return t;
	}

	public int depth() {
		return depth(tree);
	}

	int depth(PersistentTreeMap.Node t) {
		if (t == null)
			return 0;
		return 1 + Math.max(depth(t.left()), depth(t.right()));
	}

	public Object valAt(Object key, Object notFound) {
		PersistentTreeMap.Node n = entryAt(key);
		return (n != null) ? n.val() : notFound;
	}

	public Object valAt(Object key) {
		return valAt(key, null);
	}

	public int capacity() {
		return _count;
	}

	public int count() {
		return _count;
	}

	public PersistentTreeMap.Node entryAt(Object key) {
		PersistentTreeMap.Node t = tree;
		while (t != null) {
			int c = doCompare(key, t.key);
			if (c == 0)
				return t;
			else if (c < 0)
				t = t.left();
			else
				t = t.right();
		}
		return t;
	}

	public int doCompare(Object k1, Object k2) {
		// if(comp != null)
		return comp.compare(k1, k2);
		// return ((Comparable) k1).compareTo(k2);
	}

	PersistentTreeMap.Node add(PersistentTreeMap.Node t, Object key,
			Object val, Box found) {
		if (t == null) {
			if (val == null)
				return new PersistentTreeMap.Red(key);
			return new PersistentTreeMap.RedVal(key, val);
		}
		int c = doCompare(key, t.key);
		if (c == 0) {
			found.val = t;
			return null;
		}
		PersistentTreeMap.Node ins = c < 0 ? add(t.left(), key, val, found)
				: add(t.right(), key, val, found);
		if (ins == null) // found below
			return null;
		if (c < 0)
			return t.addLeft(ins);
		return t.addRight(ins);
	}

	PersistentTreeMap.Node remove(PersistentTreeMap.Node t, Object key,
			Box found) {
		if (t == null)
			return null; // not found indicator
		int c = doCompare(key, t.key);
		if (c == 0) {
			found.val = t;
			return append(t.left(), t.right());
		}
		PersistentTreeMap.Node del = c < 0 ? remove(t.left(), key, found)
				: remove(t.right(), key, found);
		if (del == null && found.val == null) // not found below
			return null;
		if (c < 0) {
			if (t.left() instanceof PersistentTreeMap.Black)
				return balanceLeftDel(t.key, t.val(), del, t.right());
			else
				return red(t.key, t.val(), del, t.right());
		}
		if (t.right() instanceof PersistentTreeMap.Black)
			return balanceRightDel(t.key, t.val(), t.left(), del);
		return red(t.key, t.val(), t.left(), del);
		// return t.removeLeft(del);
		// return t.removeRight(del);
	}

	static PersistentTreeMap.Node append(PersistentTreeMap.Node left,
			PersistentTreeMap.Node right) {
		if (left == null)
			return right;
		else if (right == null)
			return left;
		else if (left instanceof PersistentTreeMap.Red) {
			if (right instanceof PersistentTreeMap.Red) {
				PersistentTreeMap.Node app = append(left.right(), right.left());
				if (app instanceof PersistentTreeMap.Red)
					return red(app.key, app.val(), red(left.key, left.val(),
							left.left(), app.left()), red(right.key, right
							.val(), app.right(), right.right()));
				else
					return red(left.key, left.val(), left.left(), red(
							right.key, right.val(), app, right.right()));
			} else
				return red(left.key, left.val(), left.left(), append(left
						.right(), right));
		} else if (right instanceof PersistentTreeMap.Red)
			return red(right.key, right.val(), append(left, right.left()),
					right.right());
		else // black/black
		{
			PersistentTreeMap.Node app = append(left.right(), right.left());
			if (app instanceof PersistentTreeMap.Red)
				return red(app.key, app.val(), black(left.key, left.val(), left
						.left(), app.left()), black(right.key, right.val(), app
						.right(), right.right()));
			else
				return balanceLeftDel(left.key, left.val(), left.left(), black(
						right.key, right.val(), app, right.right()));
		}
	}

	static PersistentTreeMap.Node balanceLeftDel(Object key, Object val,
			PersistentTreeMap.Node del, PersistentTreeMap.Node right) {
		if (del instanceof PersistentTreeMap.Red)
			return red(key, val, del.blacken(), right);
		else if (right instanceof PersistentTreeMap.Black)
			return rightBalance(key, val, del, right.redden());
		else if (right instanceof PersistentTreeMap.Red
				&& right.left() instanceof PersistentTreeMap.Black)
			return red(right.left().key, right.left().val(), black(key, val,
					del, right.left().left()), rightBalance(right.key, right
					.val(), right.left().right(), right.right().redden()));
		else
			throw new UnsupportedOperationException("Invariant violation");
	}

	static PersistentTreeMap.Node balanceRightDel(Object key, Object val,
			PersistentTreeMap.Node left, PersistentTreeMap.Node del) {
		if (del instanceof PersistentTreeMap.Red)
			return red(key, val, left, del.blacken());
		else if (left instanceof PersistentTreeMap.Black)
			return leftBalance(key, val, left.redden(), del);
		else if (left instanceof PersistentTreeMap.Red
				&& left.right() instanceof PersistentTreeMap.Black)
			return red(left.right().key, left.right().val(), leftBalance(
					left.key, left.val(), left.left().redden(), left.right()
							.left()),
					black(key, val, left.right().right(), del));
		else
			throw new UnsupportedOperationException("Invariant violation");
	}

	static PersistentTreeMap.Node leftBalance(Object key, Object val,
			PersistentTreeMap.Node ins, PersistentTreeMap.Node right) {
		if (ins instanceof PersistentTreeMap.Red
				&& ins.left() instanceof PersistentTreeMap.Red)
			return red(ins.key, ins.val(), ins.left().blacken(), black(key,
					val, ins.right(), right));
		else if (ins instanceof PersistentTreeMap.Red
				&& ins.right() instanceof PersistentTreeMap.Red)
			return red(ins.right().key, ins.right().val(), black(ins.key, ins
					.val(), ins.left(), ins.right().left()), black(key, val,
					ins.right().right(), right));
		else
			return black(key, val, ins, right);
	}

	static PersistentTreeMap.Node rightBalance(Object key, Object val,
			PersistentTreeMap.Node left, PersistentTreeMap.Node ins) {
		if (ins instanceof PersistentTreeMap.Red
				&& ins.right() instanceof PersistentTreeMap.Red)
			return red(ins.key, ins.val(), black(key, val, left, ins.left()),
					ins.right().blacken());
		else if (ins instanceof PersistentTreeMap.Red
				&& ins.left() instanceof PersistentTreeMap.Red)
			return red(ins.left().key, ins.left().val(), black(key, val, left,
					ins.left().left()), black(ins.key, ins.val(), ins.left()
					.right(), ins.right()));
		else
			return black(key, val, left, ins);
	}

	PersistentTreeMap.Node replace(PersistentTreeMap.Node t, Object key,
			Object val) {
		int c = doCompare(key, t.key);
		return t.replace(t.key, c == 0 ? val : t.val(), c < 0 ? replace(t
				.left(), key, val) : t.left(), c > 0 ? replace(t.right(), key,
				val) : t.right());
	}

	PersistentTreeMapTC(Comparator comp, PersistentTreeMap.Node tree,
			int count, IPersistentMap meta) {
		super(meta);
		this.comp = comp;
		this.tree = tree;
		this._count = count;
	}

	static PersistentTreeMap.Red red(Object key, Object val,
			PersistentTreeMap.Node left, PersistentTreeMap.Node right) {
		if (left == null && right == null) {
			if (val == null)
				return new PersistentTreeMap.Red(key);
			return new PersistentTreeMap.RedVal(key, val);
		}
		if (val == null)
			return new PersistentTreeMap.RedBranch(key, left, right);
		return new PersistentTreeMap.RedBranchVal(key, val, left, right);
	}

	static PersistentTreeMap.Black black(Object key, Object val,
			PersistentTreeMap.Node left, PersistentTreeMap.Node right) {
		if (left == null && right == null) {
			if (val == null)
				return new PersistentTreeMap.Black(key);
			return new PersistentTreeMap.BlackVal(key, val);
		}
		if (val == null)
			return new PersistentTreeMap.BlackBranch(key, left, right);
		return new PersistentTreeMap.BlackBranchVal(key, val, left, right);
	}

	private boolean utilSame(Object o1, Object o2) {
		try {
			return ((Boolean) Reflector.invokeStaticMethod(Util.class, "same",
					new Object[] { o1, o2 })).booleanValue();
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not invoke 'same' method of Util", e);
		}
	}
}
