package org.basex.util;

import java.util.*;

import org.basex.core.*;

/**
 * This class parses command-line arguments provided by a class with main method.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MainParser {
  /** User interface. */
  private final Main main;

  /** Dash flag. */
  private boolean dash;
  /** Position in current argument. */
  private int pos;
  /** Current argument. */
  private int arg;

  /**
   * Default constructor.
   * @param main calling object
   */
  public MainParser(final Main main) {
    this.main = main;
  }

  /**
   * Checks if more arguments are available.
   * @return result of check
   */
  public boolean more() {
    // parse all arguments
    final String[] args = main.args();
    final int arl = args.length;

    while(arg < arl) {
      // analyze current argument
      final String a = args[arg];
      if(pos == 0) {
        // start from first character
        dash = false;
        // find first relevant character
        final int al = a.length();
        while(pos < al) {
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
      }
      if(pos < a.length()) {
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
    final String[] args = main.args();
    return arg < args.length && pos < args[arg].length() ? args[arg].charAt(pos++) : 0;
  }

  /**
   * Returns the next string argument.
   * @return string
   */
  public String string() {
    final String[] args = main.args();
    final int arl = args.length;
    while(arg < arl) {
      final String a = args[arg++];
      int p = pos;
      pos = 0;
      final int al = a.length();
      if(p == al) continue;
      final StringBuilder sb = new StringBuilder();
      while(p < al) sb.append(a.charAt(p++));
      final String str = sb.toString();
      return "-".equals(str) ? new Scanner(System.in).useDelimiter("\0").next() : str;
    }
    return "";
  }

  /**
   * Returns the next positive numeric argument.
   * @return positive integer
   * @throws BaseXException database exception
   */
  public int number() throws BaseXException {
    final int i = Strings.toInt(string());
    if(i < 0) throw usage();
    return i;
  }

  /**
   * Returns an exception with the command usage info.
   * @return database exception
   */
  public BaseXException usage() {
    final String name = Util.className(main).toLowerCase(Locale.ENGLISH);
    return new BaseXException(main.header() + "Usage: " + name + main.usage());
  }
}
