package org.basex.query.func.java;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;

import java.math.*;
import java.net.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;

import javax.xml.namespace.*;

import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.w3c.dom.*;

/**
 * Pre-defined Java/XQuery type mappings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
final class JavaMapping {
  /** Private constructor. */
  private JavaMapping() { }

  /** Pairs with simple type conversions. */
  private static final Entry[] ATOMIC = {
    new SimpleEntry(BigDecimal.class, DECIMAL),
    new SimpleEntry(BigInteger.class, UNSIGNED_LONG),
    new SimpleEntry(boolean.class, BOOLEAN),
    new SimpleEntry(Boolean.class, BOOLEAN),
    new SimpleEntry(byte.class, BYTE),
    new SimpleEntry(Byte.class, BYTE),
    new SimpleEntry(char.class, UNSIGNED_SHORT),
    new SimpleEntry(Character.class, UNSIGNED_SHORT),
    new SimpleEntry(double.class, DOUBLE),
    new SimpleEntry(Double.class, DOUBLE),
    new SimpleEntry(float.class, FLOAT),
    new SimpleEntry(Float.class, FLOAT),
    new SimpleEntry(int.class, INT),
    new SimpleEntry(Integer.class, INT),
    new SimpleEntry(long.class, INTEGER),
    new SimpleEntry(Long.class, INTEGER),
    new SimpleEntry(QName.class, QNAME),
    new SimpleEntry(short.class, SHORT),
    new SimpleEntry(Short.class, SHORT),
    new SimpleEntry(String.class, STRING),
    new SimpleEntry(URI.class, ANY_URI),
    new SimpleEntry(URL.class, ANY_URI),
  };
  /** Node pairs. */
  private static final Entry[] NODES = {
    new SimpleEntry(Attr.class, ATTRIBUTE),
    new SimpleEntry(Comment.class, COMMENT),
    new SimpleEntry(Document.class, DOCUMENT_NODE),
    new SimpleEntry(DocumentFragment.class, DOCUMENT_NODE),
    new SimpleEntry(Element.class, ELEMENT),
    new SimpleEntry(Node.class, NODE),
    new SimpleEntry(ProcessingInstruction.class, PROCESSING_INSTRUCTION),
    new SimpleEntry(Text.class, TEXT),
  };
  /** XQuery pairs (no conversion required). */
  private static final Entry[] XQUERY = {
    // atomic types
    new SimpleEntry(ANum.class, NUMERIC),
    new SimpleEntry(Atm.class, UNTYPED_ATOMIC),
    new SimpleEntry(B64.class, BASE64_BINARY),
    new SimpleEntry(Bln.class, BOOLEAN),
    new SimpleEntry(Dat.class, DATE),
    new SimpleEntry(Dbl.class, DOUBLE),
    new SimpleEntry(Dec.class, DECIMAL),
    new SimpleEntry(DTDur.class, DAY_TIME_DURATION),
    new SimpleEntry(Dtm.class, DATE_TIME),
    new SimpleEntry(Dur.class, DURATION),
    new SimpleEntry(Flt.class, FLOAT),
    new SimpleEntry(Hex.class, HEX_BINARY),
    new SimpleEntry(Int.class, INTEGER),
    new SimpleEntry(Item.class, ITEM),
    new SimpleEntry(QNm.class, QNAME),
    new SimpleEntry(Str.class, STRING),
    new SimpleEntry(Tim.class, TIME),
    new SimpleEntry(Uri.class, ANY_URI),
    new SimpleEntry(YMDur.class, YEAR_MONTH_DURATION),
    // node types
    new SimpleEntry(ANode.class, NODE),
    new SimpleEntry(DBNode.class, NODE),
    new SimpleEntry(FAttr.class, ATTRIBUTE),
    new SimpleEntry(FComm.class, COMMENT),
    new SimpleEntry(FDoc.class, DOCUMENT_NODE),
    new SimpleEntry(FElem.class, ELEMENT),
    new SimpleEntry(FNode.class, NODE),
    new SimpleEntry(FNSpace.class, NAMESPACE_NODE),
    new SimpleEntry(FPI.class, PROCESSING_INSTRUCTION),
    new SimpleEntry(FTxt.class, TEXT),
    // function types
    new SimpleEntry(XQArray.class, SeqType.ARRAY),
    new SimpleEntry(XQMap.class, SeqType.MAP),
  };

  /** Atomic mappings. */
  private static final Map<Class<?>, Type> ATOMIC_MAP = Map.ofEntries(ATOMIC);
  /** XQuery mappings. */
  private static final Map<Class<?>, Type> XQUERY_MAP = Map.ofEntries(XQUERY);

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
    for(final Entry<Class<?>, Type> pair : NODES) {
      if(pair.getKey().isInstance(clazz)) return pair.getValue();
    }
    return null;
  }

}
