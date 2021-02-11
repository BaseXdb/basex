package org.basex.query.up;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Tests on the various replace operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ReplaceTest extends SandboxTest {
  /**
   * Replaces the first document in a database, using lazy replace.
   */
  @Test public void lazyReplace() {
    prepare("<a/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "a.xml", " <a/>"));
    query("a, c", "<a/>\n<c/>");
    query(_DB_REPLACE.args(NAME, "c.xml", " <c/>"));
    query("a, c", "<a/>\n<c/>");
  }

  /**
   * Replaces the first document in a database, using rapid replace.
   */
  @Test public void rapidReplace() {
    prepare("<a/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "a.xml", " <a><b/></a>"));
    query("a, c", "<a>\n<b/>\n</a>\n<c/>");
    query(_DB_REPLACE.args(NAME, "c.xml", " <c>\n<d/>\n</c>"));
    query("a, c", "<a>\n<b/>\n</a>\n<c>\n<d/>\n</c>");
  }

  /**
   * Replaces the first document in a database.
   */
  @Test public void replaceWithNs() {
    // first document: introduce namespace
    prepare("<a/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "a.xml", " <a xmlns='a'/>"));
    query("*:a, *:c", "<a xmlns=\"a\"/>\n<c/>");
    // first document: remove namespace
    prepare("<a xmlns='a'/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "a.xml", " <a/>"));
    query("*:a, *:c", "<a/>\n<c/>");
    // first document: keep namespace
    prepare("<a xmlns='a'/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "a.xml", " <a xmlns='a'/>"));
    query("*:a, *:c", "<a xmlns=\"a\"/>\n<c/>");

    // second document: introduce namespace
    prepare("<a/>", "<c/>");
    query(_DB_REPLACE.args(NAME, "c.xml", " <c xmlns='c'/>"));
    query("*:a, *:c", "<a/>\n<c xmlns=\"c\"/>");
    // second document: remove namespace
    prepare("<a/>", "<c xmlns=\"c\"/>");
    query(_DB_REPLACE.args(NAME, "c.xml", " <c/>"));
    query("*:a, *:c", "<a/>\n<c/>");
    // second document: keep namespace
    prepare("<a/>", "<c xmlns=\"c\"/>");
    query(_DB_REPLACE.args(NAME, "c.xml", " <c xmlns='c'/>"));
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