package org.basex.api.xqj;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequenceType;

import org.basex.query.xquery.item.Type;
import org.basex.util.Token;

/**
 * BaseX  XQuery item type.
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

  /** Type itemType. */
  final Type type;

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
    type = t;
  }

  public int getBaseType() {
    for(int b = 0; b < BASE.length; b++) if(BASE[b] == type) return b;
    return 0;
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
      default : return XQITEMKIND_ITEM;
    }
  }

  public int getItemOccurrence() {
    return XQSequenceType.OCC_EXACTLY_ONE;
  }

  public QName getNodeName() {
    return type.node() ? new QName(type.name()) : null;
  }

  public String getPIName() {
    return null;
  }

  public URI getSchemaURI() {
    return null;
  }

  public QName getTypeName() {
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
}
