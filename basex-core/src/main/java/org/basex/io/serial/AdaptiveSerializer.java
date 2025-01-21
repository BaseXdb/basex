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
   * @param omit omit xml declaration
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
    final TokenBuilder tb = new TokenBuilder();
    final Type type = item.type;
    final boolean plain = this instanceof BaseXSerializer;
    try {
      if(type == DOUBLE) {
        final double d = item.dbl(null);
        if(Double.isNaN(d) || Double.isInfinite(d)) {
          tb.add(item.string(null));
        } else if(plain) {
          tb.add(Dbl.serialize(d));
        } else {
          synchronized(Token.AD) {
            tb.add(Token.AD.format(d).toLowerCase(Locale.ENGLISH));
          }
        }
      } else if(type == QNAME) {
        tb.add(plain ? item.string(null) : ((QNm) item).eqName());
      } else {
        final boolean simple = type == BOOLEAN || type.instanceOf(DECIMAL);
        final byte[] value = simple ? Token.token(item) : value(item.string(null), '"', false);
        if(plain || simple || type.instanceOf(STRING) || type.oneOf(UNTYPED_ATOMIC, ANY_URI)) {
          tb.add(value);
        } else {
          tb.add(Token.token(type.instanceOf(DURATION) ? DURATION : type));
          tb.add('(').add(value).add(')');
        }
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    printChars(tb.finish());
  }

  @Override
  protected final void function(final FItem item) throws IOException {
    if(item instanceof XQArray) {
      array((XQArray) item);
    } else if(item instanceof XQMap) {
      map((XQMap) item);
    } else {
      final TokenBuilder tb = new TokenBuilder();
      final QNm fn = item.funcName();
      if(fn == null) tb.add("(anonymous-function)");
      else tb.add(fn.prefixId());
      printChars(tb.add('#').addInt(item.arity()).finish());
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
    final TokenBuilder tb = new TokenBuilder().add('[');
    int c = 0;
    for(final Value value : array.iterable()) {
      if(c++ > 0) {
        tb.add(',');
        if(indent) tb.add(' ');
      }
      final long vs = value.size();
      if(vs != 1) tb.add('(');
      for(int i = 0, cc = 0; i < vs; i++, cc++) {
        if(cc > 0) {
          tb.add(',');
          if(indent) tb.add(' ');
        }
        printChars(tb.next());
        more = false;
        serialize(value.itemAt(i));
      }
      if(vs != 1) tb.add(')');
    }
    printChars(tb.add(']').finish());
  }

  /**
   * Serializes a map.
   * @param map item
   * @throws IOException I/O exception
   */
  protected void map(final XQMap map) throws IOException {
    final TokenBuilder tb = new TokenBuilder().add('{');
    int c = 0;
    ++level;
    try {
      for(final Item key : map.keys()) {
        if(c++ > 0) tb.add(',');
        printChars(tb.next());
        indent();
        more = false;
        serialize(key);
        tb.add(':');
        if(indent) tb.add(' ');
        final Value value = map.get(key);
        final boolean par = value.size() != 1;
        if(par) tb.add('(');
        int cc = 0;
        for(final Item item : value) {
          if(cc++ > 0) {
            tb.add(',');
            if(indent) tb.add(' ');
          }
          printChars(tb.next());
          more = false;
          serialize(item);
        }
        if(par) tb.add(')');
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    printChars(tb.next());
    --level;
    indent();
    printChars(tb.add('}').finish());
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
