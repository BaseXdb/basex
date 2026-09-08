package org.basex.data;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides read-only access to the compressed namespace structure of a database.
 *
 * Nodes with children ("inner nodes") are kept in main memory, as only they can be ancestors of
 * other nodes. Nodes without children ("leaves") are grouped by their parent and read on demand;
 * a single leaf occupies one byte if its distance to the preceding leaf and its set ID are small:
 *
 * <pre>
 * Bits 7-4: distance to the PRE value of the preceding leaf, minus 1 (15: escape)
 * Bits 3-0: ID of the set with the prefix/namespace URI pairs (15: escape)
 * </pre>
 *
 * Escaped values are appended as compressed numbers, distance first.
 *
 * As the entries of a group are delta-encoded, they can only be read from the start. Groups are
 * therefore split into blocks of {@link #BLOCK} entries. The PRE value and the offset at which a
 * block starts are stored with the inner nodes, so that a single entry can be located by reading
 * one block, no matter how many leaves a node has.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NSEntries {
  /** Escape marker. */
  private static final int ESCAPE = 0xF;
  /** Number of leaves per block. */
  private static final int BLOCK = 256;

  /** PRE values of the inner nodes, in ascending order. */
  private final int[] pres;
  /** Set IDs of the inner nodes. */
  private final int[] setIds;
  /** References to the parents of the inner nodes ({@code -1} for the root). */
  private final int[] parents;
  /** Numbers of leaves of the inner nodes. */
  private final int[] leaves;
  /** Offsets of the leaf groups (with one additional entry for the total length). */
  private final long[] offsets;
  /** First block of each inner node (with one additional entry for the total number). */
  private final int[] blockStarts;
  /** PRE values that precede the blocks (the first block of a node is not included). */
  private final int[] blockPres;
  /** Offsets of the blocks, relative to their group. */
  private final int[] blockOffsets;
  /** Leaf entries (can be {@code null} if no leaves exist). */
  private final DataAccess access;

  /** Last decoded block (can be {@code null}). */
  private volatile Block cached;

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param file file with the leaf entries
   * @throws IOException I/O exception
   */
  NSEntries(final DataInput in, final IOFile file) throws IOException {
    final int ns = in.readNum();
    pres = new int[ns];
    setIds = new int[ns];
    parents = new int[ns];
    leaves = new int[ns];
    offsets = new long[ns + 1];
    blockStarts = new int[ns + 1];

    final int[] children = new int[ns];
    final IntList bpres = new IntList(), boffsets = new IntList();
    int pre = -1;
    long offset = 0;
    for(int n = 0; n < ns; n++) {
      pre += in.readNum();
      pres[n] = pre;
      setIds[n] = in.readNum();
      children[n] = in.readNum();
      leaves[n] = in.readNum();
      offsets[n] = offset;
      offset += in.readNum();
      blockStarts[n] = bpres.size();
      // read the PRE values and offsets of all blocks but the first one
      int bpre = pre, boffset = 0;
      for(int b = blocks(n) - 1; b > 0; b--) {
        bpre += in.readNum();
        boffset += in.readNum();
        bpres.add(bpre);
        boffsets.add(boffset);
      }
    }
    offsets[ns] = offset;
    blockStarts[ns] = bpres.size();
    blockPres = bpres.finish();
    blockOffsets = boffsets.finish();
    access = offset == 0 ? null : new DataAccess(file, true);

    // rebuild the parent references from the depth-first order and the numbers of children
    final IntList stack = new IntList(), todo = new IntList();
    for(int n = 0; n < ns; n++) {
      while(!todo.isEmpty() && todo.peek() == 0) {
        todo.pop();
        stack.pop();
      }
      if(stack.isEmpty()) {
        parents[n] = -1;
      } else {
        parents[n] = stack.peek();
        todo.set(todo.size() - 1, todo.peek() - 1);
      }
      stack.push(n);
      todo.push(children[n]);
    }
  }

  /**
   * Closes the leaf entries.
   */
  void close() {
    if(access != null) access.close();
  }

  /**
   * Writes the inner nodes to disk. The leaf entries are left untouched.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    final int ns = pres.length;
    final int[] children = new int[ns];
    for(int n = 1; n < ns; n++) children[parents[n]]++;

    out.writeNum(ns);
    final Header header = new Header(out);
    for(int n = 0; n < ns; n++) {
      header.node(pres[n], setIds[n], children[n], leaves[n],
          (int) (offsets[n + 1] - offsets[n]));
      for(int b = blockStarts[n], bs = blockStarts[n + 1]; b < bs; b++) {
        header.block(blockPres[b], blockOffsets[b]);
      }
    }
  }

  /**
   * This class writes the inner nodes of a namespace structure. PRE values and block offsets are
   * stored as distances, so the values of the last written node are remembered.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class Header {
    /** Output stream. */
    private final DataOutput out;
    /** PRE value of the last written node. */
    private int last = -1;
    /** PRE value of the last written block. */
    private int blockPre;
    /** Offset of the last written block. */
    private int blockOffset;

    /**
     * Constructor.
     * @param out output stream
     */
    private Header(final DataOutput out) {
      this.out = out;
    }

    /**
     * Writes an inner node. Its blocks are appended with {@link #block(int, int)}.
     * @param pre PRE value
     * @param setId set ID
     * @param inner number of inner children
     * @param leaves number of leaves
     * @param bytes number of bytes of the leaf entries
     * @throws IOException I/O exception
     */
    private void node(final int pre, final int setId, final int inner, final int leaves,
        final int bytes) throws IOException {

      out.writeNum(pre - last);
      last = pre;
      out.writeNum(setId);
      out.writeNum(inner);
      out.writeNum(leaves);
      out.writeNum(bytes);
      blockPre = pre;
      blockOffset = 0;
    }

    /**
     * Writes a single block of the last written inner node.
     * @param pre PRE value that precedes the block
     * @param offset offset of the block
     * @throws IOException I/O exception
     */
    private void block(final int pre, final int offset) throws IOException {
      out.writeNum(pre - blockPre);
      out.writeNum(offset - blockOffset);
      blockPre = pre;
      blockOffset = offset;
    }
  }

  // Requesting Namespaces ========================================================================

  /**
   * Finds the inner node that is located closest to the specified PRE value.
   * @param pre PRE value
   * @param data data reference
   * @return reference to an inner node, or {@code -1} if no namespaces exist
   */
  private int find(final int pre, final Data data) {
    int n = search(pres, pre);
    while(n > 0 && pres[n] + data.size(pres[n], Data.ELEM) <= pre) n = parents[n];
    return n;
  }

  /**
   * Returns the ID of the set that is declared for the specified PRE value.
   * @param pre PRE value
   * @param data data reference
   * @return set ID ({@code 0}: no namespaces are declared)
   */
  int setId(final int pre, final Data data) {
    final int n = find(pre, data);
    return n == -1 ? 0 : pres[n] == pre ? setIds[n] : leafSetId(n, pre);
  }

  /**
   * Returns the ID of the namespace URI for the specified prefix and PRE value.
   * @param prefixId prefix reference
   * @param pre PRE value
   * @param data data reference
   * @param sets sets of prefix/namespace URI pairs
   * @return ID of the namespace URI, or {@code 0} if none is found
   */
  int uriId(final int prefixId, final int pre, final Data data, final NSSets sets) {
    int n = find(pre, data);
    if(n == -1) return 0;
    if(pres[n] != pre) {
      final int uriId = sets.uri(leafSetId(n, pre), prefixId);
      if(uriId != 0) return uriId;
    }
    for(; n != -1; n = parents[n]) {
      final int uriId = sets.uri(setIds[n], prefixId);
      if(uriId != 0) return uriId;
    }
    return 0;
  }

  /**
   * Returns the common default namespace of all documents of the database.
   * @param ns namespace reference
   * @param ndocs number of documents
   * @param data data reference
   * @return namespace, or {@code null} if there is no common namespace
   */
  byte[] defaultNs(final Namespaces ns, final int ndocs, final Data data) {
    final int size = pres.length;
    // no namespaces defined: default namespace is empty
    if(size == 0 || size == 1 && leaves[0] == 0) return Token.EMPTY;
    // give up if the root has inner children or if the number of children differs
    if(size > 1 || leaves[0] != ndocs) return null;

    int id = 0;
    for(int b = 0, bs = blocks(0); b < bs; b++) {
      final Block bl = block(0, b);
      final int cl = bl.pres.length;
      for(int l = 0; l < cl; l++) {
        id = ns.defaultNs(bl.pres[l], bl.sets[l], id, data);
        if(id == 0) return null;
      }
    }
    return ns.uri(id);
  }

  /**
   * Adds all namespace entries in the specified PRE range to a list, in ascending PRE order.
   * Only the leaf blocks that can intersect the range are read.
   * @param list list with the PRE value, parent PRE value, level and set ID of each entry
   * @param start first PRE value
   * @param end last PRE value
   */
  void entries(final IntList list, final int start, final int end) {
    final int hi = search(pres, end), preceding = search(pres, start);
    final int lo = pres[preceding] == start ? preceding : preceding + 1;

    // inner nodes in the range, and all nodes whose leaves can reach into the range
    final IntList tuples = new IntList(), groups = new IntList();
    for(int n = preceding; n != -1; n = parents[n]) {
      if(n < lo) groups.add(n);
    }
    for(int n = lo; n <= hi; n++) {
      if(parents[n] != -1) tuples.add(pres[n]).add(pres[parents[n]]).add(level(n)).add(setIds[n]);
      groups.add(n);
    }

    // leaves of the collected groups
    for(final int n : groups.finish()) {
      if(leaves[n] == 0) continue;
      final int level = level(n) + 1;
      for(int b = blockIndex(n, start), bs = blocks(n); b < bs; b++) {
        final Block bl = block(n, b);
        final int cl = bl.pres.length;
        int l = Math.max(0, search(bl.pres, start - 1) + 1);
        for(; l < cl && bl.pres[l] <= end; l++) {
          tuples.add(bl.pres[l]).add(pres[n]).add(level).add(bl.sets[l]);
        }
        if(l < cl) break;
      }
    }

    // sort the entries by their PRE values
    final int ts = tuples.size() >>> 2;
    final int[] keys = new int[ts];
    for(int t = 0; t < ts; t++) keys[t] = tuples.get(t << 2);
    final int[] order = Array.createOrder(keys, true);
    for(int t = 0; t < ts; t++) {
      final int o = order[t] << 2;
      list.add(tuples.get(o)).add(tuples.get(o + 1)).add(tuples.get(o + 2)).add(tuples.get(o + 3));
    }
  }

  /**
   * Returns the level of an inner node.
   * @param n reference to the inner node
   * @return level ({@code 0} for the root)
   */
  private int level(final int n) {
    int level = -1;
    for(int m = n; m != -1; m = parents[m]) level++;
    return level;
  }

  /**
   * Rebuilds the mutable namespace tree.
   * @return root node
   */
  NSNode inflate() {
    final int size = pres.length;
    if(size == 0) return new NSNode(-1);

    // create the inner nodes and group them by their parents (the root has no parent)
    final NSNode[] nodes = new NSNode[size];
    final int[] offs = new int[size + 1];
    for(int n = 0; n < size; n++) {
      nodes[n] = new NSNode(pres[n], setIds[n]);
      if(n > 0) offs[parents[n] + 1]++;
    }
    for(int n = 0; n < size; n++) offs[n + 1] += offs[n];
    final int[] children = new int[size - 1], cursors = offs.clone();
    for(int n = 1; n < size; n++) children[cursors[parents[n]]++] = n;

    // merge the inner children and the leaves of each node; both are sorted by their PRE values
    for(int n = 0; n < size; n++) {
      final int offset = offs[n], is = offs[n + 1] - offset, ls = leaves[n];
      if(is + ls == 0) continue;

      final NSNode[] nds = new NSNode[is + ls];
      int c = 0, i = 0;
      for(int b = 0, bs = blocks(n); b < bs; b++) {
        final Block bl = block(n, b);
        final int cl = bl.pres.length;
        for(int l = 0; l < cl; l++) {
          while(i < is && pres[children[offset + i]] < bl.pres[l]) {
            nds[c++] = nodes[children[offset + i++]];
          }
          nds[c++] = new NSNode(bl.pres[l], bl.sets[l]);
        }
      }
      while(i < is) nds[c++] = nodes[children[offset + i++]];
      nodes[n].children(nds);
    }
    return nodes[0];
  }

  // Private Methods ==============================================================================

  /**
   * Returns the number of blocks of an inner node.
   * @param n reference to the inner node
   * @return number of blocks ({@code 0} if the node has no leaves)
   */
  private int blocks(final int n) {
    return (leaves[n] + BLOCK - 1) / BLOCK;
  }

  /**
   * Returns the block of an inner node that can contain the specified PRE value.
   * @param n reference to the inner node
   * @param pre PRE value
   * @return block
   */
  private int blockIndex(final int n, final int pre) {
    // the stored PRE values precede their blocks: find the first one that is not smaller
    int l = blockStarts[n], h = blockStarts[n + 1] - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      if(blockPres[m] < pre) l = m + 1;
      else h = m - 1;
    }
    return l - blockStarts[n];
  }

  /**
   * Returns the ID of the set that is declared by a leaf of the specified inner node.
   * @param n reference to the inner node
   * @param pre PRE value
   * @return set ID ({@code 0}: no namespaces are declared)
   */
  private int leafSetId(final int n, final int pre) {
    if(leaves[n] == 0) return 0;
    final Block bl = block(n, blockIndex(n, pre));
    final int l = search(bl.pres, pre);
    return l >= 0 && bl.pres[l] == pre ? bl.sets[l] : 0;
  }

  /**
   * Returns a single block of leaves and decodes it if it is not cached.
   * @param n reference to the inner node
   * @param b block of the node
   * @return block
   */
  private Block block(final int n, final int b) {
    final Block bl = cached;
    if(bl != null && bl.node == n && bl.block == b) return bl;

    final int nl = Math.min(BLOCK, leaves[n] - b * BLOCK);
    final int[] cpres = new int[nl], csets = new int[nl];
    final int index = blockStarts[n] + b - 1;
    int pre = b == 0 ? pres[n] : blockPres[index];
    synchronized(this) {
      access.cursor(offsets[n] + (b == 0 ? 0 : blockOffsets[index]));
      for(int l = 0; l < nl; l++) {
        final int by = access.read1() & 0xFF, dist = by >>> 4, set = by & 0xF;
        pre += dist == ESCAPE ? access.readNum() : dist + 1;
        cpres[l] = pre;
        csets[l] = set == ESCAPE ? access.readNum() : set;
      }
    }
    final Block block = new Block(n, b, cpres, csets);
    cached = block;
    return block;
  }

  /**
   * A decoded block of leaves.
   * @param node reference to the inner node
   * @param block block of the node
   * @param pres PRE values
   * @param sets set IDs
   */
  private record Block(int node, int block, int[] pres, int[] sets) { }

  /**
   * Returns the position of the last array entry that is smaller than or equal to the
   * specified value.
   * @param array array with values in ascending order
   * @param value value to be found
   * @return position, or {@code -1} if all entries are greater
   */
  private static int search(final int[] array, final int value) {
    int l = 0, h = array.length - 1;
    while(l <= h) {
      final int m = l + h >>> 1, v = array[m];
      if(v == value) return m;
      if(v < value) l = m + 1;
      else h = m - 1;
    }
    return l - 1;
  }

  // Writing Namespaces ===========================================================================

  /**
   * Writes a namespace tree to disk.
   * @param root root node
   * @param sets sets of prefix/namespace URI pairs
   * @param out output stream for the inner nodes
   * @param file file for the leaf entries
   * @throws IOException I/O exception
   */
  static void write(final NSNode root, final NSSets sets, final DataOutput out, final IOFile file)
      throws IOException {

    final Writer writer = new Writer(sets, root);
    writer.compacted.write(out);
    out.writeNum(writer.nodes);
    try(DataOutput body = new DataOutput(file)) {
      writer.write(root, new Header(out), body);
    }
  }

  /**
   * This class converts a namespace tree to its compressed representation. The most frequent sets
   * are assigned the lowest IDs, and sets that are no longer referenced are dropped.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class Writer {
    /** Compacted sets. */
    private final NSSets compacted = new NSSets();
    /** Mapping from original to compacted set IDs ({@code 0}: empty set). */
    private final int[] remap;
    /** PRE values that precede the blocks of a single node. */
    private final IntList blockPres = new IntList();
    /** Offsets of the blocks of a single node. */
    private final IntList blockOffs = new IntList();
    /** Number of inner nodes. */
    private int nodes = 1;

    /**
     * Constructor.
     * @param sets sets of prefix/namespace URI pairs
     * @param root root node
     */
    private Writer(final NSSets sets, final NSNode root) {
      final int ss = sets.size() + 1;
      final IntList counts = new IntList(new int[ss]);
      count(root, counts);

      // assign the lowest IDs to the most frequent sets; drop unreferenced ones
      remap = new int[ss];
      final int[] order = counts.createOrder(false);
      for(int o = 0; o < ss && counts.get(o) > 0; o++) {
        final int setId = order[o];
        if(setId != 0) remap[setId] = compacted.put(sets.get(setId));
      }
    }

    /**
     * Counts the referenced sets and the inner nodes of a subtree.
     * @param root root node
     * @param counts set counts
     */
    private void count(final NSNode root, final IntList counts) {
      // iterative traversal: the depth of the namespace structure is not limited
      final ArrayList<NSNode> stack = new ArrayList<>();
      stack.add(root);
      while(!stack.isEmpty()) {
        final NSNode node = stack.remove(stack.size() - 1);
        final int setId = node.setId();
        counts.set(setId, counts.get(setId) + 1);
        final int cs = node.children();
        for(int c = 0; c < cs; c++) {
          final NSNode child = node.child(c);
          if(child.children() > 0) nodes++;
          stack.add(child);
        }
      }
    }

    /**
     * Writes a namespace node and its descendants.
     * @param root root node
     * @param header writer for the inner nodes
     * @param body output stream for the leaf entries
     * @throws IOException I/O exception
     */
    private void write(final NSNode root, final Header header, final DataOutput body)
        throws IOException {

      // iterative traversal: the depth of the namespace structure is not limited
      final ArrayList<NSNode> stack = new ArrayList<>();
      stack.add(root);
      while(!stack.isEmpty()) {
        final NSNode node = stack.remove(stack.size() - 1);

        // encode the leaf entries, and remember the PRE value and offset of every further block
        final int pre = node.pre(), cs = node.children();
        final long start = body.size();
        blockPres.reset();
        blockOffs.reset();
        int last = pre, leaves = 0;
        for(int c = 0; c < cs; c++) {
          final NSNode child = node.child(c);
          if(child.children() > 0) continue;
          if(leaves++ % BLOCK == 0 && leaves > 1) {
            blockPres.add(last);
            blockOffs.add((int) (body.size() - start));
          }
          final int distance = child.pre() - last, setId = remap[child.setId()];
          last = child.pre();
          final int d = distance >= 1 && distance <= ESCAPE ? distance - 1 : ESCAPE;
          final int s = setId < ESCAPE ? setId : ESCAPE;
          body.write(d << 4 | s);
          if(d == ESCAPE) body.writeNum(distance);
          if(s == ESCAPE) body.writeNum(setId);
        }

        header.node(pre, remap[node.setId()], cs - leaves, leaves, (int) (body.size() - start));
        final int bs = blockPres.size();
        for(int b = 0; b < bs; b++) header.block(blockPres.get(b), blockOffs.get(b));

        // add the inner children in reverse order: they are written from left to right
        for(int c = cs - 1; c >= 0; c--) {
          final NSNode child = node.child(c);
          if(child.children() > 0) stack.add(child);
        }
      }
    }
  }
}
