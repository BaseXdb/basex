package org.basex.test.inex;

import static org.basex.core.Text.*;
import static org.basex.test.inex.InexTest.*;
import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.server.ClientSession;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.StringList;

/**
 * Simple INEX Database test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class InexSubmit {
  /** Method used to sum up paths. */
  private static final String XQM =
    "declare namespace basex = \"http://www.basex.com\"; " +
    "declare function basex:sum-path ( $n as node()? )  as xs:string { " +
    " string-join( for $a in $n/ancestor-or-self::* " +
    " let $ssn := $a/../*[name() = name($a)] " +
    " return concat(name($a),'[',basex:index-of($ssn,$a),']'), '/')};" +
    "declare function basex:index-of (" +
    " $n as node()* , $ntf as node() )  as xs:integer* { " +
     "  for $s in (1 to count($n)) " +
     "  return $s[$n[$s] is $ntf]};";

  /** Kind of task. */
  private static final String[] TASK = new String[] {"adhoc",
    "budget10", "budget100", "budget1000", "budget10000"};
  /** Kind of type. */
  private static final String[] TYPE =
    new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
  private static final String[] QUERY = new String[] {"automatic", "manual"};

  /** Database context. */
  private final Context ctx = new Context();
  /** Queries. */
  private final StringList queries;
  /** Topic ids of the queries. */
  private final StringList tid;

  /** Collection for query result sizes. */
  private final int[] qressizes;
  /** Keeps the single query times. */
  private final double[] qt;
  /** Collection for query times. */
  private final double[] qtimes;
  /** Results of the queries. */
  private final SeqIter[] results;
  /** Number of queries. */
  private final int nqueries;

  /** Session. */
  private ClientSession session;
  /** Databases. */
  private StringList databases;
  /** PrintOutput for the submission file. */
  private PrintOutput sub;
  /** XMLSerializer for the submission file. */
  private XMLSerializer xml;
  /** Number of query times. */
  private int c;

  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new InexSubmit(args);
  }

  /**
   * Default constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private InexSubmit(final String[] args) throws Exception {
    final Performance p = new Performance();
    Main.outln(Main.name(InexSubmit.class));

    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(QUERIES));
    queries = new StringList();
    tid = new StringList();

    String l;
    while((l = br.readLine()) != null) {
      final int i1 = l.indexOf(';');
      final int i2 = l.indexOf(';', i1 + 1);
      final int i3 = l.lastIndexOf(';');
      tid.add(l.substring(i1 + 1, i2));
      queries.add(l.substring(i3 + 1));
    }
    br.close();

    // allocate space for query times
    nqueries = queries.size();
    qtimes = new double[nqueries];
    results = new SeqIter[nqueries];
    qressizes = new int[10 * nqueries];
    qt = new double[10 * nqueries];

    if(!parseArguments(args)) return;

    final BufferedReader brt = new BufferedReader(new FileReader(TIMES));
    while((l = brt.readLine()) != null) {
      final int index = l.indexOf(';');
      qt[c] = Double.parseDouble(l.substring(0, index));
      qressizes[c++] = Integer.parseInt(l.substring(index + 1));
    }
    brt.close();

    // cache database names
    databases = new StringList();
    for(final String s : List.list(ctx)) {
      if(s.startsWith(DBPREFIX)) databases.add(s);
    }

    // run test
    test();

    openSubFile();
    for(int i = 0; i < results.length; i++) {
      createQueryEntryServer(i, results[i], 1500);
    }
    closeSubFile();

    Main.outln("Total Time: " + p);
  }

  /**
   * Second test, opening each databases before running the queries.
   * @throws Exception exception
   */
  private void test() throws Exception {
    session.execute(new Set(Prop.SERIALIZE, true));
    // loop through all databases
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      session.execute(new Open(databases.get(d)));
      for(int q = 0; q < queries.size(); q++) {
        results[q] = addSortedServer(results[q], query(d, q));
      }
      session.execute(new Close());
    }
  }

  /**
   * Performs a single query within server mode.
   * @param db database offset
   * @param qu query offset
   * @return iter for the results
   * @throws Exception exception
   */
  private SeqIter query(final int db, final int qu) throws Exception {
    final int size = qressizes[db * nqueries + qu];
    final double qtime = qt[db * nqueries + qu];
    qtimes[qu] += qtime;

    if(size == 0) return new SeqIter();

    // query and cache result
    final String que = XQM + "for $i score $s in " +
        queries.get(qu) + " order by $s descending " +
        "return (basex:sum-path($i), $s, base-uri($i))";

    final Proc proc = new XQuery(que);
    final CachedOutput res = new CachedOutput();
    session.execute(proc, res);

    final SeqIter sq = new SeqIter();
    final StringTokenizer st = new StringTokenizer(res.toString(), " ");
    int z = 0;
    while(st.hasMoreTokens() && z < size) {
      final String p = st.nextToken();
      if(!st.hasMoreTokens()) break;
      final String s = st.nextToken();
      if(!st.hasMoreTokens()) break;
      String uri = st.nextToken();
      //while(uri.indexOf(".xml") == -1) uri = st.nextToken();
      uri = uri.substring(uri.lastIndexOf('/') + 1, uri.indexOf(".xml") + 4);
      final String tmp = uri + ";" + p;
      final Str str = Str.get(token(tmp));
      str.score(Double.parseDouble(s));
      sq.add(str);
      z++;
    }

    Main.outln("Query % on %: % with size: %", qu + 1,
        databases.get(db), qtime, size);
    return sq;
  }

  /**
   * Adds the contents of an iterator in descending order of the score values.
   * @param it1 entry to be added
   * @param it2 entry to be added
   * @return SeqIter with all values
   */
  private SeqIter addSortedServer(final SeqIter it1, final SeqIter it2) {
    if(it1 == null || it1.size() == 0) return it2;

    final SeqIter tmp = new SeqIter();
    Item i1 = it1.next(), i2 = it2.next();
    while(i1 != null && i2 != null) {
      if(i1.score < i2.score) {
        tmp.add(i2);
        i2 = it2.next();
      } else if(i1.score > i2.score) {
        tmp.add(i1);
        i1 = it1.next();
      } else {
        tmp.add(i2);
        i1 = it1.next();
        i2 = it2.next();
      }
    }
    while(i1 != null) {
      tmp.add(i1);
      i1 = it1.next();
    }

    while(i2 != null) {
      tmp.add(i2);
      i2 = it2.next();
    }
    return tmp;
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
          if(ca == 'u') {
            updateTimes(Arrays.copyOfRange(args, 1, args.length));
            return false;
          } else if(ca == 'x') {
            convertTopics();
            return false;
          }
          ok = false;
        }
      }
      if(ok) {
        session = new ClientSession(ctx, ADMIN, ADMIN);
        session.execute(new Set(Prop.INFO, true));
      } else {
        Main.outln("Usage: " + Main.name(this) + " [options]" + NL +
          "  -u[...] update submission times" + NL +
          "  -x      convert queries");
      }
    } catch(final Exception ex) {
      ok = false;
      Main.errln("Please run BaseXServer for using server mode.");
      ex.printStackTrace();
    }

    return ok;
  }

  /**
   * Creates and prints the submission file.
   * @throws Exception Exception
   */
  private void openSubFile() throws Exception {
    sub = new PrintOutput(SUBMISSION);

    final SerializerProp sp = new SerializerProp();
    sp.set(SerializerProp.S_DOCTYPE_SYSTEM, "efficiency-submission.dtd");
    xml = new XMLSerializer(sub, sp);

    // print header in output file
    xml.openElement(token("efficiency-submission"),
        token("participant-id"), token("304"),
        token("run-id"), token("1111111"),
        token("task"), token(TASK[0]),
        token("type"), token(TYPE[1]),
        token("query"), token(QUERY[0]),
        token("sequential"), token("yes"),
        token("no_cpu"), token("2"),
        token("ram"), token("32 GB"),
        token("index_size_bytes"), token("7869335184"),
        token("indexing_time_sec"), token("2874")
    );
    xml.emptyElement(token("topic-fields"),
        token("co_title"), token("no"),
        token("cas_title"), token("no"),
        token("xpath_title"), token("yes"),
        token("text_predicates"), token("no"),
        token("description"), token("no"),
        token("narrative"), token("no")
    );

    xml.openElement(token("general_description"));
    xml.text(token("BaseX is a native XML database and XPath/XQuery " +
        "processor, including support for the latest XQuery Full Text " +
        "recommendation. The client/server architecture of BaseX 5.75 was " +
        "used to perform the tests. The test machine has an Intel Xeon " +
        "E5345 with 2 Quad-Core CPUs and 32 GB RAM."));
    xml.closeElement();
    xml.openElement(token("ranking_description"));
    xml.text(token("As we put our main focus on efficiency and generic " +
        "evaluation of all types of XQuery requests and input documents, " +
        "our scoring model is based on a classical TF/IDF implementation. " +
        "Additional scoring calculations are performed by XQFT (ftand, ftor, " +
        "ftnot) and XQuery operators (union, location steps, ...). " +
        "A higher ranking is given to those text nodes which are closer to " +
        "the location steps of the input query than others. We decided " +
        "to stick with conjunctive query evaluation (using 'ftand' instead " +
        "of 'ftor' in the proposed topic queries), as a change to the " +
        "disjunctive mode would have led to too many changes, which could " +
        "not have been reasonably implemented in the remaining time frame. " +
        "Next, we decided to not extend the proposed queries with stemming, " +
        "stop words or thesaurus options. As a consequence, many queries " +
        "might return less results than the TopX reference engine " +
        "(and sometimes no results at all)."));
    xml.closeElement();
    xml.openElement(token("indexing_description"));
    xml.text(token("The full-text indexes of BaseX support both a " +
        "quick and sped up evaluation of simple full text queries as well " +
        "as the full evaluation of all recommended features of the upcoming " +
        "XQFT Recommendation. " +
        "Positions and pointers on the text nodes are stored in the indexes " +
        "as well as simple scoring information." +
        "Structural information, such as location paths to the text nodes, " +
        "are evaluated at runtime. To give a realistic picture, we have " +
        "included both the total time for accessing indexes as well " +
        "well as traversing the inverted specified location paths in our " +
        "final performance results."));
    xml.closeElement();
    xml.openElement(token("caching_description"));
    xml.text(token("Both the database instances as well as the full-text " +
        "indexes are completely disk-based and rely on the caching " +
        "mechanisms of the operating system."));
    xml.closeElement();
  }

  /**
   * Closes the submission file.
   * @throws Exception Exception
   */
  private void closeSubFile() throws Exception {
    xml.closeElement();
    xml.close();
    sub.close();
  }

  /**
   * Updates query times in submission.xml.
   * @param args files with times
   * @throws IOException IOException
   */
  private void updateTimes(final String[] args) throws IOException {
    final BufferedReader[] bf = new BufferedReader[args.length];
    for(int j = 0; j < bf.length; j++)
      bf[j] = new BufferedReader(new FileReader(args[j]));

    final int numdb = 10;
    final double[] qut = new double[nqueries * numdb];
    String l;
    int i = 0;
    while((l = bf[0].readLine()) != null) {
      qut[i++] = Double.parseDouble(l.substring(0, l.indexOf(';')));
    }
    bf[0].close();

    for(int j = 1; j < bf.length; j++) {
      i = 0;
      while((l = bf[j].readLine()) != null) {
        qut[i] = Math.min(qut[i], Double.parseDouble(l));
        i++;
      }
      bf[j].close();
    }

    final double[] tmp = new double[nqueries];
    for(int j = 0; j < tmp.length; j++) {
      for(int z = 0; z < numdb; z++) {
        tmp[j] += qut[j + z * nqueries];
      }
    }

    final BufferedReader br = new BufferedReader(new FileReader(SUBMISSION));
    final PrintOutput o = new PrintOutput(SUBMISSIONU);
    i = 0;
    while((l = br.readLine()) != null) {
      if(l.contains("<topic topic-id=")) {
        final int s = l.indexOf("total_time_ms=\"") +
          "total_time_ms=\"".length();
        final int e = l.lastIndexOf('"');
        final double ti = Double.parseDouble(l.substring(s, e));
        if(ti > tmp[i] || ti == 0) {
          o.print(l.substring(0, s) + tmp[i] + l.substring(e));
        } else {
          o.print(l);
        }
        i++;
      } else o.print(l);
      o.print(NL);
    }
    br.close();
    o.flush();
    o.close();
    Main.outln("Updated");
  }

  /**
   * Creates a subfile entry for a query result.
   * @param q query
   * @param res result of the query
   * @param k max number of results
   * @throws IOException IOException
   */
  private void createQueryEntryServer(final int q, final SeqIter res,
      final int k) throws IOException {

    xml.openElement(token("topic"), token("topic-id"), token(tid.get(q)),
        token("total_time_ms"), token(qtimes[q])
    );

    Item a;
    int r = 1;
    while(res != null && (a = res.next()) != null && r <= k) {
      final byte[] s = a.str();
      final int i = indexOf(s, ';');
      xml.openElement(token("result"));
      xml.openElement(token("file"));
      xml.text(substring(s, 0, i));
      xml.closeElement();
      xml.openElement(token("path"));
      xml.text(substring(s, i + 1));
      xml.closeElement();
      xml.openElement(token("rank"));
      xml.text(token(r++));
      xml.closeElement();
      xml.openElement(token("rsv"));
      xml.text(token(a.score));
      xml.closeElement();
      xml.closeElement();
    }
    xml.closeElement();
  }

  /**
   * Reads and backups all queries from the topics file.
   * @throws Exception FileNotFoundException
   */
  private void convertTopics() throws Exception {
    final File file = new File(TOPICS);
    if(!file.exists()) {
      Main.outln("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    String t = "";
    String ty = "";

    final PrintOutput out = new PrintOutput(QUERIES);
    while((line = br.readLine()) != null) {
      if(line.indexOf("topic ct_no") > -1) {
        // extract topic id
        int s0 = line.indexOf('"');
        int s1 = line.indexOf('"', s0 + 1);
        t = line.substring(s0 + 1, s1);
        // extract content id
        s0 = line.indexOf('"', s1 + 1);
        s1 = line.indexOf('"', s0 + 1);
        //ca = line.substring(s0 + 1, s1);
        // extract type
        s0 = line.indexOf('"', s1 + 1);
        s1 = line.indexOf('"', s0 + 1);
        ty = line.substring(s0 + 1, s1);
      } else if(line.indexOf("xpath_title") > -1) {
        // extract query
        final int s0 = line.indexOf('/');
        final String q = line.substring(s0, line.lastIndexOf('<'));
        out.println(t + ";" + c + ";" + ty + ";" + q);
      }
    }
    br.close();
  }
}
