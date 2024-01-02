package org.basex.io.serial.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class serializes items as JSON. The input must conform to the rules
 * defined in the {@link JsonDirectConverter} and {@link JsonAttsConverter} class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JsonBasicSerializer extends JsonSerializer {
  /** Output key (false for arrays). */
  private boolean printKey;
  /** Printed keys. */
  private TokenSet printedKeys = new TokenSet();

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonBasicSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    super(os, sopts);
  }

  @Override
  protected void node(final ANode node) throws IOException {
    if(level > 0) indent();

    final BasicNodeIter iter = node.childIter();
    final Type type = node.type;
    if(type.oneOf(NodeType.DOCUMENT_NODE, NodeType.DOCUMENT_NODE_ELEMENT)) {
      for(ANode child; (child = iter.next()) != null;) {
        node(child);
      }
    } else if(type == NodeType.ELEMENT) {
      final QNm name = node.qname();
      final byte[] local = name.local();
      if(!eq(name.uri(), QueryText.FN_URI))
        throw error("Element '%' has invalid namespace: '%'.", local, name.uri());

      byte[] key = null;
      boolean escaped = false, escapedKey = false;
      for(final ANode attr : node.attributeIter()) {
        final QNm qnm = attr.qname();
        final byte[] au = qnm.uri(), an = qnm.local(), av = attr.string();
        if(au.length != 0) {
          if(!eq(au, QueryText.FN_URI)) continue;
          throw error("Element '%' has invalid attribute: %.", local, an);
        }
        if(eq(an, KEY)) {
          key = attr.string();
        } else if(eq(an, ESCAPED_KEY) && printKey) {
          final Boolean b = Bln.parse(av);
          if(b == null) throw error("Value of '%' attribute is invalid: '%'.", an, av);
          escapedKey = b;
        } else if(eq(an, ESCAPED) && eq(local, STRING)) {
          final Boolean b = Bln.parse(av);
          if(b == null) throw error("Value of '%' attribute is invalid: '%'.", an, av);
          escaped = b;
        } else {
          throw error("Element '%' has invalid attribute: %.", local, an);
        }
      }

      if(printKey) {
        if(key == null) throw error("Element '%' has no key.", local);
        key = escape(key, escapedKey, true);
        out.print('"');
        out.print(normalize(key, form));
        out.print("\":");
      }

      if(eq(local, NULL)) {
        out.print(NULL);
        for(ANode n; (n = iter.next()) != null;) {
          if(!n.type.oneOf(NodeType.COMMENT, NodeType.PROCESSING_INSTRUCTION))
            throw error("Element '%' must have no children.", local);
        }
      } else if(eq(local, BOOLEAN)) {
        final byte[] value = value(iter, local);
        if(value == null) throw error("Element '%' has no value.", local);
        final Boolean b = Bln.parse(value);
        if(b == null) throw error("Element '%' has invalid value: '%'.", local, value);
        out.print(normalize(token(b), form));
      } else if(eq(local, STRING)) {
        final byte[] value = value(iter, local);
        out.print('"');
        if(value != null) out.print(normalize(escape(value, escaped, false), form));
        out.print('"');
      } else if(eq(local, NUMBER)) {
        final byte[] value = value(iter, local);
        if(value == null) throw error("Element '%' has no value.", local);
        final double d = toDouble(value);
        if(Double.isNaN(d) || Double.isInfinite(d))
          throw error("Element '%' has invalid value: '%'.", local, value);
        out.print(token(d));
      } else if(eq(local, ARRAY)) {
        out.print('[');
        children(iter, false);
        out.print(']');
      } else if(eq(local, MAP)) {
        out.print('{');
        children(iter, true);
        out.print('}');
      } else {
        throw error("Invalid element: '%'", name);
      }
    }
  }

  @Override
  protected void startOpen(final QNm name) {
    throw Util.notExpected();
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone) {
    throw Util.notExpected();
  }

  @Override
  protected void finishOpen() {
    throw Util.notExpected();
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) {
    throw Util.notExpected();
  }

  @Override
  protected void finishEmpty() {
    throw Util.notExpected();
  }

  @Override
  protected void finishClose() {
    throw Util.notExpected();
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    throw JSON_SERIALIZE_X.getIO("Atomic values cannot be serialized");
  }

  /**
   * Serializes child nodes.
   * @param iter iterator
   * @param pk print keys
   * @throws IOException I/O exception
   */
  private void children(final BasicNodeIter iter, final boolean pk) throws IOException {
    final boolean p = printKey;
    final TokenSet keys = printedKeys;
    printKey = pk;
    printedKeys = new TokenSet();
    level++;
    boolean comma = false;
    for(ANode child; (child = iter.next()) != null;) {
      final Type type = child.type;
      if(type == NodeType.ELEMENT) {
        if(comma) out.print(',');
        node(child);
        comma = true;
      } else if(type == NodeType.TEXT && !ws(child.string())) {
        throw error("Element '%' must have no text nodes.", child.name());
      }
    }
    level--;
    indent();
    printKey = p;
    printedKeys = keys;
  }

  /**
   * Returns the value of a node.
   * @param iter iterator
   * @param type type
   * @return value
   * @throws QueryIOException query exception
   */
  private static byte[] value(final BasicNodeIter iter, final byte[] type) throws QueryIOException {
    TokenBuilder tb = null;
    for(ANode child; (child = iter.next()) != null;) {
      final Type tp = child.type;
      if(tp == NodeType.TEXT) {
        if(tb == null) tb = new TokenBuilder();
        tb.add(child.string());
      } else if(tp == NodeType.ELEMENT) {
        throw error("Element '%' must have no child elements.", type);
      }
    }
    return tb == null ? null : tb.finish();
  }

  /**
   * Returns a possibly escaped value.
   * @param value value to escape
   * @param escaped indicates if value is already escaped
   * @param key key
   * @return escaped value
   * @throws QueryIOException I/O exception
   */
  private byte[] escape(final byte[] value, final boolean escaped, final boolean key)
      throws QueryIOException {

    // parse escaped strings, check for errors
    final byte[] unescaped = escaped && contains(value, '\\') ? unescape(value) : value;
    if(key && !printedKeys.add(unescaped)) throw error("Duplicate key: %.", value);

    // create result, based on escaped string (contains unicode sequences)
    final TokenBuilder tb = new TokenBuilder();
    boolean bs = false;
    final int vl = value.length;
    for(int v = 0; v < vl; v += cl(value, v)) {
      final int cp = cp(value, v);
      if(cp >= 0 && cp < 32 || cp >= 127 && cp < 160) {
        tb.add('\\');
        switch(cp) {
          case '\b':
            tb.add('b'); break;
          case '\f':
            tb.add('f'); break;
          case '\n':
            tb.add('n'); break;
          case '\r':
            tb.add('r'); break;
          case '\t':
            tb.add('t'); break;
          default:
            tb.add('u').add('0').add('0').add(HEX_TABLE[cp >> 4]).add(HEX_TABLE[cp & 0xF]); break;
        }
      } else {
        if((cp == '\\' || cp == '"' || cp == '/' && escapeSolidus) &&
            (!escaped || !bs && cp != '\\')) tb.add('\\');
        tb.add(cp);
      }
      bs = !bs && cp == '\\';
    }
    return tb.finish();
  }

  /**
   * Returns an unescaped representation of the value.
   * @param value value to escape
   * @return unescaped token
   * @throws QueryIOException I/O exception
   */
  private static byte[] unescape(final byte[] value) throws QueryIOException {
    final TokenBuilder tb = new TokenBuilder();
    final TokenParser tp = new TokenParser(value);
    while(tp.more()) {
      int cp = tp.next();
      if(cp == '\\') {
        if(!tp.more()) throw ESCAPE_JSON_X.getIO(value);
        cp = tp.next();
        switch(cp) {
          case 'u':
            cp = 0;
            for(int i = 0; i < 4; i++) {
              if(!tp.more()) throw ESCAPE_JSON_X.getIO(value);
              final int c = tp.next();
              if(c < 0x30 || c > 0x39 && c < 0x41 || c > 0x46 && c < 0x61 || c > 0x66)
                throw ESCAPE_JSON_X.getIO(value);
              cp = (cp << 4) + c - (c >= 0x61 ? 0x57 : c >= 0x41 ? 0x37 : 0x30);
            }
            tb.add(cp);
            break;
          case '"': case '\\': case '/':
            tb.add(cp); break;
          case 'b':
            tb.add('\b'); break;
          case 'f':
            tb.add('\f'); break;
          case 'n':
            tb.add('\n'); break;
          case 'r':
            tb.add('\r'); break;
          case 't':
            tb.add('\t'); break;
          default:
            throw ESCAPE_JSON_X.getIO(value);
        }
      } else {
        tb.add(cp);
      }
    }
    return tb.finish();
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return I/O exception
   */
  private static QueryIOException error(final String msg, final Object... ext) {
    return INVALID_JSON_X.getIO(Util.inf(msg, ext));
  }
}
