package org.basex.io.parse.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
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
public final class CsvW3Converter extends CsvW3ArraysConverter {
  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvW3Converter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws QueryException {
    final Value rows = super.finish(ii, qc);
    final MapBuilder columnIndexBuilder = new MapBuilder();
    final int hs = headers.size();
    for(int h = 0; h < hs; h++) {
      final byte[] header = headers.get(h);
      if(header.length > 0) {
        final Str column = Str.get(header);
        if(!columnIndexBuilder.contains(column)) columnIndexBuilder.put(column, Itr.get(h + 1));
      }
    }
    final XQMap columnIndex = columnIndexBuilder.map();
    // must be created last: the token list is consumed
    final Value columns = StrSeq.get(headers);

    // without a query context, the get function is unavailable and no record can be built
    if(qc == null) return new MapBuilder().put(COLUMNS, columns).
        put(COLUMN_INDEX, columnIndex).put(ROWS, rows).map();
    return XQMap.get(Records.PARSED_CSV_STRUCTURE.get(), columns, columnIndex, rows,
        Get.funcItem(rows, columnIndex, qc, ii));
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
          if(colIndex.type.instanceOf(BasicType.STRING)) {
            final Item it = (Item) columnIndex.get(colIndex);
            if(it.isEmpty()) throw CSV_COLUMNNAME_X.get(info, colIndex);
            colIndex = it;
          }
          final long index = colIndex.itr(info) - 1;
          if(index >= 0 && index < row.structSize()) return row.valueAt(index);
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
      final SeqType colType = ChoiceItemType.get(BasicType.STRING,
          BasicType.POSITIVE_INTEGER).seqType();
      final Var row = vs.addNew(new QNm("row"), rowType, qc, ii);
      final Var col = vs.addNew(new QNm("column"), colType, qc, ii);
      final Get get = new Get(ii, rows, columnIndex, new VarRef(ii, row), new VarRef(ii, col));
      final Var[] params = { row, col };
      final FuncType funcType = FuncType.get(STRING_O, rowType, colType);
      return new FuncItem(ii, get, params, AnnList.EMPTY, funcType, params.length, null);
    }
  }
}
