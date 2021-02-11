package org.basex.server;

/**
 * This class defines the available command-line commands.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum ServerCmd {
  /** Code for creating a query process: {query}0. */
  QUERY(0),
  /** Code for iterating results (obsolete). */
  NEXT(1),
  /** Code for closing the query: {id}0. */
  CLOSE(2),
  /** Code for binding an external query variable: {id}0{name}0{val}0{type}0. */
  BIND(3),
  /** Code for executing the query in an iterative manner: {id}0. */
  RESULTS(4),
  /** Code for executing the query: {id}0. */
  EXEC(5),
  /** Code for showing the query info: {id}0. */
  INFO(6),
  /** Code for showing the serializations options: {id}0. */
  OPTIONS(7),
  /** Code for creating a database: {name}0{input}0. */
  CREATE(8),
  /** Code for adding a document to a database: {path}0{input}0. */
  ADD(9),
  /** Code for replacing a document in a database: {path}0{input}0. */
  REPLACE(12),
  /** Code for storing raw data in a database: {path}0{input}0. */
  STORE(13),
  /** Code for binding a context value: {id}0{val}0{type}0. */
  CONTEXT(14),
  /** Code for returning the update flag: {id}0. */
  UPDATING(30),
  /** Code for executing a query and returning all information relevant for XQJ: {id}0. */
  FULL(31),
  /** Code for running a database command: {path}0{input}0. */
  COMMAND(-1);

  /** Control code (soon obsolete). */
  public final int code;

  /**
   * Constructor.
   * @param code control code
   */
  ServerCmd(final int code) {
    this.code = code;
  }

  /**
   * Returns the server command for the specified control byte.
   * @param code control code
   * @return server command
   */
  static ServerCmd get(final int code) {
    for(final ServerCmd value : values()) {
      if(value.code == code) return value;
    }
    // current default for unknown codes: database command.
    return COMMAND;
  }
}
