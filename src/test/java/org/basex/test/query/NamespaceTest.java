package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.io.CachedOutput;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests namespaces.
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

//  /** Test query. */
//  @Test
//  public final void simpleNsDuplicate() {
//    query(
//        "declare namespace a='aa'; insert node <a:y xmlns:a='aa'/>" +
//        "into doc('d5')/a:x",
//        "declare namespace a='aa';doc('d5')/a:x",
//        "<a:x xlmns:a='aa'><a:y/></a:x>");
//  }
//
//  /** Test query. */
//  @Test
//  public final void simpleNsDuplicate2() {
//    query(
//      "declare namespace a='aa'; insert node <a:y xmlns:a='aa'><a:b/></a:y> "+
//      "into doc('d5')/a:x",
//      "declare namespace a='aa';doc('d5')/a:x",
//      "<a:x xlmns:a='aa'><a:y><a:b/></a:y></a:x>");
//  }
//
//  /** Test query. */
//  @Test
//  public final void copy4() {
//    query(
//        "copy $c := <a:y xmlns:a='aa'><a:b/></a:y> modify () return $c",
//        "<a:y xmlns:a='aa'><a:b/></a:y>");
//  }

  /** Test query. */
  @Test
  public final void copy5() {
    query(
        "copy $c := <n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n> " +
        "modify () return $c",
        "<n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n>");
  }

  /** Test query. */
  @Test
  public final void copy1() {
    query(
        "copy $c := <x:a xmlns:x='xx'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='xx'/>");
  }

  /** Test query.
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
   * Though result correct, prefix
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

//  /** Test query.
//   * Detects duplicate prefix declarations among the insertion nodes (MemData)
//   * and the target node's data instance.
//   */
//  @Test
//  public final void insertD4intoD3() {
//    query(
//        "declare namespace b='bb';insert node doc('d4') into doc('d3')//b:y",
//        "declare namespace a='aa';doc('d3')/a:x",
//        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'><a:x><a:y/></a:x></b:y></a:x>");
//  }

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

//  /** Test query.
//   * Detects duplicate namespace declarations after insert.
//   */
//  @Test
//  public final void insertD2intoD6() {
//    query(
//        "declare namespace ns='xx';declare namespace a='aa';" +
//        "insert node doc('d2')/ns:x into doc('d6')/a:x",
//        "declare namespace a='aa';doc('d6')/a:x",
//        "<a:x xmlns='xx' xmlns:a='aa'><a:y xmlns:b='bb'/><x/></a:x>");
//  }

  /** Test query.
   * Detects general problems with namespace references.
   */
  @Test
  public final void insertD6intoD4() {
    query(
        "declare namespace a='aa';insert node doc('d6') into doc('d4')/a:x",
        "declare namespace a='aa';doc('d4')/a:x/a:y",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

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
    for(final String[] doc : docs) exec(new CreateDB(doc[0], doc[1]), null);
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
   * @param cmd command to be run
   */
  private static void exec(final Command cmd) {
    if(!cmd.exec(context)) fail(cmd.info());
  }

  /**
   * Runs a command and cached the output.
   * @param cmd command to be run
   * @param co cached output
   */
  private static void exec(final Command cmd, final CachedOutput co) {
    if(!cmd.exec(context, co)) fail(cmd.info());
  }
}
