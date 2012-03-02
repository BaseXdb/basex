package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.io.in.BufferInput;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * client/server architecture. All sent data is received by the
 * {@link ClientListener} and interpreted by the {@link QueryListener}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ClientQuery extends Query {
  /** Client session. */
  protected final ClientSession cs;
  /** Query id. */
  protected final String id;

  /**
   * Standard constructor.
   * @param query query to be run
   * @param session client session
   * @param os output stream
   * @throws IOException I/O exception
   */
  public ClientQuery(final String query, final ClientSession session,
      final OutputStream os) throws IOException {

    cs = session;
    id = cs.exec(ServerCmd.QUERY, query, null);
    out = os;
  }

  @Override
  public String info() throws IOException {
    return cs.exec(ServerCmd.INFO, id, null);
  }

  @Override
  public String options() throws IOException {
    return cs.exec(ServerCmd.OPTIONS, id, null);
  }

  @Override
  public void bind(final String n, final Object v, final String t) throws IOException {
    cs.exec(ServerCmd.BIND, id + '\0' + n + '\0' + v + '\0' + (t == null ? "" : t), null);
  }

  @Override
  public String execute() throws IOException {
    return cs.exec(ServerCmd.EXEC, id, out);
  }

  @Override
  public void close() throws IOException {
    cs.exec(ServerCmd.CLOSE, id, null);
  }

  @Override
  protected void cache() throws IOException {
    cs.sout.write(ServerCmd.ITER.code);
    cs.send(id);
    cs.sout.flush();
    final BufferInput bi = new BufferInput(cs.sin);
    cache(bi);
    if(!ClientSession.ok(bi)) throw new BaseXException(bi.readString());
  }
}
