package org.basex.core;

import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This class simplifies the composition of the string representation of
 * a database command.
 *
 * @author BaseX Team 2005-11, BSD License
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
    init(Util.name(cmd).toUpperCase());
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
    for(int a = 0; a < cmd.args.length; ++a) arg(a);
    return this;
  }

  /**
   * Adds the specified argument as XQuery string.
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder xquery(final int arg) {
    tb.add(' ');
    tb.add(cmd.args[arg]);
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
      if(key != null) {
        tb.add(' ');
        tb.add(key);
      }
      tb.add(' ');
      final boolean s = a.indexOf(' ') != -1;
      if(s) tb.add('"');
      tb.add(a);
      if(s) tb.add('"');
    }
    return this;
  }

  @Override
  public String toString() {
    return tb.toString();
  }
}
