package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.FnCsvToArrays.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public class FnParseCsv extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final IO io = new IOContent(value);
    final ParseCsvOptions options = toOptions(arg(1), new ParseCsvOptions(), qc);
    options.validate(info);

    final CsvParserOptions cpo = new CsvParserOptions();
    cpo.set(CsvOptions.SEPARATOR, options.get(CsvToArraysOptions.FIELD_DELIMITER));
    cpo.set(CsvOptions.ROW_DELIMITER, options.get(CsvToArraysOptions.ROW_DELIMITER));
    cpo.set(CsvOptions.QUOTE_CHARACTER, options.get(CsvToArraysOptions.QUOTE_CHARACTER));
    cpo.set(CsvOptions.TRIM_WHITSPACE, options.get(CsvToArraysOptions.TRIM_WHITESPACE));
    cpo.set(CsvOptions.TRIM_ROWS, options.get(ParseCsvOptions.TRIM_ROWS));
    cpo.set(CsvOptions.SELECT_COLUMNS, options.get(ParseCsvOptions.SELECT_COLUMNS));

    final Value header = options.get(ParseCsvOptions.HEADER);
    Value names = null;
    if(BOOLEAN_O.instance(header)) cpo.set(CsvOptions.HEADER, toBoolean((Item) header));
    else if(STRING_OM.instance(header)) names = header;
    else throw EXP_FOUND_X_X_X.get(ii, STRING_OM, header.seqType(), header);

    cpo.set(CsvOptions.FORMAT, CsvFormat.XQUERY);
    cpo.set(CsvOptions.QUOTES, true);
    cpo.set(CsvOptions.STRICT_QUOTING, true);

    try {
      final XQMap map = (XQMap) CsvConverter.get(cpo).convert(io, info);
      final MapBuilder result = new MapBuilder();

      if(names == null) names = map.get(CsvXQueryConverter.NAMES).atomValue(qc, ii);
      result.put(Str.get("columns"), names);

      final MapBuilder cib = new MapBuilder();
      int i = 0;
      for(Item name : names) {
        ++i;
        AStr str = toStr(name, qc);
        if(str.length(ii) > 0 && cib.get(name) == null) cib.put(name, Int.get(i));
      }

      final XQMap columnIndex = cib.map();
      result.put(Str.get("column-index"), columnIndex);
      final Value rows = map.get(CsvXQueryConverter.RECORDS);
      result.put("rows", rows);

      // create get function

      final VarScope vs = new VarScope();
      final SeqType rowType = POSITIVE_INTEGER_O;
      final Var row = vs.addNew(new QNm("row"), rowType, qc, ii);
      final SeqType colType = new ChoiceItemType(
          Arrays.asList(STRING_O, POSITIVE_INTEGER_O)).seqType();
      final Var col = vs.addNew(new QNm("column"), colType, qc, ii);
      final Get get = new Get(info, rows, columnIndex,
          new Expr[] { new VarRef(ii, row), new VarRef(ii, col)});
      final Var[] params = { row, col};
      final FuncType funcType = FuncType.get(STRING_O, rowType, colType);
      result.put("get",
          new FuncItem(ii, get, params, AnnList.EMPTY, funcType, params.length, null));

      return result.map();
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(info, ex);
    }
  }

  /**
   * Get function.
   */
  private static final class Get extends Arr {
    /** Result rows. */
    final Value rows;
    /** Column name to index mapping. */
    final XQMap columnIndex;

    /**
     * Constructor.
     * @param ii input info
     * @param rows result rows
     * @param columnIndex column name to index mapping
     * @param args function arguments
     */
    private Get(final InputInfo ii, final Value rows, final XQMap columnIndex,
        final Expr... args) {
      super(ii, STRING_O, args);
      this.rows = rows;
      this.columnIndex = columnIndex;
    }

    @Override
    public Value value(final QueryContext qc) throws QueryException {
      final long rowIndex = toLong(arg(0), qc);
      if(rowIndex > rows.size()) return Str.EMPTY;
      final XQArray row = (XQArray) rows.itemAt(rowIndex - 1);
      if(row == null) return Str.EMPTY;
      Item colIndex = toAtomItem(arg(1), qc);
      if(STRING_O.instance(colIndex)) {
        final Item it = (Item) columnIndex.get(colIndex);
        if(it.isEmpty()) throw CSV_COLUMNNAME_X.get(info, colIndex);
        colIndex = it;
      }
      final Value val = row.getInternal(colIndex, qc, info, false);
      return val == null ? Str.EMPTY : val;
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      return copyType(new Get(info, rows, columnIndex, copyAll(cc, vm, args())));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token("csv-get").params(exprs);
    }
  }

  /**
   * Options for fn:parse-csv.
   */
  public static final class ParseCsvOptions extends FnCsvToArrays.CsvToArraysOptions {
    /** parse-csv option header. */
    public static final ValueOption HEADER = new ValueOption("header", ITEM_ZM, Bln.FALSE);
    /** parse-csv option select-columns. */
    public static final NumbersOption SELECT_COLUMNS = new NumbersOption("select-columns");
    /** parse-csv option trim-rows. */
    public static final BooleanOption TRIM_ROWS = new BooleanOption("trim-rows", false);
  }
}
