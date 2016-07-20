package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Insert expression.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** First flag. */
  private final boolean first;
  /** Last flag. */
  private final boolean last;
  /** Before flag. */
  private final boolean before;
  /** After flag. */
  private final boolean after;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param src source expression
   * @param first first flag
   * @param last last
   * @param before before
   * @param after after
   * @param trg target expression
   */
  public Insert(final StaticContext sc, final InputInfo info, final Expr src, final boolean first,
      final boolean last, final boolean before, final boolean after, final Expr trg) {
    super(sc, info, trg, src);
    this.first = first;
    this.last = last;
    this.before = before;
    this.after = after;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(info, sc).add(qc, exprs[1]);
    final ANodeList cList = c.children, aList = c.atts;
    if(c.errAtt != null) throw UPNOATTRPER_X.get(info, c.errAtt);
    if(c.duplAtt != null) throw UPATTDUPL_X.get(info, c.duplAtt);

    // check target constraints
    final Iter t = qc.iter(exprs[0]);
    final Item i = t.next();
    if(i == null) throw UPSEQEMP_X.get(info, Util.className(this));

    final boolean loc = before || after;
    if(!(i instanceof ANode)) throw (loc ? UPTRGTYP2_X : UPTRGTYP_X).get(info, i);
    final Item i2 = t.next();
    if(i2 != null) throw (loc ? UPTRGSNGL2_X : UPTRGSNGL_X).get(info, ValueBuilder.concat(i, i2));

    final ANode n = (ANode) i;
    final ANode par = n.parent();
    if(loc) {
      if(n.type == NodeType.ATT || n.type == NodeType.DOC) throw UPTRGTYP2_X.get(info, n);
      if(par == null) throw UPPAREMPTY_X.get(info, n);
    } else {
      if(n.type != NodeType.ELM && n.type != NodeType.DOC) throw UPTRGTYP_X.get(info, n);
    }

    NodeUpdate up;
    DBNode dbn;
    // no update primitive is created if node list is empty
    final Updates updates = qc.updates();
    if(!aList.isEmpty()) {
      final ANode targ = loc ? par : n;
      if(targ.type != NodeType.ELM) throw (loc ? UPATTELM_X : UPATTELM2_X).get(info, targ);

      dbn = updates.determineDataRef(targ, qc);
      up = new InsertAttribute(dbn.pre(), dbn.data(), info, checkNS(aList, targ));
      updates.add(up, qc);
    }

    // no update primitive is created if node list is empty
    if(!cList.isEmpty()) {
      dbn = updates.determineDataRef(n, qc);
      if(before) up = new InsertBefore(dbn.pre(), dbn.data(), info, cList);
      else if(after) up = new InsertAfter(dbn.pre(), dbn.data(), info, cList);
      else if(first) up = new InsertIntoAsFirst(dbn.pre(), dbn.data(), info, cList);
      else if(last) up = new InsertIntoAsLast(dbn.pre(), dbn.data(), info, cList);
      else up = new InsertInto(dbn.pre(), dbn.data(), info, cList);
      updates.add(up, qc);
    }
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vs) {
    return new Insert(sc, info, exprs[1].copy(cc, vs), first, last, before, after,
        exprs[0].copy(cc, vs));
  }

  @Override
  public String toString() {
    return INSERT + ' ' + NODE + ' ' + exprs[1] + ' ' + INTO + ' ' + exprs[0];
  }
}
