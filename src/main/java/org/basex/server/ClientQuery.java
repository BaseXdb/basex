package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.io.BufferInput;
import org.basex.util.TokenBuilder;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ClientQuery extends Query {
  /** Client session. */
  private final ClientSession cs;
  /** Query id. */
  private final String id;
  /** Next result. */
  private TokenBuilder next;

  /**
   * Standard constructor.
   * @param session client session
   * @param query query to be run
   * @throws BaseXException database exception
   */
  public ClientQuery(final ClientSession session, final String query)
      throws BaseXException {

    cs = session;
    try {
      cs.out.write(0);
      cs.send(query);
      final BufferInput bi = cs.bufIn();
      id = bi.readString();
      if(!cs.ok(bi)) throw new BaseXException(bi.readString());
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public boolean more() throws BaseXException {
    // send 1 to get next result item, and {ID}0 for identification
    try {
      cs.out.write(1);
      cs.send(id);
      final BufferInput bi = cs.bufIn();
      next = bi.content();
      if(!cs.ok(bi)) throw new BaseXException(bi.readString());
      return next.size() != 0;
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void next(final OutputStream out) throws BaseXException {
    try {
      out.write(next.finish());
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String next() {
    return next.toString();
  }

  @Override
  public void close() throws BaseXException {
    // send 2 to mark end of query execution, and {ID}0 for identification
    try {
      cs.out.write(2);
      cs.send(id);
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }
}
