package org.basex.http.rest;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.util.http.*;
import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the PUT method.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RESTPutTest extends RESTTest {
  /**
   * PUT Test: create empty database.
   * @throws IOException I/O exception
   */
  @Test public void put1() throws IOException {
    put(null, NAME);
    get("0", NAME, "query", "count(/)");
    delete(200, NAME);
  }

  /**
   * PUT Test: create simple database.
   * @throws IOException I/O exception
   */
  @Test public void put2() throws IOException {
    put(new ArrayInput(token("<a>A</a>")), NAME);
    get("A", NAME, "query", "/*/text()");
    delete(200, NAME);
  }

  /**
   * PUT Test: create and overwrite database.
   * @throws IOException I/O exception
   */
  @Test public void put3() throws IOException {
    put(new FileInputStream(FILE), NAME);
    put(new FileInputStream(FILE), NAME);
    get("XML", NAME, "query", "//title/text()");
    delete(200, NAME);
  }

  /**
   * PUT Test: create two documents in a database.
   * @throws IOException I/O exception
   */
  @Test public void put4() throws IOException {
    put(null, NAME);
    put(new ArrayInput(token("<a>A</a>")), NAME + "/a");
    put(new ArrayInput(token("<b>B</b>")), NAME + "/b");
    get("2", NAME, "query", "count(//text())");
    get("2", "", "query", "count(" + _DB_GET.args(NAME) + "//text())");
    get("1", "", "query", "count(" + _DB_GET.args(NAME, "b") + "/*)");
    delete(200, NAME);
  }

  /**
   * PUT Test: replace or keep a binary resource.
   * @throws IOException I/O exception
   */
  @Test public void putBinary() throws IOException {
    final String query = _BIN_DECODE_STRING.args(_DB_GET_BINARY.args(NAME, "b.bin"));
    put(null, NAME);
    send(201, Method.PUT.name(), new ArrayInput(token("A")), MediaType.APPLICATION_OCTET_STREAM,
        NAME + "/b.bin");
    send(201, Method.PUT.name(), new ArrayInput(token("B")), MediaType.APPLICATION_OCTET_STREAM,
        NAME + "/b.bin", MainOptions.REPLACE.name(), false);
    get("A", "", "query", query);
    send(201, Method.PUT.name(), new ArrayInput(token("B")), MediaType.APPLICATION_OCTET_STREAM,
        NAME + "/b.bin");
    get("B", "", "query", query);
    delete(200, NAME);
  }

  /**
   * PUT Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test public void putOption() throws IOException {
    put(new FileInputStream(FILE), NAME + '?' + MainOptions.STRIPWS.name() + "=true");
    get("5", NAME, "query", "count(//text())");
    put(new FileInputStream(FILE), NAME + '?' + MainOptions.STRIPWS.name() + "=false");
    get("22", NAME, "query", "count(//text())");

    try(FileInputStream fis = new FileInputStream(FILE)) {
      put(400, fis, NAME + "?xxx=yyy");
    }
  }
}
