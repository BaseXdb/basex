package org.basex.query.flwor;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Implementation of the group by clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {
  /** Group by specification. */
  private final Var[] groupby;
  /** Non-grouping variables. */
  private final Var[][] nongroup;
  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   * @param ng non-grouping variables
   * @param ngc copies of non-grouping variables
   */
  public Group(final InputInfo ii, final Var[] gb, final Var[] ng,
      final Var[] ngc) {
    super(ii);
    groupby = gb;
    nongroup = new Var[][]{ ng, ngc };
  }

  /**
   * Initializes the grouping partition.
   * @param ob order by specifier
   */
  void init(final Order ob) {
    gp = new GroupPartition(groupby, nongroup, ob, input);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Var g : groupby) {
      g.comp(ctx);
      if(g.ret != null) g.ret = SeqType.get(g.ret.type, 1);
    }

    for(final Var v : nongroup[1]) ctx.vars.add(v);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    throw Util.notexpected(this);
  }

  @Override
  public boolean uses(final Use use) {
    for(final Var v : groupby) if(v.uses(use)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    // non-grouping variables must be counted here (not in the return clause)
    int c = 0;
    for(final Var g : groupby) c += g.count(v);
    for(final Var g : nongroup[0]) c += g.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    // don't allow removal if variable is used
    for(final Var g : groupby) if(g.count(v) != 0) return false;
    for(final Var g : nongroup[0]) if(g.count(v) != 0) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != groupby.length; ++o) groupby[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(" " + GROUP + " " + BY + " ").
      addSep(groupby, SEP).toString();
  }
}
