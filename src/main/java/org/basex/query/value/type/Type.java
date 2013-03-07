package org.basex.query.value.type;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Type {
  /** Type IDs for client/server communication. */
  public static enum ID {
    // function types
    /** function(*).              */ FUN(7),

    // node types
    /** node().                   */ NOD(8),
    /** text().                   */ TXT(9),
    /** processing-instruction(). */ PI(10),
    /** element().                */ ELM(11),
    /** document-node().          */ DOC(12),
    /** document-node(element()). */ DEL(13),
    /** attribute().              */ ATT(14),
    /** comment().                */ COM(15),
    /** namespace-node().         */ NSP(16),
    /** schema-element().         */ SCE(17),
    /** schema-attribute().       */ SCA(18),

    // item type
    /** item().                   */ ITEM(32),

    // atomic types
    /** xs:untyped.               */ UTY(33),
    /** xs:anyType.               */ ATY(34),
    /** xs:anySimpleType.         */ AST(35),
    /** xs:anyAtomicType.         */ AAT(36),
    /** xs:untypedAtomic.         */ ATM(37),
    /** xs:string.                */ STR(38),
    /** xs:normalizedString.      */ NST(39),
    /** xs:token.                 */ TOK(40),
    /** xs:language.              */ LAN(41),
    /** xs:NMTOKEN.               */ NMT(42),
    /** xs:Name.                  */ NAM(43),
    /** xs:NCName.                */ NCN(44),
    /** xs:ID.                    */ ID(45),
    /** xs:IDREF.                 */ IDR(46),
    /** xs:ENTITY.                */ ENT(47),
    /** xs:float.                 */ FLT(48),
    /** xs:double.                */ DBL(49),
    /** xs:decimal.               */ DEC(50),
    /** precisionDecimal().       */ PDC(51),
    /** xs:integer.               */ ITR(52),
    /** xs:nonPositiveInteger.    */ NPI(53),
    /** xs:negativeInteger.       */ NIN(54),
    /** xs:long.                  */ LNG(55),
    /** xs:int.                   */ INT(56),
    /** xs:short.                 */ SHR(57),
    /** xs:byte.                  */ BYT(58),
    /** xs:nonNegativeInteger.    */ NNI(59),
    /** xs:unsignedLong.          */ ULN(60),
    /** xs:unsignedInt.           */ UIN(61),
    /** xs:unsignedShort.         */ USH(62),
    /** xs:unsignedByte.          */ UBY(63),
    /** xs:positiveInteger.       */ PIN(64),
    /** xs:duration.              */ DUR(65),
    /** xs:yearMonthDuration.     */ YMD(66),
    /** xs:dayTimeDuration.       */ DTD(67),
    /** xs:dateTime.              */ DTM(68),
    /** dateTimeStamp().          */ DTS(69),
    /** xs:date.                  */ DAT(70),
    /** xs:time.                  */ TIM(71),
    /** xs:gYearMonth.            */ YMO(72),
    /** xs:gYear.                 */ YEA(73),
    /** xs:gMonthDay.             */ MDA(74),
    /** xs:gDay.                  */ DAY(75),
    /** xs:gMonth.                */ MON(76),
    /** xs:boolean.               */ BLN(77),
    /** binary().                 */ BIN(78),
    /** xs:base64Binary.          */ B64(79),
    /** xs:hexBinary.             */ HEX(80),
    /** xs:anyURI.                */ URI(81),
    /** xs:QName.                 */ QNM(82),
    /** xs:NOTATION.              */ NOT(83),
    /** java().                   */ JAVA(86);

    /** Node ID. */
    private final byte id;
    /**
     * Constructor.
     * @param i type id
     */
    private ID(final int i) {
      id = (byte) i;
    }

    /**
     * returns the type ID as a byte.
     * @return type ID
     */
    public byte asByte() {
      return id;
    }

    /**
     * Gets the ID for the given byte value.
     * @param b byte
     * @return type ID if found, {@code null} otherwise
     */
    public static ID get(final byte b) {
      for(final ID i : values()) if(i.id == b) return i;
      return null;
    }

    /**
     * Gets the type instance for the given ID.
     * @param b type ID
     * @return corresponding type if found, {@code null} otherwise
     */
    public static Type getType(final byte b) {
      final ID id = get(b);
      if(id == null) return null;
      if(id == FUN) return FuncType.ANY_FUN;
      Type t = AtomType.getType(id);
      return t != null ? t : NodeType.getType(id);
    }
  }

  /**
   * Casts the specified item to the XQuery data type.
   * @param it item to be converted
   * @param ctx query context
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Casts the specified Java object to the XQuery data type.
   * @param o Java object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item cast(final Object o, final InputInfo ii) throws QueryException;

  /**
   * Casts the specified string to the XQuery data type.
   * @param s string object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item castString(final String s, final InputInfo ii) throws QueryException;

  /**
   * Returns the sequence type of this data type.
   * @return sequence type
   */
  SeqType seqType();

  // PUBLIC AND STATIC METHODS ================================================

  /**
   * Checks if this type is equal to the given one.
   * @param t other type
   * @return {@code true} if both types are equal, {@code false} otherwise
   */
  boolean eq(final Type t);

  /**
   * Checks if the current type is an instance of the specified type.
   * @param t type to be checked
   * @return result of check
   */
  boolean instanceOf(final Type t);

  /**
   * Computes the union between this type and the given one, i.e. the least common
   * ancestor of both types in the type hierarchy.
   * @param t other type
   * @return union type
   */
  Type union(final Type t);

  /**
   * Computes the intersection between this type and the given one, i.e. the least
   * specific type that is sub-type of both types. If no such type exists, {@code null} is
   * returned.
   * @param t other type
   * @return intersection type or {@code null}
   */
  Type intersect(final Type t);

  /**
   * Checks if the type refers to a node.
   * @return result of check
   */
  boolean isNode();

  /**
   * Checks if the type refers to a number.
   * @return result of check
   */
  boolean isNumber();

  /**
   * Checks if the type refers to an untyped item.
   * @return result of check
   */
  boolean isUntyped();

  /**
   * Checks if the type refers to a number or an untyped item.
   * @return result of check
   */
  boolean isNumberOrUntyped();

  /**
   * Checks if the type refers to a number or a string.
   * Returns if this item is untyped or a string.
   * @return result of check
   */
  boolean isStringOrUntyped();

  /**
   * Returns the string representation of this type.
   * @return name
   */
  byte[] string();

  /**
   * Returns a type id to differentiate all types.
   * @return id
   */
  ID id();

  @Override
  String toString();

  /**
   * Checks if the type is namespace-sensitive.
   * @return result of check
   */
  boolean nsSensitive();
}
