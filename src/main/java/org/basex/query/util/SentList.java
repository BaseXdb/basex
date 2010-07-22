package org.basex.query.util;

import static org.basex.util.Token.*;
import javax.xml.parsers.SAXParserFactory;
import org.basex.core.Main;
import org.basex.query.QueryException;
import org.basex.util.TokenSet;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A container for positive and negative word lists and negations.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Oliver Egli
 */
public final class SentList extends DefaultHandler {
  /** Token sets (positive, negative, negated). */
  private final TokenSet[] words = {
      new TokenSet(), new TokenSet(), new TokenSet()
  };
  /** Current parsing mode. */
  private int posMode;

  /**
   * Default constructor.
   * @param uri path to word list
   * @throws QueryException query exception
   */
  public SentList(final String uri) throws QueryException {
    try {
      // [OE] could be extended for other XML formats (incl. negations),
      // or plain texts
      SAXParserFactory.newInstance().newSAXParser().parse(uri, this);
    } catch(final Exception ex) {
      Main.debug(ex);
      throw new QueryException(
          uri + " could not be parsed: " + ex.toString());
    }
    
    // no negations defined - add defaults
    // (should better be included in the input file)
    if(words[2].size() == 0) {
      words[2].add(token("no"));
      words[2].add(token("not"));
      words[2].add(token("never"));
      words[2].add(token("without"));
      words[2].add(token("none"));
      words[2].add(token("neither"));
      words[2].add(token("nothing"));
      words[2].add(token("nobody"));
      words[2].add(token("nowhere"));
    }
  }

  /**
   * Returns the polarity of a token. Checks if the token is
   * <ul>
   * <li>in the list of positive terms: returns +1</li>
   * <li>in the list of negative terms: returns -1</li>
   * <li>in none of the two lists: returns 0</li>
   * </ul>
   * @param term given token
   * @return -1 if term is negative, +1 if term is positive, 0 else
   */
  public int polarity(final byte[] term) {
    if(words[0].id(term) != 0) return 1;
    if(words[1].id(term) != 0) return -1;
    return 0;
  }

  /**
   * Checks if a given token is in the list of negation words.
   * @param term given token
   * @return true result of check
   */
  public boolean negates(final byte[] term) {
    return words[2].id(term) != 0;
  }

  @Override
  public void startElement(final String uri, final String ln,
      final String qName, final Attributes atts) {

    if(qName.equals("axis")) {
      final String term = atts.getValue("name");
      if(term.equals("positive")) posMode = 0;
      if(term.equals("negative")) posMode = 1;
      if(term.equals("negated"))  posMode = 2;
    } else if(qName.equals("term")) {
      words[posMode].add(lc(token(atts.getValue("name"))));
    }
  }
}
