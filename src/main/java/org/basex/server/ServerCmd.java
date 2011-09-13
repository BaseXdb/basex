package org.basex.server;

/**
 * This class defines the available command-line commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum ServerCmd {
  /** Code for creating a query process: {query}0. */
  QUERY(0),
  /** Code for binding an external query variable: {id}0{name}0{val}0{type}0. */
  BIND(3),
  /** Code for initializing the result iteration: {id}0. */
  INIT(4),
  /** Code for returning next query result: {id}0. */
  NEXT(1),
  /** Code for executing the complete query: {id}0. */
  EXEC(5),
  /** Code for showing the query info: {id}0. */
  INFO(6),
  /** Code for showing the serializations options: {id}0. */
  OPTIONS(7),
  /** Code for closing the iterator: {id}0. */
  CLOSE(2),
  /** Code for creating a database: {name}0{input}0. */
  CREATE(8),
  /** Code for adding a document to a database: {name}0{path}0{input}0. */
  ADD(9),
  /** Code for watching an event: {name}0. */
  WATCH(10),
  /** Code for unwatching an event: {name}0. */
  UNWATCH(11),
  /** Code for replacing a document in a database: {path}0{input}0. */
  REPLACE(12),
  /** Code for storing raw data in a database: {path}0{input}0. */
  STORE(13),
  /** Code for retrieving raw data from a database: {path}0. */
  RETRIEVE(14),
  /** Code for running a database command: {command}0. */
  COMMAND(-1);

  /** Control code (soon obsolete). */
  public int code;

  /**
   * Constructor.
   * @param c control code
   */
  ServerCmd(final int c) {
    code = c;
  }

  /**
   * Returns the server command for the specified control byte
   * (soon obsolete).
   * @param b control byte
   * @return server command
   */
  static ServerCmd get(final byte b) {
    for(final ServerCmd s : values()) if(s.code == b) return s;
    // current default for unknown codes: database command.
    return COMMAND;
  }
}
