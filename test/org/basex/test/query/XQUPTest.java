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
    doc =
      "<up>" +
      "<cars cat='c'>" +
        "<good>" +
          "<car id='1'>" +
            "<!-- NO COMMENT -->" +
          "</car>" +
          "<car id='2' color='blue'>" +
            "<wheels>optional</wheels>" +
          "</car>" +
        "</good>" +
        
        "<bad howbad='really bad'>" +
          "<car id='3' color='pink'>" +
            "<?advice nogo='buying this one'?>" +
            "<wheels>" +
              "positive" +
              "<count what='wheels'>2 and a half</count>" +
            "</wheels>" +
          "</car>" +
        "</bad>" +
        
        "<ugly>" +
          "<car id='4'>" +
            "<!-- nice one -->" +
          "</car>" +
        "</ugly>" +
      "</cars>" +
      "</up>";
    
    // XQUP expressions return an empty iterator thus every test query q which 
    // name starts with 'x' (=>excecute) is an update query.
    // The test query following q represents the actual test.
    queries = new Object[][] {
        
        // delete elem attr txt pi com
        { "xxxdel1", nodes(),
        "delete nodes //car" },
        { "del1", nodes(),
        "//car" },
        
        // rename elem attr pi
        
        // replace
        
        // insert
    };
  }
  @Override
  String details(final AProp prop) { return ""; }
 
  /*
  
  PRE DIS SIZ ATS  NS  KIND  CONTENT
  0   1  28   1   0  DOC   basex
  1   1  27   1   0  ELEM  up
  2   1  26   2   0  ELEM  cars
  3   1   1   1   0  ATTR  cat="c"
  4   2   9   1   0  ELEM  good
  5   1   3   2   0  ELEM  car
  6   1   1   1   0  ATTR  id="1"
  7   2   1   1   0  COMM   NO COMMENT 
  8   4   5   3   0  ELEM  car
  9   1   1   1   0  ATTR  id="2"
 10   2   1   1   0  ATTR  color="blue"
 11   3   2   1   0  ELEM  wheels
 12   1   1   1   0  TEXT  optional
 13  11  11   2   0  ELEM  bad
 14   1   1   1   0  ATTR  howbad="really bad"
 15   2   9   3   0  ELEM  car
 16   1   1   1   0  ATTR  id="3"
 17   2   1   1   0  ATTR  color="pink"
 18   3   1   1   0  PI    advice nogo='buying this one'
 19   4   5   1   0  ELEM  wheels
 20   1   1   1   0  TEXT  positive
 21   2   3   2   0  ELEM  count
 22   1   1   1   0  ATTR  what="wheels"
 23   2   1   1   0  TEXT  2 and a half
 24  22   4   1   0  ELEM  ugly
 25   1   3   2   0  ELEM  car
 26   1   1   1   0  ATTR  id="4"
 27   2   1   1   0  COMM   nice one
  
  */
}
