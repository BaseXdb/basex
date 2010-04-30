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
  /** Database context. */
  private static Context context;

  /** Test documents. */
  private static String[][] docs = {
      { "d1", "<x/>" },
      { "d2", "<x xmlns='xx'/>" },
      { "d3", "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>" },
      { "d4", "<a:x xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
      { "d5", "<a:x xmlns:a='aa'/>" },
      { "d6", "<a:x xmlns='xx' xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
      { "d7", "<x xmlns='xx'><y/></x>" },
  };

  /** Test query. */
  @Test
  public final void copy1() {
    query(
        "copy $c := <x:a xmlns:x='xx'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='xx'/>");
  }

  /** Test query.
   * [LK] 
   * Detects corrupt namespace hierarchy.
   */
  @Test
  public final void copy2() {
    query(
        "declare namespace a='aa'; copy $c:=doc('d4') modify () return $c//a:y",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }
  
  /** Test query.
   * Detects missing prefix declaration.
   */
  @Test
  public final void copy3() {
    query(
        "declare namespace a='aa'; copy $c:=doc('d4')//a:y " +
        "modify () return $c",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query. */
  @Test
  public final void insertD2intoD1() {
    query(
        "insert node doc('d2') into doc('d1')/x",
        "doc('d1')",
        "<x><x xmlns='xx'/></x>");
  }

  /** Test query. */
  @Test
  public final void insertD3intoD1() {
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*",
        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>");
  }

  /** Test query. */
  @Test
  public final void insertD3intoD1b() {
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*/*",
        "<b:y xmlns:b='bb' xmlns:a='aa'/>");
  }
  
  /** Test query.
   * Detects missing prefix declaration.
   */
  @Test
  public final void insertD4intoD1() {
    query(
        "declare namespace a='aa'; insert node doc('d4')/a:x/a:y " +
        "into doc('d1')/x",
        "doc('d1')/x",
        "<x><a:y xmlns:a='aa' xmlns:b='bb'/></x>");
  }
  
  /** Test query. 
   * Detects duplicate prefix declaration at pre=0 in MemData instance after
   * insert.
   * [LK] though result correct, prefix
   * a is declared twice. -> Solution?
   */
  @Test
  public final void insertD4intoD5() {
    query(
        "declare namespace a='aa';insert node doc('d4')//a:y " +
        "into doc('d5')/a:x",
        "declare namespace a='aa';doc('d5')//a:y",
        "<a:y xmlns:a='aa' xmlns:b='bb'/>");
  }
  
  /** Test query. 
   * Detects duplicate prefix declarations among the insertion nodes (MemData)
   * and the target node's data instance.
   * [LK] duplicates are generated in Data.insert(...   MemData correct!
   *
  @Test
  public final void insertD4intoD3() {
    query(
        "declare namespace b='bb';insert node doc('d4') into doc('d3')//b:y",
        "declare namespace a='aa';doc('d3')/a:x",
        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'><a:x><a:y/></a:x></b:y></a:x>");
  }*/
  
  /** Test query. 
   * Detects duplicate namespace declarations in MemData instance.
   */
  @Test
  public final void insertD7intoD1() {
    query(
        "declare namespace x='xx';insert node doc('d7')/x:x into doc('d1')/x",
        "doc('d1')/x",
        "<x><x xmlns='xx'><y/></x></x>");
  }
  
  /** Test query. 
   * Detects duplicate namespace declarations after insert.
   *
  @Test
  public final void insertD2intoD6() {
    query(
        "declare namespace ns='xx';declare namespace a='aa';" +
        "insert node doc('d2')/ns:x into doc('d6')/a:x",
        "declare namespace a='aa';doc('d6')/a:x",
        "<a:x xmlns='xx' xmlns:a='aa'><a:y xmlns:b='bb'/><x/></a:x>");
  }*/
  
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

    // quotes are replaced by apostrophes to simplify comparison
    final String res = co.toString().replaceAll("\\\"", "'");
    final String exp = expected.replaceAll("\\\"", "'");
    if(!exp.equals(res)) fail("\n" + res + "\n" + exp + " expected");
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
