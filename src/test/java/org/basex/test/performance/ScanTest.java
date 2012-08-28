package org.basex.test.performance;

import java.io.*;
import java.util.*;

import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class benchmarks simple table scans.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ScanTest extends SandboxTest {
  /** Number of elements to be created. */
  private static final int ELEMENTS = 1000000;
  /** Number of loops. */
  private static final int LOOPS = 3;

  /**
   * Initializes the test database.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void initDB() throws IOException {
    /* generate test file. example:
     * <XML>
     *   <SUB>ndjkeibjmfeg</SUB>
     *   <SUB>ppoapcefcf</SUB>
     * </XML>
     */
    final IOFile dbfile = new IOFile(sandbox(), NAME);
    final BufferOutput bo = new BufferOutput(dbfile.path());
    final int max = 16;
    final byte[] cache = new byte[max];
    // use constant seed to create same test document every time
    final Random rnd = new Random(0);
    try {
      bo.write(Token.token("<XML>"));
      final byte[] start = Token.token("<SUB>");
      final byte[] end = Token.token("</SUB>");
      for(int e = 0; e < ELEMENTS; e++) {
        bo.write(start);
        final int rl = rnd.nextInt(max) + 1;
        for(int r = 0; r < rl; r++) cache[r] = (byte) ('a' + rnd.nextInt(max));
        bo.write(cache, 0, rl);
        bo.write(end);
      }
      bo.write(Token.token("</XML>"));
    } finally {
      bo.close();
    }

    // create database
    new CreateDB(NAME, dbfile.path()).execute(context);
    // print file contents
    //Util.outln(dbfile.string());
    // print database info
    //Util.outln(new InfoDB().execute(context));

    // delete generated file
    //assertTrue(dbfile.delete());
  }

  /**
   * Initializes the benchmark.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void finishDB() throws IOException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Scans a table for elements with text nodes.
   * @throws Exception exception
   */
  @Test
  public void elementsWithText() throws Exception {
    query("count( //*[text()] )");
  }

  /**
   * Scans a table for elements with text nodes.
   * @throws Exception exception
   */
  @Test
  public void elementsWithTextOrAttribute() throws Exception {
    query("count( descendant::*//(*|@*) )");
  }

  /**
   * Scans a table for elements with text nodes.
   * @throws Exception exception
   */
  @Test
  public void textNotEquals() throws Exception {
    query("count( //*[text() != ' '] )");
  }

  /**
   * Performs the specified query; the result is ignored.
   * @param query query to be evaluated
   * @throws IOException I/O exception
   */
  private void query(final String query) throws IOException {
    Util.outln("Query: " + query);
    // warm up
    new XQuery(query).execute(context);
    final Performance p = new Performance();
    // run query and dump required time
    final Performance pl = new Performance();
    for(int l = 0; l < LOOPS; l++) {
      new XQuery(query).execute(context);
      Util.outln(pl);
    }
    // print average runtime
    Util.outln(p.getTime(LOOPS));
    Util.outln();
  }
}
