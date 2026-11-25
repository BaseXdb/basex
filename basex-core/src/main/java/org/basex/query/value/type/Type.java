package org.basex.query.value.type;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * XDM item types.
 *
 * @author BaseX Team, BSD License
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
    /** node().                   */ NOD(20),
    /** xnode().                  */ XND(8),
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
    /** jnode().                  */ JND(19),

    // basic types
    /** choice item type.         */ CIT(29),
    /** item().                   */ ITEM(32),

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
    /** enum().                   */ ENM(87),
    /** xs:error.                 */ ERR(88),

    /** last dummy type.          */ LAST(89);

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
     * Returns the type index, which is also used by XQJ.
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

    /**
     * Checks if this is one of the specified IDs.
     * @param ids IDs
     * @return result of check
     */
    public boolean oneOf(final ID... ids) {
      for(final ID id : ids) {
        if(this == id) return true;
      }
      return false;
    }
  }

  /** Effects of coercion on matching values, ordered by strength. */
  enum Coercion {
    /** Values are kept. */ KEEP,
    /** Records may be rebuilt. */ REBUILD,
    /** Records may be rebuilt, functions may be wrapped. */ WRAP;

    /**
     * Returns the stronger of this and the specified effect.
     * @param coercion other effect
     * @return stronger effect
     */
    public Coercion max(final Coercion coercion) {
      return compareTo(coercion) >= 0 ? this : coercion;
    }
  }

  /**
   * Casts the specified item to this type.
   * @param item item to be converted
   * @param qc query context, required by some target types (can be {@code null})
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  Value cast(Item item, QueryContext qc, InputInfo info) throws QueryException;

  /**
   * Casts the specified Java value to this type.
   * @param value Java value
   * @param qc query context, required by some target types (can be {@code null})
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  default Value cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return cast(Str.get(value, info), qc, info);
  }

  /**
   * Reads an item from the input stream.
   * @param in data input
   * @param qc query context
   * @return item
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  default Item read(final DataInput in, final QueryContext qc) throws IOException, QueryException {
    throw Util.notExpected();
  }

  /**
   * Returns a sequence type for exactly one item of this type.
   * @return sequence type
   */
  default SeqType seqType() {
    return seqType(Occ.EXACTLY_ONE);
  }

  /**
   * Creates a sequence type with this item type and the specified occurrence indicator.
   * @param occ occurrence indicator
   * @return sequence type
   * @see SeqType#get(Type, Occ)
   */
  SeqType seqType(Occ occ);

  /**
   * Checks if this is one of the specified types.
   * @param type1 first type
   * @param type2 second type
   * @return result of check
   */
  default boolean oneOf(final Type type1, final Type type2) {
    return this == type1 || this == type2;
  }

  /**
   * Checks if this is one of the specified types.
   * @param type1 first type
   * @param type2 second type
   * @param type3 third type
   * @return result of check
   */
  default boolean oneOf(final Type type1, final Type type2, final Type type3) {
    return this == type1 || this == type2 || this == type3;
  }

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

  /**
   * Returns the function type of this type if its items are functions.
   * @return function type (can be {@code null})
   */
  default FuncType funcType() {
    return null;
  }

  // PUBLIC AND STATIC METHODS ====================================================================

  /**
   * Checks if this type is equal to the given one.
   * @param type other type (dereferenced by the caller)
   * @return {@code true} if both types are equal, {@code false} otherwise
   */
  boolean eq(Type type);

  /**
   * Checks if this type is an instance of the specified type.
   * @param type type to be checked (dereferenced by the caller)
   * @return result of check
   */
  boolean instanceOf(Type type);

  /**
   * Checks if the specified item is an instance of this type.
   * @param item item to check
   * @param coerce item coercion
   * @return result of check
   */
  default boolean instance(final Item item, final boolean coerce) {
    return item.instanceOf(this, coerce);
  }

  /**
   * Computes the union of this type and the given one, i.e., their least common supertype.
   * @param type other type (dereferenced by the caller)
   * @return union type
   */
  Type union(Type type);

  /**
   * Computes the intersection of this type and the given one, i.e., their most general subtype.
   * @param type other type (dereferenced by the caller)
   * @return intersection type (can be {@code null})
   */
  Type intersect(Type type);

  /**
   * Checks if items of this type are known to atomize to numbers.
   * @return result of check
   */
  default boolean isNumber() {
    return false;
  }

  /**
   * Checks if items of this type are known to atomize to untyped values.
   * @return result of check
   */
  default boolean isUntyped() {
    return false;
  }

  /**
   * Checks if items of this type may atomize to untyped values.
   * @return result of check
   */
  default boolean mayBeUntyped() {
    final BasicType atomic = atomic();
    return atomic == null || BasicType.UNTYPED_ATOMIC.instanceOf(atomic);
  }

  /**
   * Checks if items of this type are known to atomize to numbers or untyped values.
   * @return result of check
   */
  default boolean isNumberOrUntyped() {
    return isNumber() || isUntyped();
  }

  /**
   * Checks if items of this type are known to atomize to strings, URIs or untyped values.
   * @return result of check
   */
  default boolean isStringOrUntyped() {
    return false;
  }

  /**
   * Returns the type of the atomized items if the items are atomizable and the type is known.
   * @return atomic type (can be {@code null})
   */
  BasicType atomic();

  /**
   * Returns the type of the atomized items, or {@code xs:anyAtomicType} if it is unknown.
   * @return atomic type
   */
  default BasicType atomicOrAny() {
    final BasicType at = atomic();
    return at != null ? at : BasicType.ANY_ATOMIC_TYPE;
  }

  /**
   * Returns the type ID if the type has one.
   * @return ID (can be {@code null})
   */
  ID id();

  /**
   * Returns the strongest effect that coercion to this type may have on a matching value.
   * @return coercion effect
   */
  default Coercion coercion() {
    return Coercion.KEEP;
  }

  /**
   * Checks if items of this static type are instances of the target type that are kept.
   * @param target target type
   * @param coerce coercion
   * @return result of check
   */
  default boolean keptStatic(final Type target, final boolean coerce) {
    // static function types are upper bounds: functions of the same type may still be wrapped
    // fn(xs:integer) → fn(xs:integer): false (an fn(xs:decimal) item would be wrapped)
    return !(coerce && target.coercion() == Coercion.WRAP) && keptExact(target, coerce);
  }

  /**
   * Checks if items of this exact type are instances of the target type that are kept.
   * @param target target type
   * @param coerce coercion
   * @return result of check
   */
  default boolean keptExact(final Type target, final boolean coerce) {
    // xs:integer → xs:decimal, R → R: true; fn(xs:decimal) → fn(xs:integer): false (wrapped)
    final Type tp = deref(), tt = target.deref();
    return tp == tt || tp.instanceOf(tt) && (!coerce || tp == BasicType.ERROR ||
      tt.coercion() == Coercion.KEEP || tp.eq(tt) || RecordType.derives(tp, tt));
  }

  /**
   * Checks if this type can be attached to a value.
   * @return result of check
   */
  default boolean attachable() {
    return true;
  }

  /**
   * Checks if this type can be attached to a value that matches it.
   * @param coerce item coercion
   * @return result of check
   */
  default boolean attachable(final boolean coerce) {
    return attachable() && (coerce || coercion() == Coercion.KEEP);
  }

  /**
   * Returns the type that can be attached to values of this type.
   * @return attachable type
   */
  default Type toAttachable() {
    // (xs:int | xs:string), enum('a') or array((A | B)) are never attached
    return attachable() ? this : BasicType.ITEM;
  }

  /**
   * Dereferences this type.
   * @return referenced type of a type reference, or this type
   */
  default Type deref() {
    return this;
  }

  /**
   * Checks if this type is independent of a specific query and may be cached globally.
   * @return result of check
   */
  default boolean global() {
    return false;
  }

  /**
   * Returns the intersection with the type of the specified expression if it is attachable.
   * @param expr expression (can be {@code null})
   * @return type
   */
  default Type refine(final Expr expr) {
    if(expr != null) {
      final Type tp = expr.seqType().type.intersect(this);
      if(tp != null && tp.attachable()) return tp;
    }
    return this;
  }

  /**
   * Returns an info message that suggests a type with a similar name.
   * @param qname name of the unknown type
   * @return info string
   */
  static String similar(final QNm qname) {
    final TokenList list = new TokenList();
    list.add(QueryText.ITEM).add(QueryText.FUNCTION).add(QueryText.FN);
    list.add(QueryText.MAP).add(QueryText.ARRAY);
    for(final Kind kind : Kind.values()) list.add(kind.name);
    final byte[] similar = Levenshtein.similarOrPrefix(qname.local(), list.finish());
    return QueryError.similar(qname.prefixId(Token.XML), similar);
  }

  /**
   * Returns the node kind if this is a node type.
   * @return node kind (can be {@code null})
   */
  default Kind kind() {
    return null;
  }

  /**
   * Returns the name of the type if it has one.
   * @return name (can be {@code null})
   */
  Object name();

  /**
   * Returns a string representation with the specified parameters.
   * @param separator separator (can be {@code null})
   * @param params parameters
   * @return string representation
   */
  default String toString(final String separator, final Object... params) {
    final String name = name().toString().toLowerCase(Locale.ENGLISH).replace('_', '-');
    return new QueryString().token(name).tokens(params, separator, true).toString();
  }

  @Override
  String toString();
}
