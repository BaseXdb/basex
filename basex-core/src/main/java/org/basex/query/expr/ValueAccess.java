package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
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
  private final NameTest test;

  /**
   * Constructor.
   * @param info input info
   * @param expr index expression
   * @param text text index
   * @param test test test
   * @param ictx index context
   */
  public ValueAccess(final InputInfo info, final Expr expr, final boolean text, final NameTest test,
      final IndexContext ictx) {
    super(ictx, info);
    this.expr = expr;
    this.text = text;
    this.test = test;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final ArrayList<BasicNodeIter> iter = new ArrayList<>();
    final Iter ir = qc.iter(expr);
    for(Item it; (it = ir.next()) != null;) iter.add(index(it.string(info)));
    final int is = iter.size();
    return is == 0 ? BasicNodeIter.EMPTY : is == 1 ? iter.get(0) :
      new Union(info, expr).eval(iter.toArray(new NodeIter[is])).iter();
  }

  /**
   * Returns an index iterator.
   * @param term term to be found
   * @return iterator
   */
  private BasicNodeIter index(final byte[] term) {
    // special case: empty text node
    // - no element name: return 0 results (empty text nodes are non-existent)
    // - otherwise, return scan-based element iterator
    final int tl = term.length;
    if(tl == 0 && text) return test == null ? BasicNodeIter.EMPTY : scanEmpty();

    // use index traversal if index exists and if term is not too long.
    // otherwise, scan data sequentially
    final Data data = ictx.data;
    final IndexIterator ii = (text ? data.meta.textindex : data.meta.attrindex) &&
        tl > 0 && tl <= data.meta.maxlen ? data.iter(new StringToken(text, term)) : scan(term);

    final int kind = text ? Data.TEXT : Data.ATTR;
    final DBNode tmp = new DBNode(data, 0, test == null ? kind : Data.ELEM);
    return new BasicNodeIter() {
      @Override
      public ANode next() {
        while(ii.more()) {
          if(test == null) {
            tmp.pre(ii.pre());
          } else {
            tmp.pre(data.parent(ii.pre(), kind));
            if(!test.eq(tmp)) continue;
          }
          return tmp.finish();
        }
        return null;
      }
    };
  }

  /**
   * Returns a scan-based index iterator, which looks for text nodes with the specified value.
   * @param value value to be looked up
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
   * Returns a scan-based iterator, which returns elements
   * a) matching the name test and
   * b) having no descendants.
   * @return node iterator
   */
  private BasicNodeIter scanEmpty() {
    return new BasicNodeIter() {
      final Data data = ictx.data;
      final DBNode tmp = new DBNode(data, 0, Data.ELEM);
      final int sz = data.meta.size;
      int pre = -1;

      @Override
      public DBNode next() {
        while(++pre < sz) {
          if(data.kind(pre) == Data.ELEM && data.size(pre, Data.ELEM) == 1) {
            tmp.pre(pre);
            if(test == null || test.eq(tmp)) return tmp.finish();
          }
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
    return copyType(new ValueAccess(info, expr.copy(qc, scp, vs), text, test, ictx));
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
        text ? IndexType.TEXT : IndexType.ATTRIBUTE, NAM, test), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder string = new TokenBuilder();
    string.add((text ? Function._DB_TEXT : Function._DB_ATTRIBUTE).get(
        null, info, Str.get(ictx.data.meta.name), expr).toString());
    if(test != null) string.add("/parent::").addExt(test);
    return string.toString();
  }
}
