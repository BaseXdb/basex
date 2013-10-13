package org.basex.util;

import java.util.*;

import org.basex.core.*;

/**
 * This class parses command-line arguments.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Args {
  /** Calling object. */
  private final Object obj;
  /** Program header. */
  private final String header;
  /** Usage info. */
  private final String usage;
  /** Command-line arguments. */
  private final String[] args;

  /** Dash flag. */
  private boolean dash;
  /** Position in current argument. */
  private int pos;
  /** Current argument. */
  private int arg;

  /**
   * Default constructor.
   * @param a arguments
   * @param o calling object
   * @param u usage info
   * @param h header
   */
  public Args(final String[] a, final Object o, final String u, final String h) {
    args = a;
    usage = u;
    obj = o;
    header = h;
  }

  /**
   * Checks if more arguments are available.
   * @return result of check
   */
  public boolean more() {
    // parse all arguments
    while(arg < args.length) {
      // analyze current argument
      final String a = args[arg];
      if(pos == 0) {
        // start from first character
        dash = false;
        // find first relevant character
        while(pos < a.length()) {
          final char ch = a.charAt(pos);
          if(ch == '-') {
            // treat input as flag
            dash = true;
          } else if(ch != ' ') {
            // ignore spaces
            return true;
          }
          pos++;
        }
        // treat input as string
        pos = 0;
        dash = false;
        return true;
      } else if(pos < a.length()) {
        // check next character
        return true;
      }
      arg++;
      pos = 0;
    }
    // all arguments have been parsed
    return false;
  }

  /**
   * Checks if the current argument starts with a dash
   * (i.e., introduces any flags).
   * @return result of check
   */
  public boolean dash() {
    return dash;
  }

  /**
   * Returns the next flag.
   * @return next flag
   */
  public char next() {
    return arg < args.length && pos < args[arg].length() ?
        args[arg].charAt(pos++) : 0;
  }

  /**
   * Returns the next string argument.
   * @return string
   */
  public String string() {
    while(arg < args.length) {
      final String a = args[arg++];
      int p = pos;
      pos = 0;
      if(p == a.length()) continue;
      final StringBuilder sb = new StringBuilder();
      while(p < a.length()) sb.append(a.charAt(p++));
      final String str = sb.toString();
      return str.equals("-") ? new Scanner(System.in).useDelimiter("\0").next() : str;
    }
    return "";
  }

  /**
   * Returns the next positive numeric argument.
   * @return positive integer
   * @throws BaseXException database exception
   */
  public int number() throws BaseXException {
    final int i = Token.toInt(string());
    if(i < 0) usage();
    return i;
  }

  /**
   * Throws an exception with the command usage info.
   * @throws BaseXException database exception
   */
  public void usage() throws BaseXException {
    throw new BaseXException(header +
        "Usage: " + Util.className(obj).toLowerCase(Locale.ENGLISH) + usage);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final String s : args) sb.append(s).append(' ');
    return sb.toString();
  }
}
