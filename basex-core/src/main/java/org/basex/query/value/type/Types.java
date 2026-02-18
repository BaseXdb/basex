package org.basex.query.value.type;

import static org.basex.query.value.type.BasicType.*;
import static org.basex.query.value.type.ListType.*;
import static org.basex.query.value.type.NodeType.*;
import static org.basex.query.value.type.Occ.*;

import org.basex.util.hash.*;

/**
 * Numeric access to types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Types {
  /** Zero items (single instance). */
  public static final SeqType EMPTY_SEQUENCE_Z = ITEM.seqType(ZERO);

  /** Single item. */
  public static final SeqType ITEM_O = ITEM.seqType();
  /** Zero or one item. */
  public static final SeqType ITEM_ZO = ITEM.seqType(ZERO_OR_ONE);
  /** Zero or more items. */
  public static final SeqType ITEM_ZM = ITEM.seqType(ZERO_OR_MORE);
  /** One or more items. */
  public static final SeqType ITEM_OM = ITEM.seqType(ONE_OR_MORE);

  /** Zero or one xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_O = ANY_ATOMIC_TYPE.seqType();
  /** Zero or one xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_ZO = ANY_ATOMIC_TYPE.seqType(ZERO_OR_ONE);
  /** Zero or more xs:anyAtomicType. */
  public static final SeqType ANY_ATOMIC_TYPE_ZM = ANY_ATOMIC_TYPE.seqType(ZERO_OR_MORE);

  /** Error. */
  public static final SeqType ERROR_O = ERROR.seqType();

  /** Numeric. */
  public static final SeqType NUMERIC_O = NUMERIC.seqType();
  /** Zero or one numeric. */
  public static final SeqType NUMERIC_ZO = NUMERIC.seqType(ZERO_OR_ONE);
  /** Zero or more numerics. */
  public static final SeqType NUMERIC_ZM = NUMERIC.seqType(ZERO_OR_MORE);
  /** Double number. */
  public static final SeqType DOUBLE_O = DOUBLE.seqType();
  /** Zero or one double. */
  public static final SeqType DOUBLE_ZO = DOUBLE.seqType(ZERO_OR_ONE);
  /** Double number. */
  public static final SeqType DOUBLE_ZM = DOUBLE.seqType(ZERO_OR_MORE);
  /** Float number. */
  public static final SeqType FLOAT_O = FLOAT.seqType();
  /** Decimal number. */
  public static final SeqType DECIMAL_O = DECIMAL.seqType();
  /** Zero or one decimal number. */
  public static final SeqType DECIMAL_ZO = DECIMAL.seqType(ZERO_OR_ONE);
  /** Single integer. */
  public static final SeqType INTEGER_O = INTEGER.seqType();
  /** Zero or one integer. */
  public static final SeqType INTEGER_ZO = INTEGER.seqType(ZERO_OR_ONE);
  /** Zero or more integers. */
  public static final SeqType INTEGER_ZM = INTEGER.seqType(ZERO_OR_MORE);
  /** Positive integer. */
  public static final SeqType POSITIVE_INTEGER_O = POSITIVE_INTEGER.seqType();
  /** Zero or more bytes. */
  public static final SeqType BYTE_ZM = BYTE.seqType(ZERO_OR_MORE);

  /** Single string. */
  public static final SeqType STRING_O = STRING.seqType();
  /** Zero or one strings. */
  public static final SeqType STRING_ZO = STRING.seqType(ZERO_OR_ONE);
  /** Zero or more strings. */
  public static final SeqType STRING_ZM = STRING.seqType(ZERO_OR_MORE);
  /** One or more strings. */
  public static final SeqType STRING_OM = STRING.seqType(ONE_OR_MORE);
  /** Single NCName. */
  public static final SeqType NCNAME_O = NCNAME.seqType();
  /** Zero or one NCName. */
  public static final SeqType NCNAME_ZO = NCNAME.seqType(ZERO_OR_ONE);
  /** Single language. */
  public static final SeqType LANGUAGE_O = LANGUAGE.seqType();
  /** Single untyped atomic. */
  public static final SeqType UNTYPED_ATOMIC_O = UNTYPED_ATOMIC.seqType();
  /** Zero or one untyped atomic. */
  public static final SeqType UNTYPED_ATOMIC_ZO = UNTYPED_ATOMIC.seqType(ZERO_OR_ONE);

  /** Single URI. */
  public static final SeqType ANY_URI_O = ANY_URI.seqType();
  /** Zero or one URIs. */
  public static final SeqType ANY_URI_ZO = ANY_URI.seqType(ZERO_OR_ONE);
  /** Zero or more URIs. */
  public static final SeqType ANY_URI_ZM = ANY_URI.seqType(ZERO_OR_MORE);

  /** Single QName. */
  public static final SeqType QNAME_O = QNAME.seqType();
  /** Zero or one QNames. */
  public static final SeqType QNAME_ZO = QNAME.seqType(ZERO_OR_ONE);

  /** Single xs:boolean. */
  public static final SeqType BOOLEAN_O = BOOLEAN.seqType();
  /** Zero or one xs:boolean. */
  public static final SeqType BOOLEAN_ZO = BOOLEAN.seqType(ZERO_OR_ONE);

  /** Single date. */
  public static final SeqType DATE_O = DATE.seqType();
  /** Zero or one date. */
  public static final SeqType DATE_ZO = DATE.seqType(ZERO_OR_ONE);
  /** One day-time-duration. */
  public static final SeqType DAY_TIME_DURATION_O = DAY_TIME_DURATION.seqType();
  /** Zero or one day-time-duration. */
  public static final SeqType DAY_TIME_DURATION_ZO = DAY_TIME_DURATION.seqType(ZERO_OR_ONE);
  /** One date-time. */
  public static final SeqType DATE_TIME_O = DATE_TIME.seqType();
  /** Zero or one date-time. */
  public static final SeqType DATE_TIME_ZO = DATE_TIME.seqType(ZERO_OR_ONE);
  /** One date-time-stamp. */
  public static final SeqType DATE_TIME_STAMP_O = DATE_TIME_STAMP.seqType();
  /** One time. */
  public static final SeqType TIME_O = TIME.seqType();
  /** Zero or one time. */
  public static final SeqType TIME_ZO = TIME.seqType(ZERO_OR_ONE);
  /** Zero or one duration. */
  public static final SeqType DURATION_ZO = DURATION.seqType(ZERO_OR_ONE);

  /** Single binary. */
  public static final SeqType BINARY_O = BINARY.seqType();
  /** Zero or one binary. */
  public static final SeqType BINARY_ZO = BINARY.seqType(ZERO_OR_ONE);
  /** Zero or more binary. */
  public static final SeqType BINARY_ZM = BINARY.seqType(ZERO_OR_MORE);
  /** One xs:hexBinary. */
  public static final SeqType HEX_BINARY_O = HEX_BINARY.seqType();
  /** Single xs:base64Binary. */
  public static final SeqType BASE64_BINARY_O = BASE64_BINARY.seqType();
  /** Zero or one xs:base64Binary. */
  public static final SeqType BASE64_BINARY_ZO = BASE64_BINARY.seqType(ZERO_OR_ONE);
  /** Zero or more xs:base64Binary. */
  public static final SeqType BASE64_BINARY_ZM = BASE64_BINARY.seqType(ZERO_OR_MORE);

  /** String or xs:hex-binary or xs:base64-binary. */
  public static final Type STRING_OR_BINARY =
      ChoiceItemType.get(STRING_O, HEX_BINARY_O, BASE64_BINARY_O);
  /** Single string or xs:hex-binary or xs:base64-binary. */
  public static final SeqType STRING_OR_BINARY_O = STRING_OR_BINARY.seqType();
  /** Zero or one string or xs:hex-binary or xs:base64-binary. */
  public static final SeqType STRING_OR_BINARY_ZO = STRING_OR_BINARY.seqType(ZERO_OR_ONE);
  /** Zero or more string or xs:hex-binary or xs:base64-binary. */
  public static final SeqType STRING_OR_BINARY_ZM = STRING_OR_BINARY.seqType(ZERO_OR_MORE);

  /** Single node. */
  public static final SeqType NODE_O = NODE.seqType();
  /** Zero or one nodes. */
  public static final SeqType NODE_ZO = NODE.seqType(ZERO_OR_ONE);
  /** Zero or more nodes. */
  public static final SeqType NODE_ZM = NODE.seqType(ZERO_OR_MORE);
  /** One or more nodes. */
  public static final SeqType NODE_OM = NODE.seqType(ONE_OR_MORE);
  /** One attribute node. */
  public static final SeqType ATTRIBUTE_O = ATTRIBUTE.seqType();
  /** Zero or more attributes. */
  public static final SeqType ATTRIBUTE_ZM = ATTRIBUTE.seqType(ZERO_OR_MORE);
  /** One comment node. */
  public static final SeqType COMMENT_O = COMMENT.seqType();
  /** One document node. */
  public static final SeqType DOCUMENT_O = DOCUMENT.seqType();
  /** Zero or one document node. */
  public static final SeqType DOCUMENT_ZO = DOCUMENT.seqType(ZERO_OR_ONE);
  /** Zero or more document nodes. */
  public static final SeqType DOCUMENT_ZM = DOCUMENT.seqType(ZERO_OR_MORE);
  /** One element node. */
  public static final SeqType ELEMENT_O = ELEMENT.seqType();
  /** Zero or one element node. */
  public static final SeqType ELEMENT_ZO = ELEMENT.seqType(ZERO_OR_ONE);
  /** Zero or more element nodes. */
  public static final SeqType ELEMENT_ZM = ELEMENT.seqType(ZERO_OR_MORE);
  /** Namespace node. */
  public static final SeqType NAMESPACE_O = NAMESPACE.seqType();
  /** Processing instruction. */
  public static final SeqType PROCESSING_INSTRUCTION_O = PROCESSING_INSTRUCTION.seqType();
  /** Zero or one text node. */
  public static final SeqType TEXT_ZO = TEXT.seqType(ZERO_OR_ONE);
  /** Zero or more text nodes. */
  public static final SeqType TEXT_ZM = TEXT.seqType(ZERO_OR_MORE);

  /** Single NMTOKENS. */
  public static final SeqType NMTOKENS_O = NMTOKENS.seqType();

  /** Gregorian type. */
  public static final SeqType GREGORIAN_ZO = ChoiceItemType.get(DATE_TIME.seqType(), DATE.seqType(),
      TIME.seqType(), G_YEAR.seqType(), G_YEAR_MONTH.seqType(), G_MONTH.seqType(),
      G_MONTH_DAY.seqType(), G_DAY.seqType()).seqType(ZERO_OR_ONE);

  // types that instantiate sequence types must be placed last to avoid circular dependencies

  /** The general function type. */
  public static final FuncType FUNCTION = new FuncType(null, (SeqType[]) null);
  /** Single function. */
  public static final SeqType FUNCTION_O = FUNCTION.seqType();
  /** Zero of single function. */
  public static final SeqType FUNCTION_ZO = FUNCTION.seqType(ZERO_OR_ONE);
  /** Zero of more functions. */
  public static final SeqType FUNCTION_ZM = FUNCTION.seqType(ZERO_OR_MORE);
  /** Predicate function. */
  public static final SeqType PREDICATE_O = FuncType.get(BOOLEAN_ZO, ITEM_O, INTEGER_O).seqType();
  /** Predicate function. */
  public static final SeqType PREDICATE_ZM = FuncType.get(BOOLEAN_ZO, ITEM_ZM, INTEGER_O).seqType();
  /** Predicate function. */
  public static final SeqType BIPREDICATE_O = FuncType.get(BOOLEAN_ZO, ITEM_O, ITEM_O).seqType();
  /** Action function. */
  public static final SeqType ACTION_O = FuncType.get(ITEM_ZM, ITEM_O, INTEGER_O).seqType();
  /** Java function type. */
  public static final FuncType JAVA = new FuncType(null);

  /** The general map type. */
  public static final MapType MAP = ITEM_ZM.mapType(ANY_ATOMIC_TYPE);
  /** Single map. */
  public static final SeqType MAP_O = MAP.seqType();
  /** Zero or one map. */
  public static final SeqType MAP_ZO = MAP.seqType(ZERO_OR_ONE);
  /** Zero or more maps. */
  public static final SeqType MAP_ZM = MAP.seqType(ZERO_OR_MORE);

  /** The general array type. */
  public static final ArrayType ARRAY = ITEM_ZM.arrayType();
  /** Single array. */
  public static final SeqType ARRAY_O = ARRAY.seqType();
  /** Zero or one array. */
  public static final SeqType ARRAY_ZO = ARRAY.seqType(ZERO_OR_ONE);
  /** Zero or more arrays. */
  public static final SeqType ARRAY_ZM = ARRAY.seqType(ZERO_OR_MORE);

  /** Type of fn:schema-type-record member 'variety'. */
  public static final EnumType SCHEMA_TYPE_RECORD_VARIETY =
      EnumType.get("atomic", "list", "union", "empty", "simple", "element-only", "mixed");

  /** The empty record type. */
  public static final RecordType RECORD = new RecordType(new TokenObjectMap<>(0));
  /** Single empty record. */
  public static final SeqType RECORD_O = RECORD.seqType();

  /** Indexed item types. */
  private static final Type[] TYPES = new Type[Type.ID.LAST.asByte()];

  static {
    for(final BasicType type : BasicType.values()) TYPES[type.index()] = type;
    for(final NodeType type : NodeType.TYPES.values()) TYPES[type.index()] = type;
    TYPES[Type.ID.FUN.asByte()] = FUNCTION;
    TYPES[Type.ID.MAP.asByte()] = MAP;
    TYPES[Type.ID.ARRAY.asByte()] = ARRAY;
  }

  /** Private constructor. */
  private Types() { }

  /**
   * Returns the type at the specified index.
   * @param index index
   * @return corresponding type if found, {@code null} otherwise
   */
  public static Type type(final int index) {
    return TYPES[index];
  }
}
