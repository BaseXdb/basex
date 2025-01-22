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
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnParseCsv extends Parse {
  /** Columns. */
  public static final Str COLUMNS = Str.get("columns");
  /** Column-index. */
  public static final Str COLUMN_INDEX = Str.get("column-index");
  /** Rows. */
  public static final Str ROWS = Str.get("rows");
  /** Get. */
  public static final Str GET = Str.get("get");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final CsvParserOptions options = toOptions(arg(1), new CsvW3Options(), qc).finish(info,
        CsvFormat.W3);

    try {
      final XQMap map = (XQMap) CsvConverter.get(options).convert(new IOContent(value), ii);
      Value columns = options.get(CsvOptions.HEADER);
      if(SeqType.BOOLEAN_O.instance(columns)) {
        final Value names = map.get(CsvXQueryConverter.NAMES).atomValue(qc, ii);
        if(options.get(CsvOptions.TRIM_WHITESPACE)) {
          columns = names;
        } else {
          final ValueBuilder vb = new ValueBuilder(qc);
          for(final Item name : names) {
            vb.add(Str.get(Token.trim(toZeroToken(name, qc))));
          }
          columns = vb.value();
        }
      }
      final MapBuilder columnIndexBuilder = new MapBuilder();
      int i = 0;
      for(final Item column : columns) {
        ++i;
        if(toToken(column).length > 0 && !columnIndexBuilder.contains(column)) {
          columnIndexBuilder.put(column, Int.get(i));
        }
      }
      final XQMap columnIndex = columnIndexBuilder.map();
      final Value rows = map.get(CsvXQueryConverter.RECORDS);

      final MapBuilder result = new MapBuilder();
      result.put(COLUMNS, columns);
      result.put(COLUMN_INDEX, columnIndex);
      result.put(ROWS, rows);
      result.put(GET, Get.funcItem(rows, columnIndex, qc, ii));
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
    private Get(final InputInfo ii, final Value rows, final XQMap columnIndex, final Expr... args) {
      super(ii, STRING_O, args);
      this.rows = rows;
      this.columnIndex = columnIndex;
    }

    @Override
    public Value value(final QueryContext qc) throws QueryException {
      final long rowIndex = toLong(arg(0), qc);
      if(rowIndex <= rows.size()) {
        final XQArray row = (XQArray) rows.itemAt(rowIndex - 1);
        if(row != null) {
          Item colIndex = toAtomItem(arg(1), qc);
          if(STRING_O.instance(colIndex)) {
            final Item it = (Item) columnIndex.get(colIndex);
            if(it.isEmpty()) throw CSV_COLUMNNAME_X.get(info, colIndex);
            colIndex = it;
          }
          final Value val = row.getInternal(colIndex, qc, info, false);
          if(val != null) return val;
        }
      }
      return Str.EMPTY;
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
      final Get get = new Get(ii, rows, columnIndex, new VarRef(ii, row), new VarRef(ii, col));
      final Var[] params = { row, col };
      final FuncType funcType = FuncType.get(STRING_O, rowType, colType);
      return new FuncItem(ii, get, params, AnnList.EMPTY, funcType, params.length, null);
    }
  }
}
