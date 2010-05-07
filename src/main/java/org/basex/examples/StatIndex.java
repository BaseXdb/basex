package org.basex.examples;

import static java.lang.System.*;
import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.io.CachedOutput;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Table;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class assembles statistics on the specified database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class StatIndex extends DefaultHandler {
  /** Context. */
  private final Context ctx = new Context();
  /** Result table. */
  private final Table table = new Table();
  /** Performance. */
  final Performance p = new Performance();
  /** Input document. */
  private static String db;
  
  /**
   * Main method of the example class.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new StatIndex(args);
  }
  
  /**
   * Constructor.
   * @param args command-line arguments
   */
  private StatIndex(final String[] args) {
    if(!parseArguments(args)) return;

    final TokenList tl = new TokenList();
    tl.add("Document");
    tl.add("$path");
    tl.add("$text");
    tl.add("$attr");
    tl.add("$fuzzy");
    tl.add("$trie");
    tl.add("ltext");
    tl.add("lattr");
    tl.add("lfuzzy");
    tl.add("ltrie");
    table.header = tl;

    if(db != null) {
      analyze(db);
    } else {
      for(final String d : List.list(ctx)) {
        System.out.println("- " + d);
        analyze(d);
      }
    }
    out.print(Token.string(table.finish()));
  }

  /**
   * Analyzes the specified file.
   * @param input input document
   */
  private void analyze(final String input) {
    try {
      exec(new Open(input));

      final TokenList tl = new TokenList();
      tl.add(input);

      index(CmdIndex.PATH, tl);
      final long ltxt = index(CmdIndex.TEXT, tl,
          DATATXT + 'l', DATATXT + 'r');
      final long latv = index(CmdIndex.ATTRIBUTE, tl,
          DATAATV + 'l', DATAATV + 'r');

      exec(new Set(Prop.WILDCARDS, false));
      final long lftt = index(CmdIndex.FULLTEXT, tl,
          DATAFTX + 'x', DATAFTX + 'y', DATAFTX + 'z');
      exec(new Set(Prop.WILDCARDS, true));
      final long lftf = index(CmdIndex.FULLTEXT, tl,
          DATAFTX + 'a', DATAFTX + 'b', DATAFTX + 'c');

      tl.add(ltxt);
      tl.add(latv);
      tl.add(lftf);
      tl.add(lftt);
      
      table.contents.add(tl);
    
    } catch(BaseXException ex) {
      err.println("- " + input + ": " + ex.getMessage());
    }
  }

  /**
   * Returns the length for the specified database file.
   * @param file database file
   * @return length
   */
  private long dbl(final String file) {
    return ctx.data.meta.file(file).length();
  }

  /**
   * Creates and drops the specified index.
   * @param index index
   * @param tl token list
   * @param dbf database files
   * @return length
   * @throws BaseXException exception
   */
  private long index(final CmdIndex index, final TokenList tl,
      final String... dbf) throws BaseXException {
    p.getTimer();
    exec(new CreateIndex(index));
    tl.add(p.toString().replace(" ms", ""));
    long l = 0;
    for(final String d : dbf) l += dbl(d);
    exec(new DropIndex(index));
    return l;
  }
  
  /**
   * Executes the specified command and returns the result as string.
   * @param cmd command
   * @return string result
   * @throws BaseXException exception
   */
  private String exec(final Proc cmd) throws BaseXException {
    final CachedOutput co = new CachedOutput();
    cmd.execute(ctx, co);
    return co.toString();
  }

  /**
   * Parses the command-line arguments.
   * @param args command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args);
    boolean ok = true;
    try {
      while(arg.more() && ok) {
        if(arg.dash()) {
          ok = false;
        } else {
          db = arg.string();
        }
      }
    } catch(final Exception ex) {
      ok = false;
    }
    
    if(!ok) {
      Main.outln("Usage: " + Main.name(this) + " [options] [db?]" + NL +
          "  db  database to be parsed");
    }
    return ok;
  }
}
