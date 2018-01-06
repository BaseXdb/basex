package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;

import java.io.*;
import java.text.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class serializes items in adaptive mode.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class AdaptiveSerializer extends OutputSerializer {
  /** Format for double values. */
  private static final String DOUBLES = "0.0##########################E0";
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
    itemsep("\n");
  }

  @Override
  public Serializer sc(final StaticContext sctx) {
    if(xml != null) xml.sc(sctx);
    return super.sc(sctx);
  }

  @Override
  public final void serialize(final Item item) throws IOException {
    if(more) out.print(itemsep);
    super.serialize(item);
  }

  @Override
  protected final void node(final ANode item) throws IOException {
    final Type type = item.type;
    final XMLSerializer ser = xml();
    if(type == NodeType.ATT) ser.attribute(item.name(), item.string(), true);
    else if(type == NodeType.NSP) ser.namespace(item.name(), item.string(), true);
    else ser.node(item);
    ser.out.flush();
    ser.reset();
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    final Type type = item.type;
    if(type.instanceOf(AtomType.STR) || type.instanceOf(AtomType.DEC) ||
       type == AtomType.BLN || type == AtomType.ATM || type == AtomType.URI) {
      tb.add(item.toString());
    } else if(type == AtomType.QNM) {
      tb.add(((QNm) item).eqName());
    } else if(type == AtomType.DBL) {
      final double d = ((Dbl) item).dbl();
      if(Double.isInfinite(d) || Double.isNaN(d)) tb.add(((Dbl) item).string());
      else tb.add(new DecimalFormat(DOUBLES, Token.LOC).format(d).toLowerCase());
    } else {
      final Item it = type.instanceOf(AtomType.DUR) ? new Dur((Dur) item) : item;
      try {
        tb.addExt(it.type).add('(').add(Item.toString(it.string(null), true, false)).add(')');
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
    }
    printChars(tb.finish());
  }

  @Override
  protected final void function(final FItem item) throws IOException {
    if(item instanceof Array) {
      array((Array) item);
    } else if(item instanceof Map) {
      map((Map) item);
    } else {
      final TokenBuilder tb = new TokenBuilder();
      final QNm fn = item.funcName();
      if(fn == null) tb.add("(anonymous-function)");
      else tb.add(fn.prefixId());
      printChars(tb.add('#').addInt(item.arity()).finish());
    }
  }

  @Override
  protected void printChar(final int cp) throws IOException {
    try {
      out.print(cp);
    } catch(final QueryIOException ex) {
      if(ex.getCause().error() == QueryError.SERENC_X_X) printHex(cp);
      else throw ex;
    }
  }

  /**
   * Serializes an array.
   * @param array item
   * @throws IOException I/O exception
   */
  protected void array(final Array array) throws IOException {
    final TokenBuilder tb = new TokenBuilder().add('[');
    int c = 0;
    for(final Value value : array.members()) {
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
  protected void map(final Map map) throws IOException {
    final TokenBuilder tb = new TokenBuilder().add("map");
    if(indent) tb.add(' ');
    tb.add('{');
    int c = 0;
    ++level;
    for(final Item key : map.keys()) {
      if(c++ > 0) tb.add(',');
      printChars(tb.next());
      indent();
      more = false;
      serialize(key);
      tb.add(':');
      if(indent) tb.add(' ');
      try {
        final Value value = map.get(key, null);
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
      } catch(final QueryException ex) {
        throw new QueryIOException(ex);
      }
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
