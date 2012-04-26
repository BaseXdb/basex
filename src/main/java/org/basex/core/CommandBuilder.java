package org.basex.core;

import java.util.*;

import org.basex.util.*;

/**
 * This class simplifies the composition of the string representation of
 * a database command.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CommandBuilder {
  /** String representation of the database command. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Command to be output. */
  private final Command cmd;

  /**
   * Constructor.
   * @param c command
   */
  public CommandBuilder(final Command c) {
    cmd = c;
  }

  /**
   * Initializes the builder with the class name of the command in upper case.
   * @return self instance
   */
  public CommandBuilder init() {
    init(Util.name(cmd).toUpperCase(Locale.ENGLISH));
    return this;
  }

  /**
   * Initializes the builder with the specified string.
   * @param s command string
   * @return self instance
   */
  public CommandBuilder init(final String s) {
    tb.reset();
    tb.add(s);
    return this;
  }

  /**
   * Returns a string representation of all arguments.
   * @return self instance
   */
  public CommandBuilder args() {
    final int as = cmd.args.length;
    for(int a = 0; a < as; ++a) arg(a);
    return this;
  }

  /**
   * Adds the specified argument as XQuery string.
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder xquery(final int arg) {
    tb.add(' ').add(cmd.args[arg]);
    return this;
  }

  /**
   * Adds the specified argument.
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder arg(final int arg) {
    arg(null, arg);
    return this;
  }

  /**
   * Adds an argument with an optional prefix.
   * @param key optional keyword prefix
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder arg(final String key, final int arg) {
    final String a = cmd.args.length > arg ? cmd.args[arg] : null;
    if(a != null && !a.isEmpty()) {
      if(key != null) tb.add(' ').add(key);
      tb.add(' ');
      if(a.indexOf(' ') != -1 || a.indexOf(';') != -1) {
        tb.add('"').add(a.replaceAll("\"", "\\\"")).add('"');
      } else {
        tb.add(a);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return tb.toString();
  }
}
