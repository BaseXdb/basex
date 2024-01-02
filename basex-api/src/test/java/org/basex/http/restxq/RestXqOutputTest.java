package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ outputs.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RestXqOutputTest extends RestXqTest {
  /**
   * Serialization parameters and elements.
   * @throws Exception exception
   */
  @Test public void output() throws Exception {
    get("9", "declare %R:path('') %output:method('text') function m:f() {'9'};", "");

    get("1", "declare %R:path('') function m:f() { <R:response>" +
            "  <output:serialization-parameters>" +
            "    <output:method value='text'/>" +
            "  </output:serialization-parameters>" +
            "  <http:response status='200'/>" +
            "</R:response>," +
            "<X>1</X> };", "");
    get("<X>1</X>", "declare %R:path('') %output:method('text') function m:f() {" +
            "<R:response>" +
            "  <output:serialization-parameters>" +
            "    <output:method value='xml'/>" +
            "  </output:serialization-parameters>" +
            "  <http:response status='200'/>" +
            "</R:response>," +
            "<X>1</X> };", "");
  }
  /**
   * Erroneous serialization parameters and elements.
   * @throws Exception exception
   */
  @Test public void outputErrors() throws Exception {
    // unknown serialization parameter
    get(500, "declare %R:path('') %output:xyz('abc') function m:f() {'9'};", "");
    // parameter must contain single string
    get(500, "declare %R:path('') %output:method function m:f() {'9'};", "");
    get(500, "declare %R:path('') %output:method('xml', 'html') function m:f() {'9'};", "");

    get(500, "declare %R:path('') %output:method('text') function m:f() {" +
        "<R:response>" +
        "  <output:serialization-parameters>" +
        "    <output:method value='xml'/>" +
        "  </output:serialization-parameters>" +
        "  <http:response status='200'/>" +
        "</R:response>," +
        "1 + <a/> };", "");
  }
}
