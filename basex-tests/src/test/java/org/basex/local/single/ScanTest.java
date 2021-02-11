package org.basex.local.single;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class benchmarks simple table scans.
 *
 * @author BaseX Team 2005-21, BSD License
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
  @BeforeAll public static void initDB() throws IOException {
    /* generate test file. example:
     * <XML>
     *   <SUB>ndjkeibjmfeg</SUB>
     *   <SUB>ppoapcefcf</SUB>
     * </XML>
     */
    final IOFile dbfile = new IOFile(sandbox(), NAME);
    try(BufferOutput bo = new BufferOutput(dbfile)) {
      final int max = 16;
      final byte[] cache = new byte[max];
      // use constant seed to create same test document every time
      final Random rnd = new Random(0);
      bo.write(Token.token("<XML>"));
      final byte[] start = Token.token("<SUB>"), end = Token.token("</SUB>");
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
    execute(new CreateDB(NAME, dbfile.path()));
    // print file contents
    //Util.outln(dbfile.string());
    // print database info
    //Util.outln(new InfoDB().execute(context));

    // delete generated file
    //assertTrue(dbfile.delete());
  }

  /**
   * Initializes the benchmark.
   */
  @AfterAll public static void finishDB() {
    execute(new DropDB(NAME));
  }

  /** Counts the number of elements with text node as child. */
  @Test public void elementsWithText() {
    run("count( //*[text()] )");
  }

  /** Counts the number of elements with text node or attribute as child. */
  @Test public void elementsWithTextOrAttribute() {
    run("count( descendant::*//(*|@*) )");
  }

  /** Counts the number of elements the text of which does not equal a given string. */
  @Test public void textNotEquals() {
    run("count( //*[text() != ' '] )");
  }

  /**
   * Performs the specified query; some performance measurements are output and
   * the result is ignored.
   * @param query query to be evaluated
   */
  private void run(final String query) {
    Util.outln("Query: " + query);
    // warm up
    query(query);
    final Performance p = new Performance();
    // run query and dump required time
    final Performance pl = new Performance();
    for(int l = 0; l < LOOPS; l++) {
      query(query);
      Util.outln(pl);
    }
    // print average runtime
    Util.outln(p.getTime(LOOPS));
    Util.outln();
  }
}
