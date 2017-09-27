package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-17, BSD License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** Insertion type. */
  public enum Mode {
    /** Into.   */ INTO,
    /** First.  */ FIRST,
    /** Last.   */ LAST,
    /** Before. */ BEFORE,
    /** After.  */ AFTER
  };

  /** Insertion mode. */
  private final Mode mode;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param src source expression
   * @param mode insertion mode
   * @param trg target expression
   */
  public Insert(final StaticContext sc, final InputInfo info, final Expr src, final Mode mode,
      final Expr trg) {
    super(sc, info, trg, src);
    this.mode = mode;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(info, sc).add(qc, exprs[1]);
    final ANodeList cList = c.children, aList = c.atts;
    if(c.errAtt != null) throw UPNOATTRPER_X.get(info, c.errAtt);
    if(c.duplAtt != null) throw UPATTDUPL_X.get(info, c.duplAtt);

    // check target constraints
    final Iter iter = qc.iter(exprs[0]);
    final Item it = iter.next();
    if(it == null) throw UPSEQEMP_X.get(info, Util.className(this));

    final boolean loc = mode == Mode.BEFORE || mode == Mode.AFTER;
    if(!(it instanceof ANode)) throw (loc ? UPTRGTYP2_X : UPTRGTYP_X).get(info, it);
    final Item i2 = iter.next();
    if(i2 != null) throw (loc ? UPTRGSNGL2_X : UPTRGSNGL_X).get(info, ValueBuilder.concat(it, i2));

    final ANode n = (ANode) it;
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
      switch(mode) {
        case BEFORE: up = new InsertBefore(dbn.pre(), dbn.data(), info, cList); break;
        case AFTER : up = new InsertAfter(dbn.pre(), dbn.data(), info, cList); break;
        case FIRST : up = new InsertIntoAsFirst(dbn.pre(), dbn.data(), info, cList); break;
        case LAST  : up = new InsertIntoAsLast(dbn.pre(), dbn.data(), info, cList); break;
        default    : up = new InsertInto(dbn.pre(), dbn.data(), info, cList);
      }
      updates.add(up, qc);
    }
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Insert(sc, info, exprs[1].copy(cc, vm), mode, exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Insert && mode == ((Insert) obj).mode && super.equals(obj);
  }

  @Override
  public String toString() {
    return QueryText.INSERT + ' ' + QueryText.NODE + ' ' + exprs[1] + ' ' +
        QueryText.INTO + ' ' + exprs[0];
  }
}
