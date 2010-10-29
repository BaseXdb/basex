package org.basex.server;

/**
 * This class defines the available command-line commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum ServerCmd {
  /** Code for running a database command. */
  CMD(10),
  /** Code for creating a query process. */
  QUERY(0),
  /** Code for binding an external query variable. */
  BIND(3),
  /** Code for initializing the result iteration. */
  INIT(4),
  /** Code for returning next query result. */
  NEXT(1),
  /** Code for closing the iterator. */
  CLOSE(2),
  /** Code for creating a database. */
  CREATE(8);

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
