package org.basex.query.util;

import static org.basex.util.Token.*;

import javax.xml.parsers.SAXParserFactory;
import org.basex.core.Main;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.TokenSet;
import org.basex.util.Tokenizer;
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
  
  /** Tokenizer to stem WordLists */
  final Tokenizer tk = new Tokenizer(null);
  
  
  /**
   * Default constructor.
   * @param ii input info
   * @param uri path to word list
   * @throws QueryException query exception
   */
  public SentList(final InputInfo ii, final String uri)
      throws QueryException {

    try {
      // [OE] could be extended for other XML formats
      // or plain texts
      SAXParserFactory.newInstance().newSAXParser().parse(uri, this);
    } catch(final Exception ex) {
      Main.debug(ex);
      Err.or(ii, uri + " could not be parsed: " + ex);
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

    if(qName.equals("Category")) {
      final String term = atts.getValue("name");
      if(term.equals("positive")) posMode = 0;
      if(term.equals("negative")) posMode = 1;
      if(term.equals("negated"))  posMode = 2;
    } else if(qName.equals("word")) {  
      tk.init(lc(token(atts.getValue("name"))));
      tk.st = true;
      tk.more();
      words[posMode].add(tk.get());
    }
  }
}
