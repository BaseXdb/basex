package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.gui.text.*;
import org.basex.gui.view.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * This class offers a fast text view, using the {@link TextPanel} class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class TextView extends View {
  /** Search editor. */
  private final SearchEditor editor;

  /** Home button. */
  private final AbstractButton home;
  /** Info label for total time. */
  private final BaseXLabel label;
  /** Text Area. */
  private final TextPanel text;

  /** Cached command. */
  private Command cachedCmd;
  /** Cached nodes. */
  private DBNodes cachedNodes;

  /**
   * Default constructor.
   * @param notifier view notifier
   */
  public TextView(final ViewNotifier notifier) {
    super(TEXTVIEW, notifier);
    border(5).layout(new BorderLayout(0, 5));

    text = new TextPanel(gui, false);
    text.setSyntax(new SyntaxXML());
    editor = new SearchEditor(gui, text);
    label = new BaseXLabel(" ").resize(1.25f);

    final AbstractButton save = BaseXButton.get("c_save", SAVE, false, gui);
    save.addActionListener(e -> save());

    home = BaseXButton.command(GUIMenuCmd.C_SHOW_HOME, gui);
    home.setEnabled(false);

    final BaseXToolBar buttons = new BaseXToolBar();
    buttons.add(save);
    buttons.add(home);
    buttons.add(editor.button());

    final BaseXBack north = new BaseXBack(false).layout(new BorderLayout(10, 10));
    north.add(buttons, BorderLayout.WEST);
    north.add(label, BorderLayout.CENTER);
    north.add(new BaseXHeader(RESULT), BorderLayout.EAST);
    add(north, BorderLayout.NORTH);

    add(editor, BorderLayout.CENTER);
    refreshLayout();
  }

  /**
   * Focuses the text.
   */
  public void focusText() {
    text.requestFocusInWindow();
  }

  @Override
  public void refreshInit() {
    refreshContext(true, true);
  }

  @Override
  public void refreshFocus() {
  }

  @Override
  public void refreshMark() {
    final Context context = gui.context;
    final DBNodes nodes = context.marked;
    setText(nodes != null && nodes.isEmpty() ? context.current() : nodes);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    setText(gui.context.current());
  }

  @Override
  public void refreshLayout() {
    text.setFont(mfont);
    editor.bar().refreshLayout();
  }

  @Override
  public void refreshUpdate() {
    refreshContext(true, true);
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWTEXT);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWTEXT, v);
  }

  @Override
  protected boolean db() {
    return false;
  }

  /**
   * Serializes the specified nodes.
   * @param nodes nodes to display (can be {@code null})
   */
  private void setText(final DBNodes nodes) {
    final Context context = gui.context;
    if(visible()) {
      try {
        final ArrayOutput ao = new ArrayOutput();
        long size = 0;
        if(nodes != null) {
          ao.setLimit(gui.gopts.get(GUIOptions.MAXTEXT));
          nodes.serialize(Serializer.get(ao, context.options.get(MainOptions.SERIALIZER)));
          size = nodes.size();
        } else {
        }
        setText(ao, size, null);
        cachedNodes = ao.finished() ? nodes : null;
        cachedCmd = null;
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    } else {
      home.setEnabled(context.data() != null);
    }
  }

  /**
   * Caches the output, or indicates that the query must be executed again in order to retrieve
   * the full result.
   * @param out cached output
   * @param command command
   * @param result result (can be {@code null})
   * @throws QueryException query exception
   */
  public void cache(final ArrayOutput out, final Command command, final Value result)
      throws QueryException {

    // cache command or node set
    cachedCmd = null;
    cachedNodes = null;

    final int max = gui.gopts.get(GUIOptions.MAXRESULTS);
    boolean cacheCmd = false;
    if(result != null && result.size() >= max) {
      // result was larger than number of retrieved result items: create new command instance
      cacheCmd = true;
    } else if(out.finished()) {
      // cache is exhausted... cache node set, or create new command instance
      if(result instanceof DBNodes) cachedNodes = (DBNodes) result;
      else cacheCmd = true;
    }
    // otherwise, the displayed text and the cached result are equal

    // create new command instance
    if(cacheCmd) {
      cachedCmd = CommandParser.get(command.toString(), gui.context).parseSingle();
      cachedCmd.baseURI(command.baseURI());
    }
  }

  /**
   * Sets the output text.
   * @param out cached output
   * @param results number of results
   * @param throwable error, can be {@code null}
   */
  public void setText(final ArrayOutput out, final long results, final Throwable throwable) {
    final byte[] buffer = out.buffer();
    final int size = (int) out.size();
    final byte[] chop = token(DOTS);
    final int cl = chop.length;
    if(out.finished() && size >= cl) Array.copyFromStart(chop, cl, buffer, size - cl);
    text.setText(buffer, size);

    String info;
    if(throwable != null) {
      info = throwable.getLocalizedMessage();
    } else {
      info = BaseXLayout.results(results, size, gui);
      if(out.finished()) info += " (" + CHOPPED + ')';
    }
    label.setText(info);
    home.setEnabled(gui.context.data() != null);
  }

  /**
   * Saves the displayed text.
   */
  private void save() {
    final BaseXFileChooser fc = new BaseXFileChooser(gui,
        SAVE_AS, gui.gopts.get(GUIOptions.WORKPATH)).suffix(IO.XMLSUFFIX);

    final IOFile file = fc.select(Mode.FSAVE);
    if(file == null) return;
    gui.gopts.setFile(GUIOptions.WORKPATH, file.parent());

    gui.cursor(CURSORWAIT, true);
    try(PrintOutput out = new PrintOutput(file)) {
      final Context context = gui.context;
      if(cachedCmd != null) {
        cachedCmd.execute(context, out);
      } else if(cachedNodes != null) {
        cachedNodes.serialize(Serializer.get(out, context.options.get(MainOptions.SERIALIZER)));
      } else {
        out.write(text.getText());
      }
    } catch(final IOException ex) {
      Util.debug(ex);
      BaseXDialog.error(gui, Util.info(FILE_NOT_SAVED_X, file));
    } finally {
      gui.cursor(CURSORARROW, true);
    }
  }
}
