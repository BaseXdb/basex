package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;
import org.basex.query.xquery.XQTokens;
import org.basex.query.xquery.item.Type;
import org.basex.util.Token;

/**
 * Java XQuery API - Item type.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXQItemType implements XQItemType {
  /** Base Types. */
  public static final Type[] BASE = {
      null, null, null, null, Type.AAT, Type.ATM, Type.DTD,
      Type.YMD, Type.URI, Type.B6B, Type.BLN, Type.DAT, Type.INT, Type.ITR,
      Type.SHR, Type.LNG, Type.DTM, Type.DEC, Type.DBL, Type.DUR, Type.FLT,
      Type.DAY, Type.MON, Type.MDA, Type.YEA, Type.YMO, Type.HEX, Type.NOT,
      Type.QNM, Type.STR, Type.TIM, Type.BYT, Type.NPI, Type.NNI, Type.NIN,
      Type.PIN, Type.ULN, Type.UIN, Type.USH, Type.UBY, Type.NST, Type.TOK,
      Type.LNG, Type.NAM, Type.NCN, Type.NMT, Type.ID, Type.IDR, Type.ENT,
      Type.IDR, null, Type.NMT
  };

  /** Data Type. */
  final Type type;
  /** Name. */
  final String name;
  /** Base Type. */
  final int base;
  /** Occurrence. */
  final int occ;
  /** Nillable flag. */
  final boolean nill;

  /**
   * Constructor.
   * @param b item type
   */
  public BXQItemType(final int b) {
    this(BASE[b], null, b, true);
  }

  /**
   * Constructor.
   * @param t type
   */
  public BXQItemType(final Type t) {
    this(t, null, -1, true);
  }

  /**
   * Constructor.
   * @param t type
   * @param nm name
   * @param b base type
   */
  public BXQItemType(final Type t, final String nm, final int b) {
    this(t, nm, b, true);
  }

  /**
   * Constructor.
   * @param t type
   * @param nm name
   * @param b base type
   * @param n nillable flag
   */
  public BXQItemType(final Type t, final String nm, final int b,
      final boolean n) {
    this(t, nm, b, n, XQSequenceType.OCC_EXACTLY_ONE);
  }

  /**
   * Constructor.
   * @param t type
   * @param o occurrence
   */
  public BXQItemType(final Type t, final int o) {
    this(t, null, -1, true, o);
  }

  /**
   * Constructor.
   * @param t type
   * @param nm name
   * @param b base type
   * @param n nillable flag
   * @param o occurrence
   */
  public BXQItemType(final Type t, final String nm, final int b,
      final boolean n, final int o) {
    type = t;
    name = nm;
    base = b;
    nill = n;
    occ = o;
  }

  public int getBaseType() throws BXQException {
    if(type != Type.ELM && type != Type.ATT && type != Type.ATM && type.unt)
      throw new BXQException(ELMATT);
    if(base != -1) return base;
    for(int b = 0; b < BASE.length; b++) if(BASE[b] == type) return b;
    throw new BXQException(NOBASE);
  }

  public int getItemKind() {
    switch(type) {
      case ATT: return XQITEMKIND_ATTRIBUTE;
      case COM: return XQITEMKIND_COMMENT;
      case DOC: return XQITEMKIND_DOCUMENT;
      case ELM: return XQITEMKIND_ELEMENT;
      case NOD: return XQITEMKIND_NODE;
      case PI : return XQITEMKIND_PI;
      case TXT: return XQITEMKIND_TEXT;
      case ATM: return XQITEMKIND_ATOMIC;
      case ITEM: return XQITEMKIND_ITEM;
      // [CG] Check other item kinds
      default : return type.unt ? XQITEMKIND_ITEM : XQITEMKIND_ATOMIC;
    }
  }

  public int getItemOccurrence() {
    return occ;
  }

  public QName getNodeName() throws BXQException {
    if(type != Type.ELM && type != Type.ATT) throw new BXQException(ELMATT1);
    return new QName(name);
  }

  public String getPIName() throws BXQException {
    if(type != Type.PI) throw new BXQException(PI);
    return name;
  }

  public URI getSchemaURI() {
    return null;
  }

  public QName getTypeName() throws BXQException {
    if(type != Type.ELM && type != Type.ATT && type != Type.ATM && type.unt)
      throw new BXQException(ELMATT);
    final Type t = base != -1 ? BASE[base] : type;
    return new QName(Token.string(XQTokens.XSURI), Token.string(t.name));
  }

  public boolean isAnonymousType() {
    return false;
  }

  public boolean isElementNillable() {
    return type == Type.ELM && nill;
  }

  public XQItemType getItemType() {
    return this;
  }

  @Override
  public String toString() {
    //"xs:" +
    return Token.string(type.name);
  }
}
