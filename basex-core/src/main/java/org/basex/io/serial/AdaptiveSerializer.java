package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.value.type.AtomType.*;

import java.io.*;
import java.util.*;

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
  /** XML serializer (lazy instantiation). */
  private XMLSerializer xml;

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
    if(itemsep == null) itemsep = Token.token("\n");
  }

  @Override
  public Serializer sc(final StaticContext sctx) {
    if(xml != null) xml.sc(sctx);
    return super.sc(sctx);
  }

  @Override
  public final void serialize(final Item item) throws IOException {
    separate();
    super.serialize(item);
  }

  @Override
  protected final void node(final ANode node) throws IOException {
    final Type type = node.type;
    final XMLSerializer ser = xml();
    if(type == NodeType.ATTRIBUTE) ser.attribute(node.name(), node.string(), true);
    else if(type == NodeType.NAMESPACE_NODE) ser.namespace(node.name(), node.string(), true);
    else ser.node(node);
    ser.out.flush();
    ser.reset();
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    final Type type = item.type;
    final boolean basex = this instanceof BaseXSerializer;
    try {
      if(type == DOUBLE) {
        final double d = item.dbl(null);
        if(basex) {
          printChars(Dbl.string(d));
        } else if(Double.isNaN(d) || Double.isInfinite(d)) {
          printChars(item.string(null));
        } else {
          synchronized(Token.AD) {
            printChars(Token.token(Token.AD.format(d).toLowerCase(Locale.ENGLISH)));
          }
        }
      } else if(type == QNAME) {
        printChar('#');
        printChars(basex ? item.string(null) : ((QNm) item).unique());
      } else {
        final boolean simple = type == BOOLEAN || type.instanceOf(DECIMAL);
        final byte[] value = simple ? Token.token(item) : value(item.string(null), '"', false);
        if(basex || simple || type.instanceOf(STRING) || type.oneOf(UNTYPED_ATOMIC, ANY_URI)) {
          printChars(value);
        } else {
          printChars(Token.token(type.instanceOf(DURATION) ? DURATION : type));
          printChar('(');
          printChars(value);
          printChar(')');
        }
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  protected final void function(final FItem item) throws IOException {
    if(item instanceof final XQArray array) {
      array(array);
    } else if(item instanceof final XQMap map) {
      map(map);
    } else {
      final QNm fn = item.funcName();
      if(fn == null) printChars(Token.token("(anonymous-function)"));
      else printChars(fn.prefixId());
      printChar('#');
      printChars(Token.token(item.arity()));
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
    int c = 0;
    for(final Value value : array.iterable()) {
      if(c++ > 0) printChar(',');
      if(indent) printChar(' ');
      final long vs = value.size();
      if(vs != 1) printChar('(');
      for(int i = 0, cc = 0; i < vs; i++, cc++) {
        if(cc > 0) {
          printChar(',');
          if(indent) printChar(' ');
        }
        more = false;
        serialize(value.itemAt(i));
      }
      if(vs != 1) printChar(')');
    }
    if(c > 0 && indent) printChar(' ');
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
    try {
      for(final Item key : map.keys()) {
        if(c++ > 0) printChar(',');
        indent();
        more = false;
        serialize(key);
        printChar(':');
        if(indent) printChar(' ');
        final Value value = map.get(key);
        final boolean par = value.size() != 1;
        if(par) printChar('(');
        int cc = 0;
        for(final Item item : value) {
          if(cc++ > 0) {
            printChar(',');
            if(indent) printChar(' ');
          }
          more = false;
          serialize(item);
        }
        if(par) printChar(')');
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    --level;
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
