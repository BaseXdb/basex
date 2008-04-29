package org.basex.test;

import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.PrintSerializer;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.query.xquery.XQueryProcessor;

/**
 * Pathfinder XQuery Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class PFTest {
  /** Open result. */
  private static final byte[] INDOC = token("doc(\"");
  /** Open result. */
  private static final byte[] RESOPEN = token("<XQueryResult");
  /** Close result. */
  private static final byte[] RESCLOSE = token("</XQueryResult>");
  /** Open result. */
  private static final byte[] RESERR = token("ERROR");
  /** Open result. */
  private static final byte[] RESNL = { '#' };
  /** Test Information. */
  private static final String PFPATH = "f:/Projects/basex/tests/pf";
  /** Test Information. */
  private static final String TESTINFO =
    "\nUsage: Test [options]" +
    "\n -h  show this help" +
    "\n -v  show query information";
  /** Verbose flag. */
  static boolean verbose;

  /** Number of found query files. */
  private int found;
  /** Number of unknown query files. */
  private int unknown;
  /** Number of found query files. */
  private int wrong;
  /** Number of unknown query files. */
  private int correct;


  /**
   * Main method of the test class.
   * @param args command line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {
    if(args.length == 1 && args[0].equals("-v")) {
      verbose = true;
    } else if(args.length > 0) {
      System.out.println(TESTINFO);
      return;
    }
    new PFTest();
  }

  /**
   * Constructor.
   * @throws IOException I/O exception
   */
  private PFTest() throws IOException {
    Prop.read();

    System.out.println("******** RUN TESTS ********\n");
    parse(new File(PFPATH));

    System.out.println("Found: " + found);
    System.out.println("Unknown: " + unknown);
    System.out.println("Correct: " + correct);
    System.out.println("Wrong: " + wrong);
  }

  /**
   * Parses the specified directory.
   * @param dir current directory
   * @throws IOException I/O exception
   */
  private void parse(final File dir) throws IOException {
    for(final File file : dir.listFiles()) {
      if(file.isDirectory()) {
        parse(file);
      } else {
        if(!file.getName().equals("All")) continue;
        scan(new IO(file));
      }
    }
  }

  /**
   * Tests the queries in the specified file.
   * @param file query file
   * @throws IOException I/O exception
   */
  private void scan(final IO file) throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(file.path()));
    final String path = file.path();
    String name;
    while((name = br.readLine()) != null) {
      test(name.replaceAll(".*\\?", ""), path);
    }
    br.close();
  }

  /**
   * Tests the queries in the specified file.
   * @param name query file
   * @param path file path
   * @throws IOException I/O exception
   */
  private void test(final String name, final String path) throws IOException {
    final IO in = new IO(path + name + ".xq");
    final boolean f = in.exists();
    if(f) found++;
    else unknown++;
    if(!f) return;

    byte[] qu = delete(read(in), token("<TSTSRCDIR>/"));
    int i = indexOf(qu, INDOC);
    if(i != -1) {
      qu = token(string(qu).replaceAll("doc\\(\\\"", "doc(\"" + path));
    }

    byte[] out = read(new IO(path + name + ".stable.out"));
    i = indexOf(out, RESOPEN);
    if(i != -1) {
      out = substring(out, i + RESOPEN.length + 1);
      i = indexOf(out, RESCLOSE);
      out = substring(out, 0, i);
    } else {
      out = null;
    }

    byte[] err = read(new IO(path + name + ".stable.err"));
    i = indexOf(err, RESERR);
    if(i != -1) {
      err = substring(err, i + RESERR.length);
      i = indexOf(err, RESNL);
      err = substring(err, 0, i);
      if(err.length == 0) err = null;
    } else {
      err = null;
    }

    byte[] result = null;
    final CachedOutput o = new CachedOutput();
    try {
      final XQueryProcessor query = new XQueryProcessor(string(qu));
      query.query(null).serialize(new PrintSerializer(o));
      result = o.finish();
      result = replace(replace(result, 0x0D, 0x20), 0x0A, 0x20);
    } catch(final Exception ex) {
      if(out == null) {
        if(err != null) {
          System.out.println("Crrct: " + name);
          correct++;
        } else {
          System.out.println("*** Wrong? " + name);
          wrong++;
        }
      } else {
        System.out.println("*** Wrong. " + name);
        wrong++;
      }
      final byte[] msg = delete(delete(token(ex.getMessage()),
          0x0A), 0x0D);
      System.out.println("BaseX: " + string(msg));
    }

    if(result != null) {
      if(out != null) {
        if(!eq(result, out)) {
          System.out.println("*** Wrong: " + name);
          wrong++;
        } else {
          System.out.println("Crrct: " + name);
          correct++;
        }
      } else {
        if(err != null) {
          System.out.println("Crrct: " + name);
          correct++;
        } else {
          System.out.println("??? Wrong? " + name);
          wrong++;
        }
      }
    }

    System.out.println("Query: " + string(qu));
    if(out != null) System.out.println("PathF: " + string(out));
    if(err != null) System.out.println("PathF: " + string(err));
    if(result != null) System.out.println("BaseX: " + string(result));
    System.out.println();
  }

  /**
   * Reads in the specified file.
   * @param file file to be read
   * @return content
   * @throws IOException I/O exception
   */
  private byte[] read(final IO file) throws IOException {
    final byte[] qu = file.content();
    return replace(replace(qu, 0x0A, ' '), 0x0D, ' ');
  }
}
