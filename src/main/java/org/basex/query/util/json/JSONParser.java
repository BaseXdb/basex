package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.QNm;
import org.basex.util.InputInfo;
import org.basex.util.InputParser;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Parser for converting JSON to XML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JSONParser extends InputParser {
  /** Error: invalid character. */
  private static final String INVALID = "Invalid char (%)";
  /** Error: invalid and expected character. */
  private static final String INVALEXP = "Char \"%\" found, % expected";

  /** Element: json. */
  private static final QNm E_JSON = new QNm(JSON);
  /** Element: pair. */
  private static final QNm E_PAIR = new QNm(PAIR);
  /** Element: item. */
  private static final QNm E_ITEM = new QNm(ITEM);
  /** Attribute: name. */
  private static final QNm A_NAME = new QNm(NAME);
  /** Attribute: type. */
  private static final QNm A_TYPE = new QNm(TYPE);

  /** Token builder. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Input info. */
  private final InputInfo input;

  /***
   * Constructor.
   * @param q query
   * @param ii input info
   */
  public JSONParser(final byte[] q, final InputInfo ii) {
    super(string(q));
    input = ii;
  }

  /**
   * Parses the input.
   * @return resulting node
   * @throws QueryException query exception
   */
  public ANode parse() throws QueryException {
    final FElem root = new FElem(E_JSON);
    if(object(root)) {
      root.add(new FAttr(A_TYPE, OBJ));
    } else if(array(root)) {
      root.add(new FAttr(A_TYPE, ARR));
    } else {
      error(INVALEXP, curr(), "\"{\" or \"[\"");
    }
    skipWS();
    if(more()) error(INVALEXP, curr(), "end of file");
    return root;
  }

  /**
   * Parses an object.
   * @param root root node
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean object(final FElem root) throws QueryException {
    if(!wsConsume('{')) return false;
    do {
      final byte[] key = str();
      if(key == null) {
        if(root.hasChildren()) error(INVALEXP, curr(), '"');
        break;
      }
      final FElem pair = new FElem(E_PAIR);
      pair.add(new FAttr(A_NAME, key));
      wsCheck(':');
      value(pair, true);
      root.add(pair);
    } while(wsConsume(','));
    wsCheck('}');
    return true;
  }

  /**
   * Parses an array.
   * @param root root node
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean array(final FElem root) throws QueryException {
    if(!wsConsume('[')) return false;
    do {
      final FElem item = new FElem(E_ITEM);
      if(value(item, root.hasChildren())) root.add(item);
    } while(wsConsume(','));
    wsCheck(']');
    return true;
  }

  /**
   * Parses a value.
   * @param root root node
   * @param mand mandatory flag
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean value(final FElem root, final boolean mand)
      throws QueryException {

    skipWS();
    final char ch = curr();
    byte[] type;
    if(ch == '"') {
      type = STR;
      final byte[] str = str();
      if(str.length != 0) root.add(new FTxt(str));
    } else if(digit(ch) || ch == '-') {
      type = NUM;
      root.add(new FTxt(number()));
    } else if(ch == '{') {
      type = OBJ;
      object(root);
    } else if(ch == '[') {
      type = ARR;
      array(root);
    } else if(ch == 't' || ch == 'f') {
      type = BOOL;
      root.add(new FTxt(bool()));
    } else if(ch == 'n') {
      type = NULL;
      for(final byte b : NULL) check((char) b);
    } else {
      if(mand) error(INVALEXP, curr(), '"');
      return false;
    }
    root.add(new FAttr(A_TYPE, type));
    return true;
  }

  /**
   * Parses an string.
   * @return resulting string
   * @throws QueryException query exception
   */
  private byte[] str() throws QueryException {
    if(!wsConsume('"')) return null;
    tb.reset();
    while(more()) {
      int ch = consume();
      if(ch == '"') return tb.finish();
      if(ch == '\\') {
        ch = consume();
        if(ch == 'u') {
          int i = 0;
          for(int s = 0; s < 4; s++) {
            ch = consume();
            i <<= 4;
            if(ch >= '0' && ch <= '9') i += ch - 0x30;
            else if(ch >= 'A' && ch <= 'F') i += ch - 0x37;
            else if(ch >= 'a' && ch <= 'f') i += ch - 0x57;
            else error(INVALID, ch, "hex digit");
          }
          ch = i;
        } else if(ch == 'b') {
          ch = '\b';
        } else if(ch == 'f') {
          ch = '\f';
        } else if(ch == 'n') {
          ch = '\n';
        } else if(ch == 'r') {
          ch = '\r';
        } else if(ch == 't') {
          ch = '\t';
        } else if("\"/".indexOf(ch) == -1) {
          error(INVALID, (char) ch);
        }
      }
      tb.add(ch);
    }
    throw error(INVALEXP, 0, '"');
  }

  /**
   * Parses an number.
   * @return resulting number
   * @throws QueryException query exception
   */
  private byte[] number() throws QueryException {
    tb.reset();
    if(curr() == '-') tb.add(consume());

    if(curr() == '0') {
      tb.add(consume());
    } else {
      digits();
    }
    if(curr() == '.') {
      tb.add(consume());
      digits();
    }
    if(curr() == 'e' || curr() == 'E') {
      tb.add(consume());
      if(curr() == '+' || curr() == '-') tb.add(consume());
      digits();
    }
    return tb.finish();
  }

  /**
   * Parses a boolean.
   * @return resulting number
   * @throws QueryException query exception
   */
  private byte[] bool() throws QueryException {
    if(curr() == 't') {
      for(final byte b : TRUE) check((char) b);
      return TRUE;
    }
    for(final byte b : FALSE) check((char) b);
    return FALSE;
  }


  /**
   * Consumes digits.
   * @throws QueryException query exception
   */
  private void digits() throws QueryException {
    if(!digit(curr())) throw error(INVALEXP, curr(), "digit");
    do tb.add(consume()); while(digit(curr()));
  }

  /**
   * Consumes leading whitespaces and the specified character.
   * @param c character to consume
   * @return true if token was found
   */
  private boolean wsConsume(final int c) {
    skipWS();
    return consume(c);
  }

  /**
   * Consumes consecutive whitespace characters.
   * @return true if whitespaces were found
   */
  private boolean skipWS() {
    final int p = qp;
    while(more()) {
      final int c = curr();
      if(c == 0 || c > ' ') break;
      ++qp;
    }
    return p != qp;
  }

  /**
   * Skips whitespaces, raises an error if the specified character cannot be
   * consumed.
   * @param ch character to be found
   * @return expected character
   * @throws QueryException query exception
   */
  private int wsCheck(final char ch) throws QueryException {
    if(!wsConsume(ch)) error(INVALEXP, curr(), "\"" + ch + "\"");
    return ch;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param ch character to be found
   * @return expected character
   * @throws QueryException query exception
   */
  private int check(final char ch) throws QueryException {
    if(!consume(ch)) error(INVALEXP, curr(), "\"" + ch + "\"");
    return ch;
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return build exception
   * @throws QueryException query exception
   */
  private QueryException error(final String msg, final Object... ext)
      throws QueryException {
    throw JSONPARSE.thrw(input, Util.inf(msg, ext));
  }
}
