package org.basex.query.value.node;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
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
    if(this == node) return true;
    if(!(node instanceof final JNode jnode)) return false;

    JNode n1 = this, n2 = jnode;
    while(n1 != n2 && n1 != null && n2 != null) {
      if(!n1.equals(n2)) return false;
      n1 = n1.parent;
      n2 = n2.parent;
    }
    return n1 == n2;
  }

  @Override
  public int compare(final GNode node) {
    if(this == node) return 0;
    if(!(node instanceof final JNode jnode)) return 1;

    int d1 = 0, d2 = 0;
    JNode n1 = this, n2 = jnode, r1 = n1, r2 = n2;
    while(r1.parent != null) {
      r1 = r1.parent;
      d1++;
    }
    while(r2.parent != null) {
      r2 = r2.parent;
      d2++;
    }
    while(d1 > d2) {
      n1 = n1.parent;
      if(node.equals(n1)) return 1;
      d1--;
    }
    while(d2 > d1) {
      n2 = n2.parent;
      if(equals(n2)) return -1;
      d2--;
    }
    while(n1.parent != null && !n1.parent.equals(n2.parent)) {
      n1 = n1.parent;
      n2 = n2.parent;
    }
    if(n1.parent != null && n1.parent.equals(n2.parent)) {
      final int d = Long.compare(n1.position, n2.position);
      return d != 0 ? d : Long.compare(n1.index(), n2.index());
    }
    return Integer.compare(System.identityHashCode(r1.value), System.identityHashCode(r2.value));
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
    throw Util.notExpected();
  }

  @Override
  public byte[] id() {
    final TokenBuilder tb = new TokenBuilder(Token.ID);
    for(JNode n = this; n != null; n = n.parent) {
      tb.addLong(n.parent != null ? n.index() + 1 : n.hashCode()).add('j');
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

      // map or array: check if direct lookup is possible
      final boolean direct = !descendant || (
          value instanceof XQMap ? ((MapType) value.type).valueType() :
        ((ArrayType) value.type).valueType()).type.instanceOf(BasicType.ANY_ATOMIC_TYPE);
      if(direct && test instanceof final JNodeTest nt) {
        final Item s = nt.key;
        if(s != null && s != Empty.VALUE) {
          Value c = null;
          long i = -1;
          if(struct instanceof final XQMap map) {
            try {
              c = map.getOrNull(s);
            } catch(final QueryException ex) {
              throw Util.notExpected(ex);
            }
          } else {
            final long as = struct.structSize();
            i = s instanceof final Itr itr ? itr.itr() - 1 : -1;
            if(i >= 0 && i < as) c = struct.valueAt(i);
          }
          return c != null ? singleIter(new JNode(s, c, JNode.this, i, 1)) : BasicNodeIter.EMPTY;
        }
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
    // the parent reference is ignored
    return this == obj || obj instanceof final JNode jnode &&
        JNodeTest.equals(key, jnode.key) && JNodeTest.equals(value, jnode.value);
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
    if(key == Empty.VALUE) {
      qs.paren(value);
    } else {
      qs.token('(').token(key).token(':').token(value).token(')');
    }
  }
}
