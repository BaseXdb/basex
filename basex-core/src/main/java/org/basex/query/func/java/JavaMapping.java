package org.basex.query.func.java;

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
 * @author BaseX Team 2005-20, BSD License
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
    new Pair(String.class, AtomType.STR),
    new Pair(boolean.class, AtomType.BLN),
    new Pair(Boolean.class, AtomType.BLN),
    new Pair(byte.class, AtomType.BYT),
    new Pair(Byte.class, AtomType.BYT),
    new Pair(short.class, AtomType.SHR),
    new Pair(Short.class, AtomType.SHR),
    new Pair(int.class, AtomType.INT),
    new Pair(Integer.class, AtomType.INT),
    new Pair(long.class, AtomType.LNG),
    new Pair(Long.class, AtomType.LNG),
    new Pair(float.class, AtomType.FLT),
    new Pair(Float.class, AtomType.FLT),
    new Pair(double.class, AtomType.DBL),
    new Pair(Double.class, AtomType.DBL),
    new Pair(BigDecimal.class, AtomType.DEC),
    new Pair(BigInteger.class, AtomType.ITR),
    new Pair(QName.class, AtomType.QNM),
    new Pair(char.class, AtomType.STR),
    new Pair(Character.class, AtomType.STR),
    new Pair(URI.class, AtomType.URI),
    new Pair(URL.class, AtomType.URI),
  };
  /** Node pairs. */
  private static final Pair[] NODES = {
    new Pair(Node.class, NodeType.NOD), new Pair(Element.class, NodeType.ELM),
    new Pair(Document.class, NodeType.DOC), new Pair(DocumentFragment.class, NodeType.DOC),
    new Pair(Attr.class, NodeType.ATT), new Pair(Comment.class, NodeType.COM),
    new Pair(ProcessingInstruction.class, NodeType.PI), new Pair(Text.class, NodeType.TXT),
  };
  /** XQuery pairs (no conversion required). */
  private static final Pair[] XQUERY = {
    // atomic types
    new Pair(Atm.class, AtomType.ATM),
    new Pair(B64.class, AtomType.B64),
    new Pair(Bln.class, AtomType.BLN),
    new Pair(Dat.class, AtomType.DAT),
    new Pair(Dbl.class, AtomType.DBL),
    new Pair(Dec.class, AtomType.DEC),
    new Pair(DTDur.class, AtomType.DTD),
    new Pair(Dtm.class, AtomType.DTM),
    new Pair(Dur.class, AtomType.DUR),
    new Pair(Flt.class, AtomType.FLT),
    new Pair(Hex.class, AtomType.HEX),
    new Pair(Item.class, AtomType.ITEM),
    new Pair(Int.class, AtomType.ITR),
    new Pair(ANum.class, AtomType.NUM),
    new Pair(QNm.class, AtomType.QNM),
    new Pair(Str.class, AtomType.STR),
    new Pair(Tim.class, AtomType.TIM),
    new Pair(Uri.class, AtomType.URI),
    new Pair(YMDur.class, AtomType.YMD),
    // node types
    new Pair(FAttr.class, NodeType.ATT),
    new Pair(FComm.class, NodeType.COM),
    new Pair(FDoc.class, NodeType.DOC),
    new Pair(FElem.class, NodeType.ELM),
    new Pair(ANode.class, NodeType.NOD),
    new Pair(DBNode.class, NodeType.NOD),
    new Pair(FNode.class, NodeType.NOD),
    new Pair(FNSpace.class, NodeType.NSP),
    new Pair(FPI.class, NodeType.PI),
    new Pair(FTxt.class, NodeType.TXT),
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
