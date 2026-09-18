package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the inspection of database structures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectionTest extends SandboxTest {
  /** Test document: doc (0), a (1), @b (2), text (3), comment (4), pi (5), h (6), i (7). */
  private static final String DOC = "<a b='c'>d<!--e--><?f g?><h><i/></h></a>";

  /** Drops the test database. */
  @AfterEach public void finish() {
    set(MainOptions.MAINMEM, false);
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, false);
    execute(new DropDB(NAME));
  }

  /** Consistent databases. */
  @Test public void valid() {
    execute(new CreateDB(NAME, DOC));
    assertTrue(execute(new Inspect()).contains("No inconsistencies found."));
    execute(new Close());
    assertTrue(execute(new Inspect(NAME)).contains("No inconsistencies found."));

    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME, DOC));
    execute(new XQuery("delete node db:get('" + NAME + "')//@b"));
    execute(new XQuery("insert node <x/> into db:get('" + NAME + "')/a"));
    final String info = execute(new Inspect());
    assertTrue(info.contains("id-pre: OK"), info);
    assertTrue(info.contains("No inconsistencies found."), info);
  }

  /** Consistent databases after updates, with all value indexes. */
  @Test public void updates() {
    set(MainOptions.TOKENINDEX, true);
    for(final boolean mainmem : new boolean[] { false, true }) {
      for(final boolean updindex : new boolean[] { false, true }) {
        set(MainOptions.MAINMEM, mainmem);
        set(MainOptions.UPDINDEX, updindex);
        execute(new CreateDB(NAME, "src/test/resources/factbook.zip"));
        execute(new Add("ns.xml", "<x:a xmlns:x='urn:x' xmlns='urn:d' x:b='1'>" +
            "<c xmlns:y='urn:y' y:d='t u t'><?pi c?><!--c--></c></x:a>"));
        assertValid();

        final String db = "db:get('" + NAME + "')";
        for(final String query : new String[] {
          "insert node <y:n xmlns:y='urn:y' y:a='1' b='t u t'>text</y:n> into (" + db + "//*)[1]",
          "delete node (" + db + "//@*)[position() < 10]",
          "replace value of node (" + db + "//text())[1] with 'new'",
          "replace node (" + db + "//text())[2] with <r a='b'/>",
          "rename node (" + db + "//*)[3] as 'renamed'",
          "insert node 'text' before (" + db + "//*)[5]",
          "insert node attribute { QName('urn:z', 'z:a') } { 'v w' } into (" + db + "//*)[7]",
          "delete node " + db + "//*:c",
          "db:put('" + NAME + "', <doc>x</doc>, 'put.xml')",
          "db:delete('" + NAME + "', 'ns.xml')"
        }) {
          execute(new XQuery(query));
          assertValid();
        }
        execute(new Optimize());
        assertValid();
        if(!mainmem) {
          execute(new Close());
          assertValid();
        }
      }
    }
  }

  /**
   * Asserts that the inspected database is consistent.
   */
  private static void assertValid() {
    final String info = execute(new Inspect(context.data() != null ? null : NAME));
    assertTrue(info.contains("No inconsistencies found."), info);
  }

  /** Corrupt table entries. */
  @Test public void corrupt() {
    check(data -> data.table.write1(4, 0, 7), "node-kind: 1 (first: pre 4)");
    check(data -> data.table.write4(3, 8, 10), "parent-reference: 1 (first: pre 3)");
    check(data -> data.table.write4(1, 8, 3), "parent-range: 3 (first: pre 4)");
    check(data -> data.table.write4(7, 4, 6), "parent-nesting: 1 (first: pre 7)");
    check(data -> data.table.write4(0, 8, 99), "node-size: 1 (first: pre 0)");
    check(data -> data.table.write2(1, 1, 99), "name-reference: 1 (first: pre 1)");
    check(data -> name(data, "1x"), "name-syntax: 1 (first: pre 6)");
    check(data -> name(data, "p:h"), "namespace-binding: 1 (first: pre 6)");
    check(data -> name(data, "z"), "path-index: 4 (first: pre 6)");
    check(data -> data.elemNames.stats(1).count++, "name-statistics: 1");
    check(data -> data.table.write2(6, 1, data.nameId(6) | 0x8000),
        "namespace-flag: 1 (first: pre 6)");
    check(data -> data.table.write5(3, 3, 999), "value-reference: 1 (first: pre 3)");
    check(data -> text(data, 4, "a--b"), "value-syntax: 1 (first: pre 4)");
    check(data -> text(data, 3, ""), "text-structure: 1 (first: pre 3)");
    check(data -> text(data, 3, "zzz"), "text-index: 2 (first: pre 3)");
    check(data -> text(data, 0, "/a//b"), "document-path: 1 (first: pre 0)");
    check(data -> data.table.write4(5, 12, 1), "node-id: 1 (first: pre 5)");
    check(data -> data.meta.ndocs = 2, "document-count: 1");
  }

  /**
   * Assigns a new name to the element at PRE value 6.
   * @param data data reference
   * @param name name
   */
  private static void name(final Data data, final String name) {
    data.table.write2(6, 1, data.elemNames.store(Token.token(name)));
  }

  /**
   * Assigns a new value to a node of a main-memory database.
   * @param data data reference
   * @param pre PRE value
   * @param value value
   */
  private static void text(final Data data, final int pre, final String value) {
    data.table.write5(pre, 3, ((MemData) data).values(true).put(Token.token(value)));
  }

  /**
   * Corrupts a main-memory database and checks the inspection output.
   * @param corrupt function that corrupts the database
   * @param expected expected output
   */
  private static void check(final Consumer<Data> corrupt, final String expected) {
    set(MainOptions.MAINMEM, true);
    execute(new CreateDB(NAME, DOC));
    corrupt.accept(context.data());
    final String info = execute(new Inspect());
    assertTrue(info.contains(expected), info);
    assertTrue(info.contains("Database is inconsistent"), info);
  }
}
