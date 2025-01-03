package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.expr.*;
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
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnParseCsv extends Parse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final ParseCsvOptions options = toOptions(arg(1), new ParseCsvOptions(), qc);
    options.validate(ii);

    final CsvParserOptions parserOpts = options.toCsvParserOptions();
    try {
      final XQMap map = (XQMap) CsvConverter.get(parserOpts).convert(new IOContent(value), ii);
      final Value columns;
      if(options.columnNames != null) {
        columns = options.columnNames;
      } else {
        final Value names = map.get(CsvXQueryConverter.NAMES).atomValue(qc, ii);
        if(parserOpts.get(CsvOptions.TRIM_WHITESPACE)) {
          columns = names;
        }
        else {
          final ValueBuilder vb = new ValueBuilder(qc);
          for(final Item col : names) {
            vb.add(Str.get(Token.trim(toZeroToken(col, qc))));
          };
          columns = vb.value();
        }
      }
      final MapBuilder columnIndexBuilder = new MapBuilder();
      int i = 0;
      for(final Item col : columns) {
        ++i;
        if(toStr(col, qc).length(ii) > 0 && columnIndexBuilder.get(col) == null) {
          columnIndexBuilder.put(col, Int.get(i));
        }
      }
      final XQMap columnIndex = columnIndexBuilder.map();
      final Value rows = map.get(CsvXQueryConverter.RECORDS);

      final MapBuilder result = new MapBuilder();
      result.put(Str.get("columns"), columns);
      result.put(Str.get("column-index"), columnIndex);
      result.put("rows", rows);
      result.put("get", Get.funcItem(rows, columnIndex, qc, ii));
      return result.map();
    } catch(final IOException ex) {
      throw CSV_ERROR_X.get(ii, ex);
    }
  }

  /**
   * Get function.
   */
  private static final class Get extends Arr {
    /** Result rows. */
    private final Value rows;
    /** Column name to index mapping. */
    private final XQMap columnIndex;

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

    /**
     * Create a function item for the get function.
     * @param rows result rows
     * @param columnIndex column name to index mapping
     * @param qc query context
     * @param ii input info
     * @return function item
     */
    protected static FuncItem funcItem(final Value rows, final XQMap columnIndex,
        final QueryContext qc, final InputInfo ii) {
      final VarScope vs = new VarScope();
      final SeqType rowType = POSITIVE_INTEGER_O;
      final SeqType colType = new ChoiceItemType(
          Arrays.asList(STRING_O, POSITIVE_INTEGER_O)).seqType();
      final Var row = vs.addNew(new QNm("row"), rowType, qc, ii);
      final Var col = vs.addNew(new QNm("column"), colType, qc, ii);
      final Get get = new Get(ii, rows, columnIndex,
          new Expr[] { new VarRef(ii, row), new VarRef(ii, col) });
      final Var[] params = { row, col };
      final FuncType funcType = FuncType.get(STRING_O, rowType, colType);
      return new FuncItem(ii, get, params, AnnList.EMPTY, funcType, params.length, null);
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

    /** Explicit column names. */
    public Value columnNames;
    /** Whether to extract the header from the first input row. */
    private boolean extractHeader;

    @Override
    void validate(final InputInfo ii) throws QueryException {
      super.validate(ii);
      final Value header = get(HEADER);
      if(BOOLEAN_O.instance(header)) extractHeader = toBoolean((Item) header, ii);
      else if(STRING_ZM.instance(header)) columnNames = header;
      else throw typeError(header, STRING_OM, ii);
    }

    @Override
    CsvParserOptions toCsvParserOptions() {
      final CsvParserOptions parserOpts = super.toCsvParserOptions();
      parserOpts.set(CsvOptions.TRIM_ROWS, get(TRIM_ROWS));
      parserOpts.set(CsvOptions.SELECT_COLUMNS, get(SELECT_COLUMNS));
      parserOpts.set(CsvOptions.HEADER, extractHeader);
      return parserOpts;
    }
  }
}
