package org.basex.core;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CommandParser {
  /** Command reference. */
  private Command cmd;
  /** Command. */
  private final String input;
  /** String length. */
  private final int cl;
  /** Counter. */
  private int c = -1;
  /** Iterator flag. */
  private boolean next;

  /**
   * Constructor, parsing the input.
   * @param in input
   */
  public CommandParser(final String in) {
    input = in;
    cl = input.length();
    more();
  }

  /**
   * Checks if more tokens are found.
   * @return result of check
   */
  public boolean more() {
    if(next || c >= cl) return next;

    while(++c < cl && (input.charAt(c) <= ' ' || input.charAt(c) == ';'));
    final int c1 = c;
    if(c == cl) return false;
    while(++c < cl && input.charAt(c) > ' ' && input.charAt(c) != ';');
    final int c2 = c--;

    final Commands com = Commands.find(input.substring(c1, c2));
    final String arg;

    // disallow several commands per line for XQueries
    if(com != Commands.XQENV && com != Commands.XQUERY && com != Commands.PF) {
      char qu = 0;
      while(++c < cl) {
        final char ch = input.charAt(c);
        if(qu != 0 && ch == '\\') {
          if(c + 1 < cl) ++c;
        } else {
          if(ch == ';' && qu == 0) break;
          if(ch == '\'' || ch == '"') {
            if(qu == 0) qu = ch;
            else if(qu == ch) qu = 0;
          }
        }
      }
      arg = input.substring(c2, c).trim();
    } else {
      arg = input.substring(Math.min(cl, c2 + 1));
      c = cl;
    }

    cmd = new Command(com, arg);
    next = true;
    return true;
  }

  /**
   * Returns the current command.
   * @return command
   */
  public Command next() {
    next = false;
    return cmd;
  }
}
