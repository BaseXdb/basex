package org.basex.query.func.java;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;

import java.math.*;
import java.net.*;
import java.util.*;

import javax.xml.namespace.*;

import org.basex.query.value.item.*;
import org.basex.query.value.map.XQMap;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Pre-defined Java/XQuery type mappings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class JavaMapping {
  /** Private constructor. */
  private JavaMapping() { }

  /** Atomic mappings. */
  private static final HashMap<Class<?>, Type> ATOMIC_MAP = new HashMap<>();
  /** XQuery mappings. */
  private static final HashMap<Class<?>, Type> XQUERY_MAP = new HashMap<>();

  /** Pairs with simple type conversions. */
  private static final Pair[] ATOMIC = {
    new Pair(String.class, STRING),
    new Pair(boolean.class, BOOLEAN),
    new Pair(Boolean.class, BOOLEAN),
    new Pair(byte.class, BYTE),
    new Pair(Byte.class, BYTE),
    new Pair(short.class, SHORT),
    new Pair(Short.class, SHORT),
    new Pair(int.class, INT),
    new Pair(Integer.class, INT),
    new Pair(long.class, LONG),
    new Pair(Long.class, LONG),
    new Pair(float.class, FLOAT),
    new Pair(Float.class, FLOAT),
    new Pair(double.class, DOUBLE),
    new Pair(Double.class, DOUBLE),
    new Pair(BigDecimal.class, DECIMAL),
    new Pair(BigInteger.class, INTEGER),
    new Pair(QName.class, QNAME),
    new Pair(char.class, STRING),
    new Pair(Character.class, STRING),
    new Pair(URI.class, ANY_URI),
    new Pair(URL.class, ANY_URI),
  };
  /** Node pairs. */
  private static final Pair[] NODES = {
    new Pair(Node.class, NODE),
    new Pair(Element.class, ELEMENT),
    new Pair(Document.class, DOCUMENT_NODE),
    new Pair(DocumentFragment.class, DOCUMENT_NODE),
    new Pair(Attr.class, ATTRIBUTE),
    new Pair(Comment.class, COMMENT),
    new Pair(ProcessingInstruction.class, PROCESSING_INSTRUCTION),
    new Pair(Text.class, TEXT),
  };
  /** XQuery pairs (no conversion required). */
  private static final Pair[] XQUERY = {
    // atomic types
    new Pair(Atm.class, UNTYPED_ATOMIC),
    new Pair(B64.class, BASE64_BINARY),
    new Pair(Bln.class, BOOLEAN),
    new Pair(Dat.class, DATE),
    new Pair(Dbl.class, DOUBLE),
    new Pair(Dec.class, DECIMAL),
    new Pair(DTDur.class, DAY_TIME_DURATION),
    new Pair(Dtm.class, DATE_TIME),
    new Pair(Dur.class, DURATION),
    new Pair(Flt.class, FLOAT),
    new Pair(Hex.class, HEX_BINARY),
    new Pair(Item.class, ITEM),
    new Pair(Int.class, INTEGER),
    new Pair(ANum.class, NUMERIC),
    new Pair(QNm.class, QNAME),
    new Pair(Str.class, STRING),
    new Pair(Tim.class, TIME),
    new Pair(Uri.class, ANY_URI),
    new Pair(YMDur.class, YEAR_MONTH_DURATION),
    // node types
    new Pair(FAttr.class, ATTRIBUTE),
    new Pair(FComm.class, COMMENT),
    new Pair(FDoc.class, DOCUMENT_NODE),
    new Pair(FElem.class, ELEMENT),
    new Pair(ANode.class, NODE),
    new Pair(DBNode.class, NODE),
    new Pair(FNode.class, NODE),
    new Pair(FNSpace.class, NAMESPACE_NODE),
    new Pair(FPI.class, PROCESSING_INSTRUCTION),
    new Pair(FTxt.class, TEXT),
    // function types
    new Pair(Array.class, SeqType.ARRAY),
    new Pair(XQMap.class, SeqType.MAP),
  };

  static {
    for(final Pair<Class<?>, Type> pair : ATOMIC) ATOMIC_MAP.put(pair.name(), pair.value());
    for(final Pair<Class<?>, Type> pair : XQUERY) XQUERY_MAP.put(pair.name(), pair.value());
  }

  /**
   * Returns an appropriate XQuery type for the specified Java class.
   * @param clazz Java class
   * @param atomic only check atomic mappings
   * @return type or {@code null}
   */
  static Type type(final Class<?> clazz, final boolean atomic) {
    Type type = ATOMIC_MAP.get(clazz);
    if(type == null && !atomic) {
      type = XQUERY_MAP.get(clazz);
      if(type == null) type = nodeType(clazz);
    }
    return type;
  }

  /**
   * Returns an appropriate XQuery node type for the specified Java class.
   * @param clazz Java class
   * @return type or {@code null}
   */
  private static Type nodeType(final Class<?> clazz) {
    for(final Pair<Class<?>, Type> pair : NODES) {
      if(pair.name().isInstance(clazz)) return pair.value();
    }
    return null;
  }

}
