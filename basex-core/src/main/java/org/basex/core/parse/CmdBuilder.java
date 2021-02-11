package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class simplifies the composition of the string representation of
 * a database command.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmdBuilder {
  /** String representation of the database command. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Command to be output. */
  private final Command cmd;
  /** Confidential flag. */
  private final boolean conf;

  /**
   * Constructor.
   * @param cmd command
   * @param conf confidential flag
   */
  public CmdBuilder(final Command cmd, final boolean conf) {
    this.cmd = cmd;
    this.conf = conf;
  }

  /**
   * Initializes the builder with the class name of the command in upper case.
   * @return self instance
   */
  public CmdBuilder init() {
    init(Util.className(cmd).toUpperCase(Locale.ENGLISH));
    return this;
  }

  /**
   * Initializes the builder with the specified string.
   * @param string command string
   * @return self instance
   */
  public CmdBuilder init(final String string) {
    tb.reset().add(string);
    return this;
  }

  /**
   * Returns a string representation of all arguments.
   * @return self instance
   */
  public CmdBuilder args() {
    final int as = cmd.args.length;
    for(int a = 0; a < as; ++a) arg(a);
    return this;
  }

  /**
   * Adds the specified argument unchanged.
   * @param arg argument index
   * @return self instance
   */
  public CmdBuilder add(final int arg) {
    final String s = cmd.args[arg];
    if(s != null) tb.add(' ').add(s);
    return this;
  }

  /**
   * Returns the confidential flag.
   * @return flag
   */
  public boolean conf() {
    return conf;
  }

  /**
   * Adds the specified argument.
   * @param arg argument index
   * @return self instance
   */
  public CmdBuilder arg(final int arg) {
    arg(null, arg);
    return this;
  }

  /**
   * Adds an argument with an optional prefix.
   * @param key optional keyword prefix
   * @param arg argument index
   * @return self instance
   */
  public CmdBuilder arg(final String key, final int arg) {
    return arg(key, cmd.args.length > arg ? cmd.args[arg] : null);
  }

  /**
   * Adds an argument with an optional prefix.
   * @param key optional keyword prefix
   * @param arg argument string
   * @return self instance
   */
  public CmdBuilder arg(final String key, final String arg) {
    if(arg != null && !arg.isEmpty()) {
      if(key != null) tb.add(' ').add(key);
      tb.add(' ');
      if(arg.indexOf(' ') != -1 || arg.indexOf(';') != -1) {
        tb.add('"').add(arg.replaceAll("\"", "\\\"")).add('"');
      } else {
        tb.add(arg);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return tb.toString();
  }
}
