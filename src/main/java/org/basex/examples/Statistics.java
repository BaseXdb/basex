package org.basex.examples;

import static java.lang.System.*;
import static org.basex.core.Text.*;
import java.io.File;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.io.CachedOutput;
import org.basex.util.Args;
import org.basex.util.StringList;
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
public final class Statistics extends DefaultHandler {
  /** Absolute directory path. */
  private final String abs =
    new File("").getAbsolutePath().replaceAll("\\\\", "/") + "/";
  /** Directories to be excluded from parsing. */
  private final StringList excl = new StringList();
  /** Context. */
  private final Context ctx = new Context();
  /** Result table. */
  private final Table table = new Table();
  /** Input document. */
  private static String doc;
  /** Maximum length. */
  private long max = Long.MAX_VALUE;
  /** Minimum length. */
  private long min;
  
  /**
   * Main method of the example class.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new Statistics(args);
  }
  
  /**
   * Constructor.
   * @param args command-line arguments
   */
  private Statistics(final String[] args) {
    if(!parseArguments(args)) return;
    
    final TokenList list = new TokenList();
    list.add("Document");
    list.add("Length");
    list.add("Pre");
    list.add("Attsize");
    list.add("Content(t)");
    list.add("Content(a)");
    list.add("Name(e)");
    list.add("Name(a)");
    list.add("Namespaces");
    table.header = list;
    
    if(doc == null) {
      parse(new File("."));
    } else {
      analyze(doc);
    }
    System.out.print(Token.string(table.finish()));
  }

  
  /**
   * Analyzes the specified directory.
   * @param dir directory
   */
  private void parse(final File dir) {
    final File[] files = dir.listFiles();
    if(files == null) return;

    for(final File f : files) {
      if(f.isDirectory()) {
        if(!excl.contains(f.getName())) parse(f);
      } else if(f.length() >= min && f.length() <= max) {
        String input = f.getPath();
        String suf = input.replaceAll(".*\\.", "").toLowerCase();
        if(suf.equals("xml") || suf.equals("zip") || suf.equals("gz")) {
          analyze(f.getPath());
        }
      }
    }
  }
  
  /**
   * Analyzes the specified file.
   * @param input input document
   */
  private void analyze(final String input) {
    try {
      System.out.println("- " + input);
      exec(new CreateDB(input));

      final Data data = ctx.data;
      final TokenList tl = new TokenList();
      // relative document path
      tl.add(data.meta.file.toString().replace(abs, ""));
      // file size
      tl.add(data.meta.filesize);
      // number of nodes
      tl.add(data.meta.size);
      // maximum number of attributes
      add(tl, "max(for $d in //* return count($d/@*))");
      // total string length of text nodes
      add(tl, "sum(for $d in //text() return string-length($d) + 1)");
      // total string length of attribute values
      add(tl, "sum(for $d in //@* return string-length($d) + 1)");
      // total number of element names
      add(tl, "count(distinct-values(for $d in //* return name($d)))");
      // total number of attribute names
      add(tl, "count(distinct-values(for $d in //@* return name($d)))");
      // total number of namespace URIs
      tl.add(data.ns.size());
      table.contents.add(tl);

      exec(new DropDB(data.meta.name));

    } catch(BaseXException ex) {
      err.println("- " + ex.getMessage());
    }
  }

  /**
   * Adds the query result to the table.
   * @param tl token list
   * @param qu query
   * @throws BaseXException exception
   */
  private void add(final TokenList tl, final String qu) throws BaseXException {
    tl.add(query(qu));
    //tl.add(Integer.toHexString(Integer.parseInt(query(qu))));
  }

  /**
   * Performs the specified query.
   * @param qu query
   * @return string result
   * @throws BaseXException exception
   */
  private String query(final String qu) throws BaseXException {
    return exec(new XQuery(qu)).trim();
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
          final char c = arg.next();
          if(c == '[') {
            min = arg.num();
          } else if(c == ']') {
            max = arg.num();
          } else if(c == 'e') {
            excl.add(arg.string());
          }
        } else {
          doc = arg.string();
        }
      }
    } catch(final Exception ex) {
      ok = false;
    }
    if(!ok) {
      Main.outln("Usage: " + Main.name(this) + " [options] doc" + NL +
          "  -e<dir>  exclude directory from parsing" + NL +
          "  -[<nr>   minimum file length" + NL +
          "  -]<nr>   maximum file length" + NL +
          "  doc      document to be parsed");
    }
    return ok;
  }
}
