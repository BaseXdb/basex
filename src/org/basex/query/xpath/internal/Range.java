package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.StatsKey;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.LocPathRel;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestName;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * IndexRange, performing numeric range queries.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Range extends InternalExpr {
  /** Expression. */
  final Expr expr;
  /** Minimum Value. */
  final double min;
  /** Maximum Value. */
  final double max;

  /**
   * Constructor.
   * @param e first expression
   * @param mn minimum value
   * @param mx maximum value
   */
  public Range(final Expr e, final Item mn, final Item mx) {
    expr = e;
    min = mn.num();
    max = mx.num();
  }

  @Override
  public Bool eval(final XPContext ctx)
      throws QueryException {

    final Item v = ctx.eval(expr);
    if(v.size() == 0) return Bool.FALSE;

    if(v instanceof NodeSet) {
      final NodeSet nodes = (NodeSet) v;
      final Data data = nodes.data;
      for(int n = 0; n < nodes.size; n++) {
        final double d = data.atomNum(nodes.nodes[n]);
        if(d >= min && d <= max) return Bool.TRUE;
      }
      return Bool.FALSE;
    }
    final double d = v.num();
    return Bool.get(d >= min && d <= max);
  }
  
  /** Index type. */
  private RangeToken ind;
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) {
    final LocPath path = (LocPath) expr;
    final LocPath inv = path.invertPath(curr);

    final boolean txt = ind.type == IndexToken.TYPE.TXT;
    ctx.compInfo(txt ? OPTINDEX : OPTATTINDEX);
    if(!txt) inv.steps.add(0, Axis.create(Axis.SELF, path.steps.last().test));
    return new Path(new RangeAccess(ind), inv);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int mn) {
    // check which expression is a location path
    if(!(expr instanceof LocPathRel)) return Integer.MAX_VALUE;
    final LocPathRel path = (LocPathRel) expr;
    
    final Data data = ctx.item.data;
    final boolean txt = data.meta.txtindex;
    final boolean atv = data.meta.atvindex;
    
    final Step step = path.steps.last();
    final boolean text = txt && step.test == TestNode.TEXT &&
      step.preds.size() == 0 && data.meta.chop;
    if(!text && !atv || !path.checkAxes())
      return Integer.MAX_VALUE;

    ind = new RangeToken(text, min, max);
    final StatsKey key = getKey(path, data, text);
    if(key == null) return Integer.MAX_VALUE;

    // all values out of range - no results
    if(ind.min > ind.max || ind.max < key.min || ind.min > key.max) return 0;
    ind.min = Math.max(ind.min, key.min);
    ind.max = Math.min(ind.max, key.max);

    // if index can be applied, assume data size / 10 as costs
    return key.kind != StatsKey.Kind.DBL && key.kind !=
      StatsKey.Kind.INT ? Integer.MAX_VALUE : data.size / 10;
  }
  
  /**
   * Retrieves the statistics key for the tag/attribute name.
   * @param path location path
   * @param data data reference
   * @param text text flag
   * @return key
   */
  private StatsKey getKey(final LocPathRel path, final Data data,
      final boolean text) {
    
    final int st = path.steps.size();
    if(text) {
      if(st == 1) return null;
      final Step step = path.steps.get(st - 2);
      if(!(step.test instanceof TestName)) return null;
      return data.tags.stat(((TestName) step.test).id);
    }
    
    final int id = path.steps.last().simpleName(Axis.ATTR, true);
    if(id == Integer.MIN_VALUE) return null;
    return data.atts.stat(id);
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token(MIN), Token.token(min),
        Token.token(MAX), Token.token(max));
    expr.plan(ser);
    ser.closeElement(this);
  }
  
  @Override
  public String toString() {
    return "Range(" + min + " <= " + expr + " <= " + max + ")";
  }
}
