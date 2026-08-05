package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.basex.gui.layout.*;
import org.basex.util.list.*;

/**
 * Popup window with the candidates of a code completion.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CompletionPopup {
  /** Maximum number of visible candidates. */
  private static final int ROWS = 12;
  /** Characters to show. */
  private static final int CHARS = 45;
  /** Popup delay (ms). */
  private static final int DELAY = 250;
  /** Horizontal margin of a candidate. */
  private static final int MARGIN = 4;

  /** Text panel. */
  private final TextPanel panel;
  /** Candidates. */
  private final DefaultListModel<Completion> model = new DefaultListModel<>();
  /** Displayed candidates. */
  private final JList<Completion> list = new JList<>(model);
  /** Timer for showing the popup automatically. */
  private final Timer timer;
  /** Candidates that start a new group. */
  private BoolList groups;
  /** Matched string (lower case). */
  private String input = "";
  /** Font for matched characters. */
  private Font boldFont;
  /** Popup window (can be {@code null}: popup is hidden). */
  private JWindow window;
  /** Start position of the completed string. */
  private int start;

  /**
   * Constructor.
   * @param panel text panel
   */
  CompletionPopup(final TextPanel panel) {
    this.panel = panel;
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    timer = new Timer(DELAY, e -> panel.complete(false));
    timer.setRepeats(false);

    list.setCellRenderer(new CandidateRenderer());
    list.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        final Point point = e.getPoint();
        final int index = list.locationToIndex(point);
        // the nearest candidate is returned for clicks below the last one
        if(index != -1 && list.getCellBounds(index, index).contains(point)) {
          list.setSelectedIndex(index);
          insert();
        }
      }
    });
  }

  /**
   * Indicates if the popup is visible.
   * @return result of check
   */
  boolean visible() {
    return window != null && window.isVisible();
  }

  /**
   * Returns the start position of the completed string.
   * @return position
   */
  int start() {
    return start;
  }

  /**
   * Shows the popup below the specified position.
   * @param candidates candidates
   * @param word completed string
   * @param strt start position of the completed string
   * @param point position, relative to the text panel
   */
  void show(final ArrayList<Completion> candidates, final String word, final int strt,
      final Point point) {
    hide();
    start = strt;
    // the popup must not take away the focus from the text panel
    window = new JWindow(SwingUtilities.getWindowAncestor(panel));
    window.setFocusableWindowState(false);
    window.add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
    update(candidates, word);

    final Point screen = panel.getLocationOnScreen();
    window.setLocation(screen.x + point.x, screen.y + point.y);
    window.setVisible(true);
  }

  /**
   * Assigns new candidates and adopts the size of the popup.
   * @param candidates candidates, separated by {@code null} references
   * @param word completed string
   */
  void update(final ArrayList<Completion> candidates, final String word) {
    input = word.toLowerCase(Locale.ENGLISH);
    final ArrayList<Completion> values = new ArrayList<>(candidates.size());
    groups = new BoolList(candidates.size());
    boolean group = false;
    for(final Completion candidate : candidates) {
      if(candidate == null) {
        group = true;
      } else {
        values.add(candidate);
        groups.add(group);
        group = false;
      }
    }
    model.clear();
    model.addAll(values);

    // assign a fixed cell size: the list must not measure every single candidate
    final Font f = list.getFont().deriveFont((float) popupFontSize);
    final FontMetrics fm = list.getFontMetrics(f);
    boldFont = f.deriveFont(Font.BOLD);
    list.setFont(f);
    list.setFixedCellWidth(fm.charWidth('a') * CHARS);
    list.setFixedCellHeight(fm.getHeight());
    list.setVisibleRowCount(Math.min(values.size(), ROWS));
    list.setSelectedIndex(0);
    window.pack();
  }

  /**
   * Hides the popup, discards its window and stops the timer.
   */
  void hide() {
    timer.stop();
    if(window == null) return;
    window.dispose();
    window = null;
  }

  /**
   * Restarts the timer for showing the popup automatically.
   */
  void schedule() {
    timer.restart();
  }

  /**
   * Processes a key event.
   * @param e key event
   * @return {@code true} if the event was processed
   */
  boolean key(final KeyEvent e) {
    if(ESCAPE.is(e)) {
      hide();
    } else if(ENTER.is(e) || TAB.is(e)) {
      insert();
    } else if(model.getSize() > 1) {
      // a single candidate cannot be navigated: the key is processed by the text panel
      if(NEXTLINE.is(e) && !MOVEDOWN.is(e)) move(1);
      else if(PREVLINE.is(e) && !MOVEUP.is(e)) move(-1);
      else if(NEXTPAGE.is(e)) move(ROWS);
      else if(PREVPAGE.is(e)) move(-ROWS);
      else return false;
    } else {
      return false;
    }
    return true;
  }

  /**
   * Inserts the selected candidate.
   */
  private void insert() {
    final Completion candidate = list.getSelectedValue();
    hide();
    if(candidate != null) panel.complete(candidate.value(), start);
  }

  /**
   * Moves the selection.
   * @param count number of candidates (negative: upwards)
   */
  private void move(final int count) {
    final int index = Math.max(0, Math.min(model.getSize() - 1,
      list.getSelectedIndex() + count));
    list.setSelectedIndex(index);
    list.ensureIndexIsVisible(index);
  }

  /**
   * Returns the displayed label of a candidate.
   * @param candidate candidate
   * @return label
   */
  private static String label(final Completion candidate) {
    // line breaks of multi-line candidates are indicated by a symbol
    return candidate.label().replace("\n", "⏎");
  }

  /** Renderer that highlights the matched characters of a candidate. */
  private final class CandidateRenderer extends JComponent
      implements ListCellRenderer<Completion> {
    /** Label of the rendered candidate. */
    private String label = "";
    /** Indicates if the candidate is selected. */
    private boolean selected;
    /** Indicates if the candidate starts a new group. */
    private boolean group;

    @Override
    public Component getListCellRendererComponent(final JList<? extends Completion> lst,
        final Completion value, final int index, final boolean sel, final boolean focus) {
      label = label(value);
      selected = sel;
      group = groups.get(index);
      return this;
    }

    @Override
    protected void paintComponent(final Graphics g) {
      BaseXLayout.hints(g);

      final int w = getWidth(), h = getHeight();
      g.setColor(selected ? list.getSelectionBackground() : list.getBackground());
      g.fillRect(0, 0, w, h);
      if(group) {
        g.setColor(lightGray);
        g.drawLine(0, 0, w, 0);
      }
      g.setColor(selected ? list.getSelectionForeground() : darkGray);

      final FontMetrics fm = g.getFontMetrics(boldFont);
      final int y = (h - fm.getHeight()) / 2 + fm.getAscent();

      // draw the matched characters in bold; longer labels are clipped
      final int ll = label.length(), il = input.length();
      int x = MARGIN, i = 0, s = 0;
      boolean bold = false;
      for(int l = 0; l < ll; l++) {
        final boolean match = i < il && Character.toLowerCase(label.charAt(l)) == input.charAt(i);
        if(match) i++;
        if(match != bold) {
          x = draw(g, label.substring(s, l), x, y, bold);
          s = l;
          bold = match;
        }
      }
      draw(g, label.substring(s), x, y, bold);
    }

    /**
     * Draws a string and returns the next horizontal position.
     * @param g graphics reference
     * @param string string
     * @param x horizontal position
     * @param y vertical position
     * @param bold bold flag
     * @return horizontal position
     */
    private int draw(final Graphics g, final String string, final int x, final int y,
        final boolean bold) {
      if(string.isEmpty()) return x;
      g.setFont(bold ? boldFont : list.getFont());
      g.drawString(string, x, y);
      return x + BaseXLayout.width(g, string);
    }
  }
}
