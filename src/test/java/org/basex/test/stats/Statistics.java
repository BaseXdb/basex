package org.basex.test.stats;

import static org.basex.core.Text.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.TokenList;

/**
 * This class assembles statistics on the specified database.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public abstract class Statistics {
  /** Context. */
  final Context ctx = new Context();
  /** Performance. */
  final Performance p = new Performance();

  /** Result table. */
  private final Table table = new Table();
  /** Input document. */
  private String db;
  /** Tabular output. */
  private boolean tab;
  /** Debug output. */
  private boolean debug;

  /**
   * Initializes the arguments.
   * @param args command-line arguments
   * @return success flag
   */
  final boolean init(final String[] args) {
    return parseArguments(args);
  }

  /**
   * Runs the analysis.
   * @param header columns
   */
  final void run(final String... header) {
    table.header.add("Document");
    for(final String h : header) table.header.add(h);

    if(db == null) {
      for(final String d : List.list(ctx)) analyze(d);
    } else {
      analyze(db);
    }

    if(tab) {
      Util.out(table.toString());
    } else {
      Util.out(Token.string(table.finish()));
    }
  }

  /**
   * Analyzes the specified database.
   * @param input input
   */
  private void analyze(final String input) {
    try {
      exec(new Open(input));
      final TokenList tl = new TokenList();
      // relative document path
      tl.add(input);
      analyze(tl);
      table.contents.add(tl);
    } catch(final BaseXException ex) {
      Util.errln("- " + input + ": " + ex.getMessage());
    }
  }

  /**
   * Analyzes the current database.
   * @param tl token list
   * @throws BaseXException exception
   */
  abstract void analyze(final TokenList tl) throws BaseXException;

  /**
   * Executes the specified command and returns the result as string.
   * @param cmd command
   * @return string result
   * @throws BaseXException exception
   */
  final String exec(final Command cmd) throws BaseXException {
    if(debug) Util.errln("- " + cmd);
    return cmd.execute(ctx);
  }

  /**
   * Parses the command-line arguments.
   * @param args command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  final boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this,
        " [-dt] [database?]" + NL +
        "  -d        debug flag" + NL +
        "  -o<k=v>   set database property" + NL +
        "  -t        tabular output" + NL +
        "  database  database to be parsed");

    while(arg.more()) {
      if(arg.dash()) {
        final char ch = arg.next();
        if(ch == 'd') {
          debug = true;
        } else if(ch == 'o') {
          final String[] kp = arg.string().split("=", 2);
          arg.check(new Set(kp[0], kp[1]).run(ctx));
        } else if(ch == 't') {
          tab = true;
        } else {
          arg.check(false);
        }
      } else {
        db = arg.string();
      }
    }
    return arg.finish();
  }

  /**
   * Formats a file size according to the binary size orders (KB, MB, ...),
   * adding the specified offset to the orders of magnitude.
   * @param size file size
   * @return formatted size value
   */
  static String format(final long size) {
    if(size < 10000L) return size + " B";
    if(size < 10000000L) return (size + (1L <<  9) >> 10) + " KB";
    if(size < 10000000000L) return (size + (1L <<  19) >> 20) + " MB";
    return (size + (1L << 29) >> 30) + " GB";
  }
}
