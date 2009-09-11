package org.basex.test.query;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.ALauncher;
import org.basex.core.Context;
import org.basex.core.Launcher;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.server.ClientLauncherNew;
import org.basex.util.Args;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.StringList;
import org.basex.util.TokenList;

/**
 * Simple INEX Database test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class InexDBTestNew {
  /** Queries. */
  private static final String QUERIES = "inex.queries";
  /** Database prefix (1000 instances: "pages", 10 instances: "inex"). */
  private static final String DBPREFIX = "inex";

  /** Database context. */
  private static Context ctx = new Context();
  /** Launcher. */
  private ALauncher launcher;
  /** Queries. */
  private static StringList queries;
  /** Databases. */
  private static StringList databases;
  /** Number of articles. */
  private int[] numArt = new int[]{0, 271212, 543767, 816058,
      1088475, 1360368, 1631900, 1905498, 2177546, 2450376};

  /** Maximum number of databases. */
  private static int maxdb = Integer.MAX_VALUE;
  /** Maximum number of queries. */
  private static int maxqu = Integer.MAX_VALUE;
  /** Number of runs. */
  private static int runs = 1;
  /** Use client/server architecture. */
  private static boolean server;
  /** Measure total time. */
  private static boolean total;
  /** Create submission file. */
  private static boolean subfile;
  /** Do not sum path in the submission file. */
  private static boolean dbpath;
  /** PrintOutput for the submission file. */
  private static PrintOutput sub = null;
  /** XMLSerializer for the submission file. */
  private static XMLSerializer xml = null;
  /** Kind of task. */
  private static final String[] TASK = new String[] {"adhoc",
    "budget10", "budget100", "budget1000", "budget10000"};
  /** Kind of type. */
  private static final String[] TYPE =
    new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
  private static String[] query = new String[] {"automatic", "manual"};
  /** Collection for query times. */
  private static double[] qtimes;
  /** Topic ids of the queries. */
  private static int[] tid;
  /** Content ids of the queries. */
//  private static String[] cid;
  /** Results of the queries. */
  private static SeqIter[] results;
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
  /** Shows process info. */
  private boolean info;
  /** Keeps the singe query times. */
  final double[] qt = new double[10 * 115];
  /** Number of query times. */
  int c = 0;

  /**
   * Default constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private InexDBTestNew(final String[] args) throws Exception {
    if(!parseArguments(args)) return;
    String l;
    final BufferedReader brt = new BufferedReader(new FileReader("times"));
    while((l = brt.readLine()) != null) qt[c++] = Double.parseDouble(l);
    brt.close();

    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(QUERIES));
    queries = new StringList();
    final IntList tidl = new IntList();
//    final IntList cidl = new IntList();

    while((l = br.readLine()) != null && queries.size() < maxqu) {
      int i = l.indexOf(';');
      tidl.add(Integer.parseInt(l.substring(0, i)));
      int j = l.indexOf(';', i + 1);
//      cidl.add(Integer.parseInt(l.substring(i + 1, j)));
      j = l.indexOf(';', j + 1);
//      type
      queries.add(l.substring(j + 1));
    }
    br.close();

    if (subfile) {
      // alocate space for query times
      qtimes = new double[queries.size()];
      results = new SeqIter[queries.size()];
      tid = tidl.finish();
//      cid = cidl.finish();
    }

    // cache database names
    databases = new StringList();
    for(final String s : List.list(ctx)) {
      if(s.startsWith(DBPREFIX) && databases.size() < maxdb) databases.add(s);
    }

    BaseX.outln(BaseX.name(InexDBTest.class) + " [" +
        (server ? CLIENTMODE : LOCALMODE) + "]");
    BaseX.outln("=> % queries on % databases, % runs: % time in ms\n",
        queries.size(), databases.size(), runs, total ? "total" : "evaluation");

//    sum();

    // get number of articles for each db
    /*numArt = new int[databases.size()];
    int last = 0;
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      launcher.execute(new Open(databases.get(d)));
      final Names names = ctx.data().tags;
      numArt[d] = last;
      System.out.println(last);
      last += names.stat(names.id(token("article"))).counter;
      launcher.execute(new Close());
    }
    */
    // run test
    final Performance p = new Performance();
    if(server) test();
    else testLocalNew();

    System.out.println("Total Time: " + p.getTimer());

    if(subfile) {
      openSubFile();
      for (int i = 0; i < results.length; i++) {
        if (server) createQueryEntryServer(i, results[i], 1500);
        else createQueryEntry(i, results[i], 1500);
        }
      closeSubFile();
    }
  }

  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new InexDBTestNew(args);
  }

  /**
   * Second test, opening each databases before running the queries.
   * @throws Exception exception
  private void testSingleDB(final int d) throws Exception {
    launcher.execute(new Set(Prop.SERIALIZE, true));
    // loop through all databases
    // open database and loop through all queries
    launcher.execute(new Open(databases.get(d)));
    for(int q = 0; q < queries.size(); q++) {
      final SeqIter s = queryServer(d, q);
      if (results[q] != null) results[q] = addSortedServer(results[q], s);
      else results[q] = s;
    }
    launcher.execute(new Close());
  }
   */

  /**
   * Second test, opening each databases before running the queries.
   * @throws Exception exception
   */
  private void test() throws Exception {
    launcher.execute(new Set(Prop.SERIALIZE, true));
    // loop through all databases
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      launcher.execute(new Open(databases.get(d)));
      for(int q = 0; q < queries.size(); q++) {
        final SeqIter s = queryServer(d, q);
        if (results[q] != null) results[q] = addSortedServer(results[q], s);
        else results[q] = s;
      }
      launcher.execute(new Close());
    }
  }

  /**
   * First test, caching all databases before running the queries.
   * This version runs only locally.
   * @throws Exception exception
   */
   private void testLocalNew() throws Exception {
    // cache all context nodes
    final Nodes[] roots = new Nodes[databases.size()];
    for(int d = 0; d < databases.size(); d++) {
      final Data data = Open.open(ctx, databases.get(d));
      roots[d] = new Nodes(data.doc(), data);
    }

    // loop through all databases
    for(int q = 0; q < queries.size(); q++) {
      SeqIter s = null;
      // loop through all queries
      for(int d = 0; d < databases.size(); d++) {
        // set cached context nodes and run query
        ctx.current(roots[d]);
        int oldSize = 0;
        if (s != null) {
          BaseX.outln("old:" + s.size());
          oldSize = s.size();
        } else BaseX.outln("old: 0");
        final SeqIter ni = queryNew(d, q);
        BaseX.outln("add:" + ni.size());
        s = s == null ? ni : addSorted(s, ni);
        if (s != null)
          BaseX.outln("new:" + s.size() + " " +
              (s.size() == oldSize + ni.size()));
      }
      if (subfile) results[q] = s;
    }
  }

  /**
   * Performs a single query.
   * @param db database offset
   * @param qu query offset
   * @return iter for the results
   * @throws Exception exception
   */
  private SeqIter queryNew(final int db, final int qu) throws Exception {
    // query and cache result
    final String que = subfile ? XQM + "for $i score $s in "
        + queries.get(qu)
        + " order by $s descending return (basex:sum-path($i), $s)"
        : queries.get(qu);
    final Process proc = new XQuery(que);
    if(launcher.execute(proc)) {
      launcher.output(new NullOutput());
    }
    final CachedOutput out = new CachedOutput();
    launcher.info(out);
    SeqIter itr = null;

    final String time = Pattern.compile(".*" +
        (total ? "Total Time" : "Evaluating") + ": (.*?) ms.*",
        Pattern.DOTALL).matcher(out.toString()).replaceAll("$1");
    if (subfile) {
      qtimes[qu] += Double.parseDouble(time);
      if(server) {

      }
      final Result val = proc.result();
      if(val != null && val instanceof SeqIter) {
        itr = (SeqIter) val;
        // update node path
        for (int i = 0; i < itr.size(); i++) {
           if(itr.item[i] instanceof Str) {
             String str = string(((Str) itr.item[i]).str());
             if(str.startsWith("article")) {
               final int s0 = str.indexOf('[');
               final int s1 = str.indexOf(']');
               final int count = Integer.parseInt(str.substring(s0 + 1, s1))
                 +  numArt[db];
               String tmp;
               if (dbpath) {
                 tmp = databases.get(db) + ":" + "article[" +
                     (count - numArt[db]) + str.substring(s1);
               } else
                 tmp = "article[" + count + str.substring(s1);
               itr.item[i] = Str.get(token(tmp));
             }
           }
        }
      }
    }

    // output result
    BaseX.outln("Query % on %: %", qu + 1, databases.get(db), time);
    if(info) {
      BaseX.outln("- " + Pattern.compile(".*Result: (.*?)\\n.*",
          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
    }
    return itr;
  }


  /**
   * Performs a single query within server mode.
   * @param db database offset
   * @param qu query offset
   * @return iter for the results
   * @throws Exception exception
   */
  private SeqIter queryServer(final int db, final int qu) throws Exception {
    // query and cache result
    final String que = subfile ? XQM + "for $i score $s in "
        + queries.get(qu)
        + " order by $s descending return (basex:sum-path($i), $s, " +
          "base-uri($i))"
        : queries.get(qu);
    final Process proc = new XQuery(que);
    final CachedOutput res = new CachedOutput();
    if(launcher.execute(proc)) {
      launcher.output(res);
    }
    final CachedOutput out = new CachedOutput();
    launcher.info(out);
    SeqIter sq = new SeqIter();

/*    final String time = Pattern.compile(".*" +
        (total ? "Total Time" : "Evaluating") + ": (.*?) ms.*",
        Pattern.DOTALL).matcher(out.toString()).replaceAll("$1");
*/
    if (subfile) {
//      qtimes[qu] += Double.parseDouble(time);
      StringTokenizer st = new StringTokenizer(res.toString(), " ");
      String lp = "";
      while (st.hasMoreTokens()) {
        qtimes[qu] += qt[db * 115 + qu];
        final String p = st.nextToken();
        if (!st.hasMoreTokens()) break;
        final String s = st.nextToken();
        if (!st.hasMoreTokens()) break;
        String uri = st.nextToken();
        uri = uri.substring(uri.lastIndexOf('/') + 1);
        final String tmp = uri + ";" + p;
        if (!lp.equals(tmp)) {
          final Str str = Str.get(token(uri + ";" + p));
          str.score(Double.parseDouble(s));
          sq.add(str);
          lp = tmp;
        }
      }
    }

    BaseX.outln("Query % on %: %", qu + 1, databases.get(db),
        qt[db * 115 + qu]);
    if(info) {
      BaseX.outln("- " + Pattern.compile(".*Result: (.*?)\\n.*",
          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
    }
    return sq;
  }

  /**
   * Adds the contents of an iterator in descending order of the score values.
   * @param it1 entry to be added
   * @param it2 entry to be added
   * @return SeqIter with all values
   */
  public SeqIter addSorted(final SeqIter it1, final SeqIter it2) {
    if (it1 == null && it2 != null) return it2;
    if (it2 == null && it1 != null) return it1;
    if (it1 == null && it2 == null) return new SeqIter();

    final SeqIter tmp = new SeqIter();
    Item i1 = it1.next(), i2 = it2.next();
    while(i1 != null && i2 != null) {
      if (i1.score < i2.score) {
        tmp.add(i2);
        tmp.add(it2.next());
        i2 = it2.next();
      } else if (i1.score > i2.score) {
        tmp.add(i1);
        tmp.add(it1.next());
        i1 = it1.next();
      } else {
        tmp.add(i2);
        tmp.add(it2.next());
        tmp.add(i1);
        tmp.add(it1.next());
        i1 = it1.next();
        i2 = it2.next();
      }

    }
    while((i1 = it1.next()) != null) {
      tmp.add(i1);
      tmp.add(it1.next());
    }
    while((i2 = it2.next()) != null) {
      tmp.add(i2);
      tmp.add(it2.next());
    }
    return tmp;
  }


  /**
   * Adds the contents of an iterator in descending order of the score values.
   * @param it1 entry to be added
   * @param it2 entry to be added
   * @return SeqIter with all values
   */
  public SeqIter addSortedServer(final SeqIter it1, final SeqIter it2) {
    final SeqIter tmp = new SeqIter();
    Item i1 = it1.next(), i2 = it2.next();
    while(i1 != null && i2 != null) {
      if (i1.score < i2.score) {
        tmp.add(i2);
        i2 = it2.next();
      } else if (i1.score > i2.score) {
        tmp.add(i1);
        i1 = it1.next();
      } else {
        tmp.add(i2);
//        tmp.add(i1);
        i1 = it1.next();
        i2 = it2.next();
      }

    }
    while((i1 = it1.next()) != null) tmp.add(i1);
    while((i2 = it2.next()) != null) tmp.add(i2);
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
          if(ca == 'x') {
            //readQueries();
            convertTopics();
            return false;
          } else if(ca == 'd') {
            maxdb = Integer.parseInt(arg.string());
          } else if(ca == 'q') {
            maxqu = Integer.parseInt(arg.string());
          } else if(ca == 'r') {
            runs = Integer.parseInt(arg.string());
          } else if(ca == 's') {
            server = true;
          } else if(ca == 't') {
            total = true;
          } else if(ca == 'c') {
            subfile = true;
          } else if(ca == 'v') {
            info = true;
          } else if(ca == 'p') {
            dbpath = true;
          } else if(ca == 'u') {
            final String[] s = new String[args.length - 1];
            System.arraycopy(args, 1, s, 0, s.length);
            updateTimes(s);
            return false;
          } else {
            ok = false;
          }
        } else {
          ok = false;
        }
      }

      launcher = server ? new ClientLauncherNew(ctx) : new Launcher(ctx);
      launcher.execute(new Set(Prop.SERIALIZE, total));
      launcher.execute(new Set(Prop.RUNS, runs));
      launcher.execute(new Set(Prop.INFO, true));
      launcher.execute(new Set(Prop.ALLINFO, info));
    } catch(final Exception ex) {
      if(ex instanceof IOException) ex.printStackTrace();
      ok = false;
    }

    if(!ok) {
      BaseX.outln("Usage: " + BaseX.name(this) + " [options]" + NL +
      "  -c  create submissionfile" + NL +
      "  -d<no>  maximum no/database" + NL +
      "  -q<no>  maximum no/queries" + NL +
      "  -r<no>  number of runs" + NL +
      "  -s      use server architecture" + NL +
      "  -t      measure total time" + NL +
      "  -u[...] update submission times" + NL +
      "  -v      show process info");
    }
    return ok;
  }

  /**
   * Create and print submission file.
   * @throws Exception Exception
   */
  private static void openSubFile() throws Exception {
    sub = new PrintOutput("submission.xml");
    xml = new XMLSerializer(sub, false, true);
//  <!DOCTYPE efficiency-submission SYSTEM 'efficiency-submission.dtd'>
  //  xml.doctype("efficiency-submission".getBytes(),
  //      "'efficiency-submission.dtd'".getBytes(), null);

    // print header in output file
    xml.openElement(token("efficiency-submission"),
        token("participant-id"), token("304"),
        token("run-id"), token("1111111"),
        token("task"), token(TASK[0]),
        token("type"), token(TYPE[0]),
        token("query"), token(query[0]),
        token("sequential"), token("yes")
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
    xml.text(token("Query compilation in BaseX was optimized to rewrite " +
        "all kinds of index-related XQuery Full Text requests. " +
        "Hence, our performance measurements include the total " +
        "time both for accessing the indexes as well as " +
        "traversing the specified location paths at runtime."));
    xml.closeElement();
    xml.openElement(token("ranking_description"));
    xml.text(token("We are using both content-based as well as " +
        "structural-based ranking. At first, a content-based weight " +
        "is estimated and later refined for each location step. " +
        "The weights are derived from database meta information."));
    xml.closeElement();
    xml.openElement(token("indexing_description"));
    xml.text(token("The full-text indexes of BaseX support both the quick " +
        "evaluation of simple ftcontains operators as well as advanced " +
        "features of the upcoming XQFT Recommendation. " +
        "The indexes contain token positions and pointers on the text nodes. " +
        "Structural information, such as location paths to the text nodes, " +
        "are evaluated at runtime."));
    xml.closeElement();
    xml.openElement(token("caching_description"));
    xml.text(token("Both database instances as well as the full-text " +
        "indexes are completely disk-based and rely on the caching " +
        "mechanisms of the operating system."));
    xml.closeElement();
  }

  /**
   * Create and print submission file.
   * @throws Exception Exception
   */
  private static void closeSubFile() throws Exception {
    xml.closeElement();
    xml.close();
    sub.close();
  }

  /**
   * Update query times in submission.xml.
   * @param args files with times
   * @throws IOException IOException
   */
  private void updateTimes(final String[] args) throws IOException {
    final BufferedReader[] bf = new BufferedReader[args.length];
    for (int j = 0; j < bf.length; j++)
      bf[j] = new BufferedReader(new FileReader(args[j]));

    final int numdb = 10;
    final int numq = 115;
    final double[] qut = new double[numq * numdb];
    String l;
    int i = 0;
    while ((l = bf[0].readLine()) != null) qut[i++] = Double.parseDouble(l);
    bf[0].close();

    for (int j = 1; j < bf.length; j++) {
      i = 0;
      while ((l = bf[j].readLine()) != null) {
        qut[i] = Math.min(qut[i], Double.parseDouble(l));
        i++;
      }
      bf[j].close();
    }

    final double[] tmp = new double[numq];
    for (int j = 0; j < tmp.length; j++) {
      for (int z = 0; z < numdb; z++) {
        tmp[j] += qut[j + z * numq];
      }
    }

    final BufferedReader br =
      new BufferedReader(new FileReader("submission.xml"));
    final PrintOutput o = new PrintOutput("submissionU.xml");
    i = 0;
    while ((l = br.readLine()) != null) {
      if (l.contains("<topic topic-id=")) {
        final int s = l.indexOf("total_time_ms=\"") +
          "total_time_ms=\"".length();
        final int e = l.lastIndexOf('"');
        final double ti = Double.parseDouble(l.substring(s, e));
        if (ti > tmp[i] || ti == 0) o.write((l.substring(0, s) +
            tmp[i] + l.substring(e) + NL).getBytes());
        else o.write((l + NL).getBytes());
      } else o.write((l + NL).getBytes());
    }
    br.close();
    o.flush();
    o.close();
    System.out.println("Updated");
  }
  /**
   * Create subfile entry for a query result.
   *
   * @param q query
   * @param res result of the query
   * @param k max number of results
   * @throws IOException IOException
   */
  private static void createQueryEntryServer(final int q, final SeqIter res,
      final int k) throws IOException {

    xml.openElement(token("topic"), token("topic-id"), token(tid[q]),
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
      xml.text((Double.toString(a.score)).getBytes());
      xml.closeElement();
      xml.closeElement();
    }
    xml.closeElement();
  }

  /**
   * Create query entry in the submission file.
   * @param q pointer on the query
   * @param res result set
   * @param k maximum number of results
   * @throws IOException Exception
   */
  private static void createQueryEntry(final int q, final SeqIter res,
      final int k) throws IOException {
    xml.openElement(token("topic"),
        token("topic-id"), token(tid[q]),
        token("total_time_ms"), token(qtimes[q])
    );

    Item a;
    int r = 1;
    while(res != null && (a = res.next()) != null) {
      if (r == k) break;
      final Item b = res.next();
      if(a instanceof Str && b != null &&
          b instanceof Dbl) {
        xml.openElement(token("result"));
        xml.openElement(token("file"));
        xml.text(token("pages"));
        xml.closeElement();
        xml.openElement(token("path"));
        xml.text(a.str());
        xml.closeElement();
        xml.openElement(token("rank"));
        xml.text(token(r++));
        xml.closeElement();
        xml.openElement(token("rsv"));
        xml.text(b.str());
        xml.closeElement();
        xml.closeElement();
      }
    }
    xml.closeElement();
  }

  /**
   * Read and backup all queries from file.
   * @throws Exception FileNotFoundException
   */
  public void convertTopics() throws Exception {
    final File file = new File("topics.xml");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    String t = "";
    String ty = "";
    final PrintOutput out = new PrintOutput("inex.queries");
    while((line = br.readLine()) != null) {
      if (line.indexOf("topic ct_no") > -1) {
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
      } else if (line.indexOf("xpath_title") > -1) {
        // extract query
        int s0 = line.indexOf('/');
        String q = line.substring(s0, line.lastIndexOf('<'));
        out.println(t + ";" + c + ";" + ty + ";" + q);
      }
     }
    br.close();
  }

  /**
   * Read and backup all queries from file.
   * @throws Exception FileNotFoundException
   */
  public void readQueries() throws Exception {
    final File file = new File("co.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    while((line = br.readLine()) != null) {
      // extract topic id
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      final int topicid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract content id
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
      final int contentid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract query
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

      q = replaceElements(q);
      System.out.println(topicid + ";" + contentid + ";" + q);
    }
    br.close();
  }

  /**
   * Replaces dedicated nodes by an or expression.
   * [(a|b) ftcontains "c"] => [a ftcontains "c" or [b ftcontains "c"]
   * @param str Sting query to be replaced
   * @return replaced query String
   */
  private static String replaceElements(final String str) {
    byte[] b = str.getBytes();
//    final byte[] or = new byte[]{' ', 'o', 'r', ' '};
    final byte[] co = new byte[]{' ', '|', ' '};
    final byte[] txt = new byte[]{'/', 't', 'e', 'x', 't', '(', ')'};
    int i = 0;
    int sp = -1, sb = -1, sbb = -1, os = -1, ebb = -1;
    byte[] path = new byte[]{};
    TokenList tl;
    while(i < b.length) {
      switch (b[i]) {
        case '/':
          sp = sp == -1 ? i : sp;
          break;
        case '(':
          sb = i;
          if (b[i + 1] == ')' || b[i + 1] == ' ' || b[i + 1] == '.') break;
          i++;
//          if (b[i] == ')' || b[i] == ' ' || b[i] == '.') break;
          boolean f = false;
          while(i < b.length && b[i] != ')' && !f) {
            f = b[i] == '"' || b[i] == '\'';
            i++;
          }
          if (f) break;
          i = sb;

          path = new byte[i - sp];
          System.arraycopy(b, sp, path, 0, path.length);

          tl = new TokenList();
          os = i + 1;
          while (i < b.length && b[i] != ')') {
            if (b[i] == '|') {
              final byte[] tok = new byte[i - os];
              System.arraycopy(b, os, tok, 0, tok.length);
              tl.add(tok);
              os = i + 1;
            }
            i++;
          }
          if (tl.size() > 0) {
            final byte[] tok = new byte[i - os];
            System.arraycopy(b, os, tok, 0, tok.length);
            tl.add(tok);

            // backup path
            path = new byte[sb - sp];
            System.arraycopy(b, sp, path, 0, path.length);
          }
          i++;

          // check if predicate before or after ( | )
          if (sbb > -1 && ebb > -1) {
            // predicate before ( | )
            os = 0;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for (int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, ebb + 1);
              bn = Array.add(bn, b, ebb + 1, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              if (k < tl.size() - 1)
                bn = Array.add(bn, co, 0, co.length);
            }
            i = bn.length;
            b = bn;
          } else if (sbb == -1 && ebb == -1) {
            // predicate after ( | )
            sbb = i;
            while (sbb < b.length && b[sbb] != '[') sbb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for (int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, sbb, b.length);
              if (k < tok.length - 1)
                bn = Array.add(bn, co, 0, co.length);
            }
            i = sb;
            b = bn;
          } else {
            // ( | ) inside predicate
            ebb = i + 1;
            while (ebb < b.length && !(b[ebb] == ']' ||
                ebb + 2 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'o'
                  && b[ebb + 2] == 'r' ||
                ebb + 3 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'a'
                  && b[ebb + 2] == 'n' && b[ebb + 3] == 'd'
                  )) ebb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            bn = Array.add(bn, b, 0, sbb);
            for (int k = 0; k < tl.size(); k++) {
              bn = Array.add(bn, b, sbb + (k == 0 ? 0 : 1), sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, i, ebb);
              if (k < tok.length - 1)
                bn = Array.add(bn, co, 0, co.length);
//                bn = Array.add(bn, or, 0, or.length);
            }
            bn = Array.add(bn, b, ebb, b.length);
            b = bn;
            ebb = -1;
          }
          break;
        case '[':
          ebb = -1;
          sbb = i;
          break;
        case ']':
          ebb = i;
          break;
        case 'o':
          if (ebb > 0 && i + 1 < b.length && b[i + 1] == 'r') {
            sp = -1;
            sb = -1;
            sbb = -1;
            os = -1;
            ebb = -1;
          }
          break;
        case '.':
          if (b[i + 1] != '/') {
            // . => .//text()
            final byte[] bn = new byte[b.length + 1 + txt.length];
            System.arraycopy(b, 0, bn, 0, i + 1);
            bn[i + 1] = '/';
            System.arraycopy(txt, 0, bn, i + 2, txt.length);
            System.arraycopy(b, i + 1, bn, i + 2 + txt.length,
                b.length - i - 1);
            b = bn;
          } else {
            // .//foo => .//foo/text()
            final int j = i;
            while(i < b.length && b[i] != ' ') i++;
            if (b.length - 6 > 0 && b[i - 6] == 't' && b[i - 5] == 'e'
              && b[i - 4] == 'x' && b[i - 3] == 't' && b[i - 2] == '('
              && b[i - 1] == ')') break;

            final byte[] bn = new byte[b.length + txt.length];
            System.arraycopy(b, 0, bn, 0, i);
            System.arraycopy(txt, 0, bn, i, txt.length);
            System.arraycopy(b, i, bn, i + txt.length, b.length - i);
            b = bn;
            i = j;
          }
        break;
      }
      i++;
    }
    return string(b);
  }

  /**
   * Sum up processing times.
   */
  public void sum() {
    int size = 10; //databases.size();
    final double[] sum = new double[queries.size()];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < sum.length; j++) {
        sum[j] = qt[i * sum.length + j];
      }
    }
    for (int j = 0; j < sum.length; j++) {
      System.out.println("query" + j + ":" + sum[j]);
    }
  }
}
