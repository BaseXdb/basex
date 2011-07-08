package org.basex.test.query.advanced;

import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class XQUPGeneral extends AdvancedQueryTest {
  /** Test document. */
  public static final String DOC = "etc/test/xmark.xml";
  /** Test database name. */
  public static final String DBNAME = Util.name(XQUPGeneral.class);

  /**
   * Creates a database.
   * @param input input database string, if null, then use default.
   * @throws Exception exception
   */
  private void createDB(final String input) throws Exception {
    new CreateDB(DBNAME, input == null ? DOC : input).execute(CONTEXT);
  }

  /**
   * Text merging for a simple insert into statement.
   * @throws Exception exception
   */
  @Test
  public void textMerging01() throws Exception {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging for a simple insert into statement in combination with an
   * insert before statement. Checks if pre value shifts are taken into
   * account.
   * @throws Exception exception
   */
  @Test
  public void textMerging02() throws Exception {
    createDB(null);
    query("insert node 'foo' into //item[@id='item0']/location," +
      "insert node <a/> before //item[@id='item0']");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test. Test 'insert into as first' and whether pre value
   * shifts are taken into account correctly.
   * @throws Exception exception
   */
  @Test
  public void textMerging03() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0'] return (insert node 'foo' as " +
      "first into $i/location, insert node 'foo' before $i/location, " +
      "insert node 'foo' into $i/quantity)");
    query("let $i := //item[@id='item0'] return " +
      "(($i/location/text())[1], ($i/quantity/text())[1])",
      "fooUnited States1foo");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws Exception exception
   */
  @Test
  public void textMerging04() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0'] return insert node 'foo' " +
      "before $i/location/text()");
    query("let $i := //item[@id='item0'] return " +
      "($i/location/text())[1]", "fooUnited States");
  }

  /**
   * Text merging for a simple insert into statement. Checks if pre value shifts
   * are taken into account.
   * @throws Exception exception
   */
  @Test
  public void textMerging05() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0']/location return " +
        "(insert node 'foo' into $i, delete node $i/text())");
    query("//item[@id='item0']/location/text()", "foo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging06() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0']/location return " +
        "(insert node <n/> into $i, insert node 'foo' as last into $i)");
    query("let $i := //item[@id='item0']/location return " +
    "delete node $i/n");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging07() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0']/location return " +
        "insert node 'foo' after $i/text()");
    query("(//item[@id='item0']/location/text())[1]", "United Statesfoo");
  }

  /**
   * Text merging test.
   * @throws Exception exception
   */
  @Test
  public void textMerging08() throws Exception {
    createDB(null);

    query("let $i := //item[@id='item0'] return " +
        "(insert node 'foo' after $i/location)");
    query("let $i := //item[@id='item0']/location return " +
        "(insert node 'foo' after $i, insert node 'faa' before $i, insert " +
        "node 'faa' into $i, delete node $i/text())");
    query("let $i := //item[@id='item0']/location " +
        "return ($i/text(), ($i/../text())[2])", "faafoofoo");
  }

  /**
   * Text merging test for delete operation.
   * @throws Exception exception
   */
  @Test
  public void textMerging09() throws Exception {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' after ($c//d)[1]) " +
        "return count($c//text()), $c//text()", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   * @throws Exception exception
   */
  @Test
  public void textMerging10() throws Exception {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
        "return count($c//text()), $c//text()", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   * @throws Exception exception
   */
  @Test
  public void textMerging11() throws Exception {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
        "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
        "return count($c//text()), $c//text()", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   * @throws Exception exception
   */
  @Test
  public void treeAwareUpdates() throws Exception {
    query("copy $n := <a><b><c/></b></a> " +
        "modify (replace value of node $n/b with (), " +
        "insert node <d/> into $n/b, insert node <d/> after $n/b) return $n",
        "<a><b/><d/></a>");
  }

  /**
   * Delete last node of a data instance. Checks if table limits are crossed.
   * @throws Exception exception
   */
  @Test
  public void deleteLastNode() throws Exception {
    query("copy $n := <a><b/><c/></a> modify (delete node $n//c) return $n",
      "<a><b/></a>");
  }

  /**
   * Replace last node of a data instance. Checks if table limits are crossed.
   * @throws Exception exception
   */
  @Test
  public void replaceLastNode() throws Exception {
    query("copy $n := <a><b/><c><d/><d/><d/></c></a> " +
        "modify (replace node $n//c with <c/>) return $n", "<a><b/><c/></a>");
  }

  /**
   * Replaces the value of the documents root node. Related to
   * github issue #141.
   * @throws Exception exception
   */
  @Test
  public void replaceValueOfEmptyRoot() throws Exception {
    createDB("<a/>");
    query("replace value of node /a with 'a'");
  }

  /**
   * Deletes the test db.
   * @throws Exception exception
   */
  @AfterClass
  public static void end() throws Exception {
    new DropDB(DBNAME).execute(CONTEXT);
    CONTEXT.close();
  }
}
