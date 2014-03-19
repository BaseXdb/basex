package org.basex.performance;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class benchmarks simple table scans.
 *
 * @author BaseX Team 2005-14, BSD License
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
    try(final BufferOutput bo = new BufferOutput(dbfile.path())) {
      final int max = 16;
      final byte[] cache = new byte[max];
      // use constant seed to create same test document every time
      final Random rnd = new Random(0);
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
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finishDB() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Counts the number of elements with text node as child.
   * @throws BaseXException database exception
   */
  @Test
  public void elementsWithText() throws BaseXException {
    query("count( //*[text()] )");
  }

  /**
   * Counts the number of elements with text node or attribute as child.
   * @throws BaseXException database exception
   */
  @Test
  public void elementsWithTextOrAttribute() throws BaseXException {
    query("count( descendant::*//(*|@*) )");
  }

  /**
   * Counts the number of elements the text of which does not equal a given string.
   * @throws BaseXException database exception
   */
  @Test
  public void textNotEquals() throws BaseXException {
    query("count( //*[text() != ' '] )");
  }

  /**
   * Performs the specified query; some performance measurements are output and
   * the result is ignored.
   * @param query query to be evaluated
   * @throws BaseXException database exception
   */
  private void query(final String query) throws BaseXException {
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
