package org.basex.server;

import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.io.BufferInput;
import org.basex.util.ByteList;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * client/server architecture. All sent data is received by the
 * {@link ServerProcess} and interpreted by the {@link QueryProcess}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ClientQuery extends Query {
  /** Client session. */
  protected final ClientSession cs;
  /** Query id. */
  protected final String id;
  /** Next result. */
  protected ByteList next;

  /**
   * Standard constructor.
   * @param query query to be run
   * @param session client session
   * @throws BaseXException database exception
   */
  public ClientQuery(final String query, final ClientSession session)
      throws BaseXException {

    cs = session;
    id = exec(ServerCmd.QUERY, query).toString();
  }

  @Override
  public void bind(final String n, final String v, final String t)
      throws BaseXException {
    exec(ServerCmd.BIND, id + '\0' + n + '\0' + v + '\0'
        + (t == null ? "" : t));
  }

  @Override
  public String init() throws BaseXException {
    return print(exec(ServerCmd.INIT, id));
  }

  @Override
  public boolean more() throws BaseXException {
    next = exec(ServerCmd.NEXT, id);
    return next.size() != 0;
  }

  @Override
  public String next() throws BaseXException {
    if(next == null) more();
    final ByteList bl = next;
    next = null;
    return print(bl);
  }

  @Override
  public String execute() throws BaseXException {
    return print(exec(ServerCmd.EXEC, id));
  }

  @Override
  public String info() throws BaseXException {
    return print(exec(ServerCmd.INFO, id));
  }

  @Override
  public String close() throws BaseXException {
    return print(exec(ServerCmd.CLOSE, id));
  }

  /**
   * Executes the specified command.
   * @param cmd server command
   * @param arg argument
   * @return result
   * @throws BaseXException command exception
   */
  ByteList exec(final ServerCmd cmd, final String arg) throws BaseXException {
    synchronized(cs.mutex) {
      try {
        cs.sout.write(cmd.code);
        cs.send(arg);
        cs.mutex.wait();
        BufferInput bi = cs.bi;
        final ByteList bl = new ByteList();
        if(cs.first != 0) {
         bl.add(cs.first).add(bi.content().toArray());
        }
        cs.mutex.notifyAll();
        if(!cs.ok(bi)) throw new BaseXException(bi.readString());
        return bl;
      } catch(final Exception ex) {
        throw new BaseXException(ex);
      }
    }
  }

  /**
   * Returns the specified result.
   * @param bl result
   * @return string, or {@code null} if result was sent to output stream.
   * @throws BaseXException command exception
   */
  String print(final ByteList bl) throws BaseXException {
    if(cs.out == null) return bl.toString();
    try {
      if(bl.size() != 0) cs.out.write(bl.toArray());
      return null;
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }
}
