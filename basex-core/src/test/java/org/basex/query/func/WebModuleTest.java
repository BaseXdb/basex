package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.util.http.*;
import org.junit.*;

/**
 * This class tests the functions of the Web Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class WebModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void contentType() {
    query(_WEB_CONTENT_TYPE.args("sample.mp3"), new MediaType("audio/mpeg").toString());
    query(_WEB_CONTENT_TYPE.args("a/b/input.xml"), MediaType.APPLICATION_XML.toString());
    query(_WEB_CONTENT_TYPE.args("a.xxxx"), MediaType.APPLICATION_OCTET_STREAM.toString());
  }
}
