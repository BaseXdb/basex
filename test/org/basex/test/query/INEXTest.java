package org.basex.test.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Iterator;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.util.Array;
import org.basex.util.StringList;
import org.basex.util.TokenList;
import static org.basex.util.Token.*;

/**
 * INEX performance test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class INEXTest {
  /** Database Context. */
  private final Context context = new Context();
  /** Stop after the specified number of queries. */
  private static final int STOPAFTER = Integer.MAX_VALUE;
  /** Query counter. */
  int curr = 0;
  /** Flag for short output. */
  private final boolean s = true;
  /** Kind of task. */
  private final String[] task = new String[] {"adhoc", "budget10", "budget100",
      "budget1000", "budget10000"};
  /** Kind of type. */
  private final String[] type = new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
  private String[] query = new String[] {"automatic", "manual"};
  /** Container for initial NodeSets.*/
  private Nodes[] initNodes;
  /* [SG] comment added (not used)
   * Container for data references.
  private Data[] initData;
  */
  /** Results of the queries. */
  private SeqIter[] results;
  /** Querytimes. */
  private double[] times;
  /** Number of queries. */
  private static int numq = 107;
  /** String of the queries. */
  private String[] queries; 
  /** Safe the databases names. */
  private String[] databases;
  /** Topic ids of the queries. */
  private int[] topicid;
  /** Content ids of the queries. */
  private int[] contentid;
 
  /** Method used to sum up paths. */
  private final String xqm =
    "declare namespace basex = \"http://www.basex.com\"; " +
    "declare function basex:sum-path ( $n as node()? )  as xs:string { " +
    " string-join( for $a in $n/ancestor-or-self::* " +
    " let $ssn := $a/../*[name() = name($a)] " +
    " return concat(name($a),'[',basex:index-of($ssn,$a),']'), '/')};" +
    "declare function basex:index-of (" +
    " $n as node()* , $ntf as node() )  as xs:integer* { " +
     "  for $s in (1 to count($n)) return $s[$n[$s] is $ntf]};";

  /**
   * Constructor.
   * @param d database instance
   * @param nq number of queries 
   * @param in boolean flag for index creation
   * @throws Exception exception
   */
  private INEXTest(final String d, final int nq, 
      final boolean in) throws Exception {
    numq = nq;
    queries = new String[numq];
    topicid = new int[numq];
    contentid = new int[numq];
    results = new SeqIter[numq];
    times = new double[numq];
    
    BufferedWriter out = null;
    PrintOutput sub = null;
    XMLSerializer xml = null;

    if (d != null) {
      out = new BufferedWriter(
          new FileWriter(new File("INEX/log/" + d + ".log")));
      sub = new PrintOutput("INEX/sub/" + d + ".xml");
      xml = new XMLSerializer(sub, false, true);
      xml.openElement(token("efficiency-submission"));
      test(sub, xml, d, true, in);
      xml.closeElement();
      xml.close();
      sub.close();
      out.close();
    } else {
//      openDBs();      
//      readQueries();
//      queryDBs();      
      
//      readDBs();
//      readQueries();
//      queryAll();
//      createSubFile();
//      printResults();

      final int end = readDBs();
      readQueries();
      int start = 0;
      int step = 10;
      while (start + step < end) {
        System.out.println("start:" + start);
        final int l = start + step;
        openDBs(start, l);
        queryDBs(start, l);
        closeDBs(start, l);
        start += step;
        System.out.println("end:" + start);
      }
      openDBs(start, end);
      queryDBs(start, end);
      closeDBs(start, end);
      createSubFile();
      printResults();
      
      /*final StringList dbs = List.list(context);
      final Iterator<String> i = dbs.iterator();
      while(i.hasNext()) {
        final String db = i.next();
        if (db.startsWith("pages")) {
          System.out.println(db);
          out = new BufferedWriter(
              new FileWriter(new File("INEX/log/" + db + ".log")));
          sub = new PrintOutput("INEX/sub/" + db + ".xml");
          xml = new XMLSerializer(sub, false, true);
          xml.openElement(token("efficiency-submission"));
          test(sub, xml, db, exe, in);
          xml.closeElement();
          xml.close();
          sub.close();
          out.close();
        }
      }
      */
    }
  }

  
  
  /** 
   * Open all databases and backup content nodes.
   **/
/*  private void openDBs() {
    final StringList dbs = List.list(context);
    final Iterator<String> itr = dbs.iterator();
    initNodes = new Nodes[dbs.size()];
    initData = new Data[dbs.size()];
    int c = 0;
    while (itr.hasNext()) {
      final String db = itr.next();
      if (db.startsWith("pages")) {
        new Open(db).execute(context);
        initData[c] = context.data();
        initNodes[c++] = context.current();
//        new Close().execute(context);
        System.out.println(db);
      }            
    }    
    initNodes = Array.finish(initNodes, c);
    initData = Array.finish(initData, c);
//    context.closeDB();
  }
*/
  
  
  /** 
   * Open databases and backup content nodes.
   * @param f int first database to open
   * @param l int last database to open
   * @return last database opened
   */
  private int openDBs(final int f, final int l) {
    final int la = (l == -1) ? databases.length : l;
    if (la - f == 0) return 0;
    initNodes = new Nodes[la - f];
//    initData = new Data[la - f];
    int c = 0;
    for (int i = f; i < l; i++) {
      new Open(databases[i]).execute(context);
//        initData[c] = context.data();
        initNodes[c++] = context.current();
        System.out.println(databases[i]);
    }    
    return la;
  }

  /** 
   * Close databases and backup content nodes.
   * @param f int first database to close
   * @param l int last database to close
   * @return last database closed
   */
  private int closeDBs(final int f, final int l) {
    final int la = (l == -1) ? databases.length : l;
    if (la - f == 0) return 0;
    for (int i = f; i < l; i++) {
      new Close().execute(context);
      // initData[c] = context.data();
    }    
    return la;
  }



  
  /** 
   * Open all databases and backup content nodes.
   * @return number of databases found
   */
  private int readDBs() {
    final StringList dbs = List.list(context);
    final Iterator<String> itr = dbs.iterator();
    databases = new String[dbs.size()];
    int c = 0;
    while (itr.hasNext()) {
      final String db = itr.next();
      if (db.startsWith("pages")) {
        databases[c++] = db;
        System.out.println(db);
      }            
    }    
    databases = Array.finish(databases, c);
    return c;
  }

  /**
   * Query all documents.
   * @throws Exception exception
   */
/*
  private void queryAll() throws Exception {
    BufferedWriter bf = new BufferedWriter(
        new FileWriter(new File("inex.log")));

    for (int i = 0; i < queries.length; i++) {
      final String q = xqm + "for $i score $s in " 
      + queries[i] + " order by $s return (basex:sum-path($i), $s)";
      System.out.println(queries[i]);
      bf.write(queries[i]);
      bf.newLine();
      for (int z = 0; z < databases.length; z++) {
        openDB(databases[z]);
        // process query
        final Process proc = new XQuery(q);
        if(!proc.execute(context)) {
          System.out.println("- " + proc.info());
          System.out.println("Query: " + new String(q));
        } else {
          // extract and print processing time
          final String info = proc.info();
          proc.output(new NullOutput());
//          if (info.indexOf("Applying full-text index") < 0 
//              && info.indexOf("Removing path with no index results") < 0) { 
//            iu = false;
//          }
          final int k = info.indexOf("Evaluating: ");
          final int j = info.indexOf(" ms", k);
          final String time = info.substring(k
              + "Evaluating: ".length(), j).trim();
          times[i] += Double.parseDouble(time);
          final Result val = proc.result();
          
          
          if(val instanceof SeqIter) {
            final SeqIter itr = (SeqIter) val;
            if (results[i] == null) results[i] = itr; 
            else results[i].add(itr);
          }
          
        }
        new Close().execute(context);  
      }
      bf.write((results[i] != null ? results[i].size() : 0) + " " + times[i]);
      bf.newLine();
      bf.flush();
      System.out.println((results[i] != null ? results[i].size() : 0) 
      + " " + times[i]);
      
    }
    bf.close();
  }
  */
  
  /** 
   * Open all databases and backup content nodes.
   */
/*  private void openDB(final String db) {
    Process o = new Open(db);
    context.prop.set(Prop.INFO, true);
    o.execute(context);
  }
*/
  
  
  
  /**
   * Query all databases with all queries.
   * @throws Exception Exception
   */
/*  private void queryDBs() throws Exception {
    BufferedWriter bf = new BufferedWriter(
        new FileWriter(new File("inex.log")));

    for (int i = 0; i < queries.length; i++) {
      final String q = xqm + "for $i score $s in " 
      + queries[i] + " order by $s return (basex:sum-path($i), $s)";
      System.out.println(queries[i]);
      bf.write(queries[i]);
      bf.newLine();
      for (int z = 0; z < initNodes.length; z++) {
        
        context.data(initData[z]);
        context.current(initNodes[z]);
        context.prop.set(Prop.INFO, true);
        // process query
        final Process proc = new XQuery(q);
        if(!proc.execute(context)) {
          System.out.println("- " + proc.info());
          System.out.println("Query: " + new String(q));
        } else {
          // extract and print processing time
          final String info = proc.info();
          proc.output(new NullOutput());
//          if (info.indexOf("Applying full-text index") < 0 
//              && info.indexOf("Removing path with no index results") < 0) { 
//            iu = false;
//          }
          final int k = info.indexOf("Evaluating: ");
          final int j = info.indexOf(" ms", k);
          final String time = info.substring(k
              + "Evaluating: ".length(), j).trim();
          times[i] += Double.parseDouble(time);
          final Result val = proc.result();

          if(val instanceof SeqIter) {
            final SeqIter itr = (SeqIter) val;
            if (results[i] == null) results[i] = itr; 
            else results[i].add(itr);
//            Item a;
//            int r = 1;
//            while((a = itr.next()) != null) {
//              if(a instanceof Str) {
//                  a.str();
//                } else if(a instanceof Dbl) {
//                  a.str();
//                }
//              }
          }
        }
      }
      bf.write((results[i] != null ? results[i].size() : 0) + " " + times[i]);
      bf.newLine();
      bf.flush();
      System.out.println((results[i] != null ? 
      results[i].size() : 0) + " " + times[i]);
    }
    bf.close();
  }
  */
  
  
  /**
   * Query all databases with all queries.
   * @param st first database to query
   * @param l last database to query
   * @throws Exception Exception
   */
  private void queryDBs(final int st, final int l) throws Exception {
    // [SG] dummy line to suppress warning
    if(l != l);
    
    BufferedWriter bf = new BufferedWriter(
        new FileWriter(new File("inex.log")));

    for (int i = 0; i < queries.length; i++) {
      final String q = xqm + "for $i score $s in " 
      + queries[i] + " order by $s return (basex:sum-path($i), $s)";
      System.out.println(queries[i]);
      bf.write(queries[i]);
      bf.newLine();
      for (int z = 0; z < initNodes.length; z++) {        
//        context.data(initData[z]);
        context.current(initNodes[z]);
        context.prop.set(Prop.INFO, true);
        // process query
        final Process proc = new XQuery(q);
        if(!proc.execute(context)) {
          System.out.println("- " + proc.info());
          System.out.println("Query: " + new String(q));
        } else {
          // extract and print processing time
          final String info = proc.info();
          proc.output(new NullOutput());
//          if (info.indexOf("Applying full-text index") < 0 
//              && info.indexOf("Removing path with no index results") < 0) { 
//            iu = false;
//          }
          final int k = info.indexOf("Evaluating: ");
          final int j = info.indexOf(" ms", k);
          final String time = info.substring(k
              + "Evaluating: ".length(), j).trim();
          times[st + i] += Double.parseDouble(time);
          final Result val = proc.result();

          if(val instanceof SeqIter) {
            final SeqIter itr = (SeqIter) val;
            if (results[st + i] == null) results[st + i] = itr; 
            else results[st + i].add(itr);
//            Item a;
//            int r = 1;
//            while((a = itr.next()) != null) {
//              if(a instanceof Str) {
//                  a.str();
//                } else if(a instanceof Dbl) {
//                  a.str();
//                }
//              }
          }
        }
      }
      bf.write((results[i] != null ? results[i].size() : 0) + " " + times[i]);
      bf.newLine();
      bf.flush();
      System.out.println((results[i] != null ? 
          results[i].size() : 0) + " " + times[i]);
    }
    bf.close();
  }
  

  
  /**
   * Create and print submission file.
   * @throws Exception Exception
   */
  private void createSubFile() throws Exception {
    //BufferedWriter out = new BufferedWriter(
    //    new FileWriter(new File("INEX/pages.log"))); 
    PrintOutput sub = new PrintOutput("INEX/submission.xml");
    XMLSerializer xml = new XMLSerializer(sub, false, true);
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

    for (int j = 0; j < numq; j++) {
      xml.openElement(token("topic"),
          token("topic-id"), token(topicid[j]),
          token("total_time_ms"), token(times[j])
      );
      xml.openElement(token("result"));
      xml.openElement(token("file"));
      // [SG] which file???
      xml.text(token("wikpedia"));
      xml.closeElement();
  
      Item a;
      int r = 1;
      while(results[j] != null && (a = results[j].next()) != null) {
        if(a instanceof Str) {
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
        }
      }
      xml.closeElement();
      xml.closeElement();
    }
    xml.closeElement();
  }
  
  /**
   * Print results.
   */
  private void printResults() {
    for (int i = 0; i < numq; i++) {
      System.out.println("Query " + i + " found " +  
          (results[i] != null ? results[i].size() : 0) + 
          " results, time: " + times[i] + " ms");
    }
  }
   
  /**
   * Read and backup all queries from file.
   * @throws Exception FileNotFoundException
   */
  public void readQueries() throws Exception {
    final File file = new File("INEX/co.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    int i = 0;
    while((line = br.readLine()) != null && i < numq) {
      // extract topic id
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      topicid[i] = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract content id
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
      contentid[i] = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract query
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

      q = replaceElements(q);
      queries[i++] = q;
    }
    br.close();
  }
  
  /**
   * Performs the INEXTest.
   * @param sub PrintOutput
   * @param xml XMLSerializer
   * @param db String database name
   * @param exe boolean flag for execution
   * @param in boolean flag for index creation
   * @throws Exception FileNotFoundException
   */
  private void test(final PrintOutput sub, final XMLSerializer xml,
      final String db, final boolean exe, final boolean in) throws Exception {
    new Open(db).execute(context);
    
    if (in) {
      new CreateIndex(Commands.CmdIndex.FULLTEXT.toString()).execute(context);
    }
    
    // open query file
    final File file = new File("INEX/co.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    context.prop.set(Prop.INFO, true);

    if(s) {
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

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    boolean iu = true;
    while((line = br.readLine()) != null) {
      // extract topic id
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      final int tid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract content id
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
//      final int ctid = Integer.parseInt(line.substring(s0 + 1, s1));
      // extract query
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

//      q = q.replaceAll("\\. ", ".//text() ");
      q = replaceElements(q);
      final byte[] qo = q.getBytes();
      if (exe) {
        q = xqm + "for $i score $s in " + q
          + " order by $s return (basex:sum-path($i), $s)";

//        Prop.allInfo = true;
        context.prop.set(Prop.ALLINFO, true);
        // process query
        final Process proc = new XQuery(q);

        if(!proc.execute(context)) {
          System.out.println("- " + proc.info());
          System.out.println("Query: " + new String(qo));
        } else {
          // extract and print processing time
          final String info = proc.info();
          proc.output(new NullOutput());
          if (info.indexOf("Applying full-text index") < 0
              && info.indexOf("Removing path with no index results") < 0) {
     /*       System.out.println("No index usage: " + q);
            System.out.println(q);
            System.out.println("****");
       */     iu = false;
          }
          final int i = info.indexOf("Evaluating: ");
          final int j = info.indexOf(" ms", i);
          final String time = info.substring(i
              + "Evaluating: ".length() + 2, j);

          if(s) {
            xml.openElement(token("topic"),
                token("topic-id"), token(tid),
                token("total_time_ms"), token(time)
            );
            xml.openElement(token("result"));
            xml.openElement(token("file"));
            xml.text(token(db));
            xml.closeElement();

            final Result val = proc.result();
            if(val instanceof SeqIter) {
              final SeqIter itr = (SeqIter) val;
              Item a;
              int r = 1;
              while((a = itr.next()) != null) {
                if(a instanceof Str) {
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
                }
              }
            }
            xml.closeElement();
            xml.closeElement();
            sub.flush();
          }
        }
      }

      if(++curr >= STOPAFTER) break;
    }
    if (iu) System.out.println("All queries have used the index!");
    else System.out.println("Some queries didn't use the index!");
    br.close();

    if(s) {
      xml.closeElement();
    }

/*    xml.close();
    sub.close();
    out.close();*/
  }

  /**
   * Replaces dedicated nodes by an or expression.
   * [(a|b) ftcontains "c"] => [a ftcontains "c" or [b ftcontains "c"]
   * @param str Sting query to be replaced
   * @return replaced query String
   */
  private static String replaceElements(final String str) {
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

  /**
   * Main test method.
   * @param args command-line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
   if (args.length == 0)
      new INEXTest(null, numq, false);
   else if (args.length == 1) {
     new INEXTest(null, Integer.parseInt(args[0]), false);
   } else if (args.length == 2 && args[1].startsWith("true"))
      new INEXTest(args[0], numq, true);
 /*   System.out.println("1.: " + replaceElements(
        "//sec[. ftcontains \"b\" ] or //sec[. ftcontains \"c\" ]"));
    System.out.println("2.: " + replaceElements(
        "//sec[.//ab ftcontains \"b\" ]"));
    System.out.println("3.: " + replaceElements(
        "//sec[.//(a|b) ftcontains \"b\" ]"));
    System.out.println("4.: " + replaceElements(
        "//(a|b)[. ftcontains \"b\" ]"));
    System.out.println("5.: " + replaceElements(
      "//(a|b)[.//cd ftcontains \"b\" ]"));
    System.out.println("6.: " + replaceElements(
      "//a[.//d ftcontains \"b\" ]//(e|f)"));
    System.out.println("7.: " + replaceElements(
      "//(a|b)[.//(c|d) ftcontains \"bla\"]"));
    System.out.println("8.: " + replaceElements(
      "article[. ftcontains (\"Nobel\" ftand \"prize\")]"));
    System.out.println("9.: " + replaceElements(
    "article[.//(a|b) ftcontains \"Nobel\" and" +
    ".//st ftcontains \"references\" ]"));
    System.out.println("10.: " + replaceElements(
    "//article[.//(sec|p) ftcontains (\"mean\" ftand \"average\" " +
    "ftand \"precision\") and ( .//st ftcontains (\"references\") " +
    "or .//st ftcontains (\"see\" ftand \"also\") ) and .//image " +
    "ftcontains (\"precision\" ftand \"recall\")]"));
    System.out.println("11.: " + replaceElements(
    "//*[. ftcontains (\"alchemy\") and (. ftcontains (\"Asia\") " +
    "or . ftcontains (\"Japan\" ftand \"China\" ftand \"India\"))]"));
   /* */
  }
}
