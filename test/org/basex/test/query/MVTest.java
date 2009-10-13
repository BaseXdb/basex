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
import org.basex.core.proc.XQueryMV;
import org.basex.io.NullOutput;

/**
 * MedioVis performance test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class MVTest {
  /** Database Context. */
  private final Context context = new Context();
  /** Maximum hits. */
  private static final int MAX = 1000;
  /** Maximum subhits. */
  private static final int SUB = 10;
  /** Stop after the specified number of queries. */
  private static final int STOPAFTER = Integer.MAX_VALUE;
  /** Query counter. */
  int curr;

  /**
   * Constructor.
   * @param db database instance
   * @throws Exception exception
   */
  private MVTest(final String db) throws Exception {
    new Open(db).execute(context);

    // open query file
    final BufferedWriter out =
      new BufferedWriter(new FileWriter(new File("mv1.log")));
    //final File file = new File("etc/xml/mv.txt");
    final File file = new File("mv.txt");
    if(!file.exists()) {
      System.out.println("Could not read \"" + file.getAbsolutePath() + "\"");
      return;
    }
    context.prop.set(Prop.INFO, true);

    // scan all queries
    final FileInputStream fis = new FileInputStream(file);
    //final InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-15");
    final InputStreamReader isr = new InputStreamReader(fis, "UTF8");
    final BufferedReader br = new BufferedReader(isr);
    String line = null;
    while((line = br.readLine()) != null) {
      final String[] split = line.split(" ");

      // convert query to XPath
      final StringBuilder query = new StringBuilder("/descendant::MEDIUM");
      for(int s = 0; s < split.length; s++) {
        String type = "node()/text()";
        String op = "ftcontains";
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
      query.append("[position() <= " + MAX + "]");

      // process query
      final Process proc = new XQueryMV(Integer.toString(MAX),
          Integer.toString(SUB), query.toString());

      if(!proc.execute(context)) {
        System.out.println("- " + proc.info());
        System.out.println("Query: " + query);
      } else {
        // run serialization
        proc.output(new NullOutput());

        // extract and print processing time
        final String info = proc.info();

        int i = info.indexOf("Total Time: ");
        int j = info.indexOf(" ms", i);
        final String time = info.substring(i + "Total Time: ".length(), j);
        //time = time.replace('.', ',');
        i = info.indexOf("Results   : ");
        j = info.indexOf(" Item", i);
        final String nodes = info.substring(i + "Results   : ".length(), j);

        System.out.println(time + "\t" + nodes + "\t" + query);
        out.write(nodes + "\t" + query);
        out.newLine();
      }
      if(++curr >= STOPAFTER) break;
    }
    br.close();
  }

  /**
   * Main test method.
   * @param args command-line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new MVTest(args.length == 1 ? args[0] : "mediothek");
  }
}

