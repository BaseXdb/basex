package org.basex.query.value.node;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Comment node fragment.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FComm extends FNode {
  /** Two dashes, marking the start/end of a comment. */
  private static final byte[] DASHES = { '-', '-' };

  /**
   * Constructor.
   * @param value text value
   */
  public FComm(final String value) {
    this(token(value));
  }

  /**
   * Constructor.
   * @param value text value
   */
  public FComm(final byte[] value) {
    super(NodeType.COM);
    this.value = value;
  }

  /**
   * Constructor for creating a comment from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param comment DOM node
   */
  public FComm(final Comment comment) {
    this(comment.getData());
  }

  @Override
  public FComm materialize(final QueryContext qc, final boolean copy) {
    return copy ? new FComm(value) : this;
  }

  @Override
  public String toString() {
    return new TokenBuilder("<!--").add(value).add("-->").toString();
  }

  /**
   * Checks the specified token for validity.
   * @param str token to be checked
   * @param info input info
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] str, final InputInfo info) throws QueryException {
    if(contains(str, DASHES) || endsWith(str, '-')) throw COMINVALID.get(info);
    return str;
  }
}
