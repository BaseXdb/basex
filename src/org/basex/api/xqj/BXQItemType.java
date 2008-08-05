package org.basex.api.xqj;

import static org.basex.api.xqj.BXQText.*;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;
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
  Type type;
  /** Name. */
  String name;
  /** Occurrence. */
  int occ = XQSequenceType.OCC_EXACTLY_ONE;

  /**
   * Constructor.
   * @param b item type
   */
  public BXQItemType(final int b) {
    this(BASE[b]);
  }

  /**
   * Constructor.
   * @param t type
   */
  public BXQItemType(final Type t) {
    this(t, null);
  }

  /**
   * Constructor.
   * @param t type
   * @param nm name
   */
  public BXQItemType(final Type t, final String nm) {
    type = t;
    name = nm;
  }

  /**
   * Constructor.
   * @param t type
   * @param o numeric value
   */
  public BXQItemType(final Type t, final int o) {
    type = t;
    occ = o;
  }

  public int getBaseType() throws BXQException {
    if(type != Type.ELM && type != Type.ATT && type.unt)
      throw new BXQException(ELMATT);
    for(int b = 0; b < BASE.length; b++) {
      if(BASE[b] == type) return b;
    }
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
      // [CG] not correct..
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
    if(type != Type.ELM && type != Type.ATT && type.unt)
      throw new BXQException(ELMATT);
    return new QName(Token.string(type.name));
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

  @Override
  public String toString() {
    return "xs:" + Token.string(type.name);
  }
}
