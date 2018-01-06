package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.*;
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
 * @author BaseX Team 2005-18, BSD License
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
    final Iter iter = exprs[0].iter(qc);
    final Item item = iter.next();

    // check target constraints
    if(item == null) throw UPSEQEMP_X.get(info, Util.className(this));
    final Item i2 = iter.next();
    if(i2 != null) throw UPWRTRGSINGLE_X.get(info, ValueBuilder.concat(item, i2, qc));

    final CNode ex;
    if(item.type == NodeType.ELM) {
      ex = new CElem(sc, info, exprs[1], null);
    } else if(item.type == NodeType.ATT) {
      ex = new CAttr(sc, info, false, exprs[1], Empty.SEQ);
    } else if(item.type == NodeType.PI) {
      ex = new CPI(sc, info, exprs[1], Empty.SEQ);
    } else {
      throw UPWRTRGTYP_X.get(info, item);
    }

    final QNm rename = ex.item(qc, info).qname();
    final ANode target = (ANode) item;

    // check namespace conflicts...
    if(target.type == NodeType.ELM || target.type == NodeType.ATT) {
      final byte[] rp = rename.prefix();
      final byte[] ru = rename.uri();
      final Atts at = target.nsScope(sc);
      final int as = at.size();
      for(int a = 0; a < as; a++) {
        if(eq(at.name(a), rp) && !eq(at.value(a), ru))
          throw UPNSCONFL_X_X.get(info, rename, new QNm(at.name(a), at.value(a)));
      }
    }

    final Updates updates = qc.updates();
    final DBNode dbn = updates.determineDataRef(target, qc);
    updates.add(new RenameNode(dbn.pre(), dbn.data(), info, rename), qc);
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Rename(sc, info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Rename && super.equals(obj);
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + exprs[0] + ' ' + AS + ' ' + exprs[1];
  }
}
