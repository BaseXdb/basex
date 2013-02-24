package org.basex.query.util.json;

import org.basex.query.*;
import org.basex.util.*;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class JsonParser extends InputParser {
  /** JSON specs. */
  public static enum Spec {
    /** Parse the input according to RFC 4627.           */ RFC_4627,
    /** Parse the input according to ECMA-262.           */ ECMA_262,
    /** Parse the input being as compatible as possible. */ LIBERAL;
  }

  /** Names of control characters not allowed in string literals. */
  private static final String[] CTRL = {
    // U+0000 -- U+001F
    "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
    "BS",  "TAB", "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
    "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
    "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",
  };

  /** Input info for errors. */
  private final InputInfo info;

  /** JSON spec. */
  private final Spec spec;
  /** Token builder for string literals. */
  private final TokenBuilder tb = new TokenBuilder();

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param in input string
   * @param sp JSON spec
   * @param ii input info
   */
  private JsonParser(final String in, final Spec sp, final InputInfo ii) {
    super(in);
    info = ii;
    spec = sp != null ? sp : Spec.RFC_4627;
  }

  /**
   * Parses the input JSON string and directs the parse events to the given handler.
   * @param json JSON string to parse
   * @param sp JSON spec to use
   * @param h JSON handler
   * @param ii input info
   * @throws QueryException parse exception
   */
  public static void parse(final String json, final Spec sp, final JsonHandler h,
      final InputInfo ii) throws QueryException {
    new JsonParser(json, sp, ii).parse(h);
  }

  /**
   * Parses a JSON expression.
   * @param h handler
   * @throws QueryException parse exception
   */
  private void parse(final JsonHandler h) throws QueryException {
    skipWs();
    if(spec == Spec.RFC_4627 && !(curr() == '{' || curr() == '['))
      throw error("Expected '{' or '[', found %", rest());
    value(h);
    if(more()) throw error("Unexpected trailing content: %", rest());
  }

  /**
   * Parses a JSON value.
   * @param h handler
   * @throws QueryException query exception
   */
  private void value(final JsonHandler h) throws QueryException {
    if(ip >= il) throw eof(", expected JSON value.");
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
        if(consume("true")) h.booleanLit(true);
        else if(consume("false")) h.booleanLit(false);
        else if(consume("null")) h.nullLit();
        else if(spec == Spec.LIBERAL && consume("new") &&
            Character.isWhitespace(curr())) constr(h);
        else throw error("Unexpected JSON value: '%'.", rest());
        skipWs();
    }
  }

  /**
   * Parses a JSON object.
   * @param h handler
   * @throws QueryException query exception
   */
  private void object(final JsonHandler h) throws QueryException {
    consumeWs('{', true);
    h.openObject();
    if(!consumeWs('}', false)) {
      do {
        h.openEntry(spec != Spec.LIBERAL || curr() == '"' ? string() : unquoted());
        consumeWs(':', true);
        value(h);
        h.closeEntry();
      } while(consumeWs(',', false) && !(spec == Spec.LIBERAL && curr() == '}'));
      consumeWs('}', true);
    }
    h.closeObject();
  }

  /**
   * Parses a JSON array.
   * @param h handler
   * @throws QueryException query exception
   */
  private void array(final JsonHandler h) throws QueryException {
    consumeWs('[', true);
    h.openArray();
    if(!consumeWs(']', false)) {
      do {
        h.openArrayEntry();
        value(h);
        h.closeArrayEntry();
      } while(consumeWs(',', false) && !(spec == Spec.LIBERAL && curr() == ']'));
      consumeWs(']', true);
    }
    h.closeArray();
  }

  /**
   * Parses a JSON constructor function.
   * @param h handler
   * @throws QueryException query exception
   */
  private void constr(final JsonHandler h) throws QueryException {
    skipWs();
    if(!input.substring(ip).matches("^[a-zA-Z0-9_-]+\\(.*"))
      throw error("Wrong constructor syntax: '%'", rest());

    final int p = input.indexOf('(', ip);
    h.openConstr(token(input.substring(ip, p)));
    ip = p + 1;
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
   * @throws QueryException query exception
   */
  private byte[] unquoted() throws QueryException {
    int cp = more() ? input.codePointAt(ip) : -1;
    if(cp < 0 || !Character.isJavaIdentifierStart(cp))
      throw error("Expected unquoted string, found %.", rest());
    tb.reset();
    do {
      tb.add(cp);
      cp = input.codePointAt(ip += cp < 0x10000 ? 1 : 2);
    } while(Character.isJavaIdentifierPart(cp));
    skipWs();
    return tb.finish();
  }

  /**
   * Parses a number literal.
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] number() throws QueryException {
    tb.reset();

    // integral part
    int c = consume();
    tb.addByte((byte) c);
    if(c == '-') {
      c = consume();
      if(c < '0' || c > '9') throw error("Number expected after '-'.");
      tb.addByte((byte) c);
    }

    boolean zero = c == '0';
    c = curr();
    if(zero && c >= '0' && c <= '9') throw error("No digit allowed after '0'.");
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
          ip++;
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
      if(c < '0' || c > '9') throw error("Number expected after '.'.");
      do {
        tb.addByte((byte) c);
        ip++;
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

    if(c < '0' || c > '9') throw error("Exponent expected.");
    do tb.addByte((byte) consume());
    while((c = curr()) >= '0' && c <= '9');
    skipWs();
    return tb.finish();
  }

  /**
   * Parses a string literal.
   * @return the string
   * @throws QueryException query exception
   */
  private byte[] string() throws QueryException {
    if(!consume('"')) throw error("Expected string, found '%'", curr());
    tb.reset();
    while(ip < il) {
      final int c = consume();
      if(c == '"') {
        skipWs();
        return tb.finish();
      }

      if(c == '\\') {
        final int n = consume();
        switch(n) {
          case '/':
          case '\\':
          case '"':
            tb.addByte((byte) n);
            break;
          case 'b':
            tb.addByte((byte) '\b');
            break;
          case 'f':
            tb.addByte((byte) '\f');
            break;
          case 't':
            tb.addByte((byte) '\t');
            break;
          case 'r':
            tb.addByte((byte) '\r');
            break;
          case 'n':
            tb.addByte((byte) '\n');
            break;
          case 'u':
            if(ip + 4 >= il) throw eof(", expected four-digit hex value");
            int cp = 0;
            for(int i = 0; i < 4; i++) {
              final char x = consume();
              if(x >= '0' && x <= '9')      cp = 16 * cp + x      - '0';
              else if(x >= 'a' && x <= 'f') cp = 16 * cp + x + 10 - 'a';
              else if(x >= 'A' && x <= 'F') cp = 16 * cp + x + 10 - 'A';
              else throw error("Illegal hexadecimal digit: '%'", x);
            }
            tb.add(cp);
            break;
          default:
            throw error("Unknown character escape: '\\%'", n);
        }
      } else if(spec != Spec.LIBERAL && c <= 0x1F) {
        throw error("Unescaped control character: '\\%'", CTRL[c]);
      } else {
        tb.add(c);
      }
    }
    throw eof(" in string literal");
  }

  /** Consumes all whitespace characters from the remaining query. */
  private void skipWs() {
    while(ip < il) {
      switch(input.charAt(ip)) {
        case ' ':
        case '\t':
        case '\r':
        case '\n':
        case '\u00A0': // non-breaking space
          ip++;
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
   * @throws QueryException parse error
   */
  private boolean consumeWs(final char c, final boolean err) throws QueryException {
    if(consume() != c) {
      ip--;
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
   * @throws QueryException query exception
   */
  private QueryException eof(final String desc) throws QueryException {
    throw error("Unexpected end of input%.", desc);
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
    throw BXJS_PARSE.thrw(info, lc[0], lc[1], Util.inf(msg, ext));
  }
}
