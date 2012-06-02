package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Attribute constructor.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CAttr extends CName {
  /** Generated namespace. */
  private static final byte[] NS0 = token("ns0:");
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param ii input info
   * @param c computed construction flag
   * @param n name
   * @param v attribute values
   */
  public CAttr(final InputInfo ii, final boolean c, final Expr n, final Expr... v) {
    super(ATTRIBUTE, ii, n, v);
    comp = c;
    type = SeqType.ATT;
  }

  @Override
  public FAttr item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    QNm nm = qname(ctx, ii);
    final byte[] cp = nm.prefix();
    if(comp) {
      final byte[] cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XMLURI)) CAXML.thrw(info);
      if(eq(cu, XMLNSURI)) CAINV.thrw(info, cu);
      if(eq(cp, XMLNS) || cp.length == 0 && eq(nm.string(), XMLNS))
        CAINV.thrw(info, nm.string());

      // create new standard namespace to cover most frequent cases
      if(eq(cp, EMPTY) && !eq(cu, EMPTY))
        nm = new QNm(concat(NS0, nm.string()), cu);
    }

    byte[] val = value(ctx, ii);
    if(eq(cp, XML) && eq(nm.local(), ID)) val = norm(val);

    return new FAttr(nm, val);
  }
}
