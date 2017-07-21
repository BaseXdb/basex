package org.basex.query.expr.index;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ValueAccess extends IndexAccess {
  /** Search expression. */
  private Expr expr;
  /** Index type. */
  private final IndexType type;
  /** Parent name test. */
  private final NameTest test;
  /** Trim search terms. */
  private boolean trim;

  /**
   * Constructor.
   * @param info input info
   * @param expr search expression
   * @param type index type
   * @param test test test (can be {@code null})
   * @param ictx index context
   */
  public ValueAccess(final InputInfo info, final Expr expr, final IndexType type,
      final NameTest test, final IndexContext ictx) {
    super(ictx, info);
    this.expr = expr;
    this.type = type;
    this.test = test;
  }

  /**
   * Sets the trim flag.
   * @param tr trim flag
   * @return self reference
   */
  public ValueAccess trim(final boolean tr) {
    trim = tr;
    return this;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final ArrayList<BasicNodeIter> iters = new ArrayList<>();
    final Iter iter = qc.iter(expr);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      final byte[] term = it.string(info);
      iters.add(iter(trim ? Token.trim(term) : term));
    }
    final int is = iters.size();
    return is == 0 ? BasicNodeIter.EMPTY : is == 1 ? iters.get(0) :
      new Union(info, expr).eval(iters.toArray(new NodeIter[is]), qc).iter();
  }

  /**
   * Returns an index iterator.
   * @param term term to be found
   * @return iterator
   */
  private BasicNodeIter iter(final byte[] term) {
    // special case: empty text node
    // - no element name: return 0 results (empty text nodes are non-existent)
    // - otherwise, return scan-based element iterator
    final int tl = term.length;
    if(tl == 0 && type == IndexType.TEXT)
      return test == null ? BasicNodeIter.EMPTY : scanEmpty();

    // check if index is available and if it may contain the requested term
    // otherwise, use sequential scan
    final Data data = ictx.data;
    boolean index = data.meta.index(type);
    if(type == IndexType.TEXT || type == IndexType.ATTRIBUTE) {
      index &= tl > 0 && tl <= data.meta.maxlen;
    }

    final IndexIterator ii = index ? data.iter(new StringToken(type, term)) : scan(term);
    final int kind = type == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    final DBNode tmp = new DBNode(data, 0, test == null ? kind : Data.ELEM);
    return new DBNodeIter(data) {
      @Override
      public DBNode next() {
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
      final boolean text = type == IndexType.TEXT;
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
          if(data.kind(pre) == kind) {
            if(eq(data.text(pre, text), value)) return true;
          }
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
    return new DBNodeIter(ictx.data) {
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
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final Expr sub = expr.inline(var, ex, cc);
    if(sub == null) return null;
    expr = sub;
    return optimize(cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new ValueAccess(info, expr.copy(cc, vm), type, test, ictx));
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
    addPlan(plan, planElem(DATA, ictx.data.meta.name, TYP, type, NAM, test), expr);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    final Function func = type == IndexType.TEXT ? Function._DB_TEXT : type == IndexType.ATTRIBUTE
        ? Function._DB_ATTRIBUTE : Function._DB_TOKEN;
    tb.add(func.toString(Str.get(ictx.data.meta.name), expr));
    if(test != null) tb.add("/parent::").addExt(test);
    return tb.toString();
  }
}
