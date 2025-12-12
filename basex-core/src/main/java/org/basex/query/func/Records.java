package org.basex.query.func;

import org.basex.query.*;
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
  DIVIDED_DECIMALS("divided-decimals", false,
    field("quotient", Types.DECIMAL_O),
    field("remainder", Types.DECIMAL_O)
  ),
  /** Record definition. */
  LOAD_XQUERY_MODULE("load-xquery-module", false,
    field("variables", MapType.get(AtomType.QNAME, Types.ITEM_ZO).seqType()),
    field("functions", MapType.get(AtomType.QNAME,
      MapType.get(AtomType.INTEGER, Types.FUNCTION_O).seqType()).seqType())),
  /** Record definition. */
  MEMBER("member", false,
    field("value", Types.ITEM_ZM)),
  /** Record definition. */
  PARSED_CSV_STRUCTURE("parsed-csv-structure", false,
    field("columns", Types.STRING_ZM),
    field("column-index", MapType.get(AtomType.STRING, Types.INTEGER_O).seqType(Occ.ZERO_OR_ONE)),
    field("rows", ArrayType.get(Types.STRING_O).seqType(Occ.ZERO_OR_MORE)),
    field("get", FuncType.get(Types.STRING_O, Types.POSITIVE_INTEGER_O,
      ChoiceItemType.get(Types.POSITIVE_INTEGER_O, Types.STRING_O).seqType()).seqType())),
  /** Record definition. */
  RANDOM_NUMBER_GENERATOR("random-number-generator", true),
  /** Record definition. */
  SCHEMA_TYPE("schema-type", true),
  /** Record definition. */
  URI_STRUCTURE("uri-structure", true,
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
    field("query-parameters", MapType.get(AtomType.STRING, Types.STRING_ZM).
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
        add("constructo", true, FuncType.get(Types.ANY_ATOMIC_TYPE_ZM,
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
   * @param name name of record
   * @param extensible extensible flag
   * @param fields field declarations
   */
  Records(final String name, final boolean extensible, final NamedRecordField... fields) {
    final TokenObjectMap<RecordField> map = new TokenObjectMap<>(fields.length);
    for(final NamedRecordField field : fields) {
      map.put(field.name, field.field);
    }
    final QNm qnm = new QNm(name + "-record", QueryText.FN_URI);
    type = new RecordType(map, extensible, qnm, AnnList.EMPTY);
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
