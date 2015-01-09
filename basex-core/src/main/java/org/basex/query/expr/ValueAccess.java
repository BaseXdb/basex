package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This index class retrieves texts and attribute values from the index.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ValueAccess extends IndexAccess {
  /** Expression. */
  private Expr expr;
  /** Text index. */
  private final boolean text;
  /** Parent name test. */
  private final byte[] name;

  /**
   * Constructor.
   * @param info input info
   * @param expr index expression
   * @param text text index
   * @param name parent name test
   * @param ictx index context
   */
  public ValueAccess(final InputInfo info, final Expr expr, final boolean text, final byte[] name,
      final IndexContext ictx) {
    super(ictx, info);
    this.expr = expr;
    this.text = text;
    this.name = name;
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final ArrayList<NodeIter> iter = new ArrayList<>();
    final Iter ir = qc.iter(expr);
    for(Item it; (it = ir.next()) != null;) iter.add(index(it.string(info)));
    final int is = iter.size();
    return is == 0 ? AxisMoreIter.EMPTY : is == 1 ? iter.get(0) :
      new Union(info, expr).eval(iter.toArray(new NodeIter[is]));
  }

  /**
   * Returns an index iterator.
   * @param term term to be found
   * @return iterator
   */
  private AxisIter index(final byte[] term) {
    // special case: empty string
    final int tl = term.length;
    // - no element name: return 0 results, because empty text nodes are non-existent
    // - otherwise, return scan-based element iterator (only considers leaf elements)
    if(tl == 0 && text) return name == null ? AxisMoreIter.EMPTY : scanEmpty();

    // use index traversal if index exists and if term is not too long.
    // otherwise, scan data sequentially
    final Data data = ictx.data;
    final IndexIterator ii = (text ? data.meta.textindex : data.meta.attrindex) &&
        tl > 0 && tl <= data.meta.maxlen ? data.iter(new StringToken(text, term)) : scan(term);

    final int kind = text ? Data.TEXT : Data.ATTR;
    return new AxisIter() {
      @Override
      public ANode next() {
        while(ii.more()) {
          int pre = ii.pre();
          if(name != null) {
            final int par = data.parent(pre, kind);
            if(data.kind(par) != Data.ELEM || !eq(data.name(par, Data.ELEM), name)) continue;
            pre = par;
          }
          return new DBNode(data, pre, name == null ? kind : Data.ELEM);
        }
        return null;
      }
    };
  }

  /**
   * Returns a scan-based iterator, which accepts text nodes with the specified text string.
   * @param value value to be found
   * @return node iterator
   */
  private IndexIterator scan(final byte[] value) {
    return new IndexIterator() {
      final Data data = ictx.data;
      final byte kind = text ? Data.TEXT : Data.ATTR;
      final int sz = data.meta.size;
      int pre = -1;

      @Override
      public int pre() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < sz) {
          if(data.kind(pre) == kind && eq(data.text(pre, text), value)) return true;
        }
        return false;
      }
      @Override
      public int size() {
        return Math.max(1, sz >>> 1);
      }
    };
  }

  /**
   * Returns a scan-based iterator, which accepts leaf elements without text nodes.
   * @return node iterator
   */
  private AxisIter scanEmpty() {
    return new AxisIter() {
      final Data data = ictx.data;
      final int sz = data.meta.size;
      int pre = -1;

      @Override
      public DBNode next() {
        while(++pre < sz) {
          if(data.kind(pre) == Data.ELEM && data.size(pre, Data.ELEM) == 1)
            return new DBNode(data, pre, Data.ELEM);
        }
        return null;
      }
    };
  }

  @Override
  public boolean has(final Flag flag) {
    return expr.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return expr.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    final Expr sub = expr.inline(qc, scp, var, ex);
    if(sub == null) return null;
    expr = sub;
    return optimize(qc, scp);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new ValueAccess(info, expr.copy(qc, scp, vs), text, name, ictx);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize() + 1;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DATA, ictx.data.meta.name, TYP,
        text ? IndexType.TEXT : IndexType.ATTRIBUTE, NAM, name), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder string = new TokenBuilder();
    string.add((text ? Function._DB_TEXT : Function._DB_ATTRIBUTE).get(
        null, info, Str.get(ictx.data.meta.name), expr).toString());
    if(name != null) string.add("/parent::").add(name);
    return string.toString();
  }
}
