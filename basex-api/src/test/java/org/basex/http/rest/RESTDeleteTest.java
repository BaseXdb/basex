package org.basex.http.rest;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the DELETE method.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class RESTDeleteTest extends RESTTest {
  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test public void delete1() throws IOException {
    put(new FileInputStream(FILE), NAME);
    // delete database
    assertEquals(Util.info(Text.DB_DROPPED_X, NAME), delete(200, NAME).trim());
    // no database left
    delete(404, NAME);
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test public void delete2() throws IOException {
    put(null, NAME);
    put(new ArrayInput(token("<a/>")), NAME + "/a");
    put(new ArrayInput(token("<b/>")), NAME + "/b");
    // delete 'a' directory
    assertStartsWith(delete(200, NAME + "/a"), "1 ");
    // delete 'b' directory
    assertStartsWith(delete(200, NAME + "/b"), "1 ");
    // no 'b' directory left
    assertStartsWith(delete(200, NAME + "/b"), "0 ");
    // delete database
    assertEquals(Util.info(Text.DB_DROPPED_X, NAME), delete(200, NAME).trim());
    // no database left
    delete(404, NAME);
  }

  /**
   * DELETE Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test public void deleteOption() throws IOException {
    put(null, NAME);
    delete(200, NAME + "/a?" + MainOptions.STRIPWS.name() + "=true");
    // unknown option
    delete(400, NAME + "/a?xxx=true");
  }
}
