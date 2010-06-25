package org.basex.core;

import org.basex.util.TokenBuilder;

/**
 * This class simplifies the composition of the string representation of
 * a database command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CommandBuilder {
  /** String representation of the database command. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Process cache. */
  private final Proc p;

  /**
   * Constructor.
   * @param cmd command
   */
  public CommandBuilder(final Proc cmd) {
    p = cmd;
  }
  
  /**
   * Initializes the builder with the class name of the command in upper case. 
   * @return self instance
   */
  public CommandBuilder init() {
    init(Main.name(p).toUpperCase());
    return this;
  }
  
  /**
   * Initializes the builder with the specified string.
   * @param cmd command string
   * @return self instance
   */
  public CommandBuilder init(final String cmd) {
    tb.reset();
    tb.add(cmd);
    return this;
  }

  /**
   * Returns a string representation of all arguments.
   * @return self instance
   */
  public CommandBuilder args() {
    for(int a = 0; a < p.args.length; a++) arg(a);
    return this;
  }

  /**
   * Adds the specified argument as XQuery.
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder xquery(final int arg) {
    tb.add(' ');
    tb.add(p.args[arg]);
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
   * Adds the specified keyword and argument.
   * Does nothing if the argument is {@code null} or empty. 
   * @param key keyword prefix
   * @param arg argument index
   * @return self instance
   */
  public CommandBuilder arg(final String key, final int arg) {
    final String a = p.args[arg];
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
