package org.basex.test.data;


import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Stress Testing the fast replace feature where blocks on diks are directly
 * overwritten.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FastReplaceTest {

  /** Basex context. */
  public static final Context CONTEXT = new Context();
  /** Test document. */
  public static final String DOC = "etc/xml/xmark.xml";
  /** Test database name. */
  public static final String DBNAME = "XQUPStressTest";

  /**
   * Creates the db based on xmark.xml.
   * @throws Exception excp
   */
  @Before
  public void setUp() throws Exception {
    new CreateDB(DBNAME, DOC).execute(CONTEXT);
    new XQuery("let $items := /site/regions//item " +
        "for $i in 1 to 0 " +
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

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
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

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
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
    // check number of @id for equality
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
          "with //item[@id='" + newID + "']").
      execute(CONTEXT);

      final int newIDItemCount = Integer.parseInt(
          new XQuery("count(//item[@id='" + newID + "'])").execute(CONTEXT));

      assertEquals(itemCount, newIDItemCount);

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      fail(e.getMessage());
    }
  }

  /**
   * Replaces blocks where the new subtree is bigger than the old one. Find
   * the biggest //item node in the database and replace each //item with
   * this.
   */
//  @Test
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

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      fail(e.getMessage());
    }
  }

  /**
   * Dummy which creates the database.
   */
  @Test
  public void createTestDatabase() {
    // TODO debug this one
    try {
      new XQuery("/").
      execute(CONTEXT);
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
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
    // TODO debug this one
    try {
      new XQuery("let $newitem := (let $c := max(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return replace node (//item)[last()] with $newitem").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
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
    // TODO debug this one
    try {
      final String newID =
        new XQuery("let $newitem := (let $c := min(for $i in //item " +
          "return count($i/descendant-or-self::node())) " +
          "return for $i in //item where " +
          "(count($i/descendant-or-self::node()) = $c) " +
          "return $i)[1] return $newitem/@id/data()").
      execute(CONTEXT);

      new XQuery("replace node //item[@id='item4'] with " +
          "//item[@id='" + newID + "']").
      execute(CONTEXT);

      new XQuery("count(//item)").execute(CONTEXT);

    } catch(BaseXException e) {
      // TODO Auto-generated catch block
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
  }

  /**
   * Deletes the test db.
   */
  @AfterClass
  public static void end() {
//    try {
//      new DropDB(DBNAME).execute(CONTEXT);
//    } catch(BaseXException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    CONTEXT.close();
  }
}
