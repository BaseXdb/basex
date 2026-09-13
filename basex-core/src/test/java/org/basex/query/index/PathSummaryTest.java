package org.basex.query.index;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests optimizations that are based on the path summary and its statistics.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathSummaryTest extends SandboxTest {
  /** Drops the database. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
  }

  /** Paths are evaluated iteratively if all path nodes are located on the same level. */
  @Test public void iterativeLevels() {
    // the predicate prevents the conversion of the descendant step to child steps
    execute(new CreateDB(NAME, "<a><b><c/></b><b><c/></b></a>"));
    check("/a[b]//b/c", "<c/>\n<c/>", exists(IterPath.class));
    // nested elements: results must be sorted
    execute(new CreateDB(NAME, "<a><b><c/><b><c/></b></b></a>"));
    check("/a[b]//b/c", "<c/>\n<c/>", exists(CachedPath.class));
  }

  /** Comparisons with values that do not occur in the statistics are discarded. */
  @Test public void values() {
    execute(new CreateDB(NAME, "<a><b>x</b><b>y</b><c n='1'/><c n='5'/></a>"));
    check("//b[. = 'z']", "", empty());
    check("//a[b = 'z']", "", empty());
    check("//a[b = ('z', 'zz')]", "", empty());
    check("//a[b = 'z' and c]", "", empty());
    check("//a[b = 'z' or c]/name()", "a");
    check("//a[b = 'z' or c = '7']", "", empty());
    check("//b = 'z'", false, root(Bln.class));
    check("/a/b = 'z'", false, root(Bln.class));
    check("//b[. = 'x']", "<b>x</b>");
    check("//a[b = ('x', 'z')]/c[1]", "<c n=\"1\"/>");

    check("//c[@n > 5]", "", empty());
    check("//c[@n >= 5]", "<c n=\"5\"/>");
    check("//c/@n > 10", false, root(Bln.class));
    check("//c/@n < 0", false, root(Bln.class));
    check("//c[@n = '7']", "", empty());
    check("//c[@n >= 1][@n <= 5]/@n/string()", "1\n5");

    // updated statistics
    query("insert node <b>z</b> into /a");
    query("//b[. = 'z']", "<b>z</b>");
    query("//b = 'z'", true);
  }

  /** Steps with parent, ancestor and sibling axes are checked against the path summary. */
  @Test public void axes() {
    execute(new CreateDB(NAME, "<a><b><c/></b><d/></a>"));
    check("//c/parent::d", "", empty());
    check("//c/parent::b/name()", "b");
    check("//c/ancestor::d", "", empty());
    check("//c/ancestor::a/name()", "a");
    check("//b/following-sibling::c", "", empty());
    check("//b/following-sibling::d", "<d/>");
    check("//d/preceding-sibling::c", "", empty());
    check("//d/preceding-sibling::b/name()", "b");
    check("//c/following-sibling-or-self::c/name()", "c");
    check("//c[parent::d]", "", empty());
    check("//c[../../d]/name()", "c");
    check("//c[../d]", "", empty());
  }

  /**
   * Path nodes of database nodes that are bound to the context are resolved.
   * @throws Exception exception
   */
  @Test public void nonDocumentRoots() throws Exception {
    execute(new CreateDB(NAME, "<a><b><c>1</c></b><d>2</d></a>"));
    final Data data = context.data();
    final DBNode a = new DBNode(data, 1), b = new DBNode(data, 2);
    check("descendant::d", a, "<d>2</d>", "//@axis = 'child'", "not(//@axis = 'descendant')");
    check("descendant::c", a, "<c>1</c>", "count(//@axis[. = 'child']) = 2");
    check("descendant::d", b, "", empty());
    check("c[. = '2']", b, "", empty());
    check("c[. = '1']", b, "<c>1</c>");
  }

  /**
   * Checks the query plan and the result of a query with a context node.
   * @param query query
   * @param node context node
   * @param expected expected result
   * @param tests queries on the query plan
   * @throws Exception exception
   */
  private static void check(final String query, final DBNode node, final String expected,
      final String... tests) throws Exception {
    try(QueryProcessor qp = new QueryProcessor(query, context).context(node)) {
      qp.optimize();
      final FNode plan = qp.toXml(), doc = FDoc.build().node(plan).finish();
      assertEquals(expected, qp.value().serialize().toString(), query);
      for(final String test : tests) {
        try(QueryProcessor qp2 = new QueryProcessor(test, context).context(doc)) {
          assertEquals(Bln.TRUE, qp2.value(), query + ": " + test + Prop.NL + serialize(plan));
        }
      }
    }
  }

  /** Names are resolved in databases with namespace declarations. */
  @Test public void namespaces() {
    execute(new CreateDB(NAME, "<a xmlns:p='u' xmlns:xsi='w'><p:b>1</p:b><b>2</b></a>"));
    check("declare namespace q = 'u'; //q:b/string()", 1, "not(//@axis = 'descendant')");
    check("//b/string()", 2, "not(//@axis = 'descendant')");
    check("count(//*:b)", 2);
    check("//Q{v}b", "", empty());
    check("//Q{u}b = '3'", false, root(Bln.class));
    // the local name occurs in two namespaces: no statistics
    check("//*:b = '2'", true, empty(Bln.class));
    check("//*:b = '3'", false, empty(Bln.class));

    // default namespace and prefixed declarations
    execute(new CreateDB(NAME, "<a xmlns='d' xmlns:p='u'><b>1</b><p:b>2</p:b></a>"));
    check("declare default element namespace 'd'; //b/string()", 1,
        "not(//@axis = 'descendant')");
    check("//b", "", empty());
    check("declare namespace p = 'u'; //p:b/string()", 2, "not(//@axis = 'descendant')");
    check("declare default element namespace 'd'; count(//b[. = '2'])", 0, empty(IterPath.class));

    // prefix is bound to different namespaces
    execute(new CreateDB(NAME, "<a><p:b xmlns:p='u'>1</p:b><p:b xmlns:p='v'>2</p:b></a>"));
    check("declare namespace p = 'u'; //p:b/string()", 1, "//@axis = 'descendant'");
    // a single lexical name: statistics are available
    check("count(//*:b[. = '3'])", 0, empty(IterPath.class));
  }

  /** The leaf flags and value statistics are retrieved for the addressed path. */
  @Test public void indexAccess() {
    execute(new CreateDB(NAME, "<a><b>x</b><c><b>y<i/></b></c></a>"));
    check("/a/b[. = 'x']", "<b>x</b>", exists(ValueAccess.class));
    check("//c/b[. = 'y']", "<b>y<i/></b>", empty(ValueAccess.class));
    check("count(//c/b[. = 'y'])", 1, empty(ValueAccess.class));

    execute(new CreateDB(NAME, "<a><b n='5'/><c n='x'/></a>"));
    check("//b[@n = 5]", "<b n=\"5\"/>", exists(ValueAccess.class));
    check("//b[@n >= 6]", "", empty());
    check("//b[@n >= 5]", "<b n=\"5\"/>", exists(ValueAccess.class));
  }
}
