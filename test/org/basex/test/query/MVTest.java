package org.basex.test.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Open;
import org.basex.core.proc.XPathMV;
import org.basex.io.NullOutput;

/**
 * MedioVis Performance Test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class MVTest {
  /** Database Context. */
  private final Context context = new Context();
  /** Maximum hits. */
  private static final int MAX = 1000;
  /** Maximum subhits. */
  private static final int SUB = 10;
  
  /**
   * Constructor.
   * @param db database instance
   * @param mm main memory mode
   * @throws Exception exception
   */
  private MVTest(final String db, final boolean mm) throws Exception {
    // toggle main memory mode
    Prop.mainmem = mm; // false
    
    new Open(db).execute(context, null);

    // open query file
    final File file = new File("tests/queries.mv");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    Prop.serialize = true;
    
    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    final InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-15");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    while((line = br.readLine()) != null) {
      final String[] split = line.split(" ");

      // convert query to XPath
      final StringBuilder query = new StringBuilder("\"/descendant::MEDIUM");
      for(int s = 0; s < split.length; s++) {
        String type = "node()/text()";
        String op = "contains";
        String val = split[s];
        
        if(val.startsWith("<")) {
          type = val.substring(1);
          if(type.equals("TYP")) op = "=";

          if(type.equals("YEA")) {
            val = split[++s];
            final int i = val.indexOf("-");
            final int j = val.indexOf(">", i);
            int y1 = Integer.parseInt(val.substring(0, i));
            final int y2 = Integer.parseInt(val.substring(i + 1, j));
            if(y2 - y1 < 20) {
              final StringBuilder sb = new StringBuilder();
              while(y1 <= y2) sb.append("YEA = '" + y1++ + "' or ");
              val = sb.toString().substring(0, sb.length() - 4);
            } else {
              val = "YEA >= " + y1 + " and YEA <= " + y2;
            }
          } else {
            val = "";
            do {
              val += split[++s] + " ";
            } while(!split[s].endsWith(">"));
            val = val.substring(0, val.length() - 2);
            val = type + " " + op + " \"" + val + "\"";
          }
        } else {
          val = type + " " + op + " \"" + val + "\"";
        }
        query.append("[" + val + "]");
      }
      query.append("[position() <= " + MAX + "]\"");

      // process query
      final Process proc = new XPathMV(Integer.toString(MAX),
          Integer.toString(SUB), query.toString());

      if(!proc.execute(context)) {
        System.out.println("ERR\t" + query);
      } else {
        // run serialization
        proc.output(new NullOutput());
        
        // extract and print processing time
        final String info = proc.info();
        int i = info.indexOf("Total Time: ");
        int j = info.indexOf(" ms", i);
        String time = info.substring(i + "Total Time: ".length(), j);
        time = time.replace('.', ',');
        i = info.indexOf("Results   : ");
        j = info.indexOf(" Node", i);
        final String nodes = info.substring(i + "Results   : ".length(), j);
        
        System.out.println(time + "\t" + nodes + "\t" + query);
      }
      //if(ii++ > 20) break;
    }
    br.close();
  }
  //int ii = 0;
  
  /**
   * Main test method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    if(args.length != 2) {
      System.out.println("MVTest db mm");
      System.out.println("- db: database instance");
      System.out.println("- mm: 'on'/'off' (main memory)");
      return;
    }
    new MVTest(args[0], args[1].equals("on"));
  }
}

