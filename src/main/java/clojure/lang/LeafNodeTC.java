package clojure.lang;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.PersistentHashMap.LeafNode;

final class LeafNodeTC extends AMapEntry implements INode {
	final int hash;

	final Object key;

	final Object val;

	public LeafNodeTC(int hash, Object key, Object val) {
		this.hash = hash;
		this.key = key;
		this.val = val;
	}

	public INode assoc(int shift, int hash, Object key, Object val,
			Box addedLeaf) {
		if (hash == this.hash) {
			if (Util.equals(key, this.key)) {
				if (utilSame(val, this.val))
					return this;
				// note - do not set addedLeaf, since we are replacing
				return new LeafNodeTC(hash, key, val);
			}
			// hash collision - same hash, different keys
			LeafNodeTC newLeaf = new LeafNodeTC(hash, key, val);
			addedLeaf.val = newLeaf;
			return new PersistentHashMap.HashCollisionNode(hash,
					(PersistentHashMap.LeafNode) (Object) this,
					(PersistentHashMap.LeafNode) (Object) newLeaf);

		}
		return BitmapIndexedNode.create(shift, this, hash, key, val, addedLeaf);
	}

	public INode without(int hash, Object key) {
		if (hash == this.hash && Util.equals(key, this.key))
			return null;
		return this;
	}

	public LeafNode find(int hash, Object key) {
		if (hash == this.hash && Util.equals(key, this.key))
			return (PersistentHashMap.LeafNode) (Object) this;
		return null;
	}

	public ISeq nodeSeq() {
		return RT.cons(this, null);
	}

	public int getHash() {
		return hash;
	}

	public Object key() {
		return this.key;
	}

	public Object val() {
		return this.val;
	}

	public Object getKey() {
		return this.key;
	}

	public Object getValue() {
		return this.val;
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
