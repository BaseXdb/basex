package org.basex.test.query;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
  /** Queries. */
  private static final String QUERIES = "inex.queries";
  /** Database prefix (1000 instances: "pages", 10 instances: "inex"). */
  private static final String DBPREFIX = "index";

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
  /** PrintOutput for the submission file. */
  private static PrintOutput sub = null;
  /** XMLSerializer for the submission file. */
  private static XMLSerializer xml = null;
  /** Kind of task. */
  private static final String[] task = new String[] {"adhoc", 
    "budget10", "budget100", "budget1000", "budget10000"};
  /** Kind of type. */
  private static final String[] type = 
    new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
  private static String[] query = new String[] {"automatic", "manual"};
  /** Collection for query times. */
  private static double[] qtimes;
  /** Topic ids of the queries. */
  private static int[] tid;
  /** Content ids of the queries. */
//  private static int[] cid;
  /** Results of the queries. */
  private static SeqIter[] results;
  /** Method used to sum up paths. */
  private static final String xqm =
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
  private final StringList log = new StringList(); 

  
  /**
   * Default constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private InexDBTestNew(final String[] args) throws Exception { 
    if(!parseArguments(args)) return;
    
    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(QUERIES));
    queries = new StringList();
    final IntList tidl = new IntList();
//    final IntList cidl = new IntList();
    String l;
    while((l = br.readLine()) != null && queries.size() < maxqu) {
      int i = l.indexOf(';');
      tidl.add(Integer.parseInt(l.substring(0, i)));
      int j = l.indexOf(';', i + 1);
//      cidl.add(Integer.parseInt(l.substring(i + 1, j)));
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

//    log.add(BaseX.name(InexDBTest.class) + " [" +
//        (server ? CLIENTMODE : LOCALMODE) + "]");
    BaseX.outln(BaseX.name(InexDBTest.class) + " [" +
        (server ? CLIENTMODE : LOCALMODE) + "]");
//    log.add("=> " + queries.size() + " queries on " + databases.size() + 
//        " databases, " + runs + " runs: " + (total ? "total" : "evaluation") + " time in ms\n");
    BaseX.outln("=> % queries on % databases, % runs: % time in ms\n",
        queries.size(), databases.size(), runs, (total ? "total" : "evaluation"));

    // get number of articles for each db
    /*numArt = new int[databases.size()];
    int last = 0;
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      launcher.execute(new Open(databases.get(d)));
      final Names names = ctx.data().tags;
      numArt[d] = last;
      System.out.println(last);
      last += names.stat(names.id("article".getBytes())).counter; 
      launcher.execute(new Close());
    }
    */
    // run test
    final Performance p = new Performance();
    if(server) test();
    else testLocalNew();

//    log.add("Total Time: " + p.getTimer());
//    final String[] s = log.finish();
//    for (String si : s) System.out.println(si);
    System.out.println("Total Time: " + p.getTimer());
    
    if(subfile) {
      openSubFile();
      for (int i = 0; i < results.length; i++) 
        createQueryEntry(i, results[i], 1500);
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
   */
  private void test() throws Exception {
    // loop through all databases
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      for(int q = 0; q < queries.size(); q++) query(d, q);
      launcher.execute(new Close());
    }
  }

  /**
   * First test, caching all databases before running the queries.
   * This version runs only locally.
   * @throws Exception exception
   */
  private void testLocal() throws Exception {
    // cache all context nodes
    final Nodes[] roots = new Nodes[databases.size()];
    for(int d = 0; d < databases.size(); d++) {
      final Data data = Open.open(ctx, databases.get(d));
      roots[d] = new Nodes(data.doc(), data);
    }

    // loop through all databases
    for(int q = 0; q < queries.size(); q++) {
      // loop through all queries
      for(int d = 0; d < databases.size(); d++) {
        // set cached context nodes and run query
        ctx.current(roots[d]);
        query(d, q);
      }
    }
//    if (subfile) createSubFile();
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

    if (subfile) openSubFile();
    // loop through all databases
    for(int q = 0; q < queries.size(); q++) {
      SeqIter s = null;
      // loop through all queries
      for(int d = 0; d < databases.size(); d++) {
        // set cached context nodes and run query
        ctx.current(roots[d]);
        queryNew(d, q);
        s = s == null ? queryNew(d, q) : addSorted(s, queryNew(d, q));
      }
      if (subfile) createQueryEntry(q, s, 1500);
    }
    
    if (subfile) closeSubFile();
  }

  
  /**
   * Performs a single query.
   * @param db database offset
   * @param qu query offset
   * @throws Exception exception
   */
  private void query(final int db, final int qu) throws Exception {
    // query and cache result
    final String que = subfile ? xqm + "for $i score $s in " 
        + queries.get(qu) 
        + " order by $s descending return (basex:sum-path($i), $s)"
//        + " return $i"
        : queries.get(qu);
    final Process proc = new XQuery(que);
    if(launcher.execute(proc)) {
      launcher.output(new NullOutput());
    }
    final CachedOutput out = new CachedOutput();
    launcher.info(out);
    
    final String time = Pattern.compile(".*" +
        (total ? "Total Time" : "Evaluating") + ": (.*?) ms.*",
        Pattern.DOTALL).matcher(out.toString()).replaceAll("$1");
    if (subfile) {
      qtimes[qu] += Double.parseDouble(time);
      final Result val = proc.result();
      if(val instanceof SeqIter) {
        final SeqIter itr = (SeqIter) val;
        // update node path
        for (int i = 0; i < itr.size(); i++) {
           if(itr.item[i] instanceof Str) {
             String str = new String(((Str) itr.item[i]).str());
             if(str.startsWith("article")) {
               final int s0 = str.indexOf('[');
               final int s1 = str.indexOf(']');
               final int c = Integer.parseInt(str.substring(s0 + 1, s1)) +  
                 numArt[db];
               final Str tmp = new Str(("article[" + c + 
                   str.substring(s1)).getBytes(), false);
               itr.item[i] = tmp;  
             }
           }
        }
        if (results[qu] == null) results[qu] = itr; 
//        else results[qu].add(itr);
        else results[qu] = addSorted(results[qu], itr);        
      }
    }     
    
    // output result
//    log.add("Query " + (qu + 1) +" on " + databases.get(db) + ":" + time);
    BaseX.outln("Query % on %: %", qu + 1, databases.get(db), time);
    if(info) {
//      log.add("- " + Pattern.compile(".*Result: (.*?)\\n.*",
//          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
      BaseX.outln("- " + Pattern.compile(".*Result: (.*?)\\n.*",
          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
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
    final String que = subfile ? xqm + "for $i score $s in " 
        + queries.get(qu) 
        + " order by $s descending return (basex:sum-path($i), $s)"
//        + " return $i"
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
      final Result val = proc.result();
      if(val instanceof SeqIter) {
        itr = (SeqIter) val;
        // update node path
        for (int i = 0; i < itr.size(); i++) {
           if(itr.item[i] instanceof Str) {
             String str = new String(((Str) itr.item[i]).str());
             if(str.startsWith("article")) {
               final int s0 = str.indexOf('[');
               final int s1 = str.indexOf(']');
               final int c = Integer.parseInt(str.substring(s0 + 1, s1)) +  
                 numArt[db];
               final Str tmp = new Str(("article[" + c + 
                   str.substring(s1)).getBytes(), false);
               itr.item[i] = tmp;  
             }
           }
        }
      }
    }     
    
    // output result
    log.add("Query " + (qu + 1) +" on " + databases.get(db) + ":" + time + NL);
//    BaseX.outln("Query % on %: %", qu + 1, databases.get(db), time);
    if(info) {
      log.add("- " + Pattern.compile(".*Result: (.*?)\\n.*",
          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
//      BaseX.outln("- " + Pattern.compile(".*Result: (.*?)\\n.*",
//          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
    }
    return itr;
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
          final char c = arg.next();
          if(c == 'd') {
            maxdb = Integer.parseInt(arg.string());
          } else if(c == 'q') {
            maxqu = Integer.parseInt(arg.string());
          } else if(c == 'r') {
            runs = Integer.parseInt(arg.string());
          } else if(c == 's') {
            server = true;
          } else if(c == 't') {
            total = true;
          } else if(c == 'c') {
            subfile = true;
          } else if(c == 'v') {
            info = true;          
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
      BaseX.outln("Usage: InexDBTest [options]" + NL +
      "  -c  create submissionfile" + NL +
      "  -d<no>  maximum no/database" + NL +
      "  -q<no>  maximum no/queries" + NL +
      "  -r<no>  number of runs" + NL +
      "  -s      use server architecture" + NL +
      "  -t      measure total time" + NL + 
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
    xml.openElement(token("efficiency-submission"));
  
    // print header in output file
    xml.openElement(token("efficiency-submission"),
        token("participant-id"), token("1111111"),
        token("run-id"), token("1111111"),
        token("taks"), token(task[0]),
        token("type"), token(type[0]),
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
    xml.emptyElement(token("general_description"));
    xml.emptyElement(token("ranking_description"));
    xml.emptyElement(token("indexing_description"));
    xml.emptyElement(token("caching_description"));
  }
  /**
   * Create and print submission file.
   * @throws Exception Exception
   */
  private static void closeSubFile() throws Exception {
    xml.closeElement();
    xml.closeElement();
    xml.close();
    sub.close();
  }

  /**
   * Create subfile entry for a query result. 
   * 
   * @param q query
   * @param res result of the query
   * @param k max number of results 
   * @throws IOException IOException
   */
  private static void createQueryEntry(final int q, final SeqIter res, 
      final int k) 
  throws IOException {
    xml.openElement(token("topic"),
        token("topic-id"), token(tid[q]),
        token("total_time_ms"), token(qtimes[q])
    );

    Item a;
    int r = 1;
    while(res != null && (a = res.next()) != null) {
      if (r == k) break;
      if(a instanceof Str) {
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
      } else if(a instanceof Dbl) {
        xml.openElement(token("rsv"));
        xml.text(a.str());
        xml.closeElement();
        xml.closeElement();
      }
    }
//    xml.closeElement();
    xml.closeElement();    
  }
  

  private static void print(final int q, final SeqIter res) 
  throws IOException {
    Item a;
    int r = 1;
    while(res != null && (a = res.next()) != null) {
      if(a instanceof Str) {
        System.out.println(a.str());
        System.out.println(new String(token(r++)));
      } else if(a instanceof Dbl) {
        System.out.println(new String(a.str()));
      }
    }
  }

  
  /**
   * Create and print submission file.
   * @throws Exception Exception
  private static void createSubFile() throws Exception {
    openSubFile();
    for (int j = 0; j < queries.size(); j++) 
//      print(j, results[j]);
      createQueryEntry(j, results[j]);
    closeSubFile();
  }
   */
  
  /**
   * Replaces dedicated nodes by an or expression.
   * [(a|b) ftcontains "c"] => [a ftcontains "c" or [b ftcontains "c"]
   * @param str Sting query to be replaced
   * @return replaced query String
   */
/*  private static String replaceElements(final String str) {
    byte[] b = str.getBytes();
    final byte[] or = new byte[]{' ', 'o', 'r', ' '};
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
                bn = Array.add(bn, or, 0, or.length);
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
    return new String(b);
  }
*/
}
