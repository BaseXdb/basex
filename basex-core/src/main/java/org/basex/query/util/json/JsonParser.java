package org.basex.query.util.json;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.build.JsonOptions.JsonSpec;
import org.basex.build.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class JsonParser extends InputParser {
  /** Names of control characters not allowed in string literals. */
  private static final String[] CTRL = {
    // U+0000 -- U+001F
    "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
    "BS",  "TAB", "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
    "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
    "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",
  };

  /** JSON spec. */
  private final JsonSpec spec;
  /** Unescape flag. */
  private final boolean unescape;
  /** Token builder for string literals. */
  private final TokenBuilder tb = new TokenBuilder();

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param in input string
   * @param opts json options
   * @throws QueryIOException query exception
   */
  private JsonParser(final String in, final JsonParserOptions opts) throws QueryIOException {
    super(in);
    spec = opts.spec();
    unescape = opts.get(JsonParserOptions.UNESCAPE);
  }

  /**
   * Parses the input JSON string and directs the parse events to the given handler.
   * @param input JSON string to parse
   * @param opts json options
   * @param handler JSON handler
   * @throws QueryIOException parse exception
   */
  public static void parse(final String input, final JsonParserOptions opts,
      final JsonHandler handler) throws QueryIOException {
    new JsonParser(input, opts).parse(handler);
  }

  /**
   * Parses a JSON expression.
   * @param h handler
   * @throws QueryIOException query I/O exception
   */
  private void parse(final JsonHandler h) throws QueryIOException {
    skipWs();
    if(spec == JsonSpec.RFC4627 && !(curr() == '{' || curr() == '['))
      throw error("Expected '{' or '[', found %", rest());
    value(h);
    if(more()) throw error("Unexpected trailing content: %", rest());
  }

  /**
   * Parses a JSON value.
   * @param h handler
   * @throws QueryIOException query I/O exception
   */
  private void value(final JsonHandler h) throws QueryIOException {
    if(pos >= length) throw eof(", expected JSON value.");
    switch(curr()) {
      case '[':
        array(h);
        break;
      case '{':
        object(h);
        break;
      case '"':
        // string
        h.stringLit(string());
        break;
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        // number
        h.numberLit(number());
        break;
      default:
        // boolean, null or constructor
        if(consume("true")) h.booleanLit(TRUE);
        else if(consume("false")) h.booleanLit(FALSE);
        else if(consume("null")) h.nullLit();
        else if(spec == JsonSpec.LIBERAL && consume("new") &&
            Character.isWhitespace(curr())) constr(h);
        else throw error("Unexpected JSON value: '%'", rest());
        skipWs();
    }
  }

  /**
   * Parses a JSON object.
   * @param h handler
   * @throws QueryIOException query I/O exception
   */
  private void object(final JsonHandler h) throws QueryIOException {
    consumeWs('{', true);
    h.openObject();
    if(!consumeWs('}', false)) {
      do {
        h.openPair(spec != JsonSpec.LIBERAL || curr() == '"' ? string() : unquoted());
        consumeWs(':', true);
        value(h);
        h.closePair();
      } while(consumeWs(',', false) && !(spec == JsonSpec.LIBERAL && curr() == '}'));
      consumeWs('}', true);
    }
    h.closeObject();
  }

  /**
   * Parses a JSON array.
   * @param h handler
   * @throws QueryIOException query I/O exception
   */
  private void array(final JsonHandler h) throws QueryIOException {
    consumeWs('[', true);
    h.openArray();
    if(!consumeWs(']', false)) {
      do {
        h.openItem();
        value(h);
        h.closeItem();
      } while(consumeWs(',', false) && !(spec == JsonSpec.LIBERAL && curr() == ']'));
      consumeWs(']', true);
    }
    h.closeArray();
  }

  /**
   * Parses a JSON constructor function.
   * @param h handler
   * @throws QueryIOException query I/O exception
   */
  private void constr(final JsonHandler h) throws QueryIOException {
    skipWs();
    if(!input.substring(pos).matches("^[a-zA-Z0-9_-]+\\(.*"))
      throw error("Wrong constructor syntax: '%'", rest());

    final int p = input.indexOf('(', pos);
    h.openConstr(token(input.substring(pos, p)));
    pos = p + 1;
    skipWs();
    if(!consumeWs(')', false)) {
      do {
        h.openArg();
        value(h);
        h.closeArg();
      } while(consumeWs(',', false));
      consumeWs(')', true);
    }
    h.closeConstr();
  }

  /**
   * Reads an unquoted string literal.
   * @return the string
   * @throws QueryIOException query I/O exception
   */
  private byte[] unquoted() throws QueryIOException {
    int cp = more() ? input.codePointAt(pos) : -1;
    if(cp < 0 || !Character.isJavaIdentifierStart(cp))
      throw error("Expected unquoted string, found %", rest());
    tb.reset();
    do {
      tb.add(cp);
      cp = input.codePointAt(pos += cp < 0x10000 ? 1 : 2);
    } while(Character.isJavaIdentifierPart(cp));
    skipWs();
    return tb.finish();
  }

  /**
   * Parses a number literal.
   * @return string representation
   * @throws QueryIOException query I/O exception
   */
  private byte[] number() throws QueryIOException {
    tb.reset();

    // integral part
    int c = consume();
    tb.addByte((byte) c);
    if(c == '-') {
      c = consume();
      if(c < '0' || c > '9') throw error("Number expected after '-'");
      tb.addByte((byte) c);
    }

    final boolean zero = c == '0';
    c = curr();
    if(zero && c >= '0' && c <= '9') throw error("No digit allowed after '0'");
    loop: while(true) {
      switch(c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          tb.addByte((byte) c);
          pos++;
          c = curr();
          break;
        case '.':
        case 'e':
        case 'E':
          break loop;
        default:
          skipWs();
          return tb.finish();
      }
    }

    if(consume('.')) {
      tb.addByte((byte) '.');
      c = curr();
      if(c < '0' || c > '9') throw error("Number expected after '.'");
      do {
        tb.addByte((byte) c);
        pos++;
        c = curr();
      } while(c >= '0' && c <= '9');
      if(c != 'e' && c != 'E') {
        skipWs();
        return tb.finish();
      }
    }

    // 'e' or 'E'
    tb.addByte((byte) consume());
    c = curr();
    if(c == '-' || c == '+') {
      tb.addByte((byte) consume());
      c = curr();
    }

    if(c < '0' || c > '9') throw error("Exponent expected");
    do tb.addByte((byte) consume());
    while((c = curr()) >= '0' && c <= '9');
    skipWs();
    return tb.finish();
  }

  /**
   * Parses a string literal.
   * @return the string
   * @throws QueryIOException query I/O exception
   */
  private byte[] string() throws QueryIOException {
    if(!consume('"')) throw error("Expected string, found '%'", curr());
    tb.reset();
    char hi = 0; // cached high surrogate
    while(pos < length) {
      int cp = consume();
      if(cp == '"') {
        if(hi != 0) tb.add(hi);
        skipWs();
        return tb.finish();
      }

      if(cp == '\\') {
        if(!unescape) {
          if(hi != 0) {
            tb.add(hi);
            hi = 0;
          }
          tb.addByte((byte) '\\');
        }

        final int n = consume();
        switch(n) {
          case '/':
          case '\\':
          case '"':
            cp = n;
            break;
          case 'b':
            cp = unescape ? '\b' : 'b';
            break;
          case 'f':
            cp = unescape ? '\f' : 'f';
            break;
          case 't':
            cp = unescape ? '\t' : 't';
            break;
          case 'r':
            cp = unescape ? '\r' : 'r';
            break;
          case 'n':
            cp = unescape ? '\n' : 'n';
            break;
          case 'u':
            if(pos + 4 >= length) throw eof(", expected four-digit hex value");
            if(unescape) {
              cp = 0;
              for(int i = 0; i < 4; i++) {
                final char x = consume();
                if(x >= '0' && x <= '9')      cp = 16 * cp + x      - '0';
                else if(x >= 'a' && x <= 'f') cp = 16 * cp + x + 10 - 'a';
                else if(x >= 'A' && x <= 'F') cp = 16 * cp + x + 10 - 'A';
                else throw error("Illegal hexadecimal digit: '%'", x);
              }
            } else {
              tb.addByte((byte) 'u');
              for(int i = 0; i < 4; i++) {
                final char x = consume();
                if(x >= '0' && x <= '9' || x >= 'a' && x <= 'f' || x >= 'A' && x <= 'F') {
                  if(i < 3) tb.addByte((byte) x);
                  else cp = x;
                } else throw error("Illegal hexadecimal digit: '%'", x);
              }
            }
            break;
          default:
            throw error("Unknown character escape: '\\%'", n);
        }
      } else if(spec != JsonSpec.LIBERAL && cp <= 0x1F) {
        throw error("Non-escaped control character: '\\%'", CTRL[cp]);
      }

      if(hi != 0) {
        if(cp >= 0xDC00 && cp <= 0xDFFF)
          cp = (hi - 0xD800 << 10) + cp - 0xDC00 + 0x10000;
        else tb.add(hi);
        hi = 0;
      }

      if(cp >= 0xD800 && cp <= 0xDBFF) hi = (char) cp;
      else tb.add(cp);
    }
    throw eof(" in string literal");
  }

  /** Consumes all whitespace characters from the remaining query. */
  private void skipWs() {
    while(pos < length) {
      switch(input.charAt(pos)) {
        case ' ':
        case '\t':
        case '\r':
        case '\n':
        case '\u00A0': // non-breaking space
          pos++;
          break;
        default:
          return;
      }
    }
  }

  /**
   * Tries to consume the given character. If successful, following whitespace is skipped.
   * Otherwise if the error flag is set a parse error is thrown.
   * @param c character to be consumed
   * @param err error flag
   * @return if the character was consumed
   * @throws QueryIOException parse error
   */
  private boolean consumeWs(final char c, final boolean err) throws QueryIOException {
    if(consume() != c) {
      pos--;
      if(err) throw error("Expected '%', found '%'", c, curr());
      return false;
    }
    skipWs();
    return true;
  }

  /**
   * Throws an end-of-input error.
   * @param desc description
   * @return never
   * @throws QueryIOException query I/O exception
   */
  private QueryIOException eof(final String desc) throws QueryIOException {
    throw error("Unexpected end of input%", desc);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return build exception
   * @throws QueryIOException query I/O exception
   */
  private QueryIOException error(final String msg, final Object... ext) throws QueryIOException {
    final int[] lc = new InputInfo(this).lineCol();
    throw BXJS_PARSE.thrwIO(lc[0], lc[1], Util.inf(msg, ext));
  }
}
