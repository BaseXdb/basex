package org.basex.query.func;

import static org.basex.query.QueryText.*;

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
  DATETIME(FN_URI, "dateTime",
    field("year", Types.INTEGER_O, true),
    field("month", Types.INTEGER_O, true),
    field("day", Types.INTEGER_O, true),
    field("hours", Types.INTEGER_O, true),
    field("minutes", Types.INTEGER_O, true),
    field("seconds", Types.DECIMAL_O, true),
    field("timezone", Types.DAY_TIME_DURATION_O, true)
  ),
  /** Record definition. */
  DIVIDED_DECIMALS(FN_URI, "divided-decimals",
    field("quotient", Types.DECIMAL_O),
    field("remainder", Types.DECIMAL_O)
  ),
  /** Record definition. */
  INFER_ENCODING(BIN_URI, "infer-encoding",
    field("encoding", Types.STRING_O, false),
    field("offset", Types.INTEGER_O, false)
  ),
  /** Record definition. */
  LOAD_XQUERY_MODULE(FN_URI, "load-xquery-module",
    field("variables", MapType.get(BasicType.QNAME, Types.ITEM_ZM).seqType()),
    field("functions", MapType.get(BasicType.QNAME,
      MapType.get(BasicType.INTEGER, Types.FUNCTION_O).seqType()).seqType())),
  /** Record definition. */
  MEMBER(ARRAY_URI, "member",
    field("value", Types.ITEM_ZM)),
  /** Record definition. */
  PARSED_CSV_STRUCTURE(FN_URI, "parsed-csv-structure",
    field("columns", Types.STRING_ZM),
    field("column-index", MapType.get(BasicType.STRING, Types.INTEGER_O).seqType(Occ.ZERO_OR_ONE)),
    field("rows", ArrayType.get(Types.STRING_O).seqType(Occ.ZERO_OR_MORE)),
    field("get", FuncType.get(Types.STRING_O, Types.POSITIVE_INTEGER_O,
      ChoiceItemType.get(Types.POSITIVE_INTEGER_O, Types.STRING_O).seqType()).seqType())),
  /** Record definition. */
  RANDOM_NUMBER_GENERATOR(FN_URI, "random-number-generator"),
  /** Record definition. */
  SCHEMA_TYPE(FN_URI, "schema-type"),
  /** Record definition. */
  URI_STRUCTURE(FN_URI, "uri-structure",
    field("uri", Types.STRING_ZO, true),
    field("scheme", Types.STRING_ZO, true),
    field("absolute", Types.BOOLEAN_ZO, true),
    field("hierarchical", Types.BOOLEAN_ZO, true),
    field("authority", Types.STRING_ZO, true),
    field("userinfo", Types.STRING_ZO, true),
    field("host", Types.STRING_ZO, true),
    field("port", Types.INTEGER_ZO, true),
    field("path", Types.STRING_ZO, true),
    field("query", Types.STRING_ZO, true),
    field("fragment", Types.STRING_ZO, true),
    field("path-segments", Types.STRING_ZM, true),
    field("query-parameters", MapType.get(BasicType.STRING, Types.STRING_ZM).
        seqType(Occ.ZERO_OR_ONE), true),
    field("filepath", Types.STRING_ZO, true)
  );

  /** Built-in record types. */
  public static final QNmMap<RecordType> BUILT_IN = new QNmMap<>();

  static {
    for(final Records record : values()) {
      BUILT_IN.put(record.get().name(), record.get());
    }

    // recursive definitions
    final RecordType rng = RANDOM_NUMBER_GENERATOR.get();
    rng.add("number", false, Types.DOUBLE_O).
        add("next", false, FuncType.get(rng.seqType()).seqType()).
        add("permute", false, FuncType.get(Types.ITEM_ZM, Types.ITEM_ZM).seqType());

    final RecordType stp = SCHEMA_TYPE.get();
    stp.add("name", false, Types.QNAME_ZO).
        add("is-simple", false, Types.BOOLEAN_O).
        add("base-type", false, FuncType.get(stp.seqType(Occ.ZERO_OR_ONE)).seqType()).
        add("primitive-type", true, FuncType.get(stp.seqType()).seqType()).
        add("variety", true, Types.SCHEMA_TYPE_RECORD_VARIETY.seqType()).
        add("members", true, FuncType.get(stp.seqType(Occ.ZERO_OR_MORE)).seqType()).
        add("simple-content-type", true, FuncType.get(stp.seqType()).seqType()).
        add("matches", true, FuncType.get(Types.BOOLEAN_O, Types.ANY_ATOMIC_TYPE_O).seqType()).
        add("constructor", true, FuncType.get(Types.ANY_ATOMIC_TYPE_ZM,
            Types.ANY_ATOMIC_TYPE_ZO).seqType());
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
    return field(name, type, false);
  }

  /**
   * Returns a named record field.
   * @param name name
   * @param type type of record field
   * @param optional optional flag
   * @return name/field pair
   */
  private static NamedRecordField field(final String name, final SeqType type,
      final boolean optional) {
    return field(name, new RecordField(type, optional));
  }
}
