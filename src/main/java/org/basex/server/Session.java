package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.io.ArrayOutput;

/**
 * <p>This class defines methods for evaluating commands, either locally or
 * via the client/server architecture.</p>
 *
 * <p>The results of database commands are returned as strings. If an output
 * stream is specified in the constructor or with
 * {@link #setOutputStream(OutputStream)}, results are instead serialized
 * to that stream.
 * The class is implemented by the {@link ClientSession} and
 * {@link LocalSession} classes.</p>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Session {
  /** Command info. */
  protected String info;
  /** Client output. */
  protected OutputStream out;

  /**
   * Executes a {@link Command} and returns the result as string or serializes
   * it to the specified output stream.
   * @param command command to be executed
   * @return result
   * @throws BaseXException database exception
   */
  public final String execute(final Command command) throws BaseXException {
    final ArrayOutput ao = out == null ? new ArrayOutput() : null;
    execute(command, ao != null ? ao : out);
    return ao != null ? ao.toString() : null;
  }

  /**
   * Executes a command and returns the result as string or serializes
   * it to the specified output stream.
   * @param command command to be parsed
   * @return result
   * @throws BaseXException database exception
   */
  public final String execute(final String command) throws BaseXException {
    final ArrayOutput ao = out == null ? new ArrayOutput() : null;
    execute(command, ao != null ? ao : out);
    return ao != null ? ao.toString() : null;
  }

  /**
   * Returns a query object for the specified query string.
   * @param query query string
   * @return query
   * @throws BaseXException database exception
   */
  public abstract Query query(final String query) throws BaseXException;

  /**
   * Creates a database.
   * @param name name of database
   * @param input xml input
   * @throws BaseXException database exception
   */
  public abstract void create(final String name, final InputStream input)
    throws BaseXException;

  /**
   * Adds a document to the opened database.
   * @param name name of document
   * @param target target path
   * @param input xml input
   * @throws BaseXException database exception
   */
  public abstract void add(final String name, final String target,
      final InputStream input) throws BaseXException;

  /**
   * Returns command info as a string, regardless of whether an output stream
   * was specified.
   * @return command info
   */
  public final String info() {
    return info;
  }

  /**
   * Specifies an output stream. The output stream can be invalidated by
   * passing on {@code null} as argument.
   * @param output client output stream.
   */
  public final void setOutputStream(final OutputStream output) {
    out = output;
  }

  /**
   * Closes the session.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  // PROTECTED METHODS ========================================================

  /**
   * Constructor.
   * @param output client output stream; if set to {@code null}, all
   * results will be returned as strings.
   */
  protected Session(final OutputStream output) {
    out = output;
  }

  /**
   * Executes a command and prints the result to the specified output stream.
   * @param cmd command to be parsed
   * @param os output stream
   * @throws BaseXException database exception
   */
  protected abstract void execute(final String cmd, final OutputStream os)
    throws BaseXException;

  /**
   * Executes a command and prints the result to the specified output stream.
   * @param cmd command to be executed
   * @param os output stream
   * @throws BaseXException database exception
   */
  protected abstract void execute(final Command cmd, final OutputStream os)
    throws BaseXException;
}
