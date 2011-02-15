package org.basex.query.util;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import javax.xml.parsers.SAXParserFactory;
import org.basex.core.Prop;
import org.basex.util.TokenSet;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A container for positive and negative word lists and negations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Oliver Egli
 */
public final class SentList extends DefaultHandler {
  /** Lexer instance. */
  private final FTLexer lexer = new FTLexer(new FTOpt().set(ST, true));
  /** Token sets (positive, negative, negated). */
  private final TokenSet[] words = {
      new TokenSet(), new TokenSet(), new TokenSet()
  };
  /** Current parsing mode. */
  private int posMode;
  /** Tokenizer to stem WordLists. */
  final Prop prop;

  /**
   * Default constructor.
   * @param uri path to word list
   * @param p database properties
   * @throws Exception exception
   */
  public SentList(final String uri, final Prop p) throws Exception {
    prop = p;
    // [OE] could be extended for other XML formats
    // or plain texts
    SAXParserFactory.newInstance().newSAXParser().parse(uri, this);
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
      lexer.init(token(atts.getValue("name")));
      lexer.hasNext();
      words[posMode].add(lexer.next().text);
    }
  }
}
