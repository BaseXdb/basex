package org.basex.examples;

import static java.lang.System.*;
import static org.basex.core.Text.*;
import java.io.File;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
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
  /** Drop databases after analysis. */
  private boolean drop;
  /** Tabular output. */
  private boolean tab;
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

    ctx.prop.set(Prop.INTPARSE, true);
    ctx.prop.set(Prop.TEXTINDEX, false);
    ctx.prop.set(Prop.ATTRINDEX, false);
    ctx.prop.set(Prop.PATHINDEX, false);
    
    final TokenList tl = new TokenList();
    tl.add("Document");
    tl.add("Length");
    tl.add("Pre");
    tl.add("Attsize");
    tl.add("Name(e)");
    tl.add("Name(a)");
    tl.add("URI");
    tl.add("Content(t)");
    tl.add("Content(a)");
    tl.add("Depth");
    tl.add("Docs");
    table.header = tl;
    
    if(tab) {
      for(final byte[] b : tl) {
        Main.out(b);
        Main.out('\t');
      }
      Main.outln();
    }

    if(doc == null) {
      parse(new File("."));
    } else {
      analyze(doc);
    }

    if(!tab) out.print(Token.string(table.finish()));
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
      exec(new Check(input));

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
      // total number of element names
      add(tl, "count(distinct-values(for $d in //* return name($d)))");
      // total number of attribute names
      add(tl, "count(distinct-values(for $d in //@* return name($d)))");
      // total number of namespace URIs
      tl.add(data.ns.size());
      // total string length of text nodes
      add(tl, "sum(for $d in //text() return string-length($d) + 1)");
      // total string length of attribute values
      add(tl, "sum(for $d in //@* return string-length($d) + 1)");
      // document height
      tl.add(data.meta.height);
      // number of documents
      tl.add(data.meta.ndocs);

      if(tab) {
        for(int u = 0; u < tl.size(); u++) {
          Main.out(tl.get(u));
          Main.out('\t');
        }
        Main.outln();
      } else {
        table.contents.add(tl);
      }

      if(drop) exec(new DropDB(data.meta.name));
    
    } catch(BaseXException ex) {
      err.println("- " + input + ": " + ex.getMessage());
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
    //tl.add(Long.toHexString(Long.parseLong(query(qu))));
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
          } else if(c == 'd') {
            drop = true;
          } else if(c == 'e') {
            excl.add(arg.string());
          } else if(c == 't') {
            tab = true;
          } else {
            ok = false;
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
          "  -d       drop databases after analysis" + NL +
          "  -e<dir>  exclude directory from parsing" + NL +
          "  -[<nr>   minimum file length" + NL +
          "  -]<nr>   maximum file length" + NL +
          "  -t       tabular output" + NL +
          "  doc      document to be parsed");
    }
    return ok;
  }
}
