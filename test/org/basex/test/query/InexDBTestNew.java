package org.basex.test.query;

import static org.basex.core.Text.*;
import static org.basex.test.query.InexDBTest.*;
import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.server.ClientSession;
import org.basex.util.Args;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.StringList;

/**
 * Simple INEX Database test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class InexDBTestNew {
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
  private Context ctx = new Context();
  /** Session. */
  private ClientSession session;
  /** Queries. */
  private StringList queries;
  /** Databases. */
  private StringList databases;
  /** Topic ids of the queries. */
  private IntList tid;

  /** PrintOutput for the submission file. */
  private PrintOutput sub = null;
  /** XMLSerializer for the submission file. */
  private XMLSerializer xml = null;

  /** Collection for query result sizes. */
  private int[] qressizes;
  /** Keeps the single query times. */
  private double[] qt;
  /** Collection for query times. */
  private double[] qtimes;
  /** Results of the queries. */
  private SeqIter[] results;
  /** Number of queries. */
  private int nqueries;
  /** Number of query times. */
  private int c = 0;

  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new InexDBTestNew(args);
  }

  /**
   * Default constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private InexDBTestNew(final String[] args) throws Exception {
    final Performance p = new Performance();
    Main.outln(Main.name(InexDBTestNew.class));

    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(QUERIES));
    queries = new StringList();
    tid = new IntList();

    String l;
    while((l = br.readLine()) != null) {
      tid.add(Integer.parseInt(l.substring(0, l.indexOf(';'))));
      queries.add(l.substring(l.lastIndexOf(';') + 1));
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

    if(size == 0) return new SeqIter();

    // query and cache result
    final String que = XQM + "for $i score $s in " +
        queries.get(qu) + " order by $s descending " +
        "return (basex:sum-path($i), $s, base-uri($i))";

    final Process proc = new XQuery(que);
    final CachedOutput res = new CachedOutput();
    if(session.execute(proc)) session.output(res);
    session.info(new CachedOutput());

    final SeqIter sq = new SeqIter();
    final StringTokenizer st = new StringTokenizer(res.toString(), " ");
    String lp = "";
    int z = 0;
    while(st.hasMoreTokens() && z < size) {
      qtimes[qu] += qtime;
      final String p = st.nextToken();
      if(!st.hasMoreTokens()) break;
      final String s = st.nextToken();
      if(!st.hasMoreTokens()) break;
      String uri = st.nextToken();
      //while(uri.indexOf(".xml") == -1) uri = st.nextToken();
      uri = uri.substring(uri.lastIndexOf('/') + 1);
      final String tmp = uri + ";" + p;
      if(!lp.equals(tmp)) {
        final Str str = Str.get(token(uri + ";" + p));
        str.score(Double.parseDouble(s));
        sq.add(str);
        lp = tmp;
      }
      z++;
    }

    Main.outln("Query % on %: %", qu + 1, databases.get(db), qtime);
    return sq;
  }

  /**
   * Adds the contents of an iterator in descending order of the score values.
   * @param it1 entry to be added
   * @param it2 entry to be added
   * @return SeqIter with all values
   */
  private SeqIter addSortedServer(final SeqIter it1, final SeqIter it2) {
    if(it1 == null && it2 != null) return it2;
    if(it2 == null && it1 != null) return it1;

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
          if(ca == 'u') {
            final String[] s = new String[args.length - 1];
            System.arraycopy(args, 1, s, 0, s.length);
            updateTimes(s);
            return false;
          } else if(ca == 'x') {
            convertTopics();
            return false;
          }

          ok = false;
        }
      }
      session = new ClientSession(ctx);
      session.execute(new Set(Prop.INFO, true));
    } catch(final Exception ex) {
      ok = false;
      Main.errln("Please run BaseXServer for using server mode.");
      ex.printStackTrace();
    }

    if(!ok) {
      Main.outln("Usage: " + Main.name(this) + " [options]" + NL +
        "  -u[...] update submission times" + NL +
        "  -x      convert queries");
    }
    return ok;
  }

  /**
   * Creates and prints the submission file.
   * @throws Exception Exception
   */
  private void openSubFile() throws Exception {
    sub = new PrintOutput(SUBMISSION);

    xml = new XMLSerializer(sub, false, true);
    xml.doctype(token("efficiency-submission"),
        token("\"efficiency-submission.dtd\""), null);

    // print header in output file
    xml.openElement(token("efficiency-submission"),
        token("participant-id"), token("304"),
        token("run-id"), token("1111111"),
        token("task"), token(TASK[0]),
        token("type"), token(TYPE[0]),
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
    xml.text(token("The client/server architecture of BaseX 5.72 was used " +
        "to perform the tests. The test machine has an Intel Xeon E5345 " +
        "with 2 Quad-Core CPUs and 32 GB RAM."));
    xml.closeElement();
    xml.openElement(token("ranking_description"));
    xml.text(token("We are using both content-based as well as " +
        "structural-based ranking. At first, a content-based weight " +
        "is estimated and later refined for each location step. " +
        "The weights are derived from database meta information."));
    xml.closeElement();
    xml.openElement(token("indexing_description"));
    xml.text(token("The full-text indexes of BaseX support both an " +
        "sped up evaluation of simple ftcontains operators as well " +
        "as advanced features of the upcoming XQFT Recommendation. " +
        "The indexes contain token positions and pointers on the text nodes. " +
        "Structural information, such as location paths to the text nodes, " +
        "are evaluated at runtime. As a consequence, our performance " +
        "measurements include the total time both for accessing the indexes " +
        "as well as traversing the inverted specified location paths."));
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
  private void closeSubFile() throws Exception {
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

    final BufferedReader br =
      new BufferedReader(new FileReader(SUBMISSION));
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

  /*
   * Reads and backups all queries from the topics file.
   * @throws Exception FileNotFoundException
  private void readQueries() throws Exception {
    final File file = new File("co.que");
    if(!file.exists()) {
      BaseX.outln("Could not read \"" + file.getAbsolutePath() + "\"");
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
      BaseX.outln(topicid + ";" + contentid + ";" + q);
    }
    br.close();
  }
   */

  /*
   * Replaces dedicated nodes by an or expression.
   * [(a|b) ftcontains "c"] => [a ftcontains "c" or [b ftcontains "c"]
   * @param str Sting query to be replaced
   * @return replaced query String
  private static String replaceElements(final String str) {
    byte[] b = token(str);
//    final byte[] or = new byte[]{' ', 'o', 'r', ' '};
    final byte[] co = new byte[]{' ', '|', ' '};
    final byte[] txt = new byte[]{'/', 't', 'e', 'x', 't', '(', ')'};
    int i = 0;
    int sp = -1, sb = -1, sbb = -1, os = -1, ebb = -1;
    byte[] path = new byte[]{};
    TokenList tl;
    while(i < b.length) {
      switch(b[i]) {
        case '/':
          sp = sp == -1 ? i : sp;
          break;
        case '(':
          sb = i;
          if(b[i + 1] == ')' || b[i + 1] == ' ' || b[i + 1] == '.') break;
          i++;
//          if(b[i] == ')' || b[i] == ' ' || b[i] == '.') break;
          boolean f = false;
          while(i < b.length && b[i] != ')' && !f) {
            f = b[i] == '"' || b[i] == '\'';
            i++;
          }
          if(f) break;
          i = sb;

          path = new byte[i - sp];
          System.arraycopy(b, sp, path, 0, path.length);

          tl = new TokenList();
          os = i + 1;
          while(i < b.length && b[i] != ')') {
            if(b[i] == '|') {
              final byte[] tok = new byte[i - os];
              System.arraycopy(b, os, tok, 0, tok.length);
              tl.add(tok);
              os = i + 1;
            }
            i++;
          }
          if(tl.size() > 0) {
            final byte[] tok = new byte[i - os];
            System.arraycopy(b, os, tok, 0, tok.length);
            tl.add(tok);

            // backup path
            path = new byte[sb - sp];
            System.arraycopy(b, sp, path, 0, path.length);
          }
          i++;

          // check if predicate before or after ( | )
          if(sbb > -1 && ebb > -1) {
            // predicate before ( | )
            os = 0;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for(int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, ebb + 1);
              bn = Array.add(bn, b, ebb + 1, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              if(k < tl.size() - 1)
                bn = Array.add(bn, co, 0, co.length);
            }
            i = bn.length;
            b = bn;
          } else if(sbb == -1 && ebb == -1) {
            // predicate after ( | )
            sbb = i;
            while(sbb < b.length && b[sbb] != '[') sbb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            for(int k = 0; k < tok.length; k++) {
              bn = Array.add(bn, b, 0, sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, sbb, b.length);
              if(k < tok.length - 1)
                bn = Array.add(bn, co, 0, co.length);
            }
            i = sb;
            b = bn;
          } else {
            // ( | ) inside predicate
            ebb = i + 1;
            while(ebb < b.length && !(b[ebb] == ']' ||
                ebb + 2 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'o'
                  && b[ebb + 2] == 'r' ||
                ebb + 3 < b.length && b[ebb] == ' ' && b[ebb + 1] == 'a'
                  && b[ebb + 2] == 'n' && b[ebb + 3] == 'd'
                  )) ebb++;
            byte[] bn = new byte[]{};
            final byte[][] tok = tl.finish();
            bn = Array.add(bn, b, 0, sbb);
            for(int k = 0; k < tl.size(); k++) {
              bn = Array.add(bn, b, sbb + (k == 0 ? 0 : 1), sb);
              bn = Array.add(bn, tok[k], 0, tok[k].length);
              bn = Array.add(bn, b, i, ebb);
              if(k < tok.length - 1)
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
          if(ebb > 0 && i + 1 < b.length && b[i + 1] == 'r') {
            sp = -1;
            sb = -1;
            sbb = -1;
            os = -1;
            ebb = -1;
          }
          break;
        case '.':
          if(b[i + 1] != '/') {
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
            if(b.length - 6 > 0 && b[i - 6] == 't' && b[i - 5] == 'e'
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
   */

  /*
   * Sum up processing times.
  private void sum() {
    final int size = 10; //databases.size();
    final double[] sum = new double[queries.size()];
    for(int i = 0; i < size; i++) {
      for(int j = 0; j < sum.length; j++) {
        sum[j] = qt[i * sum.length + j];
      }
    }
    for(int j = 0; j < sum.length; j++) {
      BaseX.outln("query" + j + ":" + sum[j]);
    }
  }
   */
}
