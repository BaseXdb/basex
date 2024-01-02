package org.basex.query.up;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Tests on the various replace operations.
 *
 * @author BaseX Team 2005-24, BSD License
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