package org.basex.query.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Comment node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FComm extends FNode {
  /** Two dashes, marking the start/end of a comment. */
  private static final byte[] DASHES = { '-', '-' };

  /**
   * Constructor.
   * @param t text value
   */
  public FComm(final byte[] t) {
    super(NodeType.COM);
    val = t;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param com DOM node
   */
  FComm(final Comment com) {
    this(token(com.getData()));
  }

  @Override
  public FNode copy() {
    return new FComm(val).parent(par);
  }

  @Override
  public String toString() {
    return Util.info("<!--%-->", val);
  }

  /**
   * Checks the specified token for validity.
   * @param str token to be checked
   * @param ii input info
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] str, final InputInfo ii) throws QueryException {
    if(contains(str, DASHES) || endsWith(str, '-')) COMINVALID.thrw(ii, str);
    return str;
  }
}
