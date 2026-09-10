package org.basex.index;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the deferred updates of the value indexes: additions and key removals are applied
 * once per transaction.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdatableDiskValuesTest extends SandboxTest {
  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.AUTOFLUSH, true);
  }

  /**
   * An entry that is changed twice in one transaction (a rename re-adds the entries of a
   * node): the second change discards the pending addition of the first one.
   */
  @Test public void changedTwice() {
    execute(new CreateDB(NAME, "<x><a>one</a><a b='p q'>two</a></x>"));
    final String db = _DB_GET.args(NAME);
    query("let $a := " + db + "//a[. = 'one'] return (" +
      "replace value of node $a with 'three', rename node $a as 'c')");
    text("one");
    text("three", "three");
    text("two", "two");
    // same for tokens
    query("let $b := " + db + "//@b return (" +
      "replace value of node $b with 'q r', rename node $b as 'd')");
    token("p");
    token("q", "q r");
    token("r", "q r");
    check("//c[text() = 'three']");
    check("//a[contains-token(@d, 'r')]");
  }

  /**
   * A key that is emptied and re-added in one transaction.
   */
  @Test public void emptiedAndReadded() {
    execute(new CreateDB(NAME, "<x><a>one</a><a>two</a><a>three</a></x>"));
    final String db = _DB_GET.args(NAME);
    // 'one' is emptied and re-added by another node, 'two' is emptied and stays empty
    query("replace value of node " + db + "//a[. = 'one'] with 'x', " +
      "replace value of node " + db + "//a[. = 'two'] with 'one'");
    text("one", "one");
    text("two");
    text("x", "x");
    text("three", "three");
    check("//a[text() = 'one']");
    // renaming an element and back yields the original state
    set(MainOptions.TEXTINCLUDE, "a");
    execute(new CreateDB(NAME, "<x><a>one</a><b>two</b></x>"));
    query("rename node " + db + "//a as 'c', rename node " + db + "//b as 'a'");
    text("one");
    text("two", "two");
    query("rename node " + db + "//c as 'a', rename node " + db + "//a[. = 'two'] as 'b'");
    text("one", "one");
    text("two");
    set(MainOptions.TEXTINCLUDE, "");
  }

  /**
   * Many nodes are inserted, changed and deleted in single transactions.
   */
  @Test public void bulk() {
    execute(new CreateDB(NAME, "<x/>"));
    final String db = _DB_GET.args(NAME);
    query("insert node (for $i in 1 to 500 return <a t='t{ $i } u'>v{ $i }</a>) into " +
      db + "/x");
    check("//a[text() = 'v250']");
    check("//a[contains-token(@t, 't250')]");
    query("for $a in " + db + "//a return replace value of node $a with 'w' || $a");
    text("v250");
    text("wv250", "wv250");
    check("//a[text() = 'wv250']");
    query("delete node " + db + "//a[position() mod 2 = 0]");
    check("//a[text() = 'wv250']");
    check("//a[text() = 'wv251']");
    check("//a[contains-token(@t, 'u')]");
    query("delete node " + db + "//a");
    text("wv251");
    token("u");
    query(_DB_INFO.args(NAME) + "//textindex/text()", true);
  }

  /**
   * Pending changes are written when the database is closed without autoflush.
   */
  @Test public void closeWithoutFlush() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    set(MainOptions.AUTOFLUSH, false);
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'two'");
    execute(new Close());
    text("one");
    text("two", "two");
    execute(new Close());
    execute(new Open(NAME));
    text("two", "two");
  }

  /**
   * Checks a text index lookup.
   * @param text text
   * @param results expected results
   */
  private static void text(final String text, final String... results) {
    query(_DB_TEXT.args(NAME, text) + " ! string()", String.join("\n", results));
  }

  /**
   * Checks a token index lookup.
   * @param token token
   * @param results expected results
   */
  private static void token(final String token, final String... results) {
    query(_DB_TOKEN.args(NAME, token) + " ! string()", String.join("\n", results));
  }

  /**
   * Compares index access with a scan.
   * @param path path
   */
  private static void check(final String path) {
    queryIndexScan(path);
  }
}
