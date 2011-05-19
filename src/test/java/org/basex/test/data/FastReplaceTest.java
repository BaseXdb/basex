package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Stress Testing the fast replace feature where blocks on disks are directly
 * overwritten.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class FastReplaceTest {
  /** Database context. */
  public static final Context CONTEXT = new Context();
  /** Test document. */
  public static final String DOC = "etc/xml/xmark.xml";
  /** Test database name. */
  public static final String DBNAME = Util.name(FastReplaceTest.class);

  /**
   * Creates the db based on xmark.xml.
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    new CreateDB(DBNAME, DOC).execute(CONTEXT);
    new XQuery("let $items := /site/regions//item " +
        "for $i in 1 to 10 " +
        "return (insert node $items into /site/regions, " +
        "insert node $items before /site/regions, " +
        "insert node $items after /site/closed_auctions)").execute(CONTEXT);
  }

  /**
   * Replaces blocks of equal size distributed over the document.
   */
  @Test
  public void replaceEqualBlocks() {
    try {
      new XQuery("for $i in //item/location/text() " +
        "return replace node $i with $i").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks of equal size distributed over the document.
   */
  @Test
  public void replaceEqualBlocks2() {
    try {
      new XQuery("for $i in //item return replace node $i with $i").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks where the new subtree is smaller than the old one. Find
   * the smallest //item node in the database and replace each //item with
   * this.
   */
  @Test
  public void replaceWithSmallerTree() {
    try {
      final String newID =
        new XQuery("let $newitem := (let $c := min(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return $newitem/@id/data()").
      execute(CONTEXT);
      final int itemCount = Integer.parseInt(
        new XQuery("count(//item)").execute(CONTEXT));

      new XQuery("for $i in //item return replace node $i " +
          "with (//item[@id='" + newID + "'])[1]").
      execute(CONTEXT);

      final int newIDItemCount = Integer.parseInt(
          new XQuery("count(//item[@id='" + newID + "'])").execute(CONTEXT));

      assertEquals(itemCount, newIDItemCount);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace each //item with
   * this.
   */
  @Test
  public void replaceWithBiggerTree() {
    try {
      new XQuery("let $newitem := (let $c := max(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return for $i in //item " +
          "return replace node $i with $newitem").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace the last item in the
   * database with this.
   */
  @Test
  public void replaceSingleWithBiggerTree() {
    try {
      new XQuery("let $newitem := (let $c := max(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return replace node (//item)[last()] with $newitem").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace the last item in the
   * database with this.
   */
  @Test
  public void replaceSingleWithSmallerTree() {
    try {
      final String newID =
        new XQuery("let $newitem := (let $c := min(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return $newitem/@id/data()").
      execute(CONTEXT);

      new XQuery("replace node (//item)[last()] with " +
          "(//item[@id='" + newID + "'])[1]").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces a single attribute with two attributes. Checks for correct
   * updating of the parent's attribute size.
   */
  @Test
  public void replaceAttributes() {
    try {
      new XQuery("replace node (//item)[1]/attribute() with " +
      "(attribute att1 {'0'}, attribute att2 {'1'})").
      execute(CONTEXT);
      final String r = new XQuery("(//item)[1]/attribute()").execute(CONTEXT);

      assertEquals(r.trim(), "att1=\"0\" att2=\"1\"");

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Replaces a single attribute with two attributes for each item. Checks for
   * correct updating of the parent's attribute size.
   */
  @Test
  public void replaceAttributes2() {
    try {
      new XQuery("for $i in //item return replace node $i/attribute() with " +
      "(attribute att1 {'0'}, attribute att2 {'1'})").
      execute(CONTEXT);
      new XQuery("//item/attribute()").execute(CONTEXT);

    } catch(final BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Test setup.
   */
  @BeforeClass
  public static void start() {
    final Prop p = CONTEXT.prop;
    p.set(Prop.TEXTINDEX, false);
    p.set(Prop.ATTRINDEX, false);
    /* Running this junit test in main memory doesn't make that much sense as
     * updates in main memory are non persistent - assertions will fail.
       Just for debugging purposes. */
//    p.set(Prop.MAINMEM, true);
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
}