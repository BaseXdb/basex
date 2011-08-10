package org.basex.query.util.json;

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
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Parser for JSON expressions.
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
  private static final QNm E_JSON = new QNm(token("json"));
  /** Element: pair. */
  private static final QNm E_PAIR = new QNm(token("pair"));
  /** Element: pair. */
  private static final QNm E_ITEM = new QNm(token("item"));
  /** Element: name. */
  private static final QNm A_NAME = new QNm(token("name"));
  /** Attribute: type. */
  private static final QNm A_TYPE = new QNm(token("type"));
  /** Value: string. */
  private static final byte[] STR = token("string");
  /** Value: string. */
  private static final byte[] NUM = token("number");
  /** Value: object. */
  private static final byte[] OBJ = token("object");
  /** Value: array. */
  private static final byte[] ARR = token("array");
  /** Value: boolean. */
  private static final byte[] BOOL = token("boolean");
  /** Value: null. */
  private static final byte[] NULL = token("null");

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
    super(Token.string(q));
    input = ii;
  }

  /**
   * Parses the input.
   * @return resulting node
   * @throws QueryException query exception
   */
  public ANode parse() throws QueryException {
    final FElem root = new FElem(E_JSON);
    root.add(new FAttr(A_TYPE, OBJ));
    object(root);
    return root;
  }

  /**
   * Parses an object.
   * @param root root node
   * @throws QueryException query exception
   */
  private void object(final FElem root)
      throws QueryException {

    if(!wsConsume('{')) return;
    do {
      final byte[] key = string();
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
  }

  /**
   * Parses an array.
   * @param root root node
   * @throws QueryException query exception
   */
  private void array(final FElem root) throws QueryException {
    if(!wsConsume('[')) return;
    do {
      final FElem item = new FElem(E_ITEM);
      if(value(item, root.hasChildren())) root.add(item);
    } while(wsConsume(','));
    wsCheck(']');
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
      root.add(new FTxt(string()));
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
  private byte[] string() throws QueryException {
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
            i *= 0x10;
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
   * Consumes the surrounding whitespace and the specified character.
   * @param c character to consume
   * @return true if token was found
   */
  private boolean wsConsume(final int c) {
    skipWS();
    return consume(c);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
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
   * @param c character to be found
   * @return expected character
   * @throws QueryException query exception
   */
  private int wsCheck(final char c) throws QueryException {
    if(!wsConsume(c)) error(INVALEXP, curr(), "\"" + c + "\"");
    return c;
  }

  /**
   * Raises an error if the specified character cannot be consumed.
   * @param c character to be found
   * @return expected character
   * @throws QueryException query exception
   */
  private int check(final char c) throws QueryException {
    if(!consume(c)) error(INVALEXP, curr(), "\"" + c + "\"");
    return c;
  }

  /**
   * Skips whitespaces, raises an error if the specified string cannot be
   * consumed.
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
