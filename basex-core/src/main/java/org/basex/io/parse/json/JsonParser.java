package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.build.json.JsonParserOptions.JsonDuplicates;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team 2005-21, BSD License
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

  /** Converter. */
  private final JsonConverter conv;
  /** Spec. */
  private final boolean liberal;
  /** Escape flag. */
  private final boolean escape;
  /** Duplicates. */
  private final JsonDuplicates duplicates;
  /** Token builder for string literals. */
  private final TokenBuilder tb = new TokenBuilder();

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param input input string
   * @param opts options
   * @param conv converter
   */
  public JsonParser(final String input, final JsonParserOptions opts, final JsonConverter conv) {
    super(input);
    liberal = opts.get(JsonParserOptions.LIBERAL);
    escape = opts.get(JsonParserOptions.ESCAPE);
    final JsonDuplicates dupl = opts.get(JsonParserOptions.DUPLICATES);
    duplicates = dupl != null ? dupl : opts.get(JsonOptions.FORMAT) == JsonFormat.BASIC ?
      JsonDuplicates.RETAIN : JsonDuplicates.USE_FIRST;
    this.conv = conv;
  }

  /**
   * Parses a JSON expression.
   * @throws QueryIOException query I/O exception
   */
  public void parse() throws QueryIOException {
    consume('\uFEFF');
    skipWs();
    try {
      value();
    } catch(final StackOverflowError er) {
      throw error("Input is too deeply nested");
    }
    if(more()) throw error("Unexpected trailing content: %", remaining());
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
        else throw error("Unexpected JSON value: '%'", remaining());
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
          throw error(JSON_DUPL_X_X_X, "Key \"%\" occurs more than once", key);

        final boolean add = !(dupl && duplicates == JsonDuplicates.USE_FIRST);
        conv.openPair(key, add);
        consumeWs(':', true);
        value();
        conv.closePair(add);
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
   * Reads an unquoted string literal.
   * @return the string
   * @throws QueryIOException query I/O exception
   */
  private byte[] unquoted() throws QueryIOException {
    int cp = more() ? input.codePointAt(pos) : -1;
    if(cp < 0 || !Character.isJavaIdentifierStart(cp))
      throw error("Expected unquoted string, found %", remaining());
    tb.reset();
    do {
      tb.add(cp);
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
    tb.add(ch);
    if(ch == '-') {
      ch = consume();
      if(ch < '0' || ch > '9') throw error("Number expected after '-'");
      tb.add(ch);
    }

    final boolean zero = ch == '0';
    ch = curr();
    if(zero && ch >= '0' && ch <= '9') throw error("No digit allowed after '0'");

    LOOP:
    while(true) {
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
          tb.add(ch);
          pos++;
          ch = curr();
          break;
        case '.':
        case 'e':
        case 'E':
          break LOOP;
        default:
          skipWs();
          return tb.toArray();
      }
    }

    if(consume('.')) {
      tb.add('.');
      ch = curr();
      if(ch < '0' || ch > '9') throw error("Number expected after '.'");
      do {
        tb.add(ch);
        pos++;
        ch = curr();
      } while(ch >= '0' && ch <= '9');
      if(ch != 'e' && ch != 'E') {
        skipWs();
        return tb.toArray();
      }
    }

    // 'e' or 'E'
    tb.add(consume());
    ch = curr();
    if(ch == '-' || ch == '+') {
      tb.add(consume());
      ch = curr();
    }

    if(ch < '0' || ch > '9') throw error("Exponent expected");
    do tb.add(consume());
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
    char high = 0; // cached high surrogate
    while(pos < length) {
      final int p = pos;
      int ch = consume();

      // string is closed..
      if(ch == '"') {
        // unpaired surrogate?
        if(high != 0) add(high, pos - 7, p);
        skipWs();
        return tb.toArray();
      }

      // escape sequence
      if(ch == '\\') {
        ch = consume();
        switch(ch) {
          case '\\':
          case '/':
          case '"':
            break;
          case 'b':
            ch = '\b';
            break;
          case 'f':
            ch = '\f';
            break;
          case 'n':
            ch = '\n';
            break;
          case 'r':
            ch = '\r';
            break;
          case 't':
            ch = '\t';
            break;
          case 'u':
            if(pos + 4 >= length) throw eof(", expected four-digit hex value");
            ch = 0;
            for(int i = 0; i < 4; i++) {
              final char x = consume();
              if(x >= '0' && x <= '9')      ch = 16 * ch + x      - '0';
              else if(x >= 'a' && x <= 'f') ch = 16 * ch + x + 10 - 'a';
              else if(x >= 'A' && x <= 'F') ch = 16 * ch + x + 10 - 'A';
              else throw error("Illegal hexadecimal digit: '%'", x);
            }
            break;

          default:
            throw error("Unknown character escape: '\\%'", ch);
        }
      } else if(!liberal && ch <= 0x1F) {
        throw error("Non-escaped control character: '\\%'", CTRL[ch]);
      }

      if(high != 0) {
        if(ch >= 0xDC00 && ch <= 0xDFFF) {
          // compute resulting codepoint
          ch = (high - 0xD800 << 10) + ch - 0xDC00 + 0x10000;
        } else {
          // add invalid high surrogate, treat expected low surrogate as new character
          add(high, p, pos);
        }
        high = 0;
      }

      if(ch >= 0xD800 && ch <= 0xDBFF) {
        // remember high surrogate
        high = (char) ch;
      } else {
        add(ch, p, pos);
      }
    }
    throw eof(" in string literal");
  }

  /**
   * Adds the specified character.
   * @param ch character
   * @param s start position of invalid unicode sequence
   * @param e end position
   */
  private void add(final int ch, final int s, final int e) {
    if(escape) {
      if(ch == '\\') {
        tb.add("\\\\");
      } else if(ch == '\b') {
        tb.add("\\b");
      } else if(ch == '\f') {
        tb.add("\\f");
      } else if(ch == '\n') {
        tb.add("\\n");
      } else if(ch == '\r') {
        tb.add("\\r");
      } else if(ch == '\t') {
        tb.add("\\t");
      } else if(XMLToken.valid(ch)) {
        tb.add(ch);
      } else {
        tb.add('\\').add('u').add(HEX_TABLE[ch >> 12 & 0xF]).add(HEX_TABLE[ch >> 8 & 0xF]);
        tb.add(HEX_TABLE[ch >> 4 & 0xF]).add(HEX_TABLE[ch & 0xF]);
      }
    } else if(XMLToken.valid(ch)) {
      tb.add(ch);
    } else if(conv.fallback == null) {
      tb.add(REPLACEMENT);
    } else {
      tb.add(conv.fallback.convert(input.substring(s, e)));
    }
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
    if(consume(ch)) {
      skipWs();
      return true;
    }
    if(err) throw error("Expected '%', found '%'", ch, curr());
    return false;
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
    return error(JSON_PARSE_X_X_X, msg, ext);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @param err error code
   * @return build exception
   */
  private QueryIOException error(final QueryError err, final String msg, final Object... ext) {
    final InputInfo ii = new InputInfo(this);
    return new QueryIOException(err.get(ii, ii.line(), ii.column(), Util.inf(msg, ext)));
  }
}
