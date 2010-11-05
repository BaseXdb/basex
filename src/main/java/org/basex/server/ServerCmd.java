package org.basex.server;

/**
 * This class defines the available command-line commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  /** Code for closing the iterator: {id}0. */
  CLOSE(2),
  /** Code for creating a database: {name}0{input}0. */
  CREATE(8),
  /** Code for adding a document to a database: {name}0{path}0{input}0. */
  ADD(9),
  /** Code for running a database command: {command} \0. */
  CMD(-1);

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
    return CMD;
  }

  /**
   * Returns the server command for the specified control byte.
   * @param code control code
   * @return server command
   */
  static ServerCmd get(final String code) {
    try {
      return valueOf(code);
    } catch(final Exception ex) {
      // current default for unknown codes: database command.
      return CMD;
    }
  }
}
