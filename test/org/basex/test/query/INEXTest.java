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
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import static org.basex.util.Token.*;

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
    PrintOutput sub = new PrintOutput("INEX/sub.xml");
    XMLSerializer xml = new XMLSerializer(sub, false, true);
    
    final File file = new File("INEX/co1.que");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    Prop.serialize = true;
    Prop.info = true;

    if (s) {
      // print header in output file
      xml.openElement(token("efficiency-submission"), 
          token("participant-id"), token("1111111"),
          token("run-id"), token("1111111"),
          token("taks"), token(task[0]),
          token("type"), token(type[0]),
          token("query"), token("automatic"),
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
    while((line = br.readLine()) != null) {
      int s0 = line.indexOf('"');
      int s1 = line.indexOf('"', s0 + 1);
      final int tid = Integer.parseInt(line.substring(s0 + 1, s1));
      s0 = line.indexOf('"', s1 + 1);
      s1 = line.indexOf('"', s0 + 1);
      
      s0 = line.indexOf('/', s1);
      String q = line.substring(s0);

      // [SG] simple query rewritings to fit queries to our index model
      // ...some more could be added here, e.g. for (a|b)
      q = q.replaceAll("\\. ", ".//text() ");
      
      // [SG] [...] basex function [...] yes, that's completely ok for the
      //   first tests. If we discover that index storage will be advantageous.
      //   we can still work on this later.
      q = xqm + "for $i score $s in " + q + " return (basex:sum-path($i), $s)";
      
      // process query
      final Process proc = new XQuery(q);

      if(!proc.execute(context)) {
        System.out.println("- " + proc.info());
        System.out.println("Query: " + q);
      } else {
        // extract and print processing time
        final String info = proc.info();

        // [SG] Total Time will only be available after calling proc.output().
        //   Currently, Parsing time is extracted here (i = -1..)
        int i = info.indexOf("Total Time: ");
        int j = info.indexOf(" ms", i);
        String time = info.substring(i + "Total Time: ".length() + 2, j);
      
        if (s) {
          xml.openElement(token("topic"),
              token("topic-id"), token(tid),
              token("total_time_ms"), token(time)
          );
          xml.openElement(token("result"));
          xml.openElement(token("file"));
          xml.text(token(file.toString()));
          xml.closeElement();

          Result val = proc.result();
          if (val instanceof SeqIter) {
            SeqIter itr = (SeqIter) val;
            Item a;
            int r = 1;
            while ((a = itr.next()) != null) {
              if (a instanceof Str) {
                xml.openElement(token("path"));
                xml.text(a.str());
                xml.closeElement();
                xml.openElement(token("rank"));
                xml.text(token(r++));
                xml.closeElement();
              } else if (a instanceof Dbl) { 
                xml.openElement(token("rsv"));
                xml.text(a.str());
                xml.closeElement();
              }
            }
          }
          xml.closeElement();
          xml.closeElement();
          // [SG] ..to see the results in the output file
          //   before the code has completely been processed..
          sub.flush();
        }
      }

      if(++curr >= STOPAFTER) break;
    }
    br.close();

    if (s) {
      xml.closeElement();
    }

    out.close();
    xml.close();
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
