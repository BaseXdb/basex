package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Attribute constructor.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CAttr extends CName {
  /** Generated namespace. */
  private static final byte[] NS0 = token("ns0:");
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param comp computed construction flag
   * @param name name
   * @param values attribute values
   */
  public CAttr(final StaticContext sc, final InputInfo info, final boolean comp, final Expr name,
      final Expr... values) {
    super(ATTRIBUTE, sc, info, name, values);
    this.comp = comp;
    seqType = SeqType.ATT;
  }

  @Override
  public FAttr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    QNm nm = qname(qc, ii);
    final byte[] cp = nm.prefix();
    if(comp) {
      final byte[] cu = nm.uri();
      if(eq(cp, XML) ^ eq(cu, XML_URI)) throw CAXML.get(info);
      if(eq(cu, XMLNS_URI)) throw CAINV_.get(info, cu);
      if(eq(cp, XMLNS) || cp.length == 0 && eq(nm.string(), XMLNS))
        throw CAINV_.get(info, nm.string());

      // create new standard namespace to cover most frequent cases
      if(eq(cp, EMPTY) && !eq(cu, EMPTY))
        nm = new QNm(concat(NS0, nm.string()), cu);
    }
    if(!nm.hasURI() && nm.hasPrefix()) throw INVPREF_X.get(info, nm);

    byte[] val = value(qc, ii);
    if(eq(cp, XML) && eq(nm.local(), ID)) val = normalize(val);

    return new FAttr(nm, val);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CAttr(sc, info, comp, name.copy(qc, scp, vs), copyAll(qc, scp, vs, exprs));
  }
}
