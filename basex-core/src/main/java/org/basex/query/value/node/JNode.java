package org.basex.query.value.node;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * JNode.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JNode extends GNode {
  /** Key ({@link Empty#VALUE for root node}). */
  public final Item key;
  /** Value. */
  public final Value value;
  /** Sequence position (starts with {@code 1}). */
  public final long position;

  /** Parent node (can be {@code null}). */
  private final JNode parent;
  /** Child index ({@code -1} if not initialized yet). */
  private long index;

  /**
   * Root node constructor.
   * @param value value
   */
  public JNode(final Value value) {
    this(Empty.VALUE, value, null, -1, 0);
  }

  /**
   * Constructor.
   * @param parent parent node
   * @param index index ({@code -1} if not initialized yet)
   */
  public JNode(final JNode parent, final long index) {
    this((XQStruct) parent.value, parent, index, 1);
  }

  /**
   * Constructor.
   * @param struct map or array with key and value
   * @param parent parent node
   * @param index index ({@code -1} if not initialized yet)
   * @param position sequence position (starts with {@code 1})
   */
  public JNode(final XQStruct struct, final JNode parent, final long index, final long position) {
    this(struct.keyAt(index), struct.valueAt(index), parent, index, position);
  }

  /**
   * Constructor.
   * @param key key ({@link Empty#VALUE for root node})
   * @param value value
   * @param parent (can be {@code null})
   * @param index index ({@code -1} if not initialized yet)
   * @param position sequence position (starts with {@code 1})
   */
  public JNode(final Item key, final Value value, final JNode parent, final long index,
      final long position) {
    super(NodeType.get(key, value.seqType()));
    this.key = key;
    this.value = value;
    this.parent = parent;
    this.index = index;
    this.position = position;
  }

  /**
   * Indicates if this is a root node.
   * @return result of check
   */
  public boolean isRoot() {
    return key == Empty.VALUE;
  }

  /**
   * Returns the map or array that contains this non-root node.
   * @return container ({@code null} for the root node)
   */
  public XQStruct container() {
    return parent != null ? (XQStruct) parent.value.itemAt(position - 1) : null;
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return value.atomValue(qc, ii);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    // overrides Item#atomItem
    return atomValue(qc, ii).item(qc, ii);
  }

  @Override
  public Iter unwrappedIter(final QueryContext qc) throws QueryException {
    return unwrappedValue(qc).iter();
  }

  @Override
  public Value unwrappedValue(final QueryContext qc) {
    return value.unwrappedValue(qc);
  }

  @Override
  public boolean is(final GNode node) {
    return this == node || node instanceof JNode && compare(node) == 0;
  }

  @Override
  public int compare(final GNode node) {
    if(this == node) return 0;
    if(!(node instanceof final JNode jnode)) return 1;

    // determine roots and depths
    JNode r1 = this, r2 = jnode;
    int d1 = 0, d2 = 0;
    while(r1.parent != null) { r1 = r1.parent; d1++; }
    while(r2.parent != null) { r2 = r2.parent; d2++; }
    // different trees: arbitrary but stable order based on the root node ID
    if(r1 != r2) return Integer.signum(r1.id - r2.id);

    // same tree: compare the index paths leading from the root to both nodes
    final long[] p1 = new long[d1], p2 = new long[d2];
    JNode n = this;
    for(int i = d1 - 1; i >= 0; i--) { p1[i] = n.index(); n = n.parent; }
    n = jnode;
    for(int i = d2 - 1; i >= 0; i--) { p2[i] = n.index(); n = n.parent; }
    return Integer.signum(Arrays.compare(p1, p2));
  }

  /**
   * Computes or returns the child index of the key.
   * @return index ({@code -1} for root node)
   */
  private long index() {
    if(parent == null) return -1;

    long i = index;
    if(i != -1) return i;
    if(parent.value instanceof final XQMap map) {
      i = 0;
      for(final Item k : map.keys()) {
        if(JNodeTest.equals(k, key)) break;
        i++;
      }
    } else {
      i = ((Itr) key).itr() - 1;
    }
    index = i;
    return i;
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return atomItem(null, ii).compare(item, coll, transitive, ii);
  }

  @Override
  public byte[] id() {
    final TokenBuilder tb = new TokenBuilder(Token.ID);
    for(JNode n = this; n != null; n = n.parent) {
      tb.addLong(n.parent != null ? n.index() + 1 : n.id).add('j');
    }
    return tb.removeLast().finish();
  }

  @Override
  public GNode root() {
    return parent != null ? parent.root() : this;
  }

  @Override
  public GNode parent() {
    return parent;
  }

  @Override
  public boolean hasChildren() {
    return value instanceof final XQStruct struct && struct.structSize() != 0;
  }

  @Override
  public BasicNodeIter attributeIter() {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public BasicNodeIter childIter(final Test test, final boolean descendant) {
    // single maps or arrays, leaves
    if(value instanceof Item) {
      // leaves have no children
      if(!(value instanceof final XQStruct struct)) return BasicNodeIter.EMPTY;

      // direct lookup is always possible for the child axis; for the descendant axis it is
      // only safe over atomic-leaf values, as nested structures would hide deeper matches
      final boolean direct = !descendant || (
          value instanceof XQMap ? ((MapType) value.type).valueType() :
        ((ArrayType) value.type).valueType()).type.instanceOf(BasicType.ANY_ATOMIC_TYPE);
      if(direct && test instanceof final JNodeTest nt && nt.key != null && nt.key != Empty.VALUE) {
        final JNode child = child(nt.key);
        return child != null ? singleIter(child) : BasicNodeIter.EMPTY;
      }

      // map or array: sequential scan
      return new BasicNodeIter() {
        final long ss = struct.structSize();
        long s;

        @Override
        public GNode next() {
          return s < ss ? get(s++) : null;
        }
        @Override
        public long size() {
          return ss;
        }
        @Override
        public GNode get(final long i) {
          return new JNode(JNode.this, i);
        }
      };
    }

    // sequences: multiple scans
    return new BasicNodeIter() {
      XQStruct struct;
      long p, s, ss;

      @Override
      public GNode next() {
        while(true) {
          if(struct != null && s < ss) return new JNode(struct, JNode.this, s++, p);
          if(p == value.size()) return null;
          if(value.itemAt(p++) instanceof final XQStruct st) {
            struct = st;
            ss = st.structSize();
            s = 0;
          } else {
            struct = null;
          }
        }
      }
    };
  }

  /**
   * Returns the child node selected by a key via direct lookup.
   * @param item key (map: any atomic key; array: in-range integral numeric key, 1-based)
   * @return child node or {@code null} if no child is selected
   */
  public JNode child(final Item item) {
    if(value instanceof final XQMap map) {
      final Value v = map.value(item);
      return v != null ? new JNode(item, v, this, -1, 1) : null;
    }
    if(value instanceof final XQArray array && item instanceof final ANum num) {
      final double d = num.dbl();
      final long i = (long) d - 1;
      if(d == i + 1 && i >= 0 && i < array.structSize()) {
        return new JNode(item, array.valueAt(i), this, i, 1);
      }
    }
    return null;
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return item instanceof final JNode jnode && deep.equal(value, jnode.value);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    final Item item = value.item(null, ii);
    return item.isEmpty() ? Token.EMPTY : item.string(ii);
  }

  @Override
  public byte[] string() {
    throw Util.notExpected(this);
  }

  @Override
  public byte[] name() {
    throw Util.notExpected(this);
  }

  @Override
  public QNm qname() {
    return key instanceof final QNm qnm ? qnm : null;
  }

  @Override
  public Object toJava() throws QueryException {
    return value.toJava();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final JNode jnode && is(jnode);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), key, value);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return false;
  }

  @Override
  public Value materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    return value.materialize(test, ii, qc);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(QueryText.JTREE);
    if(isRoot()) {
      qs.paren(value);
    } else {
      qs.token('(').token(key).token(':').token(value).token(')');
    }
  }
}
