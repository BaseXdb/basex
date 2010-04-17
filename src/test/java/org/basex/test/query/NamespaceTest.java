package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.core.Context;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class NamespaceTest {
  /** Test documents. */
  private static String[][] docs = {
      { "d1", "<x/>" },
      { "d2", "<x xmlns='a'/>" },
      { "d3", "<a:x xmlns:a='a'/>" },
  };

  /** Database context. */
  private static Context context;

  /** Creates the database context. */
  @BeforeClass
  public static void start() {
    context = new Context();
    // turn off pretty printing
    exec(new Set(Prop.SERIALIZER, "indent=no"));
  }

  /** Creates all test database. */
  @Before
  public void startTest() {
    // create all test databases
    for(final String[] doc : docs) exec(new CreateDB(doc[1], doc[0]), null);
  }

  /** Removes test databases and closes the database context. */
  @AfterClass
  public static void finish() {
    // drop all test databases
    for(final String[] doc : docs) exec(new DropDB(doc[0]), null);
    context.close();
  }
  
  /** Test query. */
  @Test
  public final void query0() {
    query(
        "insert node doc('d2') into doc('d1')/x",
        "doc('d1')",
        "<x><x xmlns='a'/></x>"
    );
  }
  
  /** Test query. */
  @Test
  public final void query1() {
    query(
        "copy $c := <x:a xmlns:x='A'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='A'/>"
    );
  }

  /**
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private void query(final String query, final String expected) {
    query(null, query, expected);
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private void query(final String first, final String second,
      final String expected) {

    if(first != null) exec(new XQuery(first));
    final CachedOutput co = new CachedOutput();
    exec(new XQuery(second), co);
    
    final String result = co.toString().replaceAll("\\\"", "'");
    if(!expected.equals(result)) {
      fail("\n" + co + "\n" + expected + " expected");
    }
  }
  
  /**
   * Runs a command.
   * @param proc process to be run
   */
  private static void exec(final Proc proc) {
    if(!proc.exec(context)) fail(proc.info());
  }
  
  /**
   * Runs a command and cached the output.
   * @param proc process to be run
   * @param co cached output
   */
  private static void exec(final Proc proc, final CachedOutput co) {
    if(!proc.exec(context, co)) fail(proc.info());
  }
}
