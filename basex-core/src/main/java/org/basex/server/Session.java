package org.basex.server;

import java.io.*;

import org.basex.core.*;
import org.basex.io.out.*;

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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Session {
  /** Client output stream. */
  OutputStream out;
  /** Command info. */
  String info;

  /**
   * Executes a {@link Command} and returns the result as string or serializes
   * it to the specified output stream.
   * @param command command to be executed
   * @return result, or {@code null} reference
   * @throws IOException I/O exception
   */
  public final String execute(final Command command) throws IOException {
    final ArrayOutput ao = out == null ? new ArrayOutput() : null;
    execute(command, ao != null ? ao : out);
    return ao != null ? ao.toString() : null;
  }

  /**
   * Executes a command and returns the result as string or serializes
   * it to the specified output stream.
   * @param command command to be parsed
   * @return result, or {@code null} reference
   * @throws IOException I/O exception
   */
  public final String execute(final String command) throws IOException {
    final ArrayOutput ao = out == null ? new ArrayOutput() : null;
    execute(command, ao != null ? ao : out);
    return ao != null ? ao.toString() : null;
  }

  /**
   * Returns a query object for the specified query string.
   * @param query query string
   * @return query
   * @throws IOException I/O exception
   */
  public abstract Query query(final String query) throws IOException;

  /**
   * Creates a database.
   * @param name name of database
   * @param input xml input
   * @throws IOException I/O exception
   */
  public abstract void create(final String name, final InputStream input) throws IOException;

  /**
   * Adds a document to the opened database.
   * @param path target path
   * @param input xml input
   * @throws IOException I/O exception
   */
  public abstract void add(final String path, final InputStream input) throws IOException;

  /**
   * Replaces a document in an open database.
   * @param path document(s) to replace
   * @param input new content
   * @throws IOException I/O exception
   */
  public abstract void replace(final String path, final InputStream input) throws IOException;

  /**
   * Stores raw data in an open database.
   * @param path target path
   * @param input binary input
   * @throws IOException I/O exception
   */
  public abstract void store(final String path, final InputStream input) throws IOException;

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
   * @param output client output stream
   */
  public final void setOutputStream(final OutputStream output) {
    out = output;
  }

  /**
   * Returns the assigned output stream.
   * @return client output stream
   */
  public OutputStream getOutputStream() {
    return out;
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
   * results will be returned as strings
   */
  Session(final OutputStream output) {
    out = output;
  }

  /**
   * Executes a command and prints the result to the specified output stream.
   * @param cmd command to be parsed
   * @param os output stream
   * @throws IOException I/O exception
   */
  protected abstract void execute(final String cmd, final OutputStream os) throws IOException;

  /**
   * Executes a command and prints the result to the specified output stream.
   * @param cmd command to be executed
   * @param os output stream
   * @throws IOException I/O exception
   */
  protected abstract void execute(final Command cmd, final OutputStream os) throws IOException;
}
