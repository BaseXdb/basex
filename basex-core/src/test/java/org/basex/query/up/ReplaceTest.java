package org.basex.query.up;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Tests on the various replace operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ReplaceTest extends SandboxTest {
  /**
   * Replaces the first document in a database, using lazy replace.
   */
  @Test public void lazyReplace() {
    prepare("<a/>", "<c/>");
    query(_DB_PUT.args(NAME, " <a/>", "a.xml"));
    query("a, c", "<a/>\n<c/>");
    query(_DB_PUT.args(NAME, " <c/>", "c.xml"));
    query("a, c", "<a/>\n<c/>");
  }

  /**
   * Replaces the first document in a database, using rapid replace.
   */
  @Test public void rapidReplace() {
    prepare("<a/>", "<c/>");
    query(_DB_PUT.args(NAME, " <a><b/></a>", "a.xml"));
    query("a, c", "<a><b/></a>\n<c/>");
    query(_DB_PUT.args(NAME, " <c><d/></c>", "c.xml"));
    query("a, c", "<a><b/></a>\n<c><d/></c>");
  }

  /**
   * Replaces the first document in a database.
   */
  @Test public void replaceWithNs() {
    // first document: introduce namespace
    prepare("<a/>", "<c/>");
    query(_DB_PUT.args(NAME, " <a xmlns='a'/>", "a.xml"));
    query("*:a, *:c", "<a xmlns=\"a\"/>\n<c/>");
    // first document: remove namespace
    prepare("<a xmlns='a'/>", "<c/>");
    query(_DB_PUT.args(NAME, " <a/>", "a.xml"));
    query("*:a, *:c", "<a/>\n<c/>");
    // first document: keep namespace
    prepare("<a xmlns='a'/>", "<c/>");
    query(_DB_PUT.args(NAME, " <a xmlns='a'/>", "a.xml"));
    query("*:a, *:c", "<a xmlns=\"a\"/>\n<c/>");

    // second document: introduce namespace
    prepare("<a/>", "<c/>");
    query(_DB_PUT.args(NAME, " <c xmlns='c'/>", "c.xml"));
    query("*:a, *:c", "<a/>\n<c xmlns=\"c\"/>");
    // second document: remove namespace
    prepare("<a/>", "<c xmlns=\"c\"/>");
    query(_DB_PUT.args(NAME, " <c/>", "c.xml"));
    query("*:a, *:c", "<a/>\n<c/>");
    // second document: keep namespace
    prepare("<a/>", "<c xmlns=\"c\"/>");
    query(_DB_PUT.args(NAME, " <c xmlns='c'/>", "c.xml"));
    query("*:a, *:c", "<a/>\n<c xmlns=\"c\"/>");
  }

  /**
   * Replaces nodes with namespaces, using lazy replace if possible.
   */
  @Test public void lazyReplaceNs() {
    final String doc = "<a xmlns='u' xmlns:p='v' p:x='1'><b>x</b></a>";
    final String ns = "<a xmlns=\"u\" xmlns:p=\"v\" p:x=\"1\">";
    final String b = "replace node " + _DB_GET.args(NAME) + "//*:b with ";
    final UnaryOperator<String> put = d -> _DB_PUT.args(NAME, " " + d, "a.xml");

    // identical document
    lazyReplace(doc, put.apply(doc), ns + "<b>x</b></a>", true);
    // value updates
    lazyReplace(doc, put.apply("<a xmlns='u' xmlns:p='v' p:x='2'><b>y</b></a>"),
        "<a xmlns=\"u\" xmlns:p=\"v\" p:x=\"2\"><b>y</b></a>", true);
    // redundant namespace declaration
    lazyReplace(doc, b + "<b xmlns='u'>y</b>", ns + "<b>y</b></a>", true);
    // undeclared default namespace
    lazyReplace("<a xmlns='u'><b xmlns=''>x</b></a>", b + "<b>y</b>",
        "<a xmlns=\"u\"><b xmlns=\"\">y</b></a>", true);

    // different namespace URI
    lazyReplace(doc, put.apply("<a xmlns='w' xmlns:p='v' p:x='1'><b>x</b></a>"),
        "<a xmlns=\"w\" xmlns:p=\"v\" p:x=\"1\"><b>x</b></a>", false);
    // different prefix
    lazyReplace(doc, put.apply("<a xmlns='u' xmlns:q='v' q:x='1'><b>x</b></a>"),
        "<a xmlns=\"u\" xmlns:q=\"v\" q:x=\"1\"><b>x</b></a>", false);
    // additional namespace declaration
    lazyReplace(doc, b + "<b xmlns='u' xmlns:r='z'>x</b>", ns + "<b xmlns:r=\"z\">x</b></a>",
        false);
    // namespace introduced in a database without namespaces
    lazyReplace("<a><b/></a>", b + "<b xmlns='u'/>", "<a><b xmlns=\"u\"/></a>", false);
    // no namespace
    lazyReplace(doc, b + "<b>x</b>", ns + "<b xmlns=\"\">x</b></a>", false);
  }

  /**
   * Runs an update on a single document and checks if the node IDs have been kept.
   * @param doc document
   * @param update update query
   * @param result expected document
   * @param keep indicates if node IDs are expected to be kept
   */
  private static void lazyReplace(final String doc, final String update, final String result,
      final boolean keep) {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", doc));
    final String ids = _DB_GET.args(NAME) + "/(descendant-or-self::node(), .//@*) ! " +
        _DB_NODE_ID.args(" .");
    final String before = query(ids);
    query(update);
    query(_DB_GET.args(NAME) + "/*", result);
    assertEquals(keep, before.equals(query(ids)));
  }

  /**
   * Prepares the updates.
   * @param docs documents to add
   */
  private static void prepare(final String... docs) {
    execute(new CreateDB(NAME));
    for(final String doc : docs) {
      // choose first letter of input as document name
      execute(new Add(doc.replaceAll("^.*?(\\w).*", "$1") + ".xml", doc));
    }
  }
}