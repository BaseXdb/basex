package org.basex.query.parse;

/**
 * Formats a Markdown table with aligned columns, which is also readable when it is not rendered.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class MarkdownTable {
  /** Private constructor. */
  private MarkdownTable() { }

  /**
   * Formats a table with right-aligned columns.
   * @param heads column headers
   * @param rows rows: cells by column
   * @return table
   */
  static String format(final String[] heads, final String[][] rows) {
    return format(heads, rows, new boolean[heads.length]);
  }

  /**
   * Formats a table.
   * @param heads column headers
   * @param rows rows: cells by column
   * @param left indicates for each column if it is left-aligned, and right-aligned otherwise
   * @return table
   */
  static String format(final String[] heads, final String[][] rows, final boolean[] left) {
    // columns are as wide as their header and cells
    final int[] widths = new int[heads.length];
    for(int c = 0; c < widths.length; c++) {
      widths[c] = heads[c].length();
      for(final String[] row : rows) widths[c] = Math.max(widths[c], row[c].length());
    }
    final StringBuilder sb = new StringBuilder();
    line(sb, heads, widths, left);
    sb.append('|');
    for(int c = 0; c < widths.length; c++) {
      final String dashes = "-".repeat(widths[c] + 1);
      sb.append(left[c] ? ":" + dashes : dashes + ':').append('|');
    }
    sb.append(System.lineSeparator());
    for(final String[] row : rows) line(sb, row, widths, left);
    return sb.toString();
  }

  /**
   * Adds a table row.
   * @param sb string builder
   * @param cells cells
   * @param widths column widths
   * @param left left-aligned columns
   */
  private static void line(final StringBuilder sb, final String[] cells, final int[] widths,
      final boolean[] left) {
    sb.append("| ");
    for(int c = 0; c < cells.length; c++) {
      sb.append(c == 0 ? "" : " ").
        append(String.format("%" + (left[c] ? "-" : "") + widths[c] + "s |", cells[c]));
    }
    sb.append(System.lineSeparator());
  }
}
