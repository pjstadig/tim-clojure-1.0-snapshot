/**
 Copyright (c) 2007-2008, Rich Hickey
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above
 copyright notice, this list of conditions and the following
 disclaimer in the documentation and/or other materials provided
 with the distribution.

 * Neither the name of Clojure nor the names of its contributors
 may be used to endorse or promote products derived from this
 software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 **/

/* rich Jun 18, 2007 */

package clojure.lang;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * A persistent rendition of Phil Bagwell's Hash Array Mapped Trie
 * 
 * Uses path copying for persistence HashCollision leaves vs. extended hashing
 * Node polymorphism vs. conditionals No sub-tree pools or root-resizing Any
 * errors are my own
 */

public class PersistentHashMapTC extends APersistentMap {

	final int count;

	final PersistentHashMap.INode root;

	final public static PersistentHashMap EMPTY = new PersistentHashMap(0,
			new PersistentHashMap.EmptyNode());

	static public IPersistentMap create(Map other) {
		IPersistentMap ret = EMPTY;
		for (Object o : other.entrySet()) {
			Map.Entry e = (Entry) o;
			ret = ret.assoc(e.getKey(), e.getValue());
		}
		return ret;
	}

	/*
	 * @param init {key1,val1,key2,val2,...}
	 */
	public static PersistentHashMap create(Object... init) {
		IPersistentMap ret = EMPTY;
		for (int i = 0; i < init.length; i += 2) {
			ret = ret.assoc(init[i], init[i + 1]);
		}
		return (PersistentHashMap) ret;
	}

	public static PersistentHashMap create(List init) {
		IPersistentMap ret = EMPTY;
		for (Iterator i = init.iterator(); i.hasNext();) {
			Object key = i.next();
			if (!i.hasNext())
				throw new IllegalArgumentException(String.format(
						"No value supplied for key: %s", key));
			Object val = i.next();
			ret = ret.assoc(key, val);
		}
		return (PersistentHashMap) ret;
	}

	static public PersistentHashMap create(ISeq items) {
		IPersistentMap ret = EMPTY;
		for (; items != null; items = items.next().next()) {
			if (items.next() == null)
				throw new IllegalArgumentException(String.format(
						"No value supplied for key: %s", items.first()));
			ret = ret.assoc(items.first(), RT.second(items));
		}
		return (PersistentHashMap) ret;
	}

	/*
	 * @param init {key1,val1,key2,val2,...}
	 */
	public static PersistentHashMap create(IPersistentMap meta, Object... init) {
		IPersistentMap ret = EMPTY.withMeta(meta);
		for (int i = 0; i < init.length; i += 2) {
			ret = ret.assoc(init[i], init[i + 1]);
		}
		return (PersistentHashMap) ret;
	}

	PersistentHashMapTC(int count, PersistentHashMap.INode root) {
		this.count = count;
		this.root = root;
	}

	public PersistentHashMapTC(IPersistentMap meta, int count,
			PersistentHashMap.INode root) {
		super(meta);
		this.count = count;
		this.root = root;
	}

	public boolean containsKey(Object key) {
		return entryAt(key) != null;
	}

	public IMapEntry entryAt(Object key) {
		return root.find(Util.hash(key), key);
	}

	public IPersistentMap assoc(Object key, Object val) {
		Box addedLeaf = new Box(null);
		PersistentHashMap.INode newroot = root.assoc(0, Util.hash(key), key,
				val, addedLeaf);
		if (newroot == root)
			return this;
		return new PersistentHashMap(meta(), addedLeaf.val == null ? count
				: count + 1, newroot);
	}

	public Object valAt(Object key, Object notFound) {
		IMapEntry e = entryAt(key);
		if (e != null)
			return e.val();
		return notFound;
	}

	public Object valAt(Object key) {
		return valAt(key, null);
	}

	public IPersistentMap assocEx(Object key, Object val) throws Exception {
		if (containsKey(key))
			throw new Exception("Key already present");
		return assoc(key, val);
	}

	public IPersistentMap without(Object key) {
		PersistentHashMap.INode newroot = root.without(Util.hash(key), key);
		if (newroot == root)
			return this;
		if (newroot == null)
			return EMPTY.withMeta(meta());
		return new PersistentHashMap(meta(), count - 1, newroot);
	}

	public Iterator iterator() {
		return new SeqIterator(seq());
	}

	public int count() {
		return count;
	}

	public ISeq seq() {
		return root.nodeSeq();
	}

	public IPersistentCollection empty() {
		return EMPTY.withMeta(meta());
	}

	static int mask(int hash, int shift) {
		// return ((hash << shift) >>> 27);// & 0x01f;
		return (hash >>> shift) & 0x01f;
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

	public PersistentHashMap withMeta(IPersistentMap meta) {
		return new PersistentHashMap(meta, count, root);
	}

	/*
	 * final static int[] pathmasks = new int[32]; static{ pathmasks[0] = 0;
	 * for(int i=1;i<32;i++) pathmasks[i] = 0x80000000 >> (i - 1); } //final
	 * static int pathmask(int hash, int shift){ // //return hash & (0x80000000 >>
	 * (shift - 1)); // return hash & pathmasks[shift]; // }
	 * 
	 * static boolean diffPath(int shift, int hash1, int hash2){ return shift >
	 * 0 && ((hash1^hash2) & pathmasks[shift]) != 0; //return shift > 0 &&
	 * pathmask(hash1^hash2,shift) != 0; }
	 */

	/*
	 * static interface ILeaf extends PersistentHashMap.INode{ int getHash(); }
	 */

	final static class FullNode implements PersistentHashMap.INode {
		final PersistentHashMap.INode[] nodes;

		final int shift;

		final int _hash;

		static int bitpos(int hash, int shift) {
			return 1 << mask(hash, shift);
		}

		FullNode(PersistentHashMap.INode[] nodes, int shift) {
			this.nodes = nodes;
			this.shift = shift;
			this._hash = nodes[0].getHash();
		}

		public PersistentHashMap.INode assoc(int levelShift, int hash,
				Object key, Object val, Box addedLeaf) {
			// if(levelShift < shift && diffPath(shift,hash,_hash))
			// return BitmapIndexedNode.create(levelShift, this, hash, key, val,
			// addedLeaf);
			int idx = mask(hash, shift);

			PersistentHashMap.INode n = nodes[idx].assoc(shift + 5, hash, key,
					val, addedLeaf);
			if (n == nodes[idx])
				return this;
			else {
				PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length];
				System.arraycopy(nodes, 0, newnodes, 0, nodes.length);
				newnodes[idx] = n;
				return new FullNode(newnodes, shift);
			}
		}

		public PersistentHashMap.INode without(int hash, Object key) {
			// if(diffPath(shift,hash,_hash))
			// return this;
			int idx = mask(hash, shift);
			PersistentHashMap.INode n = nodes[idx].without(hash, key);
			if (n != nodes[idx]) {
				if (n == null) {
					PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length - 1];
					System.arraycopy(nodes, 0, newnodes, 0, idx);
					System.arraycopy(nodes, idx + 1, newnodes, idx,
							nodes.length - (idx + 1));
					return new BitmapIndexedNode(~bitpos(hash, shift),
							newnodes, shift);
				}
				PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length];
				System.arraycopy(nodes, 0, newnodes, 0, nodes.length);
				newnodes[idx] = n;
				return new FullNode(newnodes, shift);
			}
			return this;
		}

		public PersistentHashMap.LeafNode find(int hash, Object key) {
			// if(diffPath(shift,hash,_hash))
			// return null;
			return nodes[mask(hash, shift)].find(hash, key);
		}

		public ISeq nodeSeq() {
			return PersistentHashMap.FullNode.Seq.create(
					(PersistentHashMap.FullNode) (Object) this, 0);
		}

		public int getHash() {
			return _hash;
		}
	}

	final static class BitmapIndexedNode implements PersistentHashMap.INode {
		final int bitmap;

		final PersistentHashMap.INode[] nodes;

		final int shift;

		final int _hash;

		static int bitpos(int hash, int shift) {
			return 1 << mask(hash, shift);
		}

		final int index(int bit) {
			return Integer.bitCount(bitmap & (bit - 1));
		}

		BitmapIndexedNode(int bitmap, PersistentHashMap.INode[] nodes, int shift) {
			this.bitmap = bitmap;
			this.nodes = nodes;
			this.shift = shift;
			this._hash = nodes[0].getHash();
		}

		static PersistentHashMap.INode create(int bitmap,
				PersistentHashMap.INode[] nodes, int shift) {
			if (bitmap == -1)
				return new FullNode(nodes, shift);
			return new BitmapIndexedNode(bitmap, nodes, shift);
		}

		static PersistentHashMap.INode create(int shift,
				PersistentHashMap.INode branch, int hash, Object key,
				Object val, Box addedLeaf) {
			// int hx = branch.getHash()^hash;
			// while(mask(hx,shift) == 0)
			// shift += 5;
			// if(mask(branch.getHash(),shift) == mask(hash,shift))
			// return create(shift+5,branch,hash,key,val,addedLeaf);
			return (new BitmapIndexedNode(bitpos(branch.getHash(), shift),
					new PersistentHashMap.INode[] { branch }, shift)).assoc(
					shift, hash, key, val, addedLeaf);
		}

		public PersistentHashMap.INode assoc(int levelShift, int hash,
				Object key, Object val, Box addedLeaf) {
			// if(levelShift < shift && diffPath(shift,hash,_hash))
			// return create(levelShift, this, hash, key, val, addedLeaf);
			int bit = bitpos(hash, shift);
			int idx = index(bit);
			if ((bitmap & bit) != 0) {
				PersistentHashMap.INode n = nodes[idx].assoc(shift + 5, hash,
						key, val, addedLeaf);
				if (n == nodes[idx])
					return this;
				else {
					PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length];
					System.arraycopy(nodes, 0, newnodes, 0, nodes.length);
					newnodes[idx] = n;
					return new BitmapIndexedNode(bitmap, newnodes, shift);
				}
			} else {
				PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length + 1];
				System.arraycopy(nodes, 0, newnodes, 0, idx);
				addedLeaf.val = newnodes[idx] = new LeafNode(hash, key, val);
				System.arraycopy(nodes, idx, newnodes, idx + 1, nodes.length
						- idx);
				return create(bitmap | bit, newnodes, shift);
			}
		}

		public PersistentHashMap.INode without(int hash, Object key) {
			// if(diffPath(shift,hash,_hash))
			// return this;
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) != 0) {
				int idx = index(bit);
				PersistentHashMap.INode n = nodes[idx].without(hash, key);
				if (n != nodes[idx]) {
					if (n == null) {
						if (bitmap == bit)
							return null;
						// if(nodes.length == 2)
						// return nodes[idx == 0?1:0];
						PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length - 1];
						System.arraycopy(nodes, 0, newnodes, 0, idx);
						System.arraycopy(nodes, idx + 1, newnodes, idx,
								nodes.length - (idx + 1));
						return new BitmapIndexedNode(bitmap & ~bit, newnodes,
								shift);
					}
					PersistentHashMap.INode[] newnodes = new PersistentHashMap.INode[nodes.length];
					System.arraycopy(nodes, 0, newnodes, 0, nodes.length);
					newnodes[idx] = n;
					return new BitmapIndexedNode(bitmap, newnodes, shift);
				}
			}
			return this;
		}

		public PersistentHashMap.LeafNode find(int hash, Object key) {
			// if(diffPath(shift,hash,_hash))
			// return null;
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) != 0) {
				return nodes[index(bit)].find(hash, key);
			} else
				return null;
		}

		public int getHash() {
			return _hash;
		}

		public ISeq nodeSeq() {
			return PersistentHashMap.BitmapIndexedNode.Seq.create(
					(PersistentHashMap.BitmapIndexedNode) (Object) this, 0);
		}
	}

	final static class LeafNode extends AMapEntry implements
			PersistentHashMap.INode {
		final int hash;

		final Object key;

		final Object val;

		public LeafNode(int hash, Object key, Object val) {
			this.hash = hash;
			this.key = key;
			this.val = val;
		}

		public PersistentHashMap.INode assoc(int shift, int hash, Object key,
				Object val, Box addedLeaf) {
			if (hash == this.hash) {
				if (Util.equals(key, this.key)) {
					if (utilSame(val, this.val))
						return this;
					// note - do not set addedLeaf, since we are replacing
					return new LeafNode(hash, key, val);
				}
				// hash collision - same hash, different keys
				LeafNode newLeaf = new LeafNode(hash, key, val);
				addedLeaf.val = newLeaf;
				return new PersistentHashMap.HashCollisionNode(hash,
						(PersistentHashMap.LeafNode) (Object) this,
						(PersistentHashMap.LeafNode) (Object) newLeaf);
			}
			return BitmapIndexedNode.create(shift, this, hash, key, val,
					addedLeaf);
		}

		public PersistentHashMap.INode without(int hash, Object key) {
			if (hash == this.hash && Util.equals(key, this.key))
				return null;
			return this;
		}

		public PersistentHashMap.LeafNode find(int hash, Object key) {
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
	}

	/*
	 * public static void main(String[] args){ try { ArrayList words = new
	 * ArrayList(); Scanner s = new Scanner(new File(args[0]));
	 * s.useDelimiter(Pattern.compile("\\W")); while(s.hasNext()) { String word =
	 * s.next(); words.add(word); } System.out.println("words: " +
	 * words.size()); IPersistentMap map = PersistentHashMap.EMPTY;
	 * //IPersistentMap map = new PersistentTreeMap(); //Map ht = new
	 * Hashtable(); Map ht = new HashMap(); Random rand;
	 * 
	 * System.out.println("Building map"); long startTime = System.nanoTime();
	 * for(Object word5 : words) { map = map.assoc(word5, word5); } rand = new
	 * Random(42); IPersistentMap snapshotMap = map; for(int i = 0; i <
	 * words.size() / 200; i++) { map =
	 * map.without(words.get(rand.nextInt(words.size() / 2))); } long
	 * estimatedTime = System.nanoTime() - startTime; System.out.println("count = " +
	 * map.count() + ", time: " + estimatedTime / 1000000);
	 * 
	 * System.out.println("Building ht"); startTime = System.nanoTime();
	 * for(Object word1 : words) { ht.put(word1, word1); } rand = new
	 * Random(42); for(int i = 0; i < words.size() / 200; i++) {
	 * ht.remove(words.get(rand.nextInt(words.size() / 2))); } estimatedTime =
	 * System.nanoTime() - startTime; System.out.println("count = " + ht.size() + ",
	 * time: " + estimatedTime / 1000000);
	 * 
	 * System.out.println("map lookup"); startTime = System.nanoTime(); int c =
	 * 0; for(Object word2 : words) { if(!map.contains(word2)) ++c; }
	 * estimatedTime = System.nanoTime() - startTime;
	 * System.out.println("notfound = " + c + ", time: " + estimatedTime /
	 * 1000000); System.out.println("ht lookup"); startTime = System.nanoTime();
	 * c = 0; for(Object word3 : words) { if(!ht.containsKey(word3)) ++c; }
	 * estimatedTime = System.nanoTime() - startTime;
	 * System.out.println("notfound = " + c + ", time: " + estimatedTime /
	 * 1000000); System.out.println("snapshotMap lookup"); startTime =
	 * System.nanoTime(); c = 0; for(Object word4 : words) {
	 * if(!snapshotMap.contains(word4)) ++c; } estimatedTime = System.nanoTime() -
	 * startTime; System.out.println("notfound = " + c + ", time: " +
	 * estimatedTime / 1000000); } catch(FileNotFoundException e) {
	 * e.printStackTrace(); } }
	 */
}
