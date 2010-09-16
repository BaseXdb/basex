package org.basex.query.item;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;
import static org.basex.util.Token.*;
import org.w3c.dom.ProcessingInstruction;

/**
 * PI node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
   * @param p parent
   */
  public FPI(final QNm n, final byte[] v, final Nod p) {
    super(Type.PI);
    name = n;
    val = v;
    par = p;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param pi DOM node
   * @param parent parent reference
   */
  FPI(final ProcessingInstruction pi, final Nod parent) {
    this(new QNm(token(pi.getTarget())), token(pi.getData()), parent);
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] nname() {
    return name.atom();
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.pi(name.atom(), val);
  }

  @Override
  public FPI copy() {
    return new FPI(name, val, par);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.atom(), VAL, val);
  }

  @Override
  public String toString() {
    return Util.info("<?% %?>", name.atom(), val);
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
