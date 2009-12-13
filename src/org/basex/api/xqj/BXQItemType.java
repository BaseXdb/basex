package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;
import org.basex.query.QueryTokens;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * Java XQuery API - Item type.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class BXQItemType implements XQItemType {
  /** Existing base types. */
  private static final Type[] BASE = {
      null    , null    , null    , null    , Type.AAT, Type.ATM, Type.DTD, //0
      Type.YMD, Type.URI, Type.B6B, Type.BLN, Type.DAT, Type.INT, Type.ITR, //7
      Type.SHR, Type.LNG, Type.DTM, Type.DEC, Type.DBL, Type.DUR, Type.FLT, //14
      Type.DAY, Type.MON, Type.MDA, Type.YEA, Type.YMO, Type.HEX, Type.NOT, //21
      Type.QNM, Type.STR, Type.TIM, Type.BYT, Type.NPI, Type.NNI, Type.NIN, //28
      Type.PIN, Type.ULN, Type.UIN, Type.USH, Type.UBY, Type.NST, Type.TOK, //35
      Type.LAN, Type.NAM, Type.NCN, Type.NMT, Type.ID , Type.IDR, Type.ENT, //42
      null, null, null // 49
  };
  /** Default item type. */
  static final BXQItemType DEFAULT = new BXQItemType(
      Type.ITEM, null, -1, XQSequenceType.OCC_ZERO_OR_MORE);
  /** Name. */
  private final QName name;
  /** Base type. */
  private final int base;
  /** Occurrence. */
  private final int occ;
  /** Data type. */
  private final Type type;

  /**
   * Constructor.
   * @param b item type
   * @throws BXQException exception
   */
  BXQItemType(final int b) throws BXQException {
    this(BASE[b], null, b);
    if(type == null) throw new BXQException(ATOM);
  }

  /**
   * Constructor.
   * @param t type
   */
  BXQItemType(final Type t) {
    this(t, null, -1);
  }

  /**
   * Constructor.
   * @param t type
   * @param n name
   * @param b base type
   */
  BXQItemType(final Type t, final QName n, final int b) {
    this(t, n, b, XQSequenceType.OCC_EXACTLY_ONE);
  }

  /**
   * Constructor.
   * @param t type
   * @param n name
   * @param b base type
   * @param o occurrence
   */
  BXQItemType(final Type t, final QName n, final int b, final int o) {
    name = n;
    type = t;
    base = b;
    occ = o;
  }

  public int getBaseType() throws BXQException {
    if(type.unt) check(Type.DEL, Type.ELM, Type.ATT, Type.ATM);

    if(base != -1) return base;
    for(int b = 0; b < BASE.length; b++) if(BASE[b] == type) return b;
    throw new BXQException(NOBASE);
  }

  public int getItemKind() {
    switch(type) {
      case ATT : return XQITEMKIND_ATTRIBUTE;
      case COM : return XQITEMKIND_COMMENT;
      case DOC : return XQITEMKIND_DOCUMENT;
      case DEL : return XQITEMKIND_DOCUMENT_ELEMENT;
      case ELM : return XQITEMKIND_ELEMENT;
      case ITEM: return XQITEMKIND_ITEM;
      case NOD : return XQITEMKIND_NODE;
      case PI  : return XQITEMKIND_PI;
      case TXT : return XQITEMKIND_TEXT;
      default  : return XQITEMKIND_ATOMIC;
    }
  }

  public int getItemOccurrence() {
    return occ;
  }

  public QName getNodeName() throws BXQException {
    check(Type.DEL, Type.ELM, Type.ATT);
    return name;
  }

  public String getPIName() throws BXQException {
    if(type != Type.PI) throw new BXQException(PI);
    return name == null ? null : name.getLocalPart();
  }

  public URI getSchemaURI() {
    return null;
  }

  public QName getTypeName() throws BXQException {
    if(type.unt) check(Type.DEL, Type.ELM, Type.ATT, Type.ATM);
    if(type == Type.ITEM) throw new BXQException(TYPE);

    final Type t = base != -1 ? BASE[base] : type;
    return new QName(Token.string(QueryTokens.XSURI), t.name);
  }

  public boolean isAnonymousType() {
    return false;
  }

  public boolean isElementNillable() {
    return false;
  }

  public XQItemType getItemType() {
    return this;
  }

  /**
   * Returns the item type.
   * @return type
   */
  Type getType() {
    return type;
  }

  /**
   * Matches the input types against the instance type.
   * @param types input types
   * @throws BXQException exception
   */
  private void check(final Type... types) throws BXQException {
    for(final Type t : types) if(type == t) return;
    throw new BXQException(TYPE);
  }

  @Override
  public String toString() {
    return type.name;
  }
}
