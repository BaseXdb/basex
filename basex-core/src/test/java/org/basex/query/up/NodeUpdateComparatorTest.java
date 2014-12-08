package org.basex.query.up;

import static org.junit.Assert.*;

import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.node.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.*;

/**
 * Tests {@link NodeUpdateComparator} that creates an order on update primitives
 * and is part of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class NodeUpdateComparatorTest extends AdvancedQueryTest {
  /** Expected exception. */
  @Rule
  public final ExpectedException thrown = ExpectedException.none();
  /** Test document. */
  private static final String TESTDOCUMENT =
      "<n1>" +
          "<n2><n3/></n2>" +
          "<n4><n5><n6/></n5></n4>" +
          "<n7/><n8/><n9/>" +
          "<n10 id11='0' id12='0'><n13/>TEXT14<n15><n16/></n15></n10><n17/>" +
          "<n18 at19='0' at20='0'><n21><n22 at23='0'/></n21><n24><n25/></n24></n18>" +
          "<n26><n27/></n26><n28/>" +
          "<n29><n30/></n29><n31><n32/></n31>" +
          "<n33 at34='0'/><n35/>" +
      "</n1>";

  /**
   * Single delete.
   */
  @Test
  public void scoring01() {
    final String doc = "<a><b/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new DeleteNode(2, d, null)
    });
    query(transform(doc, "delete node $input//b"), "<a/>");
  }

  /**
   * Two basic deletes called on siblings.
   */
  @Test
  public void scoring02() {
    final String doc = "<a><b/><c/><d/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new DeleteNode(4, d, null),
        new DeleteNode(2, d, null)
    });
    query(transform(doc, "delete node ($input//b, $input//d)"), "<a><c/></a>");
  }

  /**
   * Three basic deletes called on siblings.
   */
  @Test
  public void scoring03() {
    final String doc = "<a><b/><c/><d/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new DeleteNode(4, d, null),
        new DeleteNode(3, d, null),
        new DeleteNode(2, d, null)
    });
    query(transform(doc, "delete node ($input//child::node())"), "<a/>");
  }

  /**
   * 'Insert after' and 'insert before' statement on the same target node.
   */
  @Test
  public void scoring04() {
    final String doc = "<a><b/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new InsertAfter(2, d, null, null),
        new InsertBefore(2, d, null, null)
    });
    query(transform(doc,
        "insert node <before/> before $input/b, insert node <after/> after $input/b"),
        "<a><before/><b/><after/></a>");
  }

  /**
   * Two insert into statements performed on a node A and its child B.
   *
   * Tests if an insert into statement on target T is scored higher (hence executed first)
   * then all insert into statements on any of its descendant nodes.
   */
  @Test
  public void scoring05() {
    final String doc = "<a><b/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new InsertInto(1, d, null, null),
        new InsertInto(2, d, null, null),
    });
    query(transform(doc,
        "insert node <smallerpre/> into $input/b, insert node <largerpre/> into $input"),
        "<a><b><smallerpre/></b><largerpre/></a>");
  }

  /**
   * An insert into statement on target T and an insert after on its single child.
   *
   * Tests if the insert into on T is correctly score to be executed first.
   */
  @Test
  public void scoring06() {
    final String doc = "<a><b/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new InsertInto(1, d, null, null),
        new InsertAfter(2, d, null, null),
    });
    query(transform(doc,
        "insert node <smallerpre/> after $input/b, insert node <largerpre/> into $input"),
        "<a><b/><smallerpre/><largerpre/></a>");
  }

  /**
   * An insert into and an insert after statement on the same target T that has a subtree
   * size > 1.
   *
   * Tests if two corrected insert into/insert after statements are correctly scored
   * depending on their type (insert after must come first).
   */
  @Test
  public void scoring07() {
    final String doc = "<a><b><c/></b></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new InsertAfter(2, d, null, null),
        new InsertInto(2, d, null, null),
    });
    query(transform(doc,
        "insert node <smallerpre/> into $input/b," +
        "insert node <largerpre/> after $input/b"),
        "<a><b><c/><smallerpre/></b><largerpre/></a>");
  }

  /**
   * Tests if an insert attribute statement on T and a delete statement on the single
   * attribute of T are scored correctly.
   */
  @Test
  public void scoring08() {
    final String doc = "<a id=\"0\"/>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new DeleteNode(2, d, null),
        new InsertAttribute(1, d, null, null),
    });
    query(transform(doc,
        "delete node $input/attribute::node()," +
        "insert node attribute {'idnew'} {0} into $input"),
        "<a idnew=\"0\"/>");
  }

  /**
   * Test if an exception is thrown for duplicate {@link NodeUpdate}.
   */
  @Test
  public void duplicateScore() {
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Ambiguous order of UpdatePrimitives: ");
    final String doc = "<a><b/></a>";
    final Data d = data(doc);
    compare(
        new NodeUpdate[] {
        new DeleteNode(2, d, null),
        new DeleteNode(2, d, null),
    });
  }

  /**
   * Simple tests for the {@link NodeUpdate} comparator.
   *
   * Compares two primitives A and B based on the triple (LOCATION, SHIFTED, SUBTREE, TYPE):
   * <ul>
   *   <li> LOCATION is the pre value where the update affects the table
   *   <li> SHIFTED states if the primitives location has been updated to support
   *        InsertInto, InsertAfter statements
   *   <li> SUBTREE states if one update takes place in the subtree of the other's target node
   *   <li> TYPE relates to the {@link UpdateType} hierarchy
   * </ul>
   */
  @Test
  public void comparatorTest() {
    // JUST ADD NEW SCENARIOS TO THE END OF THE DOCUMENT
    final Data d = data(TESTDOCUMENT);

    // ****** Tests on single target node T and on the single descendant of T
    //(A=B, A=B, A=B, A>B)
    compare(new NodeUpdate[] {
        new InsertIntoAsFirst(3, d, null, null),
        new InsertBefore(3, d, null, null)
    });
    //(A=B, A>B, A>B, A=B)
    compare(new NodeUpdate[] {
        new InsertInto(2, d, null, null),
        new InsertInto(3, d, null, null),
    });
    //(A=B, A>B, A=B, A>B, A<B)
    compare(new NodeUpdate[] {
        new InsertInto(2, d, null, null),
        new InsertAfter(3, d, null, null),
    });
    //(A=B, A>B, A>B, A>B)
    compare(new NodeUpdate[] {
        new InsertAfter(2, d, null, null),
        new InsertInto(3, d, null, null),
    });
    //(A>B, A>B, A>B, A<B)
    compare(new NodeUpdate[] {
        new InsertInto(2, d, null, null),
        new InsertAfter(3, d, null, null),
    });

    // ****** Tests on target node T and on the subtree of T
    //(A=B, A=B, A=B, A>B)
    compare(new NodeUpdate[] {
        new InsertIntoAsFirst(5, d, null, null),
        new InsertBefore(5, d, null, null)
    });
    //(A=B, A=B, A>B, A=B)
    compare(new NodeUpdate[] {
        new InsertInto(4, d, null, null),
        new InsertInto(5, d, null, null),
    });
    //(A=B, A=B, A>B, A<B)
    compare(new NodeUpdate[] {
        new InsertInto(4, d, null, null),
        new InsertAfter(5, d, null, null),
    });
    //(A=B, A=B, A>B, A>B)
    compare(new NodeUpdate[] {
        new InsertAfter(4, d, null, null),
        new InsertInto(5, d, null, null),
    });

    // ***** Tests on neighboring target nodes
    //(A=B, A=B, A=B, A>B)
    compare(new NodeUpdate[] {
        new InsertAfter(7, d, null, null),
        new InsertInto(7, d, null, null),
    });
    //(A=B, A>B, A=B, A=B)
    compare(new NodeUpdate[] {
        new InsertInto(7, d, null, null),
        new InsertInto(4, d, null, null),
    });
    //(A>B, A=B, A=B, A=B)
    compare(new NodeUpdate[] {
        new DeleteNode(9, d, null),
        new DeleteNode(8, d, null),
    });
  }

  /**
   * Tests {@link InsertIntoAsFirst} on T and a {@link DeleteNode} on the attribute of T.
   */
  @Test
  public void shiftInsertIntoAsFirst() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
       new InsertIntoAsFirst(22, d, null, null),
       new DeleteNode(23, d, null),
    });
  }

  /**
   * Tests if {@link NodeUpdate} are ordered correctly for a target node T.
   */
  @Test
  public void compareOnSingleNode() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
        new InsertAfter(10, d, null, null),
        new InsertInto(10, d, null, null),
        new InsertIntoAsFirst(10, d, null, null),
        new InsertAttribute(10, d, null, null),
        new ReplaceValue(10, d, null, new byte[] {' '}),
        new RenameNode(10, d, null, null),
        new ReplaceNode(10, d, null, null),
        new DeleteNode(10, d, null),
        new InsertBefore(10, d, null, null),
    });
  }

  /**
   * Tests if {@link NodeUpdate} are ordered correctly for two sibling nodes.
   */
  @Test
  public void compareOnSiblings() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
        new InsertAfter(8, d, null, null),
        new InsertInto(8, d, null, null),
        new InsertIntoAsFirst(8, d, null, null),
        new InsertAttribute(8, d, null, null),
        new ReplaceValue(8, d, null, new byte[] {' '}),
        new RenameNode(8, d, null, null),
        new ReplaceNode(8, d, null, null),
        new DeleteNode(8, d, null),
        new InsertBefore(8, d, null, null),
        new InsertAfter(7, d, null, null),
        new InsertInto(7, d, null, null),
        new InsertIntoAsFirst(7, d, null, null),
        new InsertAttribute(7, d, null, null),
        new ReplaceValue(7, d, null, new byte[] {' '}),
        new RenameNode(7, d, null, null),
        new ReplaceNode(7, d, null, null),
        new DeleteNode(7, d, null),
        new InsertBefore(7, d, null, null),
    });
  }

  /**
   * Tests if {@link NodeUpdate} are ordered correctly for a target node T.
   */
  @Test
  public void compareComplexRelationships() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
        // 25
        new InsertAfter(18, d, null, null),
        new InsertInto(18, d, null, null),
        new InsertAfter(24, d, null, null),
        new InsertInto(24, d, null, null),
        new InsertAfter(25, d, null, null),
        new InsertInto(25, d, null, null),
        new InsertIntoAsFirst(25, d, null, null),
        new InsertAttribute(25, d, null, null),
        new ReplaceValue(25, d, null, new byte[] {' '}),
        new RenameNode(25, d, null, null),
        new ReplaceNode(25, d, null, null),
        new DeleteNode(25, d, null),
        new InsertBefore(25, d, null, null),
     // 24
        new InsertIntoAsFirst(24, d, null, null), //i==13
        new InsertAttribute(24, d, null, null),
        new ReplaceValue(24, d, null, new byte[] {' '}),
        new RenameNode(24, d, null, null),
        new ReplaceNode(24, d, null, null),
        new DeleteNode(24, d, null),
        new InsertBefore(24, d, null, null),
     // 23
        new InsertAfter(21, d, null, null),
        new InsertInto(21, d, null, null),
        new InsertAfter(22, d, null, null),
        new InsertInto(22, d, null, null),
        new InsertIntoAsFirst(22, d, null, null),
        new ReplaceValue(23, d, null, new byte[] {' '}),
        new RenameNode(23, d, null, null),
        new ReplaceNode(23, d, null, null),
        new DeleteNode(23, d, null),
     // 22
        new InsertAttribute(22, d, null, null),
        new ReplaceValue(22, d, null, new byte[] {' '}),
        new RenameNode(22, d, null, null),
        new ReplaceNode(22, d, null, null),
        new DeleteNode(22, d, null),
        new InsertBefore(22, d, null, null),
     // 21
        new InsertIntoAsFirst(21, d, null, null),
        new InsertAttribute(21, d, null, null),
        new ReplaceValue(21, d, null, new byte[] {' '}),
        new RenameNode(21, d, null, null),
        new ReplaceNode(21, d, null, null),
        new DeleteNode(21, d, null),
        new InsertBefore(21, d, null, null),
     // 20
        new InsertIntoAsFirst(18, d, null, null),
        new ReplaceValue(20, d, null, new byte[] {' '}),
        new RenameNode(20, d, null, null),
        new ReplaceNode(20, d, null, null),
        new DeleteNode(20, d, null),
     // 19
        new ReplaceValue(19, d, null, new byte[] {' '}),
        new RenameNode(19, d, null, null),
        new ReplaceNode(19, d, null, null),
        new DeleteNode(19, d, null),
     // 18
        new InsertAttribute(18, d, null, null),
        new ReplaceValue(18, d, null, new byte[] {' '}),
        new RenameNode(18, d, null, null),
        new ReplaceNode(18, d, null, null),
        new DeleteNode(18, d, null),
        new InsertBefore(18, d, null, null),
    });
  }

  /**
   * Tests order of {@link NodeUpdate} for 2 siblings with the first sibling having
   * a single child node.
   */
  @Test
  public void compareSiblingsComplex() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
        // node 28
        new InsertAfter(28, d, null, null),
        new InsertInto(28, d, null, null),
        new InsertIntoAsFirst(28, d, null, null),
        new InsertAttribute(28, d, null, null),
        new ReplaceValue(28, d, null, new byte[] {' '}),
        new RenameNode(28, d, null, null),
        new ReplaceNode(28, d, null, null),
        new DeleteNode(28, d, null),
        new InsertBefore(28, d, null, null),
        // node 26
        new InsertAfter(26, d, null, null),
        new InsertInto(26, d, null, null),
        new InsertIntoAsFirst(26, d, null, null),
        new InsertAttribute(26, d, null, null),
        new ReplaceValue(26, d, null, new byte[] {' '}),
        new RenameNode(26, d, null, null),
        new ReplaceNode(26, d, null, null),
        new DeleteNode(26, d, null),
        new InsertBefore(26, d, null, null),
    });
  }

  /**
   * Tests order of {@link NodeUpdate} for 2 siblings with the first sibling having
   * a single child node.
   */
  @Test
  public void compareSiblingsSimple() {
    final Data d = data(TESTDOCUMENT);
    compare(new NodeUpdate[] {
        new InsertInto(28, d, null, null),
        new InsertInto(26, d, null, null),
    });
    compare(new NodeUpdate[] {
        new InsertInto(31, d, null, null),
        new InsertInto(29, d, null, null),
    });
    compare(new NodeUpdate[] {
        new DeleteNode(28, d, null),
        new InsertInto(26, d, null, null),
    });
    // two empty siblings
    compare(new NodeUpdate[] {
        new InsertInto(8, d, null, null),
        new InsertInto(7, d, null, null),
    });
    // first sibling has attribute, second one is empty
    compare(new NodeUpdate[] {
        new DeleteNode(35, d, null),
        new InsertIntoAsFirst(33, d, null, null),
        new DeleteNode(34, d, null),
    });
    // two empty siblings
    compare(new NodeUpdate[] {
        new DeleteNode(8, d, null),
        new InsertInto(7, d, null, null),
    });
  }

  /**
   * Creates a database instance from the given string.
   * @param s database content string
   * @return database instance
   */
  private static Data data(final String s) {
    try {
      new CreateDB(NAME, s).execute(context);
    } catch(final BaseXException ex) {
      fail(Util.message(ex));
    }
    return context.data();
  }

  /**
   * Helper function to test score calculations of a list of update primitives. Tests
   * for both directions!
   *
   * @param order update primitives (ordered as expected, that means the first given
   * primitive is the first to be executed, hence has the highest score)
   */
  private static void compare(final NodeUpdate[] order) {
    final List<NodeUpdate> l = new ArrayList<>();
    Collections.addAll(l, order);
    final List<NodeUpdate> l2 = new ArrayList<>();
    for(final NodeUpdate p : order) l2.add(0, p);

    // primitives are sorted ASCENDING
    final NodeUpdateComparator c = new NodeUpdateComparator();
    Collections.sort(l, c);
    Collections.sort(l2, c);

    final int s = order.length;
    // check if primitives are ordered as expected
    for(int i = 0; i < s; i++) {
      // ordered list must be traversed back-to-front as elements are sorted ascending
      final NodeUpdate p = l.get(s - i - 1);
      final NodeUpdate p2 = l2.get(s - i - 1);
      if(!p.equals(order[i]) || !p2.equals(order[i]))
        fail("Unexpected order of updates at position: " + i);
    }
  }
}
