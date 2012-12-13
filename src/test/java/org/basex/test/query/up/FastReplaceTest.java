package org.basex.test.query.up;

import static org.junit.Assert.*;

import org.basex.core.cmd.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * Stress Testing the fast replace feature where blocks on disk are directly
 * overwritten.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class FastReplaceTest extends AdvancedQueryTest {
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";

  /**
   * Creates the db based on xmark.xml.
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    new CreateDB(NAME, DOC).execute(context);
    query("let $items := /site/regions//item " +
      "for $i in 1 to 10 " +
      "return (insert node $items into /site/regions, " +
      "insert node $items before /site/regions, " +
      "insert node $items after /site/closed_auctions)");
  }

  /**
   * Replaces blocks of equal size distributed over the document.
   */
  @Test
  public void replaceEqualBlocks() {
    query("for $i in //item/location/text() return replace node $i with $i");
    query("count(//item)", "186");
  }

  /**
   * Replaces blocks of equal size distributed over the document.
   */
  @Test
  public void replaceEqualBlocks2() {
    query("for $i in //item return replace node $i with $i");
    query("count(//item)", "186");
  }

  /**
   * Replaces blocks where the new subtree is smaller than the old one. Find
   * the smallest //item node in the database and replace each //item with
   * this.
   */
  @Test
  public void replaceWithSmallerTree() {
    final String id =
      query("let $newitem := (let $c := min(for $i in //item " +
        "return count($i/descendant-or-self::node())) " +
        "return for $i in //item where " +
        "(count($i/descendant-or-self::node()) = $c) " +
        "return $i)[1] return $newitem/@id/data()");
    final String count1 = query("count(//item)");

    query("for $i in //item return replace node $i " +
        "with (//item[@id='" + id + "'])[1]");
    final String count2 =  query("count(//item[@id='" + id + "'])");

    assertEquals(count1, count2);
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace each //item with
   * this.
   */
  @Test
  public void replaceWithBiggerTree() {
    query("let $newitem := (let $c := max(for $i in //item " +
      "return count($i/descendant-or-self::node())) " +
      "return for $i in //item where " +
      "(count($i/descendant-or-self::node()) = $c) " +
      "return $i)[1] return for $i in //item " +
      "return replace node $i with $newitem");
    query("count(//item)", "186");
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace the last item in the
   * database with this.
   */
  @Test
  public void replaceSingleWithBiggerTree() {
    query("let $newitem := (let $c := max(for $i in //item " +
      "return count($i/descendant-or-self::node())) " +
      "return for $i in //item where " +
      "(count($i/descendant-or-self::node()) = $c) " +
      "return $i)[1] return replace node (//item)[last()] with $newitem");
    query("count(//item)", "186");
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace the last item in the
   * database with this.
   */
  @Test
  public void replaceSingleWithSmallerTree() {
    final String id =
      query("let $newitem := (let $c := min(for $i in //item " +
        "return count($i/descendant-or-self::node())) " +
        "return for $i in //item where " +
        "(count($i/descendant-or-self::node()) = $c) " +
        "return $i)[1] return $newitem/@id/data()");
    query("replace node (//item)[last()] with (//item[@id='" + id + "'])[1]");
    query("count(//item)", "186");
  }

  /**
   * Replaces a single attribute with two attributes. Checks for correct
   * updating of the parent's attribute size.
   */
  @Test
  public void replaceAttribute() {
    query("replace node (//item)[1]/attribute() with " +
      "(attribute att1 {'0'}, attribute att2 {'1'})");
    query("(//item)[1]/attribute()", " att1=\"0\" att2=\"1\"");
  }

  /**
   * Replaces a single attribute with two attributes for each item. Checks for
   * correct updating of the parent's attribute size.
   */
  @Test
  public void replaceAttribute2() {
    query("for $i in //item return replace node $i/attribute() with " +
    "(attribute att1 {'0'}, attribute att2 {'1'})");
    query("//item/attribute()");
  }
}