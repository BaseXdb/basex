package org.basex.query.value.jnode;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.junit.jupiter.api.*;

/**
 * {@link JNode} tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JNodeTest extends SandboxTest {
  /** Empty node. */
  private static JNode emptyNode;

  /** String representation. */
  private static String string = "{\"a\":{\"x\":1,\"y\":{\"z\":2}},\"b\":{\"x\":{\"y\":2}}}";
  /** JTree query. */
  private static String jtree = JTREE.args(" " + string);
  /** Node: root. */
  private static JNode rootNode;
  /** Node: root.a. */
  private static JNode aNode;
  /** Node: root.a.x. */
  private static JNode axNode;
  /** Node: root.a.y. */
  private static JNode ayNode;
  /** Node: root.a.y.z. */
  private static JNode ayzNode;
  /** Node: root.b. */
  private static JNode bNode;
  /** Node: root.b.x. */
  private static JNode bxNode;
  /** Node: root.b.x.y. */
  private static JNode bxyNode;
  /** All nodes in document order. */
  private static JNode[] nodes;

  /**
   * Creates nodes for testing.
   * @throws QueryException query exception
   */
  @BeforeAll public static void setup() throws QueryException {
    // maps (equal strings with single characters have the same identity)
    final XQMap ay = new MapBuilder().put(Str.get("z"), Itr.get(2)).map();
    final XQMap bx = new MapBuilder().put(Str.get("y"), Itr.get(2)).map();
    final XQMap a = new MapBuilder().put(Str.get("x"), Itr.get(1)).put(Str.get("y"), ay).map();
    final XQMap b = new MapBuilder().put(Str.get("x"), bx).map();
    final XQMap root = new MapBuilder().put(Str.get("a"), a).put(Str.get("b"), b).map();

    // tree hierarchy
    emptyNode = new JNode(XQMap.empty());

    rootNode = new JNode(root);
    aNode = new JNode(rootNode, 0);
    axNode = new JNode(aNode, 0);
    ayNode = new JNode(aNode, 1);
    ayzNode = new JNode(ayNode, 0);

    bNode = new JNode(rootNode, 1);
    bxNode = new JNode(bNode, 0);
    bxyNode = new JNode(bxNode, 0);

    // {"a":{"x":1,"y":{"z":2}},"b":{"x":{"y":2}}}
    nodes = new JNode[] { rootNode, aNode, axNode, ayNode, ayzNode, bNode, bxNode, bxyNode };
  }

  /**
   * Value atomization.
   * @throws QueryException query exception
   */
  @Test public void atomValue() throws QueryException {
    try(QueryContext qc = new QueryContext(context)) {
      assertEquals(Itr.get(1), axNode.atomValue(qc, null));
      assertEquals(Itr.get(2), ayzNode.atomValue(qc, null));
      assertEquals(Itr.get(2), bxyNode.atomValue(qc, null));

      try {
        rootNode.atomValue(qc, null);
        fail("Error expected");
      } catch(final QueryException ex) {
        assertEquals(QueryError.FIATOMIZE_X, ex.error());
      }
    }
  }

  /**
   * Item atomization.
   * @throws QueryException query exception
   */
  @Test public void atomItem() throws QueryException {
    try(QueryContext qc = new QueryContext(context)) {
      assertEquals(Itr.get(1), axNode.atomItem(qc, null));
    }
  }

  /** Instance check. */
  @Test public void instanceOf() {
    assertTrue(rootNode.instanceOf(NodeType.JNODE, false));
    assertTrue(rootNode.instanceOf(NodeType.get(null, null), false));
    assertTrue(rootNode.instanceOf(NodeType.get(null, Types.ITEM_ZM), false));
    assertTrue(rootNode.instanceOf(NodeType.get(null, Types.ITEM_O), false));
    assertTrue(rootNode.instanceOf(NodeType.get(null, Types.MAP_O), false));
    assertFalse(rootNode.instanceOf(NodeType.get(Str.KEY, Types.MAP_O), false));

    assertTrue(aNode.instanceOf(NodeType.get(null, Types.MAP_O), false));
    assertTrue(aNode.instanceOf(NodeType.get(Str.get("a"), Types.MAP_O), false));
    assertFalse(aNode.instanceOf(NodeType.get(Str.get("b"), Types.MAP_O), false));
    assertFalse(aNode.instanceOf(NodeType.get(null, Types.INTEGER_O), false));
    assertFalse(aNode.instanceOf(NodeType.get(Str.get("a"), Types.INTEGER_O), false));

    assertTrue(axNode.instanceOf(NodeType.get(null, Types.INTEGER_O), false));
    assertTrue(axNode.instanceOf(NodeType.get(Str.get("x"), Types.INTEGER_O), false));
  }

  /** Equality. */
  @Test public void is() {
    for(int i = 0; i < nodes.length; i++) {
      for(int j = 0; j < nodes.length; j++) {
        assertEquals(i == j, nodes[i].is(nodes[j]));
        assertEquals(i == j, nodes[j].is(nodes[i]));
      }
    }

    assertFalse(emptyNode.is(rootNode));
    assertFalse(rootNode.is(emptyNode));

    query(jtree + " ! (a is a)", true);
    query(jtree + " ! (a is b)", false);
    query(jtree + "/a ! (. is .)", true);
    query(jtree + " ! (*/.. is .)", true);
    query(jtree + "//z ! (../z is .)", true);

    query("jtree([ 1, 2 ]) ! (jnode(1) is jnode(1))", true);
    query("jtree([ 1, 2 ]) ! (jnode(1) is jnode(2))", false);
    query("jtree([ 1, 2 ]) ! (jnode(2) is jnode(1))", false);
  }

  /** Comparison. */
  @Test public void compare() {
    for(final JNode node : nodes) assertEquals(node.compare(node), 0);
    for(int i = 0; i < nodes.length; i++) {
      for(int j = i + 1; j < nodes.length; j++) {
        assertEquals(-1, nodes[i].compare(nodes[j]));
        assertEquals(1,  nodes[j].compare(nodes[i]));
      }
    }

    assertTrue(rootNode.compare(emptyNode) != 0);
    assertEquals(0, rootNode.compare(emptyNode) + emptyNode.compare(rootNode));

    query(jtree + " ! (. << .)", false);
    query(jtree + " ! (. >> .)", false);
    query(jtree + " ! (a << a)", false);
    query(jtree + " ! (a >> a)", false);
    query(jtree + " ! (a << b)", true);
    query(jtree + " ! (a >> b)", false);
    query(jtree + "/a ! (. << .)", false);
    query(jtree + " ! (*/.. << .)", false);
    query(jtree + " ! (*/.. >> .)", false);

    query("jtree([ 1, 2 ]) ! (jnode(1) is jnode(1))", true);
    query("jtree([ 1, 2 ]) ! (jnode(1) << jnode(2))", true);
    query("jtree([ 1, 2 ]) ! (jnode(1) >> jnode(2))", false);
    query("jtree([ 1, 2 ]) ! (jnode(2) << jnode(1))", false);
    query("jtree([ 1, 2 ]) ! (jnode(2) >> jnode(1))", true);
  }

  /** Root. */
  @Test public void root() {
    for(int i = 1; i < nodes.length; i++) {
      assertEquals(rootNode, nodes[i].root());
    }
  }

  /** Parent. */
  @Test public void parent() {
    assertEquals(null, rootNode.parent());
    assertEquals(rootNode, aNode.parent());
    assertEquals(rootNode, bNode.parent());
    assertEquals(aNode.parent(), bNode.parent());
    assertNotEquals(aNode.parent(), ayzNode.parent());
  }

  /** Child check. */
  @Test public void hasChildren() {
    assertTrue(rootNode.hasChildren());
    assertTrue(aNode.hasChildren());
    assertFalse(bxyNode.hasChildren());
  }

  /**
   * Deep equality.
   * @throws QueryException query exception
   */
  @Test public void deepEqual() throws QueryException {
    final DeepEqual de = new DeepEqual();
    assertTrue(rootNode.deepEqual(rootNode, de));
    assertTrue(ayzNode.deepEqual(bxyNode, de));
    assertFalse(rootNode.deepEqual(aNode, de));
  }


  /**
   * Serialization.
   * @throws QueryIOException query I/O exception
   */
  @Test public void serialize() throws QueryIOException {
    assertEquals(string, rootNode.serialize().toString());
    query(jtree, string);
    query(jtree + "/.", string);
  }

  /** Node tests. */
  @Test public void nodeTest() {
    query(jtree + "//gnode() => count()", 7);
    query(jtree + "//node() => count()", 0);
    query(jtree + "//element() => count()", 0);
    query(jtree + "//jnode() => count()", 7);
    query(jtree + "//jnode(*) => count()", 7);
    query(jtree + "//jnode(*, *) => count()", 7);
    query(jtree + "//jnode(x) => count()", 2);
    query(jtree + "//jnode(x, *) => count()", 2);
    query(jtree + "//jnode(*, xs:integer) => count()", 3);
    query(jtree + "//jnode(x, xs:integer) => count()", 1);
    query(jtree + "//jnode('x', xs:integer) => count()", 1);
    query(jtree + "//jnode(\"x\", xs:integer) => count()", 1);
    query(jtree + "//jnode(\"x\", xs:numeric) => count()", 1);
    query(jtree + "//jnode(x, xs:anyAtomicType) => count()", 1);
    query(jtree + "//jnode(x, item()) => count()", 2);
    query(jtree + "//jnode(x, map(*)) => count()", 1);
    query(jtree + "//jnode(x, item()*) => count()", 2);

    query(jtree + "//jnode(X)", "");
    query(jtree + "//jnode(#Q{}x)", "");
    query(jtree + "//jnode(1)", "");
    query(jtree + "//jnode(1.0)", "");
    query(jtree + "//jnode(1e0)", "");
    query(jtree + "//jnode(0xF)", "");
    query(jtree + "//jnode(true())", "");
    query(jtree + "//jnode(false())", "");

    error("[ 8, 9 ]/jnode(-0x8000000000000000)", RANGE_X);
    query("[ 8, 9 ]/jnode(-0x7FFFFFFFFFFFFFFF)", "");
    query("[ 8, 9 ]/jnode(-1)", "");
    query("[ 8, 9 ]/jnode(0)", "");
    query("[ 8, 9 ]/jnode(1)", 8);
    query("[ 8, 9 ]/jnode(2)", 9);
    query("[ 8, 9 ]/jnode(3)", "");
    query("[ 8, 9 ]/jnode(0x7FFFFFFF)", "");
    query("[ 8, 9 ]/jnode(0x7FFFFFFFFFFF)", "");
    query("[ 8, 9 ]/jnode(0x7FFFFFFFFFFFFFFF)", "");
    error("[ 8, 9 ]/jnode(0x8000000000000000)", RANGE_X);
  }

  /** Attribute step. */
  @Test public void axisAttribute() {
    query(jtree + "/@*", "");
    query(jtree + "/attribute::*", "");
    query(jtree + "/attribute::attribute()", "");
    query(jtree + "/attribute::attribute(a)", "");
  }

  /** Child step. */
  @Test public void axisChild() {
    query(jtree + "/b", "{\"x\":{\"y\":2}}");
    query(jtree + "/b/x", "{\"y\":2}");
    query(jtree + "/b/x/y", 2);

    query(jtree + "/*", "{\"x\":1,\"y\":{\"z\":2}}\n{\"x\":{\"y\":2}}");
    query(jtree + "/*/*", "1\n{\"z\":2}\n{\"y\":2}");
    query(jtree + "/*/*/*", "2\n2");
    query(jtree + "/*/*/*/*", "");

    query(jtree + "/* => count()", 2);
    query(jtree + "/*/* => count()", 3);
    query(jtree + "/*/*/* => count()", 2);
    query(jtree + "/*/*/*/* => count()", 0);
  }

  /** Descendant step. */
  @Test public void axisDescendant() {
    query(jtree + "//b", "{\"x\":{\"y\":2}}");
    query(jtree + "//x", "1\n{\"y\":2}");
    query(jtree + "//y", "{\"z\":2}\n2");

    query(jtree + "//* => count()", 7);
    query(jtree + "//*//* => count()", 5);
    query(jtree + "//*//*//* => count()", 2);
    query(jtree + "//*//*//*//* => count()", 0);
    query(jtree + "//x => count()", 2);
  }

  /** Descendant-or-self step. */
  @Test public void axisDescendantOrSelf() {
    query(jtree + "/descendant-or-self::* => count()", 8);
    query(jtree + "/descendant-or-self::*/descendant-or-self::* => count()", 8);
    query(jtree + "/descendant-or-self::*/descendant-or-self::*/descendant-or-self::* "
        + "=> count()", 8);
  }

  /** Ancestor step. */
  @Test public void axisAncestor() {
    query(jtree + "//y/ancestor::y", "");
    query(jtree + "//y/ancestor::x", "{\"y\":2}");
    query(jtree + "//y/ancestor::b", "{\"x\":{\"y\":2}}");
    query(jtree + "//y/ancestor::* => count()", 4);
    query(jtree + "//y/ancestor::gnode() => count()", 4);
  }

  /** Ancestor-or-self step. */
  @Test public void axisAncestorOrSelf() {
    query(jtree + "//y/ancestor-or-self::y", "{\"z\":2}\n2");
    query(jtree + "//y/ancestor-or-self::x", "{\"y\":2}");
    query(jtree + "//y/ancestor-or-self::b", "{\"x\":{\"y\":2}}");
    query(jtree + "//y/ancestor-or-self::* => count()", 6);
    query(jtree + "//y/ancestor-or-self::gnode() => count()", 6);
  }

  /** Self step. */
  @Test public void axisSelf() {
    query(jtree + "//*/self::y", "{\"z\":2}\n2");
    query(jtree + "//*/self::z", 2);
    query(jtree + "//*/self::w", "");

    query(jtree + "//z/self::z", 2);
    query(jtree + "//z/self::*", 2);
    query(jtree + "//z/self::jnode()", 2);
    query(jtree + "//z/self::gnode()", 2);
    query(jtree + "//z/self::node()", "");
  }

  /** Parent step. */
  @Test public void axisParent() {
    query(jtree + "//z/..", "{\"z\":2}");
    query(jtree + "//z/../..", "{\"x\":1,\"y\":{\"z\":2}}");
    query(jtree + "/*/..", string);
    query(jtree + "/*/../..", "");
  }

  /** Preceding-sibling step. */
  @Test public void axisPrecedingSibling() {
    query(jtree + "//y/preceding-sibling::*", 1);
    query(jtree + "//x/preceding-sibling::*", "");
  }

  /** Following-sibling step. */
  @Test public void axisFollowingSibling() {
    query(jtree + "//y/following-sibling::*", "");
    query(jtree + "//x/following-sibling::*", "{\"z\":2}");
  }

  /** Following step. */
  @Test public void axisFollowing() {
    query(jtree + "/*/following::* => count()", 3);
    query(jtree + "/*/following::* => count()", 3);
    query(jtree + "//z/following::* => count()", 3);
    query(jtree + "//b/following::* => count()", 0);
    query(jtree + "//a/following::* => count()", 3);
 }

  /** Preceding step. */
  @Test public void axisPreceding() {
    query(jtree + "/*/preceding::* => count()", 4);
    query(jtree + "//*/preceding::* => count()", 4);
    query(jtree + "//z/preceding::*", 1);
    query(jtree + "//b/preceding::* => count()", 4);
    query(jtree + "//a/preceding::* => count()", 0);
 }
}
