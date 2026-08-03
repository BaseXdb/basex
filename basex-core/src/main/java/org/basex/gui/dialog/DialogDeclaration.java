package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.*;
import org.basex.util.list.*;

/**
 * Dialog window for jumping to a declaration of the current file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DialogDeclaration extends BaseXDialog {
  /** Distance from the bottom right corner of the main window. */
  private static final int MARGIN = 20;

  /** Dialog. */
  private static DialogDeclaration dialog;

  /** Declarations that match the current filter. */
  private final ArrayList<Declaration> filtered = new ArrayList<>();
  /** Filter input. */
  private final BaseXTextField filter;
  /** Filtered declarations. */
  private final JList<String> list = new JList<>();

  /** All declarations. */
  private List<Declaration> declarations;
  /** Consumer for jumping to a declaration. */
  private IntConsumer jump;
  /** Indicates if a new selection is jumped to. */
  private boolean jumping;
  /** Last filter input. */
  private String last;

  /**
   * Default constructor.
   * @param gui reference to the main window
   * @param declarations declarations of the current text
   * @param pos current caret position
   * @param jump consumer for jumping to a declaration
   */
  private DialogDeclaration(final GUI gui, final List<Declaration> declarations, final int pos,
      final IntConsumer jump) {
    super(gui, DECLARATIONS);

    filter = new BaseXTextField(this).hint(Text.FIND + ELLIPSIS);
    filter.addKeyListener((KeyPressedListener) e -> {
      // cursor keys scroll through the list instead of moving the caret
      final int index = list.getSelectedIndex(), page = Math.max(1, list.getVisibleRowCount());
      final int next;
      if(NEXTLINE.is(e)) next = index + 1;
      else if(PREVLINE.is(e)) next = index - 1;
      else if(NEXTPAGE.is(e)) next = index + page;
      else if(PREVPAGE.is(e)) next = index - page;
      else return;
      select(Math.max(0, Math.min(filtered.size() - 1, next)));
      e.consume();
    });

    list.setFocusable(false);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener((MouseClickedListener) e -> {
      final int index = list.locationToIndex(e.getPoint());
      if(index == -1) return;
      list.setSelectedIndex(index);
      if(e.getClickCount() == 2) close();
    });
    list.addListSelectionListener(e -> {
      final Declaration declaration = declaration();
      if(jumping && declaration != null) this.jump.accept(declaration.pos());
    });
    list.setFont(list.getFont().deriveFont((float) GUIConstants.dmfont.getSize() + 2));
    filter.setFont(list.getFont());

    final JScrollPane scroll = new JScrollPane(list);
    final int width = gui.getWidth() / 5;
    BaseXLayout.setWidth(filter, width);
    BaseXLayout.setWidth(scroll, width);
    BaseXLayout.setHeight(scroll, list.getFont().getSize() * 20);

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(filter, BorderLayout.NORTH);
    p.add(scroll, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    init(declarations, pos, jump);
    setResizable(true);
    finish();
  }

  /**
   * Activates the dialog window.
   * @param gui reference to the main window
   * @param declarations declarations of the current text
   * @param pos current caret position
   * @param jump consumer for jumping to a declaration
   */
  public static void show(final GUI gui, final List<Declaration> declarations, final int pos,
      final IntConsumer jump) {
    if(dialog == null) {
      dialog = new DialogDeclaration(gui, declarations, pos, jump);
    } else {
      dialog.init(declarations, pos, jump);
      dialog.setVisible(true);
    }
    // release the navigated editor
    dialog.init(List.of(), 0, null);
  }

  @Override
  protected void place() {
    setLocation(gui.getX() + gui.getWidth() - getWidth() - MARGIN,
        gui.getY() + gui.getHeight() - getHeight() - MARGIN);
  }

  @Override
  public void close() {
    // the selection does not change if the list has a single entry: jump explicitly
    final Declaration declaration = declaration();
    if(jumping && declaration != null) jump.accept(declaration.pos());
    super.close();
  }

  @Override
  public void action(final Object source) {
    final String text = filter.getText();
    if(!text.equals(last)) {
      last = text;
      refresh();
      select(0);
    }
  }

  /**
   * Initializes the dialog with the declarations of the current text.
   * @param decls declarations
   * @param pos current caret position
   * @param consumer consumer for jumping to a declaration
   */
  private void init(final List<Declaration> decls, final int pos, final IntConsumer consumer) {
    // the preselected declaration is not jumped to: the caret is already there
    jumping = false;
    declarations = decls;
    jump = consumer;
    last = "";
    filter.setText("");
    refresh();

    // preselect the declaration that encloses the caret
    int index = 0;
    final int ds = decls.size();
    for(int d = 0; d < ds; d++) {
      if(decls.get(d).pos() <= pos) index = d;
    }
    select(index);
    jumping = consumer != null;
  }

  /**
   * Returns the selected declaration.
   * @return declaration (can be {@code null})
   */
  private Declaration declaration() {
    final int index = list.getSelectedIndex();
    return index >= 0 && index < filtered.size() ? filtered.get(index) : null;
  }

  /**
   * Refreshes the list of declarations.
   */
  private void refresh() {
    final String text = last.toLowerCase(Locale.ENGLISH);
    final StringList names = new StringList();
    filtered.clear();
    for(final Declaration declaration : declarations) {
      final String name = declaration.name();
      if(name.toLowerCase(Locale.ENGLISH).contains(text)) {
        filtered.add(declaration);
        names.add(name);
      }
    }
    list.setListData(names.finish());
  }

  /**
   * Selects the specified list entry.
   * @param index entry index
   */
  private void select(final int index) {
    if(index >= 0 && index < filtered.size()) {
      list.setSelectedIndex(index);
      list.ensureIndexIsVisible(index);
    }
  }
}
