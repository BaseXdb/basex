package org.basex.core;

import org.basex.core.proc.Proc;
import org.basex.util.StringList;

/**
 * This class stores information on a single command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Command {
  /** Commands flag: standard. */
  public static final int STANDARD = 0;
  /** Commands flag: printing command. */
  public static final int PRINTING = 1;
  /** Commands flag: updating command. */
  public static final int UPDATING = 2;
  /** Commands flag: data reference needed. */
  public static final int DATAREF = 4;
  /** Commands flag: local command. */
  public static final int LOCAL = 8;
  /** Commands flag: server command. */
  public static final int SERVER = 16;
  
  /** Command name. */
  public final Commands name;
  /** Arguments. */
  private String[] arg;
  /** Complete command. */
  private String args;

  /**
   * Constructor.
   * @param input command input
   */
  public Command(final String input) {
    final int cl = input.length();
    int c = -1;
    while(++c < cl && input.charAt(c) == ' ');
    final int c1 = c;
    while(++c < cl && input.charAt(c) != ' ');

    name = Commands.find(input.substring(c1, c));
    arg(input.substring(c));
  }

  /**
   * Constructor.
   * @param comm command to be set
   * @param a arguments
   */
  public Command(final Commands comm, final String a) {
    name = comm;
    arg(a);
  }

  /**
   * Parse arguments.
   * @param a arguments to be parsed
   */
  public void arg(final String a) {
    args = a == null ? "" : a;
    final StringList list = new StringList();
    final StringBuilder token = new StringBuilder();
    final int cl = args.length();
    int c = -1;
    char qu = 0;
    boolean q = false;
    while(++c < cl) {
      char ch = args.charAt(c);
      if(qu != 0 && ch == '\\') {
        if(++c < cl) ch = args.charAt(c);
      } else {
        if(ch == ' ' && qu == 0) {
          add(token, list, q);
          q = false;
          continue;
        }
        if(ch == '\'' || ch == '"') {
          if(qu == 0) qu = ch;
          else if(qu == ch) qu = 0;
          q = true;
        }
      }
      token.append(ch);
    }
    add(token, list, q);
    arg = list.finish();
  }

  /**
   * Adds an argument to the specified list.
   * @param token argument
   * @param list string list
   * @param q quote flag
   */
  private void add(final StringBuilder token, final StringList list,
      final boolean q) {
    String ar = token.toString().trim();
    // remove quotes
    int l = ar.length();
    int c = l == 0 ? 0 : ar.charAt(0);
    if(c == '"' || c == '\'') ar = ar.substring(1);
    l = ar.length();
    c = l == 0 ? 0 : ar.charAt(l - 1);
    if(c == '"' || c == '\'') ar = ar.substring(0, --l);
    if(l != 0 || q) list.add(ar);
    token.setLength(0);
  }

  /**
   * Returns a process instance of the command.
   * @param ctx context
   * @return process
   */
  public Proc proc(final Context ctx) {
    return name.newInstance(this, ctx);
  }

  /**
   * Returns complete argument string.
   * @return arguments
   */
  public String args() {
    return args;
  }

  /**
   * Returns the specified argument.
   * @param i argument index
   * @return argument
   */
  public String arg(final int i) {
    return i >= arg.length ? "" : arg[i];
  }

  /**
   * Returns the number of arguments.
   * @return number of arguments
   */
  public int nrArgs() {
    return arg.length;
  }

  @Override
  public String toString() {
    return name + " " + args;
  }
}
