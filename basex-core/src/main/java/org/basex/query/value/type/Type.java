package org.basex.query.value.type;

import java.io.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery types.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public interface Type {
  /** Type IDs. */
  enum ID {
    // function types
    /** function(*).              */ FUN(7),
    /** map(*).                   */ MAP(30),
    /** array(*).                 */ ARRAY(31),

    // node types
    /** node().                   */ NOD(8),
    /** text().                   */ TXT(9),
    /** processing-instruction(). */ PI(10),
    /** element().                */ ELM(11),
    /** document-node().          */ DOC(12, true),
    /** document-node(element()). */ DEL(13, true),
    /** attribute().              */ ATT(14, true),
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
    /** xs:QName.                 */ QNM(82, true),
    /** xs:NOTATION.              */ NOT(83),
    /** xs:numeric.               */ NUM(84),
    /** java().                   */ JAVA(86),
    /** last dummy type.          */ LAST(87);

    /** Type index. */
    private final byte index;
    /** Extended type information. */
    private final boolean extended;

    /**
     * Constructor.
     * @param index type index
     */
    ID(final int index) {
      this(index, false);
    }

    /**
     * Constructor.
     * @param index type index
     * @param extended extended type information
     */
    ID(final int index, final boolean extended) {
      this.index = (byte) index;
      this.extended = extended;
    }

    /**
     * Returns the type index. Also called by XQJ.
     * @return type index
     */
    public byte asByte() {
      return index;
    }

    /**
     * Indicates if this type returns extended type information.
     * @return result of check
     */
    public boolean isExtended() {
      return extended;
    }
  }

  /**
   * Casts the specified item to this type.
   * @param item item to be converted
   * @param qc query context (can be {@code null} if the target type needs no reference)
   * @param sc static context (only required for {@link AtomType#QNAME} conversion)
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  Value cast(Item item, QueryContext qc, StaticContext sc, InputInfo info) throws QueryException;

  /**
   * Casts the specified Java value to this type.
   * @param value Java value
   * @param qc query context (can be {@code null} if the target type needs no reference
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  Value cast(Object value, QueryContext qc, InputInfo info) throws QueryException;

  /**
   * Reads an item from the input stream.
   * @param in data input
   * @param qc query context
   * @return item
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  Item read(DataInput in, QueryContext qc) throws IOException, QueryException;

  /**
   * Returns a sequence type with a single number of occurrence.
   * @return sequence type
   */
  default SeqType seqType() {
    return seqType(Occ.EXACTLY_ONE);
  }

  /**
   * Internal function for creating a sequence type with the specified occurrence indicator.
   * @param occ occurrence indicator
   * @return sequence type
   * @see SeqType#get(Type, Occ)
   */
  SeqType seqType(Occ occ);

  /**
   * Checks if this is one of the specified types.
   * @param types types
   * @return result of check
   */
  default boolean oneOf(final Type... types) {
    for(final Type type : types) {
      if(this == type) return true;
    }
    return false;
  }

  // PUBLIC AND STATIC METHODS ====================================================================

  /**
   * Checks if this type is equal to the given one.
   * @param type other type
   * @return {@code true} if both types are equal, {@code false} otherwise
   */
  boolean eq(Type type);

  /**
   * Checks if the current type is an instance of the specified type.
   * @param type type to be checked
   * @return result of check
   */
  boolean instanceOf(Type type);

  /**
   * Computes the union between this type and the given one, i.e. the least common ancestor
   * of both types in the type hierarchy.
   * @param type other type
   * @return union type
   */
  Type union(Type type);

  /**
   * Computes the intersection between this type and the given one, i.e. the least specific type
   * that is subtype of both types. If no such type exists, {@code null} is returned.
   * @param type other type
   * @return intersection type or {@code null}
   */
  Type intersect(Type type);

  /**
   * Checks if items with this type are numbers.
   * @return result of check
   */
  boolean isNumber();

  /**
   * Checks if items with this type are untyped.
   * @return result of check
   */
  boolean isUntyped();

  /**
   * Checks if items with this type are numbers or untyped.
   * @return result of check
   */
  boolean isNumberOrUntyped();

  /**
   * Checks if items of this type are strings or untyped.
   * Returns if this item is untyped or a string.
   * @return result of check
   */
  boolean isStringOrUntyped();

  /**
   * Checks if items of this type are sortable.
   * @return result of check
   */
  boolean isSortable();

  /**
   * Returns the atomic type.
   * @return atomic type (can be {@code null}, e.g. for function types)
   */
  AtomType atomic();

  /**
   * Returns the type id.
   * @return id
   */
  ID id();

  /**
   * Returns the type index.
   * @return id
   */
  default byte index() {
    return id().asByte();
  }

  /**
   * Checks if the type is namespace-sensitive.
   * @return result of check
   */
  boolean nsSensitive();

  /**
   * Returns the given type, or the type of the specified expression if it is more specific.
   * @param expr expression
   * @return node type
   */
  default Type refine(final Expr expr) {
    if(expr != null) {
      final Type t = expr.seqType().type.intersect(this);
      if(t != null) return t;
    }
    return this;
  }

  @Override
  String toString();
}
