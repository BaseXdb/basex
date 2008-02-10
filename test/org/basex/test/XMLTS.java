package org.basex.test;

import java.io.File;

import org.basex.BaseX;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Check;
import org.basex.core.proc.Proc;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.xpath.XPathProcessor;
import org.basex.util.TokenBuilder;

/**
 * XML Conformance Test Suite Wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XMLTS {
  /** Path to the XQuery Test Suite. */
  private static  final String FILE =
    "/home/dbis/xml/xmlts/ibm/ibm_oasis_not-wf.xml";
  //private static  final String FILE = "h:/xmlts/ibm/ibm_oasis_not-wf.xml";
  /** Path to the XQuery Test Suite. */
  private static  final String PATH = FILE.replaceAll("[^/]+$", "");
  /** Verbose flag. */
  private boolean verbose = false;
  /** Data reference. */
  private Data data;

  /**
   * Main method of the test class.
   * @param args command line arguments (ignored)
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
        BaseX.outln("\nXML Conformance Tests\n -v verbose output");
        return;
      }
    }

    Prop.read();
    Prop.textindex = false;
    Prop.attrindex = false;
    Prop.onthefly = true;
    Prop.mainmem = true;

    data = Check.check(FILE);
    final Context ctx = new Context();

    int ok = 0;
    int wrong = 0;

    final Nodes root = new Nodes(0, data);
    BaseX.outln("\nXML Conformance Tests\n");
    BaseX.outln("file = (expected result) -> BaseX result");

    for(final int t : nodes("//TEST", root).pre) {
      final Nodes srcRoot = new Nodes(t, data);
      final String uri = text("@URI", srcRoot);
      final boolean valid = text("@TYPE", srcRoot).equals("valid");

      final Proc proc = Proc.get(ctx, Commands.CREATEXML, PATH + uri);
      final boolean success = proc.execute();
      final boolean correct = valid == success;

      BaseX.out(uri + " = " + (valid ? "correct" : "wrong") + " -> ");
      BaseX.out(success ? "correct" : "wrong");
      BaseX.outln(correct ? " (OK)" : " (WRONG)");
      if(verbose) BaseX.outln(proc.info() + "\n");

      if(correct) ok++;
      else wrong++;
    }

    BaseX.outln("\nResult of Test \"" + new File(FILE).getName() + "\":");
    BaseX.outln("Successful: " + ok);
    BaseX.outln("Wrong: " + wrong);
  }

  /**
   * Returns the resulting query nodes.
   * Adds a "ts:" prefix due to the missing XPath namespaces support.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private Nodes nodes(final String qu, final Nodes root) throws Exception {
    final Nodes n = new XPathProcessor(qu).queryNodes(root);
    return n.size != 0 || qu.startsWith("@") ? n :
      new XPathProcessor("ts:" + qu).queryNodes(root);
  }

  /**
   * Returns the resulting query text (text node or attribute value).
   * Adds a "ts:" prefix due to the missing XPath namespaces support.
   * @param qu query
   * @param root root node
   * @return attribute value
   * @throws Exception exception
   */
  private String text(final String qu, final Nodes root) throws Exception {
    final Nodes n = nodes(qu, root);
    final TokenBuilder sb = new TokenBuilder();
    for(int i = 0; i < n.size; i++) {
      if(i != 0) sb.add("/");
      sb.add(data.atom(n.pre[i]));
    }
    return sb.toString();
  }
}
