/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jan 1, 2009 */

package clojure.lang;

final public class AtomTC extends ARef {
	Object state;

	public AtomTC(Object state) {
		this.state = state;
	}

	public AtomTC(Object state, IPersistentMap meta) {
		super(meta);
		this.state = state;
	}

	public Object deref() {
		return state;
	}

	public synchronized Object swap(IFn f) throws Exception {
		Object v = deref();
		Object newv = f.invoke(v);
		validate(newv);
		state = newv;
		notifyWatches(v, newv);
		return newv;
	}

	public synchronized Object swap(IFn f, Object arg) throws Exception {
		Object v = deref();
		Object newv = f.invoke(v, arg);
		validate(newv);
		state = newv;
		notifyWatches(v, newv);
		return newv;
	}

	public synchronized Object swap(IFn f, Object arg1, Object arg2)
			throws Exception {
		Object v = deref();
		Object newv = f.invoke(v, arg1, arg2);
		validate(newv);
		state = newv;
		notifyWatches(v, newv);
		return newv;
	}

	public synchronized Object swap(IFn f, Object x, Object y, ISeq args)
			throws Exception {
		Object v = deref();
		Object newv = f.applyTo(RT.listStar(v, x, y, args));
		validate(newv);
		state = newv;
		notifyWatches(v, newv);
		return newv;
	}

	public synchronized boolean compareAndSet(Object oldv, Object newv) {
		validate(newv);
		boolean ret = utilSame(state, oldv);
		if (ret) {
			state = newv;
			notifyWatches(oldv, newv);
		}
		return ret;
	}

	public synchronized Object reset(Object newval) {
		Object oldval = state;
		validate(newval);
		state = newval;
		notifyWatches(oldval, newval);
		return newval;
	}

	private static boolean utilSame(Object o1, Object o2) {
		try {
			return RT.booleanCast(Reflector.invokeStaticMethod(Util.class,
					"same", new Object[] { o1, o2 }));
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not invoke 'same' method of Util", e);
		}
	}
}
