package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * PI node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** Closing processing instruction. */
  private static final byte[] CLOSE = { '?', '>' };

  /** PI name. */
  private final QNm name;

  /**
   * Constructor.
   * @param n name
   * @param v value
   */
  public FPI(final QNm n, final byte[] v) {
    super(NodeType.PI);
    name = n;
    val = v;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param pi DOM node
   */
  public FPI(final ProcessingInstruction pi) {
    this(new QNm(pi.getTarget()), token(pi.getData()));
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] name() {
    return name.string();
  }

  @Override
  public FNode copy() {
    return new FPI(name, val).parent(par);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name.string(), VAL, val));
  }

  @Override
  public String toString() {
    return Util.info("<?% %?>", name.string(), val);
  }

  /**
   * Checks the specified token for validity.
   * @param atom token to be checked
   * @param ii input info
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] atom, final InputInfo ii)
      throws QueryException {

    if(contains(atom, CLOSE)) CPICONT.thrw(ii, atom);
    return atom;
  }
}
