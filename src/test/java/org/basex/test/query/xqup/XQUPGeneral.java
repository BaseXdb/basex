package org.basex.test.query.xqup;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class XQUPGeneral {
  /** Database context. */
  public static final Context CONTEXT = new Context();
  /** Test document. */
  public static final String DOC = "etc/test/xmark.xml";
  /** Test database name. */
  public static final String DBNAME = Util.name(XQUPGeneral.class);

  /**
   * Creates a database.
   * @throws BaseXException exception
   */
  private void createDB() throws BaseXException {
    new CreateDB(DBNAME, DOC).execute(CONTEXT);
  }

  /**
   * Text merging for a simple insert into statement.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging01() throws BaseXException {
    createDB();

    new XQuery("insert node 'foo' into //item[@id='item0']/location").
    execute(CONTEXT);
    final String r = new XQuery("(//item[@id='item0']/location/text())[1]").
    execute(CONTEXT);
    assertEquals("United Statesfoo", n(r));
  }

  /**
   * Text merging for a simple insert into statement in combination with an
   * insert before statement. Checks if pre value shifts are taken into
   * account.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging02() throws BaseXException {
    createDB();

    new XQuery("insert node 'foo' into //item[@id='item0']/location," +
      "insert node <a/> before //item[@id='item0']").
    execute(CONTEXT);
    final String r = new XQuery("(//item[@id='item0']/location/text())[1]").
    execute(CONTEXT);
    assertEquals("United Statesfoo", n(r));
  }

  /**
   * Text merging test. Test 'insert into as first' and whether pre value
   * shifts are taken into account correctly.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging03() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0'] return (insert node 'foo' as " +
        "first into $i/location, insert node 'foo' before $i/location, " +
        "insert node 'foo' into $i/quantity)").
    execute(CONTEXT);
    final String r = new XQuery("let $i := //item[@id='item0'] return " +
      "(($i/location/text())[1], ($i/quantity/text())[1])").
    execute(CONTEXT);
    assertEquals("fooUnited States1foo", n(r));
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging04() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0'] return insert node 'foo' " +
        "before $i/location/text()").
    execute(CONTEXT);
    final String r = new XQuery("let $i := //item[@id='item0'] return " +
      "($i/location/text())[1]").
    execute(CONTEXT);
    assertEquals("fooUnited States", n(r));
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging05() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0']/location return " +
        "(insert node 'foo' into $i, delete node $i/text())").
    execute(CONTEXT);
    final String r = new XQuery("//item[@id='item0']/location/text()").
    execute(CONTEXT);
    assertEquals("foo", n(r));
  }

  /**
   * Text merging test.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging06() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0']/location return " +
        "(insert node <n/> into $i, insert node 'foo' as last into $i)").
    execute(CONTEXT);
    new XQuery("let $i := //item[@id='item0']/location return " +
    "delete node $i/n").
    execute(CONTEXT);
    final String r = new XQuery("(//item[@id='item0']/location/text())[1]").
    execute(CONTEXT);
    assertEquals("United Statesfoo", n(r));
  }

  /**
   * Text merging test.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging07() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0']/location return " +
        "insert node 'foo' after $i/text()").
        execute(CONTEXT);
    final String r = new XQuery("(//item[@id='item0']/location/text())[1]").
      execute(CONTEXT);
    assertEquals("United Statesfoo", n(r));
  }

  /**
   * Text merging test.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging08() throws BaseXException {
    createDB();

    new XQuery("let $i := //item[@id='item0'] return " +
        "(insert node 'foo' after $i/location)").
         execute(CONTEXT);
    new XQuery("let $i := //item[@id='item0']/location return " +
        "(insert node 'foo' after $i, insert node 'faa' before $i, insert " +
        "node 'faa' into $i, delete node $i/text())").
        execute(CONTEXT);
    final String r = new XQuery("let $i := //item[@id='item0']/location " +
        "return ($i/text(), ($i/../text())[2])").
        execute(CONTEXT);
    assertEquals("faafoofoo", n(r));
  }

  /**
   * Text merging test for delete operation.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging09() throws BaseXException {
    final String r = new XQuery("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' after ($c//d)[1]) " +
        "return count($c//text()), $c//text()").
         execute(CONTEXT);
    assertEquals("1aabbcc", n(r));
  }

  /**
   * Text merging test for delete operation.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging10() throws BaseXException {
    final String r = new XQuery("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
        "return count($c//text()), $c//text()").
         execute(CONTEXT);
    assertEquals("1aabbcc", n(r));
  }

  /**
   * Text merging test for delete operation.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void textMerging11() throws BaseXException {
    final String r = new XQuery("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
        "return count($c//text()), $c//text()").
         execute(CONTEXT);
    assertEquals("1aabbcc", n(r));
  }

  /**
   * Text merging test for delete operation.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void treeAwareUpdates() throws BaseXException {
    final String r = new XQuery("copy $n := <a><b><c/></b></a> " +
        "modify (replace value of node $n/b with (), " +
        "insert node <d/> into $n/b, insert node <d/> after $n/b) return $n").
         execute(CONTEXT);
    assertEquals("<a><b/><d/></a>", n(r));
  }

  /**
   * Delete last node of a data instance. Checks if table limits are crossed.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void deleteLastNode() throws BaseXException {
    final String r = new XQuery("copy $n := <a><b/><c/></a> " +
        "modify (delete node $n//c) " +
        "return $n").
         execute(CONTEXT);
    assertEquals("<a><b/></a>", n(r));
  }

  /**
   * Replace last node of a data instance. Checks if table limits are crossed.
   * @throws BaseXException BaseX exception
   */
  @Test
  public void replaceLastNode() throws BaseXException {
    final String r = new XQuery("copy $n := <a><b/><c><d/><d/><d/></c></a> " +
        "modify (replace node $n//c with <c/>) return $n").
         execute(CONTEXT);
    assertEquals("<a><b/><c/></a>", n(r));
  }

  /**
   * Test setup.
   */
  @BeforeClass
  public static void start() {
    final Prop p = CONTEXT.prop;
    p.set(Prop.TEXTINDEX, false);
    p.set(Prop.ATTRINDEX, false);
  }

  /**
   * Deletes the test db.
   */
  @AfterClass
  public static void end() {
    try {
      new DropDB(DBNAME).execute(CONTEXT);
    } catch(final BaseXException e) {
      e.printStackTrace();
    }
    CONTEXT.close();
  }

  /**
   * Normalizes a given string - removes CR and LF.
   * @param s string
   * @return normalized string
   */
  public static String n(final String s) {
    final String n = s.replaceAll("(\\r|\\n) *", "");
    return n;
  }
}