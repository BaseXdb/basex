package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.io.in.BufferInput;
import org.basex.io.out.ArrayOutput;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * client/server architecture. All sent data is received by the
 * {@link ClientListener} and interpreted by the {@link QueryListener}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ClientQuery extends Query {
  /** Client session. */
  private final ClientSession cs;
  /** Query id. */
  private final String id;
  /** Next result. */
  private ArrayOutput next;

  /**
   * Standard constructor.
   * @param query query to be run
   * @param session client session
   * @throws IOException I/O exception
   */
  public ClientQuery(final String query, final ClientSession session)
      throws IOException {

    cs = session;
    id = string(ServerCmd.QUERY, query).toString();
  }

  @Override
  public String info() throws IOException {
    return string(ServerCmd.INFO, id).toString();
  }

  @Override
  public void bind(final String n, final Object v, final String t)
      throws IOException {
    execute(ServerCmd.BIND, id + '\0' + n + '\0' + v + '\0' +
        (t == null ? "" : t));
  }

  @Override
  public String init() throws IOException {
    return execute(ServerCmd.INIT, id);
  }

  @Override
  public String execute() throws IOException {
    return execute(ServerCmd.EXEC, id);
  }

  @Override
  public boolean more() throws IOException {
    next = string(ServerCmd.NEXT, id);
    return next.size() != 0;
  }

  @Override
  public String next() throws IOException {
    if(next == null) more();
    final ArrayOutput ao = next;
    next = null;
    if(cs.out == null) return ao.toString();
    cs.out.write(ao.toArray());
    return null;
  }

  @Override
  public String close() throws IOException {
    return execute(ServerCmd.CLOSE, id);
  }

  /**
   * Executes a command.
   * @param cmd server command
   * @param arg argument
   * @return string, or {@code null} if result was sent to output stream.
   * @throws IOException I/O exception
   */
  private String execute(final ServerCmd cmd, final String arg)
      throws IOException {

    if(cs.out == null) return string(cmd, arg).toString();
    exec(cmd, arg, cs.out);
    return null;
  }

  /**
   * Executes a command and returns the result as string.
   * @param cmd server command
   * @param arg argument
   * @return string, or {@code null} if result was sent to output stream.
   * @throws IOException I/O exception
   */
  private ArrayOutput string(final ServerCmd cmd, final String arg)
      throws IOException {

    final ArrayOutput ao = new ArrayOutput();
    exec(cmd, arg, ao);
    return ao;
  }

  /**
   * Executes a command and sends the result to the specified output stream.
   * @param cmd server command
   * @param arg argument
   * @param os target output stream
   * @throws IOException I/O exception
   */
  private void exec(final ServerCmd cmd, final String arg,
      final OutputStream os) throws IOException {

    cs.sout.write(cmd.code);
    cs.send(arg);
    final BufferInput bi = new BufferInput(cs.sin);
    for(byte l; (l = bi.readByte()) != 0;) os.write(l);
    if(!cs.ok(bi)) throw new BaseXException(bi.readString());
  }
}
