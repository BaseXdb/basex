package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;
import org.basex.query.QueryText;
import org.basex.query.item.AtomType;
import org.basex.query.item.NodeType;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * Java XQuery API - Item type.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class BXQItemType implements XQItemType {
  /** Existing base types. */
  private static final Type[] BASE = {
      null        , null        , null        , null        ,
      AtomType.AAT, AtomType.ATM, AtomType.DTD, AtomType.YMD,
      AtomType.URI, AtomType.B64, AtomType.BLN, AtomType.DAT,
      AtomType.INT, AtomType.ITR, AtomType.SHR, AtomType.LNG,
      AtomType.DTM, AtomType.DEC, AtomType.DBL, AtomType.DUR,
      AtomType.FLT, AtomType.DAY, AtomType.MON, AtomType.MDA,
      AtomType.YEA, AtomType.YMO, AtomType.HEX, AtomType.NOT,
      AtomType.QNM, AtomType.STR, AtomType.TIM, AtomType.BYT,
      AtomType.NPI, AtomType.NNI, AtomType.NIN, AtomType.PIN,
      AtomType.ULN, AtomType.UIN, AtomType.USH, AtomType.UBY,
      AtomType.NST, AtomType.TOK, AtomType.LAN, AtomType.NAM,
      AtomType.NCN, AtomType.NMT, AtomType.ID , AtomType.IDR,
      AtomType.ENT, null        , null        , null
  };
  /** Default item type. */
  static final BXQItemType DEFAULT = new BXQItemType(
      AtomType.ITEM, null, -1, XQSequenceType.OCC_ZERO_OR_MORE);

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

  @Override
  public int getBaseType() throws BXQException {
    if(type.isUntyped()) check(NodeType.DEL, NodeType.ELM, NodeType.ATT,
        AtomType.ATM);

    if(base != -1) return base;
    for(int b = 0; b < BASE.length; ++b) if(BASE[b] == type) return b;
    throw new BXQException(NOBASE);
  }

  @Override
  public int getItemKind() {
    if(type instanceof NodeType) {
      switch((NodeType) type) {
        case ATT : return XQITEMKIND_ATTRIBUTE;
        case COM : return XQITEMKIND_COMMENT;
        case DOC : return XQITEMKIND_DOCUMENT;
        case DEL : return XQITEMKIND_DOCUMENT_ELEMENT;
        case ELM : return XQITEMKIND_ELEMENT;
        case NOD : return XQITEMKIND_NODE;
        case PI  : return XQITEMKIND_PI;
        case TXT : return XQITEMKIND_TEXT;
        default  : return XQITEMKIND_ATOMIC;
      }
    }
    return type == AtomType.ITEM ? XQITEMKIND_ITEM : XQITEMKIND_ATOMIC;
  }

  @Override
  public int getItemOccurrence() {
    return occ;
  }

  @Override
  public QName getNodeName() throws BXQException {
    check(NodeType.DEL, NodeType.ELM, NodeType.ATT);
    return name;
  }

  @Override
  public String getPIName() throws BXQException {
    if(type != NodeType.PI) throw new BXQException(PI);
    return name == null ? null : name.getLocalPart();
  }

  @Override
  public URI getSchemaURI() {
    return null;
  }

  @Override
  public QName getTypeName() throws BXQException {
    if(type.isUntyped()) check(NodeType.DEL, NodeType.ELM, NodeType.ATT,
        AtomType.ATM);
    if(type == AtomType.ITEM) throw new BXQException(TYPE);

    final Type t = base != -1 ? BASE[base] : type;
    return new QName(Token.string(QueryText.XSURI), Token.string(t.string()));
  }

  @Override
  public boolean isAnonymousType() {
    return false;
  }

  @Override
  public boolean isElementNillable() {
    return false;
  }

  @Override
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
    return Token.string(type.string());
  }
}
