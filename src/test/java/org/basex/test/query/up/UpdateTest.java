package org.basex.test.query.up;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * General tests of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class UpdateTest extends AdvancedQueryTest {
  /** Test database name. */
  private static final String DB = Util.name(UpdateTest.class);
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";

  /**
   * Creates a database.
   * @param input input database string, if null, then use default.
   * @throws BaseXException database exception
   */
  private static void createDB(final String input) throws BaseXException {
    new CreateDB(DB, input == null ? DOC : input).execute(CONTEXT);
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
   */
  @Test
  public void textMerging09() {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' after ($c//d)[1]) " +
      "return (count($c//text()), $c//text())", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void textMerging10() {
    query("copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void textMerging11() {
    query(
      "copy $c := <n>aa<d/><d/>cc</n> " +
      "modify (delete node $c//d, insert node 'bb' before ($c//d)[2]) " +
      "return (count($c//text()), $c//text())", "1aabbcc");
  }

  /**
   * Text merging test for delete operation.
   */
  @Test
  public void treeAwareUpdates() {
    query(
      "copy $n := <a><b><c/></b></a> " +
      "modify (replace value of node $n/b with (), " +
      "insert node <d/> into $n/b, insert node <d/> after $n/b) return $n",
      "<a><b/><d/></a>");
  }

  /**
   * Delete last node of a data instance. Checks if table limits are crossed.
   */
  @Test
  public void deleteLastNode() {
    query(
      "copy $n := <a><b/><c/></a> modify (delete node $n//c) return $n",
      "<a><b/></a>");
  }

  /**
   * Replace last node of a data instance. Checks if table limits are crossed.
   */
  @Test
  public void replaceLastNode() {
    query(
      "copy $n := <a><b/><c><d/><d/><d/></c></a> " +
      "modify (replace node $n//c with <c/>) return $n",
      "<a><b/><c/></a>");
  }

  /**
   * Replaces the value of the documents root node. Related to
   * github issue #141.
   * @throws BaseXException database exception
   */
  @Test
  public void replaceValueOfEmptyRoot() throws BaseXException {
    createDB("<a/>");
    query("replace value of node /a with 'a'");
    query("/", "<a>a</a>");
  }

  /**
   * Insertion into an empty element.
   */
  @Test
  public void emptyInsert1() {
    query(
      "copy $x := <X/> modify insert nodes <A/> into $x return $x",
      "<X><A/></X>");
  }

  /**
   * Insertion into an empty document.
   */
  @Test
  public void emptyInsert2() {
    query(
      "copy $x := document {()} modify insert nodes <X/> into $x return $x",
      "<X/>");
  }

  /**
   * Insertion into an empty document.
   * @throws BaseXException database exception
   */
  @Test
  public void emptyInsert3() throws BaseXException {
    createDB("<a/>");
    query("delete node /a");
    query("insert nodes <X/> into doc('" + DB + "')");
    query("/", "<X/>");
  }

  /**
   * Tests a simple call of the optimize command.
   * @throws BaseXException database exception
   */
  @Test
  public void optimize() throws BaseXException {
    createDB(null);
    query("let $w := //item[@id = 'item0'] " +
      "return (if($w/@id) " +
      "then (delete node $w/@id, db:optimize('" + DB + "')) else ())");
  }

  /** Variable from the inner scope shouldn't be visible. */
  @Test
  public void outOfScope() {
    error("let $d := copy $e := <a/> modify () return $e return $e",
        Err.VARUNDEF);
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag() {
    query("declare namespace x='x';" +
      "copy $x := <x/> modify insert node attribute x:x {} into $x return $x",
      "<x xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag2() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x return $x/a",
      "<a xmlns:x=\"x\"/>");
  }

  /**
   * The new-namespace flag has to be set for the parent of an inserted
   * attribute.
   */
  @Test
  public void setNSFlag3() {
    query("declare namespace x='x';" +
      "copy $x := <x><a/></x> " +
      "modify insert node attribute x:x {} into $x/a return $x/a",
      "<a xmlns:x=\"x\" x:x=\"\"/>");
  }

  /**
   * Deletes the test db.
   * @throws Exception exception
   */
  @AfterClass
  public static void end() throws Exception {
    new DropDB(DB).execute(CONTEXT);
    CONTEXT.close();
  }
}
