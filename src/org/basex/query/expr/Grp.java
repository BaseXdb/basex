package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;

/**
 * Single Group Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Michael Seiferle
 */
public class Grp extends Expr {
  /** Sequence. */
  private SeqIter seq;
  /** Grouping expression. */
  private Var var;

  /**
   * Constructor.
   * @param v variable
   */
  public Grp(final Var v) {
    seq = new SeqIter();
    var = v;
  }
  
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    var = var.comp(ctx);
    return this;
  }

  /**
   * Adds an item for the membership check.
   * @param ctx query context
   * @throws QueryException query exception
   */
  void add(final QueryContext ctx) throws QueryException {
    if(seq != null) {
      final Iter iter = ctx.iter(var);
      Item it = iter.next();
      if(it != null) {
        if(iter.next() != null) Err.or(XPSORT);
        if(it.node()) it = Str.get(it.str());
        else if(it.n() && Double.isNaN(it.dbl())) it = null;
      }
      seq.add(it);
    }
  }

  /**
   * Resets the built sequence.
   */
  void finish() {
    if(seq != null) seq = new SeqIter();
  }

  /**
   * Returns the specified item.
   * @param i item index
   * @return item
   */
  Item item(final int i) {
    return seq == null ? Itr.get(i) : seq.item[i];
  }

  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    return var.uses(use, ctx);
  }

  @Override
  public Grp remove(final Var v) {
    return this;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return var.returned(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    var.plan(ser);
  }
  
  @Override
  public String toString() {
    return var.toString();
  }
}
