package org.basex.core;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.io.NullOutput;

/**
 * This class allows a generic command execution.
 * It is implemented both by the local as well as the client/server model.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Session {
  /**
   * Executes a command and prints the result to the specified stream.
   * @param cmd command to be executed
   * @param out output stream
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final String cmd, final OutputStream out)
    throws IOException;

  /**
   * Executes a command and prints the result to the specified stream.
   * @param cmd command to be executed
   * @param out output stream
   * @throws IOException I/O exception
   * @return success of operation
   */
  public abstract boolean execute(final Command cmd, final OutputStream out)
    throws IOException;

  /**
   * Executes a command. {@link #execute(Command, OutputStream)} should be
   * called if textual results are expected.
   * @param cmd command to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public final boolean execute(final Command cmd) throws IOException {
    return execute(cmd, new NullOutput());
  }

  /**
   * Executes a command. {@link #execute(String, OutputStream)} should be
   * called if textual results are expected.
   * @param cmd command to be executed
   * @throws IOException I/O exception
   * @return success of operation
   */
  public final boolean execute(final String cmd) throws IOException {
    return execute(cmd, new NullOutput());
  }

  /**
   * Returns command info.
   * @return command info
   */
  public abstract String info();

  /**
   * Closes the session.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;
}
