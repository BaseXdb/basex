package org.basex.query.util.json;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.InputParser;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * <p>This class converts a JSON document to a tree representation.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JSONParser extends InputParser {
  /** Error: invalid character. */
  private static final String INVALID = "Invalid character: \"%\"";
  /** Error: invalid and expected character. */
  private static final String INVALEXP = "Char \"%\" found, % expected";

  /** Token builder. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Input info. */
  private final InputInfo input;

  /**
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
  public JStruct parse() throws QueryException {
    JStruct root = object();
    if(root == null) root = array();
    if(root == null) error(INVALEXP, curr(), "\"{\" or \"[\"");
    skipWS();
    if(more()) error(INVALEXP, curr(), "end of file");
    return root;
  }

  /**
   * Returns an object.
   * @return success flag
   * @throws QueryException query exception
   */
  private JObject object() throws QueryException {
    if(!wsConsume('{')) return null;
    final JObject o = new JObject();
    do {
      final byte[] key = str();
      if(key == null) {
        if(o.size() != 0) error(INVALEXP, curr(), '"');
        break;
      }
      wsCheck(':');
      o.add(key, value(true));
    } while(wsConsume(','));
    wsCheck('}');
    return o;
  }

  /**
   * Parses an array.
   * @return success flag
   * @throws QueryException query exception
   */
  private JArray array() throws QueryException {
    if(!wsConsume('[')) return null;
    final JArray a = new JArray();
    do {
      final JValue val = value(a.size() != 0);
      if(val != null) a.add(val);
    } while(wsConsume(','));
    wsCheck(']');
    return a;
  }

  /**
   * Parses a value.
   * @param mand mandatory flag
   * @return success flag
   * @throws QueryException query exception
   */
  private JValue value(final boolean mand) throws QueryException {
    skipWS();
    final char ch = curr();
    if(digit(ch) || ch == '-') return new JNumber(number());
    if(ch == '"') return new JString(str());
    if(ch == '{') return object();
    if(ch == '[') return array();
    if(ch == 't' || ch == 'f') return new JBoolean(bool());
    if(ch == 'n') {
      for(final byte b : NULL) check((char) b);
      return new JNull();
    }
    if(mand) error(INVALEXP, curr(), '"');
    return null;
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
        } else if("\\\"/".indexOf(ch) == -1) {
          error(INVALID, "\\" + (char) ch);
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

    final int[] lc = new InputInfo(this).lineCol();
    throw JSONPARSE.thrw(input, lc[0], lc[1], Util.inf(msg, ext));
  }
}
