package org.basex.http.rest;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the PUT method.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RESTPutTest extends RESTTest {
  /**
   * PUT Test: create empty database.
   * @throws IOException I/O exception
   */
  @Test public void put1() throws IOException {
    put(NAME, null);
    assertEquals("0", get(NAME + "?query=count(/)"));
    delete(NAME);
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test public void put2() throws IOException {
    put(NAME, new ArrayInput(token("<a>A</a>")));
    assertEquals("A", get(NAME + "?query=/*/text()"));
    delete(NAME);
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test public void put3() throws IOException {
    put(NAME, new FileInputStream(FILE));
    put(NAME, new FileInputStream(FILE));
    assertEquals("XML", get(NAME + "?query=//title/text()"));
    delete(NAME);
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test public void put4() throws IOException {
    put(NAME, null);
    put(NAME + "/a", new ArrayInput(token("<a>A</a>")));
    put(NAME + "/b", new ArrayInput(token("<b>B</b>")));
    assertEquals("2", get(NAME + "?query=count(//text())"));
    assertEquals("2", get("?query=count(db:open('" + NAME + "')//text())"));
    assertEquals("1", get("?query=count(db:open('" + NAME + "','b')/*)"));
    delete(NAME);
  }

  /**
   * PUT Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test public void putOption() throws IOException {
    put(NAME + '?' + MainOptions.CHOP.name() + "=true", new FileInputStream(FILE));
    assertEquals("5", get(NAME + "?query=count(//text())"));
    put(NAME + '?' + MainOptions.CHOP.name() + "=false", new FileInputStream(FILE));
    assertEquals("22", get(NAME + "?query=count(//text())"));

    try(FileInputStream fis = new FileInputStream(FILE)) {
      put(NAME + "?xxx=yyy", fis);
      fail("Error expected.");
    } catch(final IOException ignored) { }
  }
}
