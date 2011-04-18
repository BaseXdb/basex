package org.basex.test.query.simple;


/**
 * XQuery Update Tests.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class XQUPTest extends QueryTest {
  /** Testing sequence containing different kinds of fragments for
   * insert / replace expressions. */
  static final String SEQ1 = "(<a/>, 5, 'fooboo')";
  /** S.a. */
  static final String SEQ2 = "(<a/>, 5, 'fooboo', attribute n{'b'})";
  /** S.a. */
  static final String SEQ3 = "(attribute n{'b'}, attribute c{'b'})";
  /** S.a. */
  static final String SEQ4 =
    "(attribute n{'b'}, attribute n{'b'}, attribute c{'b'})";
  /** S.a. */
  static final String SEQ5 = "(attribute n{'b'}, <a/>, 5, 'fooboo')";
  /** S.a. */
  static final String SEQ6 =
    "(attribute n{'b'}, 'fooboo', 5, <a/>, 'fooboo', 'aaa')";

  /**
   * Constructor.
   */
  static {
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
      "<?dohere what='how'?>" +
      "</up>";

    // XQUP expressions return an empty iterator thus every test query q which
    // name starts with 'xxx' is an update query.
    // The test query following q represents the actual test.
    queries = new Object[][] {
        { "xxxxxxxxxxxxx", node(11),
        "/up/cars/good/car/wheels[text()='optional']" },

        // delete
        { "xxxdel1", empty(),
        "delete nodes /up/cars/good/car[1]" },
        { "del1", node(5, 12, 22),
        "//car" },
        { "xxxdel2", empty(),
        "delete nodes //car" },
        { "del2", empty(),
        "//car" },
        { "xxxdel3", empty(),
        "delete node /up/cars/good/car[1]/@id" },
        { "del3", empty(),
        "/up/cars/good/car[1]/@id" },
        { "xxxdel4", empty(),
        "delete node //wheels/text()" },
        { "del4", empty(),
        "//wheels/text()" },
        { "xxxdel5", empty(),
        "delete node //comment()" },
        { "del5", empty(),
        "//comment()" },
        { "xxxdel6", empty(),
        "delete node //processing-instruction()" },
        { "del6", empty(),
        "//processing-instruction()" },

        // rename
        { "xxxren1", empty(),
        "rename node /up/cars as 'CARS'" },
        { "ren1", node(2),
        "/up/CARS" },
        { "xxxren2", empty(),
        "rename node /up/cars/good/car[1]/@id as 'ID'" },
        { "ren2", node(6),
        "//car/@ID" },
        { "xxxren3", empty(),
        "rename node //processing-instruction('dohere') as 'BADVICE'" },
        { "ren3", node(28),
        "//processing-instruction('BADVICE')" },

        // replace element
        { "xxxrep1", empty(),
        "replace node /up/cars/good/car[1] with /up/cars/good/car[2]" },
        { "rep1", node(7),
        "/up/cars/good/car[1]/@color" },
        // replace attribute
        { "xxxrep2", empty(),
        "replace node /up/cars/good/car[1]/@id with " +
        "/up/cars/good/car[2]/@color" },
        { "rep2", node(6),
        "/up/cars/good/car[1]/@color, /up/cars/good/car[1]/@id" },
        // replace text
        { "xxxrep3", empty(),
        "replace node /up/cars/good/car/wheels/text() with 'snap'" },
        { "rep3", node(11),
        "/up/cars/good/car/wheels[text()='snap']" },
        { "xxxrep4", empty(),
        "replace node /up/cars/good/car/wheels/text() with " + SEQ1},
        { "rep4", node(11),
        "/up/cars/good/car/wheels[text()='5 fooboo']" },
        // replace attribute
        { "xxxrep5", empty(),
        "replace node /up/cars/good/car[@id='1']/@id with " + SEQ3},
        { "rep5", node(6, 7), "/up/cars/good/car/@n, /up/cars/good/car/@c" },
        // replace comment
        { "xxxrep6", empty(),
        "replace node /up/cars/good/car/comment() with " + SEQ1},
        { "rep6", node(8),
        "/up/cars/good/car/text()" },
        // replace processing instruction
        { "xxxrep7", empty(),
        "replace node /up/cars/bad/car/processing-instruction() with " + SEQ1},
        { "rep7", node(18, 19),
        "/up/cars/bad/car/a, /up/cars/bad/car/text()" },
        // replace element content
        { "xxxrep8", empty(),
        "replace value of node //car[@id=1] with 'foo'"},
        { "rep8", node(5),
        "//car[text()='foo']" },
        // "no man's land"
        { "xxxrep9", empty(),
        "replace value of node //car[@id=1] with \"no man's land\""},
        { "rep9", node(5),
        "//car[text()=\"no man's land\"]" },

        // insert
        { "xxxins1", empty(),
        "insert node " + SEQ1 + "into /up/cars/good/car[@id='1']"},
        { "ins1", node(8, 9),
        "/up/cars/good/car/a, /up/cars/good/car/text()" },
        { "xxxins2", empty(),
        "insert node " + SEQ5 + "into /up/cars/good/car[@id=1]"},
        { "ins2", node(6, 9, 10),
        "/up/cars/good/car/@n, /up/cars/good/car/a, " +
        "/up/cars/good/car/text()" },

        // merge text nodes
        { "xxxMERGEins", empty(),
          "insert node 'foo' into /up/cars/good/car/wheels"},
          { "MERGEins", node(11),
          "/up/cars/good/car/wheels[text()='optionalfoo']" },

        // tree aware updates - mainly tests to avoid IOOB exceptions
        { "xxxTREEAWARE1", empty(), "delete node /up, delete node /up/cars"},
        { "TREEAWARE1", empty(), "/up"},
        { "xxxTREEAWARE2", empty(), "delete node /up/cars, insert node " +
          "<test/> before /up/cars"},
          {"TREEAWARE2", node(2), "/up/test"},
          { "xxxTREEAWARE3", empty(), "delete node /up/cars/bad, " +
            "insert node <test/> into /up/cars/bad/car, " +
            "insert node <test/> into //good, " +
            "insert node <test/> into //car[@id='4']"},
          { "TREEAWARE3", node(13, 18),
              "//good/test, //bad, //ugly/descendant::test"}
    };
  }

  /*

  PRE DIS SIZ ATS  NS  KIND  CONTENT
  0   1  29   1   0  DOC   basex
  1   1  28   1   0  ELEM  up
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
 28  27   1   1   0  PI    dohere what='how'

  */
}
