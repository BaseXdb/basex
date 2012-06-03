package org.basex.query.flwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Implementation of the group by clause.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {
  /** Group by specification. */
  private final GroupSpec[] groupby;
  /** Non-grouping variables. */
  private final Var[][] nongroup;
  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   * @param ng non-grouping variables and their copies
   */
  public Group(final InputInfo ii, final GroupSpec[] gb, final Var[][] ng) {
    super(ii);
    groupby = gb;
    nongroup = ng;
  }

  /**
   * Initializes the grouping partition.
   * @param ob order by specifier
   */
  void init(final Order ob) {
    gp = new GroupPartition(groupby, nongroup, ob, info);
  }

  @Override
  public void checkUp() throws QueryException {
    for(final GroupSpec g : groupby) g.checkUp();
  }

  @Override
  public Expr analyze(final QueryContext ctx) throws QueryException {
    for(final GroupSpec g : groupby) g.analyze(ctx);
    return this;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    for(final GroupSpec g : groupby) {
      g.compile(ctx);
      if(g.grp.ret != null) g.grp.ret = SeqType.get(g.grp.ret.type, 1);
      ctx.vars.add(g.grp);
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
    for(final GroupSpec v : groupby) if(v.uses(use)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    // non-grouping variables must be counted here (not in the return clause)
    int c = 0;
    for(final GroupSpec g : groupby) c += g.count(v);
    for(final Var g : nongroup[0]) c += g.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    // don't allow removal if variable is used
    for(final GroupSpec g : groupby) if(g.count(v) != 0) return false;
    for(final Var g : nongroup[0]) if(g.count(v) != 0) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), groupby);
  }

  @Override
  public String toString() {
    return new TokenBuilder(' ' + GROUP + ' ' + BY + ' ').
      addSep(groupby, SEP).toString();
  }
}
