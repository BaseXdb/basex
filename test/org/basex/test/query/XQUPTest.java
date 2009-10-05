package org.basex.test.query;

import org.basex.core.AProp;

/**
 * XQuery Update Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class XQUPTest extends AbstractTest{
  
  /**
   * Constructor.
   */
  public XQUPTest() {
    doc = "<uptest>" +
    		"<a name='aa'/>" +
    		"<b/>" +
    		"</uptest>";
    
    // XQUP expressions return an empty iterator thus every
    // test query q which name starts with 'x' (=>excecute) is an update query.
    // the test query following q represents the actual test.
    queries = new Object[][] {
        { "xdel1", nodes(),
        "delete node /uptest/a" },
        { "del1", nodes(),
        "/uptest/a" },
//        { "del2", nodes(),
//        "delete node /a/@name" },
//        { "del2", nodes(),
//        "/a/@name" },
    };
  }

  @Override
  String details(AProp prop) {
    return null;
  }

}
