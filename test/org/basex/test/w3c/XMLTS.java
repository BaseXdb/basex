package org.basex.test.w3c;

import static org.basex.core.Text.*;
import java.io.File;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryProcessor;
import org.basex.util.TokenBuilder;

/**
 * XML Conformance Test Suite wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XMLTS {
  /** Root directory. */
  private static final String ROOT = "/home/dbis/xml/xmlts/";
    // "h:/xmlts/";
  /** Path to the XQuery Test Suite. */
  private static final String FILE = ROOT +
    "oasis/oasis.xml";
    //"sun/sun-not-wf.xml";
    //"ibm/ibm_oasis_not-wf.xml";
    //"xmltest/xmltest.xml";
  /** Path to the XQuery Test Suite. */
  private static final String PATH = FILE.replaceAll("[^/]+$", "");
  /** Verbose flag. */
  private boolean verbose;
  /** Data reference. */
  private Data data;
  /** Context. */
  private Context context;

  /**
   * Main method of the test class.
   * @param args command-line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XMLTS(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private XMLTS(final String[] args) throws Exception {
    // modifying internal query arguments...
    for(final String arg : args) {
      if(arg.equals("-v")) {
        verbose = true;
      } else {
        Main.outln("\nXML Conformance Tests\n -v verbose output");
        return;
      }
    }

    context = new Context();
    context.prop.set(Prop.TEXTINDEX, false);
    context.prop.set(Prop.ATTRINDEX, false);
    //context.prop.set(MAINMEM, true);

    new CreateDB(FILE).execute(context);
    data = context.data;

    int ok = 0;
    int wrong = 0;

    final Nodes root = new Nodes(0, data);
    Main.outln("\nXML Conformance Tests\n");
    Main.outln("file = (expected result) -> " + NAME + " result");

    for(final int t : nodes("//*:TEST", root).nodes) {
      final Nodes srcRoot = new Nodes(t, data);
      final String uri = text("@URI", srcRoot);
      final boolean valid = text("@TYPE", srcRoot).equals("valid");

      context.prop.set(Prop.INTPARSE, true);
      Process proc = new CreateDB(PATH + uri);
      final boolean success = proc.execute(context);
      final boolean correct = valid == success;

      if(verbose || !correct) {
        Main.outln(uri + " = " + (valid ? "correct" : "wrong") + " -> " +
            (success ? "correct" : "wrong") + (correct ? " (OK)" : " (WRONG)"));
        if(verbose) {
          String inf = proc.info();
          if(!inf.isEmpty()) Main.outln("[BASEX ] " + inf);
          context.prop.set(Prop.INTPARSE, false);
          new Close().execute(context);
          proc = new CreateDB(PATH + uri);
          proc.execute(context);
          inf = proc.info();
          if(!inf.isEmpty()) Main.outln("[XERCES] " + inf);
        }
      }
      if(correct) ok++;
      else wrong++;

      new Close().execute(context);
    }

    Main.outln("\nResult of Test \"" + new File(FILE).getName() + "\":");
    Main.outln("Successful: " + ok);
    Main.outln("Wrong: " + wrong);
  }

  /**
   * Returns the resulting query text (text node or attribute value).
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private String text(final String qu, final Nodes root) throws Exception {
    final Nodes n = nodes(qu, root);
    final TokenBuilder sb = new TokenBuilder();
    for(int i = 0; i < n.size(); i++) {
      if(i != 0) sb.add("/");
      sb.add(data.atom(n.nodes[i]));
    }
    return sb.toString();
  }

  /**
   * Returns the resulting query nodes.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private Nodes nodes(final String qu, final Nodes root) throws Exception {
    return new QueryProcessor(qu, root, context).queryNodes();
  }
}
