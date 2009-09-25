package org.basex.test.query;

import static org.basex.core.Text.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.server.ClientSession;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.StringList;

/**
 * Simple word frequency collector for INEXDBtests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class InexTFTest {
  /** Words file. */
  static final String WORDS = "words";
  /** Query file. */
  static final String WORDSF = "words.freq";
  /** Database prefix. */
  static final String DBPREFIX = "inex";

  /** Database context. */
  private final Context ctx = new Context();
  /** Session. */
  private ClientSession session;
  /** Queries. */
  private StringList words;
  /** Databases. */
  private StringList databases;
  /** Frequency of each word. */
  private int[] freq;
  /** Container for qtimes and results. */
  private PrintOutput res = new PrintOutput(WORDSF);


  /**
   * Main test method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new InexTFTest(args);
  }

  /**
   * Default constructor.
   * @param args arguments
   * @throws Exception exception
   */
  private InexTFTest(final String[] args) throws Exception {
    final Performance p = new Performance();
    Main.outln(Main.name(InexTFTest.class));
    databases = new StringList();
    
    if(!parseArguments(args)) return;
    
    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(WORDS));
    words = new StringList();
    String l;
    while((l = br.readLine()) != null) 
        words.add(l);      
    br.close();
    
    freq = new int[words.size()];
    
    // cache database names
    if (databases.size() == 0)
      for(final String s : List.list(ctx)) 
        if(s.startsWith(DBPREFIX)) databases.add(s);

    Main.outln("=> % words on % databases: time in ms\n",
        words.size(), databases.size());

    // run test
    test();

    for (int i = 0; i < freq.length; i++) 
      res.println(words.get(i) + ";" + freq[i]);
    res.close();
    
    Main.outln("Total Time: " + p);
  }
  
  /**
   * Parses the command line arguments.
   * @param args the command line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args);
    boolean ok = true;
    try {
      while(arg.more() && ok) {
        if(arg.dash()) {
          final char ca = arg.next();
          if(ca == 'd') {
            databases.add(arg.string());
          } else ok = false;
        }
      }
      if(ok) {
        session = new ClientSession(ctx);
        session.execute(new Set(Prop.INFO, true));
        session.execute(new Set(Prop.ALLINFO, false));
      } else {
        Main.outln("Usage: " + Main.name(this) + " [options]" + NL +
          "  -d database");
      }
    } catch(final Exception ex) {
      ok = false;
      Main.errln("Please run BaseXServer for using server mode.");
      ex.printStackTrace();
    }

    return ok;
  }

  /**
   * Second test, opening each databases before running the queries.
   * @throws Exception exception
   */
  private void test() throws Exception {
    // loop through all databases
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      session.execute(new Open(databases.get(d)));
      for(int q = 0; q < words.size(); q++) {
        query(d, q);
      }
      session.execute(new Close());
    }
  }

  /**
   * Performs a single query.
   * @param db database offset
   * @param qu query offset
   * @throws Exception exception
   */
  private void query(final int db, final int qu)
      throws Exception {

    if(session.execute(new XQuery(
        "distinct-values((for $i in //*[text() ftcontains \"" 
        + words.get(qu) + "\"] return base-uri($i)))"))) {
      session.output(new CachedOutput());

      final String str = session.info();
      final String items = find(str, "Results   : ([0-9]+) Item");
      
      // output result
      Main.outln("Query % on %: % items",
          qu + 1, databases.get(db), items);
      final int n = Integer.parseInt(items);
      if (n > 0) freq[qu] += n;
    } else  Main.outln(session.info());    
  }

  /**
   * Finds a string in the specified pattern.
   * @param str input string
   * @param pat regular pattern
   * @return resulting string
   */
  private String find(final String str, final String pat) {
    final Matcher m = Pattern.compile(pat).matcher(str);
    return m.find() ? m.group(1) : "";
  }
}
