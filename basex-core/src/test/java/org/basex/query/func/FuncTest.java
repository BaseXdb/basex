package org.basex.query.func;

import org.basex.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FuncTest extends QueryTest {
  /** Constructor. */
  static {
    doc =
      "<desclist xml:lang='en'>" +
      "<desc xml:lang='en-US'><line>blue</line></desc>" +
      "<desc xml:lang='fr'><line>bleu</line></desc>" +
      "</desclist>";

    queries = new Object[][] {
      { "sum 1", itr(1), "sum(1)" },
      { "sum 2", itr(55), "sum(1 to 10)" },
      { "sum 3", itr(4611686016981624750L), "sum(1 to 3037000499)" },
      { "sum 4", itr(4611686020018625250L), "sum(1 to 3037000500)" },
      { "sum 5", itr(9223372034707292160L), "sum(1 to 4294967295)" },
      { "distinct-values 2", itr(2),
        "count(distinct-values(//line/text()))" },
    };
  }

  /* TABLE REPRESENTATION
  PRE  DIS  SIZ  ATS  NS  KIND  CONTENT
  -------------------------------------------------
    0    1   11    1  +0  DOC   test.xml
    1    1   10    2   0  ELEM  desclist
    2    1    1    1   0  ATTR  xml:lang="en"
    3    2    4    2   0  ELEM  desc
    4    1    1    1   0  ATTR  xml:lang="en-US"
    5    2    2    1   0  ELEM  line
    6    1    1    1   0  TEXT  A line of text.
    7    6    4    2   0  ELEM  desc
    8    1    1    1   0  ATTR  xml:lang="fr"
    9    2    2    1   0  ELEM  line
   10    1    1    1   0  TEXT  Une ligne de texte.
  */
}
