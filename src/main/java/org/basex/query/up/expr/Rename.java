package org.basex.query.up.expr;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CElem;
import org.basex.query.expr.CFrag;
import org.basex.query.expr.CPI;
import org.basex.query.expr.Expr;
import org.basex.query.item.DBNode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.RenameNode;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Rename expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();

    // check target constraints
    if(i == null) throw UPSEQEMP.thrw(input, Util.name(this));
    if(t.next() != null) UPWRTRGTYP.thrw(input);

    CFrag ex = null;
    if(i.type == NodeType.ELM) {
      ex = new CElem(input, expr[1], new Atts(), true);
    } else if(i.type == NodeType.ATT) {
      ex = new CAttr(input, false, expr[1]);
    } else if(i.type == NodeType.PI) {
      ex = new CPI(input, expr[1], Empty.SEQ);
    } else {
      throw UPWRTRGTYP.thrw(input);
    }

    // check namespace conflicts...
    final QNm rename = ex.item(ctx, input).qname();
    final ANode targ = (ANode) i;
    final ANode test = i.type == NodeType.ELM ? targ :
      i.type == NodeType.ATT ? targ.parent() : null;

    if(test != null) {
      final byte[] uri = test.uri(rename.pref(), ctx);
      if(uri != null && !eq(rename.uri().atom(), uri)) UPNSCONFL.thrw(input);
    }
    final DBNode dbn = ctx.updates.determineDataRef(targ, ctx);
    ctx.updates.add(new RenameNode(dbn.pre, dbn.data, input, rename), ctx);
    return null;
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + expr[0] + ' ' + AS + ' ' + expr[1];
  }
}
