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
