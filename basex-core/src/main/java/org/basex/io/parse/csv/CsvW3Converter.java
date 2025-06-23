package org.basex.io.parse.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

import org.basex.build.csv.*;
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
 * This class converts CSV data to the representation defined by fn:parse-csv.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvW3Converter extends CsvXQueryConverter {
  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvW3Converter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws QueryException {
    final XQMap map = (XQMap) super.finish(ii, qc);
    Value columns = copts.get(CsvOptions.HEADER);
    if(columns.seqType().instanceOf(SeqType.BOOLEAN_O)) {
      columns = map.get(CsvXQueryConverter.NAMES).atomValue(qc, ii);
    }
    final MapBuilder columnIndexBuilder = new MapBuilder();
    int i = 0;
    for(final Item column : columns) {
      ++i;
      if(column.string(ii).length > 0 && !columnIndexBuilder.contains(column)) {
        columnIndexBuilder.put(column, Int.get(i));
      }
    }
    final XQMap columnIndex = columnIndexBuilder.map();
    final Value rows = map.get(CsvXQueryConverter.RECORDS);

    final MapBuilder result = new MapBuilder();
    result.put(COLUMNS, columns);
    result.put(COLUMN_INDEX, columnIndex);
    result.put(ROWS, rows);
    if(qc != null) result.put(GET, Get.funcItem(rows, columnIndex, qc, ii));
    return result.map();
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
          if(colIndex.type.instanceOf(AtomType.STRING)) {
            final Item it = (Item) columnIndex.get(colIndex);
            if(it.isEmpty()) throw CSV_COLUMNNAME_X.get(info, colIndex);
            colIndex = it;
          }
          final Value value = row.getOrNull(colIndex, qc, info);
          if(value != null) return value;
        }
      }
      return Str.EMPTY;
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
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
    private static FuncItem funcItem(final Value rows, final XQMap columnIndex,
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
