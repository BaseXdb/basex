package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Rename expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param trg target expression
   * @param name new name expression
   */
  public Rename(final StaticContext sc, final InputInfo info, final Expr trg, final Expr name) {
    super(sc, info, trg, name);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter t = qc.iter(exprs[0]);
    final Item i = t.next();

    // check target constraints
    if(i == null) throw UPSEQEMP_X.get(info, Util.className(this));
    if(t.next() != null) throw UPWRTRGTYP.get(info);

    final CNode ex;
    if(i.type == NodeType.ELM) {
      ex = new CElem(sc, info, exprs[1], null);
    } else if(i.type == NodeType.ATT) {
      ex = new CAttr(sc, info, false, exprs[1], Empty.SEQ);
    } else if(i.type == NodeType.PI) {
      ex = new CPI(sc, info, exprs[1], Empty.SEQ);
    } else {
      throw UPWRTRGTYP.get(info);
    }

    final QNm rename = ex.item(qc, info).qname();
    final ANode targ = (ANode) i;

    // check namespace conflicts...
    if(targ.type == NodeType.ELM || targ.type == NodeType.ATT) {
      final byte[] rp = rename.prefix();
      final byte[] ru = rename.uri();
      final Atts at = targ.nsScope();
      final int as = at.size();
      for(int a = 0; a < as; a++) {
        if(eq(at.name(a), rp) && !eq(at.value(a), ru)) throw UPNSCONFL.get(info);
      }
    }

    final Updates updates = qc.resources.updates();
    final DBNode dbn = updates.determineDataRef(targ, qc);
    updates.add(new RenameNode(dbn.pre, dbn.data, info, rename), qc);
    return null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Rename(sc, info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs));
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + exprs[0] + ' ' + AS + ' ' + exprs[1];
  }
}
