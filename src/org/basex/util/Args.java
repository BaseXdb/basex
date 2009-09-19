package org.basex.util;

/**
 * This class parses command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Args {
  /** Command-line arguments. */
  private String args = "";
  /** Dash flag. */
  private boolean dash;
  /** Current argument. */
  private int c;

  /**
   * Default constructor.
   * @param a arguments
   */
  public Args(final String[] a) {
    for(final String s : a) args += s + ' ';
  }

  /**
   * Checks if more arguments are available.
   * @return result of check
   */
  public boolean more() {
    while(c < args.length()) {
      final char ch = args.charAt(c);
      if(ch == ' ') {
        if(dash) dash = false;
      } else if(!dash && ch == '-') {
        dash = true;
      } else {
        return true;
      }
      c++;
    }
    return false;
  }

  /**
   * Checks if the current argument starts with a dash.
   * @return result of check
   */
  public boolean dash() {
    return dash;
  }

  /**
   * Returns the next character.
   * @return next character
   */
  public char next() {
    return args.charAt(c++);
  }

  /**
   * Returns the next string.
   * @return string
   */
  public String string() {
    while(args.charAt(c) == ' ') if(++c == args.length()) return "";
    final int i = args.indexOf(' ', c);
    final String s = args.substring(c, i);
    c = i;
    return s.trim();
  }

  /**
   * Returns the next number.
   * @return number as int value
   */
  public int num() {
    return Integer.parseInt(string());
  }

  /**
   * Returns the remaining string.
   * @return remaining string
   */
  public String remaining() {
    while(args.charAt(c) == ' ') if(++c == args.length()) return "";
    final String s = args.substring(c);
    c = args.length();
    return s.trim();
  }
}
