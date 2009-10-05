package org.basex.test.query;

import org.basex.core.AProp;

/**
 * XQuery Update Tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class XQUPTest extends AbstractTest{
  
  /**
   * Constructor.
   */
  public XQUPTest() {
    doc = "<uptest>" +
    		"<a name='aa'>" +
    		"txt" +
    		"</a>" +
    		"<b/>" +
    		"</uptest>";
    
    // XQUP expressions return an empty iterator thus every test query q which 
    // name starts with 'x' (=>excecute) is an update query.
    // The test query following q represents the actual test.
    queries = new Object[][] {
        
        // delete
        { "xdel1", nodes(),
        "delete node /uptest/a" },
        { "del1", nodes(),
        "/uptest/a" },
        
        { "xdel2", nodes(),
        "delete node /uptest/a/@name" },
        { "del2", nodes(),
        "/uptest/a/@name" },
        
        { "xdel3", nodes(),
        "delete node /uptest/a/text()" },
        { "del3", nodes(),
        "/uptest/a/text()" },
    };
  }
  @Override
  String details(final AProp prop) { return ""; }
 
  /*
  
  PRE DIS SIZ ATS  NS  KIND  CONTENT
  0   1   6   1   0  DOC   basex
  1   1   5   1   0  ELEM  uptest
  2   1   3   2   0  ELEM  a
  3   1   1   1   0  ATTR  name="aa"
  4   2   1   1   0  TEXT  txt
  5   4   1   1   0  ELEM  b
  
  */
}
