package org.basex.http.restxq;

import org.junit.*;

/**
 * This test contains RESTXQ outputs.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RestXqOutputTest extends RestXqTest {
  /**
   * Serialization parameters and elements.
   * @throws Exception exception
   */
  @Test
  public void output() throws Exception {
    // correct syntax
    get("declare %R:path('') %output:method('text') function m:f() {'9'};", "", "9");
    // unknown serialization parameter
    getE("declare %R:path('') %output:xyz('abc') function m:f() {'9'};", "");
    // parameter must contain single string
    getE("declare %R:path('') %output:method function m:f() {'9'};", "");
    getE("declare %R:path('') %output:method('xml','html') function m:f() {'9'};", "");

    get("declare %R:path('') function m:f() { <R:response>" +
        "  <output:serialization-parameters>" +
        "    <output:method value='text'/>" +
        "  </output:serialization-parameters>" +
        "  <http:response status='200'/>" +
        "</R:response>," +
        "<X>1</X> };", "", "1");
    get("declare %R:path('') %output:method('text') function m:f() {" +
        "<R:response>" +
        "  <output:serialization-parameters>" +
        "    <output:method value='xml'/>" +
        "  </output:serialization-parameters>" +
        "  <http:response status='200'/>" +
        "</R:response>," +
        "<X>1</X> };", "", "<X>1</X>");
    getE("declare %R:path('') %output:method('text') function m:f() {" +
        "<R:response>" +
        "  <output:serialization-parameters>" +
        "    <output:method value='xml'/>" +
        "  </output:serialization-parameters>" +
        "  <http:response status='200'/>" +
        "</R:response>," +
        "1+<a/> };", "");
  }
}
