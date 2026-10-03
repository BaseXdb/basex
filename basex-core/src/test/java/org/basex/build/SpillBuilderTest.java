package org.basex.build;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for building databases that are moved from main memory to disk.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SpillBuilderTest extends SandboxTest {
  /** Resets the memory limit. */
  @AfterEach public void reset() {
    MemoryLimit.max(MemoryLimit.MAX);
  }

  /**
   * Compares a database that is moved to disk while being built with a main-memory database.
   * @throws IOException I/O exception
   */
  @Test public void spill() throws IOException {
    final String xml = xml(20000);
    final Data mem = MemBuilder.build(NAME, new DirParser(new IOContent(xml), context.options));
    MemoryLimit.max(1 << 20);
    final Data data = new SpillBuilder(NAME, new DirParser(new IOContent(xml), context.options),
        context.soptions).build();
    try {
      assertInstanceOf(DiskData.class, data);
      final int size = mem.meta.size;
      assertEquals(size, data.meta.size);
      assertEquals(mem.meta.ndocs, data.meta.ndocs);
      for(int pre = 0; pre < size; pre++) {
        final int kind = mem.kind(pre);
        assertEquals(kind, data.kind(pre), "kind: " + pre);
        assertEquals(mem.dist(pre, kind), data.dist(pre, kind), "dist: " + pre);
        assertEquals(mem.size(pre, kind), data.size(pre, kind), "size: " + pre);
        if(kind == Data.ELEM || kind == Data.ATTR) {
          assertEquals(string(mem.name(pre, kind)), string(data.name(pre, kind)), "name: " + pre);
          assertArrayEquals(mem.qname(pre, kind), data.qname(pre, kind), "uri: " + pre);
        }
        if(kind == Data.ELEM) {
          assertEquals(mem.attSize(pre, kind), data.attSize(pre, kind), "attSize: " + pre);
          assertEquals(mem.nsFlag(pre), data.nsFlag(pre), "nsFlag: " + pre);
        } else {
          assertEquals(string(mem.text(pre, kind != Data.ATTR)),
              string(data.text(pre, kind != Data.ATTR)), "text: " + pre);
        }
      }
    } finally {
      DropDB.drop(data, context.soptions);
    }
  }

  /** Creates and updates databases with multiple inputs that are moved to disk. */
  @Test public void inputs() {
    final String xml1 = xml(5000), xml2 = xml(3000);
    MemoryLimit.max(1 << 18);
    query(_DB_CREATE.args(NAME, " ('" + xml1 + "', <n/>, '" + xml2 + "')",
        " ('a.xml', 'b.xml', 'c.xml')"));
    query(_DB_LIST.args(NAME), "a.xml\nb.xml\nc.xml");
    same("a.xml", xml1);
    same("c.xml", xml2);

    query(_DB_ADD.args(NAME, " ('" + xml2 + "', <m/>)", " ('d.xml', 'e.xml')"));
    query(_DB_LIST.args(NAME), "a.xml\nb.xml\nc.xml\nd.xml\ne.xml");
    same("d.xml", xml2);
    query(_DB_PUT.args(NAME, " '" + xml1 + "'", "a.xml"));
    same("a.xml", xml1);
    noTempDbs();
  }

  /** Drops temporary databases of prepared updates if a later update fails. */
  @Test public void discard() {
    query(_DB_CREATE.args(NAME));
    MemoryLimit.max(0);
    error(_DB_ADD.args(NAME, " '<a/>'", "a.xml") + ", " +
        _DB_CREATE.args(NAME + "2", " '<broken>'", "x.xml"), UPDBERROR_X);
    noTempDbs();
  }

  /** Drops temporary databases of replaced documents and new databases if a later update fails. */
  @Test public void discardPutCreate() {
    query(_DB_CREATE.args(NAME, " '<a/>'", "a.xml"));
    MemoryLimit.max(0);
    error(_DB_PUT.args(NAME, " '<b/>'", "a.xml") + ", " +
        _DB_CREATE.args(NAME + "2", " '<broken>'", "x.xml"), UPDBERROR_X);
    // Sandbox3 is prepared before Sandbox4
    error(_DB_CREATE.args(NAME + "3", " '<c/>'", "c.xml") + ", " +
        _DB_CREATE.args(NAME + "4", " '<broken>'", "x.xml"), UPDBERROR_X);
    noTempDbs();
    query(_DB_GET.args(NAME, "a.xml"), "<a/>");
  }

  /**
   * Checks if a stored document equals the specified XML document.
   * @param path path of the stored document
   * @param xml XML document
   */
  private static void same(final String path, final String xml) {
    query("deep-equal(" + _DB_GET.args(NAME, path) + ", parse-xml('" + xml + "'))", true);
  }

  /**
   * Checks that no database other than the sandbox database has been created.
   */
  private static void noTempDbs() {
    query("file:list(db:option('dbpath'))[starts-with(., '" + NAME + "')][. != '" + NAME +
        "' || file:dir-separator()]", "");
  }

  /**
   * Returns an XML document with namespaces, attributes, comments and processing instructions.
   * @param items number of items
   * @return document
   */
  private static String xml(final int items) {
    final StringBuilder sb = new StringBuilder("<r xmlns=\"d\" xmlns:p=\"u\">");
    for(int i = 0; i < items; i++) {
      sb.append("<p:e a=\"").append(i).append("\" b=\"x").append(i).append("\">");
      sb.append("<f xmlns:q=\"q").append(i % 10).append("\" q:x=\"1\">text ").append(i);
      sb.append("<!--c--><?pi x?></f>");
      if(i % 1000 == 0) {
        sb.append("<g");
        for(int a = 0; a < 40; a++) sb.append(" a").append(a).append("=\"").append(a).append('"');
        sb.append("/>");
      }
      sb.append("</p:e>");
    }
    return sb.append("</r>").toString();
  }
}
