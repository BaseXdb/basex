package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
final class JsonParser extends InputParser {
  /** Names of control characters not allowed in string literals. */
  private static final String[] CTRL = {
    // U+0000 -- U+001F
    "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
    "BS",  "TAB", "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
    "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
    "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",
  };

  /** Converter. */
  private final JsonConverter conv;
  /** Spec. */
  private final boolean liberal;
  /** Unescape flag. */
  private final boolean unescape;
  /** Duplicates. */
  private final JsonDuplicates duplicates;
  /** Token builder for string literals. */
  private final TokenBuilder tb = new TokenBuilder();

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param in input string
   * @param opts options
   * @param cnv converter
   */
  private JsonParser(final String in, final JsonParserOptions opts, final JsonConverter cnv) {
    super(in);
    liberal = opts.get(JsonParserOptions.LIBERAL);
    unescape = opts.get(JsonParserOptions.UNESCAPE);
    duplicates = opts.get(JsonParserOptions.DUPLICATES);
    conv = cnv;
  }

  /**
   * Parses the input string, directs the parse events to the given handler and returns
   * the resulting value.
   * @param input input string
   * @param path input path (can be {@code null)}
   * @param opts options
   * @param conv converter
   * @throws QueryIOException parse exception
   */
  static void parse(final String input, final String path, final JsonParserOptions opts,
      final JsonConverter conv) throws QueryIOException {
    final JsonParser parser = new JsonParser(input, opts, conv);
    parser.file = path;
    parser.parse();
  }

  /**
   * Parses a JSON expression.
   * @throws QueryIOException query I/O exception
   */
  private void parse() throws QueryIOException {
    skipWs();
    value();
    if(more()) throw error("Unexpected trailing content: %", rest());
  }

  /**
   * Parses a JSON value.
   * @throws QueryIOException query I/O exception
   */
  private void value() throws QueryIOException {
    if(pos >= length) throw eof(", expected JSON value.");
    switch(curr()) {
      case '[':
        array();
        break;
      case '{':
        object();
        break;
      case '"':
        // string
        conv.stringLit(string());
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
        conv.numberLit(number());
        break;
      default:
        // boolean, null or constructor
        if(consume("true")) conv.booleanLit(TRUE);
        else if(consume("false")) conv.booleanLit(FALSE);
        else if(consume("null")) conv.nullLit();
        else if(liberal && consume("new") && Character.isWhitespace(curr())) constr();
        else throw error("Unexpected JSON value: '%'", rest());
        skipWs();
    }
  }

  /**
   * Parses a JSON object.
   * @throws QueryIOException query I/O exception
   */
  private void object() throws QueryIOException {
    consumeWs('{', true);
    conv.openObject();
    if(!consumeWs('}', false)) {
      final TokenSet set = new TokenSet();
      do {
        final byte[] key = !liberal || curr() == '"' ? string() : unquoted();
        final boolean dupl = set.contains(key);
        if(dupl && duplicates == JsonDuplicates.REJECT)
          throw error(BXJS_DUPLICATE_X, "Key '%' occurs more than once.", key);

        conv.openPair(key);
        consumeWs(':', true);
        value();
        conv.closePair(!dupl || duplicates == JsonDuplicates.USE_LAST);
        set.put(key);
      } while(consumeWs(',', false) && !(liberal && curr() == '}'));
      consumeWs('}', true);
    }
    conv.closeObject();
  }

  /**
   * Parses a JSON array.
   * @throws QueryIOException query I/O exception
   */
  private void array() throws QueryIOException {
    consumeWs('[', true);
    conv.openArray();
    if(!consumeWs(']', false)) {
      do {
        conv.openItem();
        value();
        conv.closeItem();
      } while(consumeWs(',', false) && !(liberal && curr() == ']'));
      consumeWs(']', true);
    }
    conv.closeArray();
  }

  /**
   * Parses a JSON constructor function.
   * @throws QueryIOException query I/O exception
   */
  private void constr() throws QueryIOException {
    skipWs();
    if(!input.substring(pos).matches("^[a-zA-Z0-9_-]+\\(.*"))
      throw error("Wrong constructor syntax: '%'", rest());

    final int p = input.indexOf('(', pos);
    conv.openConstr(token(input.substring(pos, p)));
    pos = p + 1;
    skipWs();
    if(!consumeWs(')', false)) {
      do {
        conv.openArg();
        value();
        conv.closeArg();
      } while(consumeWs(',', false));
      consumeWs(')', true);
    }
    conv.closeConstr();
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
      add(cp);
      cp = input.codePointAt(pos += cp < 0x10000 ? 1 : 2);
    } while(Character.isJavaIdentifierPart(cp));
    skipWs();
    return tb.toArray();
  }

  /**
   * Parses a number literal.
   * @return string representation
   * @throws QueryIOException query I/O exception
   */
  private byte[] number() throws QueryIOException {
    tb.reset();

    // integral part
    int ch = consume();
    add(ch);
    if(ch == '-') {
      ch = consume();
      if(ch < '0' || ch > '9') throw error("Number expected after '-'");
      add(ch);
    }

    final boolean zero = ch == '0';
    ch = curr();
    if(zero && ch >= '0' && ch <= '9') throw error("No digit allowed after '0'");
    loop: while(true) {
      switch(ch) {
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
          add(ch);
          pos++;
          ch = curr();
          break;
        case '.':
        case 'e':
        case 'E':
          break loop;
        default:
          skipWs();
          return tb.toArray();
      }
    }

    if(consume('.')) {
      add('.');
      ch = curr();
      if(ch < '0' || ch > '9') throw error("Number expected after '.'");
      do {
        add(ch);
        pos++;
        ch = curr();
      } while(ch >= '0' && ch <= '9');
      if(ch != 'e' && ch != 'E') {
        skipWs();
        return tb.toArray();
      }
    }

    // 'e' or 'E'
    add(consume());
    ch = curr();
    if(ch == '-' || ch == '+') {
      add(consume());
      ch = curr();
    }

    if(ch < '0' || ch > '9') throw error("Exponent expected");
    do add(consume());
    while((ch = curr()) >= '0' && ch <= '9');
    skipWs();
    return tb.toArray();
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
      int ch = consume();
      if(ch == '"') {
        if(hi != 0) add(hi);
        skipWs();
        return tb.toArray();
      }

      if(ch == '\\') {
        if(!unescape) {
          if(hi != 0) {
            add(hi);
            hi = 0;
          }
          add('\\');
        }

        final int n = consume();
        switch(n) {
          case '/':
          case '\\':
          case '"':
            ch = n;
            break;
          case 'b':
            ch = unescape ? '\b' : 'b';
            break;
          case 'f':
            ch = unescape ? '\f' : 'f';
            break;
          case 't':
            ch = unescape ? '\t' : 't';
            break;
          case 'r':
            ch = unescape ? '\r' : 'r';
            break;
          case 'n':
            ch = unescape ? '\n' : 'n';
            break;
          case 'u':
            if(pos + 4 >= length) throw eof(", expected four-digit hex value");
            if(unescape) {
              ch = 0;
              for(int i = 0; i < 4; i++) {
                final char x = consume();
                if(x >= '0' && x <= '9')      ch = 16 * ch + x      - '0';
                else if(x >= 'a' && x <= 'f') ch = 16 * ch + x + 10 - 'a';
                else if(x >= 'A' && x <= 'F') ch = 16 * ch + x + 10 - 'A';
                else throw error("Illegal hexadecimal digit: '%'", x);
              }
            } else {
              add('u');
              for(int i = 0; i < 4; i++) {
                final char x = consume();
                if(x >= '0' && x <= '9' || x >= 'a' && x <= 'f' || x >= 'A' && x <= 'F') {
                  if(i < 3) add(x);
                  else ch = x;
                } else throw error("Illegal hexadecimal digit: '%'", x);
              }
            }
            break;
          default:
            throw error("Unknown character escape: '\\%'", n);
        }
      } else if(!liberal && ch <= 0x1F) {
        throw error("Non-escaped control character: '\\%'", CTRL[ch]);
      }

      if(hi != 0) {
        if(ch >= 0xDC00 && ch <= 0xDFFF) ch = (hi - 0xD800 << 10) + ch - 0xDC00 + 0x10000;
        else add(hi);
        hi = 0;
      }

      if(ch >= 0xD800 && ch <= 0xDBFF) hi = (char) ch;
      else add(ch);
    }
    throw eof(" in string literal");
  }

  /**
   * Adds the specified character.
   * @param ch character
   * @throws QueryIOException exception
   */
  private void add(final int ch) throws QueryIOException {
    if(!XMLToken.valid(ch)) throw error(BXJS_INVALID_X, "Character \\u% is invalid.", ch);
    tb.add(ch);
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
   * @param ch character to be consumed
   * @param err error flag
   * @return if the character was consumed
   * @throws QueryIOException parse error
   */
  private boolean consumeWs(final char ch, final boolean err) throws QueryIOException {
    if(consume() != ch) {
      pos--;
      if(err) throw error("Expected '%', found '%'", ch, curr());
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
   */
  private QueryIOException error(final String msg, final Object... ext) {
    return error(BXJS_PARSE_X_X_X, msg, ext);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @param err error code
   * @return build exception
   */
  private QueryIOException error(final QueryError err, final String msg, final Object... ext) {
    final InputInfo info = new InputInfo(this);
    return new QueryIOException(err.get(info, info.line(), info.column(), Util.inf(msg, ext)));
  }
}
