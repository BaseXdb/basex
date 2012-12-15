package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Rename expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param ii input info
   * @param tg target expression
   * @param n new name expression
   */
  public Rename(final InputInfo ii, final Expr tg, final Expr n) {
    super(ii, tg, n);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();

    // check target constraints
    if(i == null) throw UPSEQEMP.thrw(info, Util.name(this));
    if(t.next() != null) UPWRTRGTYP.thrw(info);

    final CFrag ex;
    if(i.type == NodeType.ELM) {
      ex = new CElem(info, expr[1], null);
    } else if(i.type == NodeType.ATT) {
      ex = new CAttr(info, false, expr[1], Empty.SEQ);
    } else if(i.type == NodeType.PI) {
      ex = new CPI(info, expr[1], Empty.SEQ);
    } else {
      throw UPWRTRGTYP.thrw(info);
    }

    final QNm rename = ex.item(ctx, info).qname();
    final ANode targ = (ANode) i;

    // check namespace conflicts...
    if(targ.type == NodeType.ELM || targ.type == NodeType.ATT) {
      final byte[] rp = rename.prefix();
      final byte[] ru = rename.uri();
      final Atts at = targ.nsScope();
      final int as = at.size();
      for(int a = 0; a < as; a++) {
        if(eq(at.name(a), rp) && !eq(at.string(a), ru)) UPNSCONFL.thrw(info);
      }
    }

    final DBNode dbn = ctx.updates.determineDataRef(targ, ctx);
    ctx.updates.add(new RenameNode(dbn.pre, dbn.data, info, rename), ctx);
    return null;
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + expr[0] + ' ' + AS + ' ' + expr[1];
  }
}
