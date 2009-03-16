/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Nov 17, 2007 */

package clojure.lang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentTC extends ARef {
	static class Action implements Runnable {
		final AgentTC agent;

		final IFn fn;

		final ISeq args;

		final boolean solo;

		public Action(Agent agent, IFn fn, ISeq args, boolean solo) {
			this.agent = (AgentTC) (Object) agent;
			this.args = args;
			this.fn = fn;
			this.solo = solo;
		}

		void execute() {
			if (solo)
				soloExecutor.execute(this);
			else
				pooledExecutor.execute(this);
		}

		static void doRun(Agent.Action action) {
			synchronized (action.agent) {
				try {
					Var.pushThreadBindings(RT.map(RT.AGENT, action.agent));
					nested.set(PersistentVector.EMPTY);

					boolean hadError = false;
					try {
						Object oldval = action.agent.state;
						Object newval = action.fn.applyTo(RT.cons(
								action.agent.state, action.args));
						action.agent.setState(newval);
						action.agent.notifyWatches(oldval, newval);
					} catch (Throwable e) {
						// todo report/callback
						action.agent.errors = RT.cons(e, action.agent.errors);
						hadError = true;
					}

					if (!hadError) {
						releasePendingSends();
					}

					IPersistentStack next = ((AgentTC) (Object) action.agent).q
							.pop();
					((AgentTC) (Object) action.agent).q = next;

					if (next.count() > 0)
						((Action) next.peek()).execute();

				} finally {
					nested.set(null);
					Var.popThreadBindings();
				}
			}
		}

		public void run() {
			doRun((Agent.Action) (Object) this);
		}
	}

	volatile Object state;

	IPersistentStack q = PersistentQueue.EMPTY;

	volatile ISeq errors = null;

	final public static ExecutorService pooledExecutor = Executors
			.newFixedThreadPool(2 + Runtime.getRuntime().availableProcessors());

	final public static ExecutorService soloExecutor = Executors
			.newCachedThreadPool();

	final transient static ThreadLocal<IPersistentVector> nested = new ThreadLocal<IPersistentVector>();

	public static void shutdown() {
		soloExecutor.shutdown();
		pooledExecutor.shutdown();
	}

	public AgentTC(Object state) throws Exception {
		this(state, null);
	}

	public AgentTC(Object state, IPersistentMap meta) throws Exception {
		super(meta);
		setState(state);
	}

	synchronized boolean setState(Object newState) throws Exception {
		validate(newState);
		boolean ret = state != newState;
		state = newState;
		return ret;
	}

	public Object deref() throws Exception {
		if (errors != null) {
			throw new Exception("Agent has errors", (Exception) RT
					.first(errors));
		}
		return state;
	}

	public ISeq getErrors() {
		return errors;
	}

	public void clearErrors() {
		errors = null;
	}

	public Object dispatch(IFn fn, ISeq args, boolean solo) {
		if (errors != null) {
			throw new RuntimeException("Agent has errors", (Exception) RT
					.first(errors));
		}
		Action action = new Action((Agent) (Object) this, fn, args, solo);
		dispatchAction((Agent.Action) (Object) action);

		return this;
	}

	static void dispatchAction(Agent.Action action) {
		LockingTransaction trans = LockingTransaction.getRunning();
		if (trans != null)
			trans.enqueue(action);
		else if (nested.get() != null) {
			nested.set(nested.get().cons(action));
		} else
			action.agent.enqueue(action);
	}

	synchronized void enqueue(Agent.Action action) {
		IPersistentStack prior = q;
		q = (IPersistentStack) q.cons(action);

		if (prior.count() == 0)
			action.execute();
	}

	public synchronized int getQueueCount() {
		return q.count();
	}

	static public int releasePendingSends() {
		IPersistentVector sends = nested.get();
		if (sends == null)
			return 0;
		for (int i = 0; i < sends.count(); i++) {
			Agent.Action a = (Agent.Action) sends.valAt(i);
			a.agent.enqueue(a);
		}
		nested.set(PersistentVector.EMPTY);
		return sends.count();
	}
}
