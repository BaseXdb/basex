package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.value.type.BasicType.*;

import java.io.*;

import org.basex.io.out.PrintOutput.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class serializes items in adaptive mode.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class AdaptiveSerializer extends OutputSerializer {
  /** Original output stream. */
  private final OutputStream os;
  /** XML serializer (can be {@code null}; lazy instantiation). */
  private XMLSerializer xml;
  /** Nesting depth (0 = top level). */
  protected int depth;
  /** Serialize values as XQuery expressions. */
  protected final boolean expression;

  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  protected AdaptiveSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {
    this(os, sopts, true);
  }

  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @param omit omit XML declaration
   * @throws IOException I/O exception
   */
  protected AdaptiveSerializer(final OutputStream os, final SerializerOptions sopts,
      final boolean omit) throws IOException {

    super(os, sopts);
    this.os = os;

    if(omit) sopts.set(OMIT_XML_DECLARATION, YesNo.YES);
    indent = sopts.yes(INDENT);
    expression = sopts.yes(EXPRESSION);
    if(itemsep == null) itemsep = Token.token(expression ? ", " : "\n");
  }

  @Override
  public Serializer sc(final StaticContext sctx) {
    if(xml != null) xml.sc(sctx);
    return super.sc(sctx);
  }

  @Override
  public final void serialize(final Item item) throws IOException {
    if(expression && !more) printChar('(');
    separate();
    super.serialize(item);
  }

  @Override
  public void close() throws IOException {
    if(expression) {
      if(!more) printChar('(');
      printChar(')');
    }
    super.close();
  }

  @Override
  protected void jnode(final JNode jnode) throws IOException {
    ++depth;
    if(jnode.isRoot()) {
      printChars(QueryText.JTREE);
      printChar('(');
    } else {
      printChars(Token.token(QueryText.JNODE));
      printChar('(');
      super.serialize(jnode.key);
      printChar(':');
      if(indent) printChar(' ');
    }
    int cc = 0;
    if(jnode.value.size() > 1) printChar('(');
    for(final Item item : jnode.value) {
      if(cc++ > 0) {
        printChar(',');
        if(indent) printChar(' ');
      }
      super.serialize(item);
    }
    if(jnode.value.size() > 1) printChar(')');
    printChar(')');
    --depth;
  }

  @Override
  protected final void node(final XNode node) throws IOException {
    final Kind kind = node.kind();
    if(expression && (kind == Kind.ATTRIBUTE || kind == Kind.TEXT || kind == Kind.DOCUMENT)) {
      if(kind == Kind.ATTRIBUTE) {
        printChars(Token.token("attribute "));
        printChars(node.name());
      } else {
        printChars(Token.token(kind == Kind.TEXT ? "text" : "document"));
      }
      printChars(Token.token(" { "));
      if(kind == Kind.DOCUMENT) {
        for(final GNode child : node.childIter()) {
          if(child instanceof final XNode xnode) node(xnode);
        }
      } else {
        printChars(value(node.string(), true, false, false));
      }
      printChars(Token.token(" }"));
      return;
    }
    final XMLSerializer ser = xml();
    if(kind == Kind.ATTRIBUTE) ser.attribute(node.name(), node.string(), true);
    else if(kind == Kind.NAMESPACE) ser.namespace(node.name(), node.string(), true);
    else ser.node(node);
    ser.out.flush();
    ser.reset();
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    final Type type = item.type;
    try {
      final boolean literal = !expression || type.oneOf(INTEGER, DECIMAL) ||
        type == DOUBLE && Double.isFinite(((ANum) item).dbl());
      if(type == QNAME) {
        printChar('#');
        printChars(((QNm) item).prefixId());
      } else if(type == BOOLEAN) {
        printChars(Token.token(item));
      } else if(type.instanceOf(NUMERIC) && literal) {
        final byte[] number = expression ? item.string(null) : ((ANum) item).jsonString();
        printChars(number);
        // decimal point and exponent tell a decimal and a double from an integer literal
        if(expression) {
          if(type == DECIMAL && !Token.contains(number, '.')) {
            printChars(Token.token(".0"));
          } else if(type == DOUBLE && !Token.contains(number, 'e') &&
              !Token.contains(number, 'E')) {
            printChars(Token.token("E0"));
          }
        }
      } else {
        final Type tp = constructor(type);
        if(tp == null && depth == 0 && !expression) {
          // top-level string: omit the enclosing quotes (Text output method)
          printChars(item.string(null));
        } else {
          if(tp != null) {
            printChars(Token.token(tp));
            printChar('(');
          }
          printChars(value(item.string(null), true, false, false));
          if(tp != null) {
            printChar(')');
          }
        }
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Returns the type whose name wraps an atomic value in a constructor function.
   * @param type atomic type
   * @return constructor type or {@code null}
   */
  protected Type constructor(final Type type) {
    if(expression) return type.oneOf(STRING, BOOLEAN, INTEGER) ? null : type;
    return type.instanceOf(STRING) || type.oneOf(UNTYPED_ATOMIC, ANY_URI) ? null :
      type.instanceOf(DURATION) ? DURATION : type;
  }

  @Override
  protected final void function(final FItem item) throws IOException {
    if(item instanceof final XQArray array) {
      array(array);
    } else if(item instanceof final XQMap map) {
      map(map);
    } else {
      printChars(Token.token(item.toErrorString()));
    }
  }

  /** Fallback function. */
  private final Fallback fallback = this::printHex;

  @Override
  protected void printChar(final int cp) throws IOException {
    out.print(cp, fallback);
  }

  /**
   * Serializes an array.
   * @param array item
   * @throws IOException I/O exception
   */
  protected void array(final XQArray array) throws IOException {
    printChar('[');
    ++depth;
    int c = 0;
    for(final Value value : array.members()) {
      if(c++ > 0) printChar(',');
      if(indent) printChar(' ');
      final long vs = value.size();
      if(vs != 1) printChar('(');
      for(int i = 0, cc = 0; i < vs; i++, cc++) {
        if(cc > 0) {
          printChar(',');
          if(indent) printChar(' ');
        }
        super.serialize(value.itemAt(i));
      }
      if(vs != 1) printChar(')');
    }
    if(c > 0 && indent) printChar(' ');
    --depth;
    printChar(']');
  }

  /**
   * Serializes a map.
   * @param map item
   * @throws IOException I/O exception
   */
  protected void map(final XQMap map) throws IOException {
    printChar('{');
    int c = 0;
    ++level;
    ++depth;
    for(final XQMap.Entry entry : map.entries()) {
      if(c++ > 0) printChar(',');
      indent();
      super.serialize(entry.key());
      printChar(':');
      if(indent) printChar(' ');
      final Value value = entry.value();
      final boolean par = value.size() != 1;
      if(par) printChar('(');
      int cc = 0;
      for(final Item item : value) {
        if(cc++ > 0) {
          printChar(',');
          if(indent) printChar(' ');
        }
        super.serialize(item);
      }
      if(par) printChar(')');
    }
    --level;
    --depth;
    if(c > 0) indent();
    printChar('}');
  }

  /**
   * Returns an XML serializer.
   * @return serializer
   * @throws IOException I/O exception
   */
  private XMLSerializer xml() throws IOException {
    if(xml == null) {
      xml = new XMLSerializer(os, sopts);
      xml.sc(sc);
    }
    return xml;
  }
}
