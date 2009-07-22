package org.basex.test.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.data.Result;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;

/**
 * INEX Performance Test.
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
  private String[] task = new String[] {"adhoc", "budget10", "budget100", 
      "budget1000", "budget10000"};
  /** Kind of type. */
  private String[] type = new String[] {"focused", "thorough", "article"};
  /** Kind of query. */
//  private String[] query = new String[] {"automatic", "manual"};
  /** Method used to sum pathes. */
  private String xqm = 
    "declare namespace basex = \"http://www.basex.com\"; " +  
    "declare function basex:sum-path ( $n as node()? )  as xs:string { " + 
    " string-join( for $a in $n/ancestor-or-self::* " + 
    " let $ssn := $a/../*[name() = name($a)] " +
    " return concat(name($a),'[',basex:index-of($ssn,$a),']'), '/')};" +  
    "declare function basex:index-of (" + 
    " $n as node()* , $ntf as node() )  as xs:integer* { " +
     "  for $s in (1 to count($n)) return $s[$n[$s] is $ntf]} ;";

  /**
   * Constructor.
   * @param db database instance
   * @throws Exception exception
   */
  private INEXTest(final String db) throws Exception {
    new Open(db).execute(context, null);

    // open query file
    BufferedWriter out =
      new BufferedWriter(new FileWriter(new File("INEX/INEX1.log")));
    BufferedWriter sub =
      new BufferedWriter(new FileWriter(new File("INEX/sub")));
    
    //final File file = new File("etc/xml/mv.txt");
    final File file = new File("INEX/co1.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    Prop.serialize = true;
    Prop.info = true;

    if (s) {
      // print header in output file
      sub.write("<efficiency-submission ");
      sub.write("participant-id=\"1111111\" ");
      sub.write("run-id=\"1111111\" ");
      sub.write("taks=\"" + task[0] + "\" ");
      sub.write("type=\"" + type[0] + "\" ");
      sub.write("query=\"automatic\" ");
      sub.write("sequential=\"yes\"");
      sub.write(">");
      sub.newLine();
      sub.write("<topic-fields ");
      sub.write("co_title=" + "\"no\" ");
      sub.write("cas_title=" + "\"no\" ");
      sub.write("xpath_title=" + "\"yes\" ");
      sub.write("text_predicates=" + "\"no\" ");
      sub.write("description=" + "\"no\" ");
      sub.write("narrative=" + "\"no\" ");
      sub.write("/>");
      sub.newLine();
      sub.write("<general_description>");
      sub.newLine();
      sub.write("</general_description>");
      sub.newLine();
      sub.write("<ranking_description>");
      sub.newLine();
      sub.write("</ranking_description>");
      sub.newLine();
      sub.write("<indexing_description>");
      sub.newLine();
      sub.write("</indexing_description>");
      sub.newLine();
      sub.write("<caching_description>");
      sub.newLine();
      sub.write("</caching_description>");
      sub.newLine();
    }
    
    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    //final InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-15");
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    while((line = br.readLine()) != null) {
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      final int tid = Integer.parseInt(line.substring(s0 + 1, s1));
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
//      final int ctno = Integer.parseInt(line.substring(s0 + 1, s1));
      
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);
      // [CG] I think it's faster to provide a basex function instead 
      // of using an xquery function to sum up the path - or to store 
      // it in the index (fastest?)
      q = xqm + "for $i score $s in " + q + " return (basex:sum-path($i), $s)";
      
      // process query
      final Process proc = new XQuery(q);

      if(!proc.execute(context)) {
        System.out.println("- " + proc.info());
        System.out.println("Query: " + q);
      } else {
        // extract and print processing time
        final String info = proc.info();

        int i = info.indexOf("Total Time: ");
        int j = info.indexOf(" ms", i);
        String time = info.substring(i + "Total Time: ".length() + 2, j);
      
        if (s) {
          sub.write("<topic ");
          sub.write("topic-id=\"" + tid + "\" ");
          sub.write("total_time_ms=\"" + time + "\" ");
          sub.write(">");
          sub.newLine();
          sub.write("<result>");
          sub.newLine();
          sub.write("<file>" + file + "</file>");
          sub.newLine();
          Result val = proc.result();
          if (val instanceof SeqIter) {
            SeqIter itr = (SeqIter) val;
            System.out.println(itr.size());
            Item a;
            int r = 1;
            while ((a = itr.next()) != null) {
              if (a instanceof Str) {
                sub.write("<path>" + a + "</path>");
                sub.newLine();
                sub.write("<rank>" + r++ + "</rank>");
                sub.newLine();
              } else if (a instanceof Dbl) { 
                sub.write("<rsv>" + ((Dbl) a).dbl() + "</rsv>");
                sub.newLine();
              }
            }
          }
          sub.write("</result>");
          sub.newLine();
          sub.write("</topic>");
          sub.newLine();
        }
      }

      if(++curr >= STOPAFTER) break;
    }
    out.flush();
    out.close();
    br.close();
    if (s) {
      sub.write("</efficiency-submission>");
      sub.flush();
      sub.close();

    }
  }

  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new INEXTest(args.length == 1 ? args[0] : "pages999");
  }
}

