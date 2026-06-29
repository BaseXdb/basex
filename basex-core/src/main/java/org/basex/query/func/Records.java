package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Built-in record types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Records {
  /** Record definition. */
  ATTRIBUTE_CONVERSION_PLAN(FN_URI, "attribute-conversion-plan",
    field("type", EnumType.get("integer", "decimal", "double", "boolean", "string", "skip").
        seqType())),
  /** Record definition. */
  COMPILED_REGEX(FN_URI, "compiled-regex"),
  /** Record definition. */
  DATETIME(FN_URI, "dateTime",
    field("year", Types.INTEGER_ZO),
    field("month", Types.INTEGER_ZO),
    field("day", Types.INTEGER_ZO),
    field("hours", Types.INTEGER_ZO),
    field("minutes", Types.INTEGER_ZO),
    field("seconds", Types.DECIMAL_ZO),
    field("timezone", Types.DAY_TIME_DURATION_ZO)
  ),
  /** Record definition. */
  DIVIDED_DECIMALS(FN_URI, "divided-decimals",
    field("quotient", Types.DECIMAL_O),
    field("remainder", Types.DECIMAL_O)
  ),
  /** Record definition. */
  ELEMENT_CONVERSION_PLAN(FN_URI, "element-conversion-plan",
    field("layout", EnumType.get("empty", "empty-plus", "simple", "simple-plus", "list",
        "list-plus", "record", "sequence", "mixed", "xml", "error", "deep-skip").seqType()),
    field("child", Types.STRING_ZO),
    field("type", EnumType.get("integer", "decimal", "double", "boolean", "string").
        seqType(Occ.ZERO_OR_ONE))),
  /** Record definition. */
  INFER_ENCODING(BIN_URI, "infer-encoding",
    field("encoding", Types.STRING_O),
    field("offset", Types.INTEGER_O)
  ),
  /** Record definition. */
  LOAD_XQUERY_MODULE(FN_URI, "load-xquery-module",
    field("variables", MapType.get(BasicType.QNAME, Types.ITEM_ZM).seqType()),
    field("functions", MapType.get(BasicType.QNAME,
      MapType.get(BasicType.INTEGER, Types.FUNCTION_O).seqType()).seqType())),
  /** Record definition. */
  MATCHING_GROUP(FN_URI, "matching-group",
    field("group", Types.STRING_O),
    field("position", Types.INTEGER_O)),
  /** Record definition. */
  MATCHING_SEGMENT(FN_URI, "matching-segment",
    field("substring", Types.STRING_O),
    field("position", Types.INTEGER_O),
    field("groups", MapType.get(BasicType.INTEGER, MATCHING_GROUP.get().seqType()).seqType())),
  /** Record definition. */
  MEMBER(ARRAY_URI, "member",
    field("value", Types.ITEM_ZM)),
  /** Record definition. */
  PARSED_CSV_STRUCTURE(FN_URI, "parsed-csv-structure",
    field("columns", Types.STRING_ZM),
    field("column-index", MapType.get(BasicType.STRING, Types.INTEGER_O).seqType(Occ.ZERO_OR_ONE)),
    field("rows", ArrayType.get(Types.STRING_O).seqType(Occ.ZERO_OR_MORE)),
    field("get", FuncType.get(Types.STRING_O, Types.POSITIVE_INTEGER_O,
      ChoiceItemType.get(BasicType.POSITIVE_INTEGER, BasicType.STRING).seqType()).seqType())),
  /** Record definition. */
  RANDOM_NUMBER_GENERATOR(FN_URI, "random-number-generator"),
  /** Record definition. */
  SCHEMA_TYPE(FN_URI, "schema-type"),
  /** Record definition. */
  URI_STRUCTURE(FN_URI, "uri-structure",
    field("uri", Types.STRING_ZO),
    field("scheme", Types.STRING_ZO),
    field("absolute", Types.BOOLEAN_ZO),
    field("hierarchical", Types.BOOLEAN_ZO),
    field("authority", Types.STRING_ZO),
    field("userinfo", Types.STRING_ZO),
    field("host", Types.STRING_ZO),
    field("port", Types.INTEGER_ZO),
    field("path", Types.STRING_ZO),
    field("query", Types.STRING_ZO),
    field("fragment", Types.STRING_ZO),
    field("path-segments", Types.STRING_ZM),
    field("query-parameters", MapType.get(BasicType.STRING, Types.STRING_ZM).
        seqType(Occ.ZERO_OR_ONE)),
    field("filepath", Types.STRING_ZO)
  );

  /** Built-in record types. */
  public static final QNmMap<RecordType> BUILT_IN = new QNmMap<>();

  static {
    for(final Records record : values()) {
      BUILT_IN.put(record.get().name(), record.get());
    }

    // definitions requiring (possibly recursive) forward references
    final RecordType rng = RANDOM_NUMBER_GENERATOR.get();
    rng.add("number", Types.DOUBLE_O).
        add("next", FuncType.get(rng.seqType()).seqType()).
        add("permute", FuncType.get(Types.ITEM_ZM, Types.ITEM_ZM).seqType());

    final RecordType stp = SCHEMA_TYPE.get();
    stp.add("name", Types.QNAME_ZO).
        add("is-simple", Types.BOOLEAN_O).
        add("base-type", FuncType.get(stp.seqType(Occ.ZERO_OR_ONE)).seqType()).
        add("primitive-type", FuncType.get(stp.seqType()).seqType(Occ.ZERO_OR_ONE)).
        add("variety", Types.SCHEMA_TYPE_RECORD_VARIETY.seqType(Occ.ZERO_OR_ONE)).
        add("members", FuncType.get(stp.seqType(Occ.ZERO_OR_MORE)).seqType(Occ.ZERO_OR_ONE)).
        add("simple-content-type", FuncType.get(stp.seqType()).seqType(Occ.ZERO_OR_ONE)).
        add("matches",
            FuncType.get(Types.BOOLEAN_O, Types.ANY_ATOMIC_TYPE_O).seqType(Occ.ZERO_OR_ONE)).
        add("constructor", FuncType.get(Types.ANY_ATOMIC_TYPE_ZM,
            Types.ANY_ATOMIC_TYPE_ZO).seqType(Occ.ZERO_OR_ONE));

    final RecordType crx = COMPILED_REGEX.get();
    crx.add("pattern", Types.STRING_O).
        add("flags", Types.STRING_O).
        add("matches", FuncType.get(Types.BOOLEAN_O, Types.STRING_O).seqType()).
        add("tokenize", FuncType.get(Types.STRING_ZM, Types.STRING_O).seqType()).
        add("replace", FuncType.get(Types.STRING_O, Types.STRING_O, ChoiceItemType.get(
            BasicType.STRING, FuncType.get(Types.ITEM_ZO, Types.UNTYPED_ATOMIC_O,
                Types.UNTYPED_ATOMIC_ZM)).seqType(Occ.ZERO_OR_ONE)).seqType()).
        add("analyze-string", FuncType.get(
            NodeType.get(NameTest.get(FnAnalyzeString.Q_ANALYZE_STRING_RESULT)).seqType(),
            Types.STRING_O).seqType()).
        add("matching-segments", FuncType.get(
            MATCHING_SEGMENT.get().seqType(Occ.ZERO_OR_MORE), Types.STRING_O).seqType());
  }

  /** Record type. */
  private final RecordType type;

  /**
   * Returns the record type.
   * @return type
   */
  public RecordType get() {
    return type;
  }

  /**
   * Named record field.
   * @param name name
   * @param field record field
   */
  private record NamedRecordField(byte[] name, RecordField field) { }

  /**
   * Constructor.
   * @param namespace namespace of record
   * @param name name of record
   * @param fields field declarations
   */
  Records(final byte[] namespace, final String name, final NamedRecordField... fields) {
    final TokenObjectMap<RecordField> map = new TokenObjectMap<>(fields.length);
    for(final NamedRecordField field : fields) {
      map.put(field.name, field.field);
    }
    final QNm qnm = new QNm(name + "-record", namespace);
    type = new RecordType(map, qnm, AnnList.EMPTY);
  }

  /**
   * Returns a named record field.
   * @param name name
   * @param field record field
   * @return name/field pair
   */
  private static NamedRecordField field(final String name, final RecordField field) {
    return new NamedRecordField(Token.token(name), field);
  }

  /**
   * Returns a named record field.
   * @param name name
   * @param type type of record field
   * @return name/field pair
   */
  private static NamedRecordField field(final String name, final SeqType type) {
    return field(name, new RecordField(type));
  }
}
