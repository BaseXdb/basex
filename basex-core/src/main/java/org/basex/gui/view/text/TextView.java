package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.view.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class offers a fast text view, using the {@link Editor} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextView extends View {
  /** Search editor. */
  final SearchEditor search;

  /** Header string. */
  private final BaseXLabel label;
  /** Home button. */
  private final BaseXButton home;
  /** Text Area. */
  private final Editor text;

  /** Result command. */
  private Command cmd;
  /** Result nodes. */
  private Nodes ns;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TextView(final ViewNotifier man) {
    super(TEXTVIEW, man);
    border(5).layout(new BorderLayout(0, 5));

    label = new BaseXLabel(RESULT, true, false);
    label.setForeground(GUIConstants.GRAY);

    home = BaseXButton.command(GUICommands.C_HOME, gui);
    home.setEnabled(false);

    final BaseXButton save = new BaseXButton(gui, "save", H_SAVE_RESULT);
    final BaseXButton srch = new BaseXButton(gui, "search",
        BaseXLayout.addShortcut(SEARCH, BaseXKeys.FIND.toString()));

    final BaseXBack buttons = new BaseXBack(Fill.NONE);
    buttons.layout(new TableLayout(1, 3, 1, 0)).border(0, 0, 4, 0);
    buttons.add(save);
    buttons.add(home);
    buttons.add(srch);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());
    b.add(buttons, BorderLayout.WEST);
    b.add(label, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    text = new Editor(false, gui);
    text.setSyntax(new SyntaxXML());
    search = new SearchEditor(gui, text).button(srch);
    add(search, BorderLayout.CENTER);

    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        save();
      }
    });
    refreshLayout();
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
    setText(gui.context.marked);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    setText(gui.context.current());
  }

  @Override
  public void refreshLayout() {
    label.border(-6, 0, 0, 2).setFont(GUIConstants.lfont);
    text.setFont(mfont);
    search.bar().refreshLayout();
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
   * @param n nodes to display
   */
  private void setText(final Nodes n) {
    if(visible()) {
      try {
        final ArrayOutput ao = new ArrayOutput().max(gui.gopts.get(GUIOptions.MAXTEXT));
        if(n != null) n.serialize(Serializer.get(ao));
        setText(ao);
        cmd = null;
        ns = ao.finished() ? n : null;
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    } else {
      home.setEnabled(gui.context.data() != null);
    }
  }

  /**
   * Caches the output.
   * @param out cached output
   * @param c command
   * @param r result
   * @throws QueryException query exception
   */
  public void cacheText(final ArrayOutput out, final Command c, final Result r)
      throws QueryException {

    // cache command or node set
    cmd = null;
    ns = null;

    final int mh = gui.context.options.get(MainOptions.MAXHITS);
    boolean parse = false;
    if(mh >= 0 && r != null && r.size() >= mh) {
      parse = true;
    } else if(out.finished()) {
      if(r instanceof Nodes) ns = (Nodes) r;
      else parse = true;
    }
    // create new command instance
    if(parse) cmd = new CommandParser(c.toString(), gui.context).parseSingle();
  }

  /**
   * Sets the output text.
   * @param out cached output
   */
  public void setText(final ArrayOutput out) {
    final byte[] buf = out.buffer();
    final int size = (int) out.size();
    final byte[] chop = token(DOTS);
    if(out.finished() && size >= chop.length) {
      System.arraycopy(chop, 0, buf, size - chop.length, chop.length);
    }
    text.setText(buf, size);
    label.setText((out.finished() ? CHOPPED : "") + RESULT);
    home.setEnabled(gui.context.data() != null);
  }

  /**
   * Saves the displayed text.
   */
  void save() {
    final BaseXFileChooser fc = new BaseXFileChooser(SAVE_AS,
        gui.gopts.get(GUIOptions.WORKPATH), gui).suffix(IO.XMLSUFFIX);

    final IO file = fc.select(Mode.FSAVE);
    if(file == null) return;
    gui.gopts.set(GUIOptions.WORKPATH, file.path());

    PrintOutput out = null;
    gui.cursor(CURSORWAIT, true);
    final MainOptions opts = gui.context.options;
    final int mh = opts.get(MainOptions.MAXHITS);
    opts.set(MainOptions.MAXHITS, -1);
    opts.set(MainOptions.CACHEQUERY, false);

    try {
      out = new PrintOutput(file.toString());
      if(cmd != null) {
        cmd.execute(gui.context, out);
      } else if(ns != null) {
        ns.serialize(Serializer.get(out));
      } else {
        final byte[] txt = text.getText();
        for(final byte t : txt) if(t < 0 || t > ' ' || ws(t)) out.write(t);
      }
    } catch(final IOException ex) {
      BaseXDialog.error(gui, FILE_NOT_SAVED);
    } finally {
      if(out != null) try { out.close(); } catch(final IOException ignored) { }
      opts.set(MainOptions.MAXHITS, mh);
      opts.set(MainOptions.CACHEQUERY, true);
      gui.cursor(CURSORARROW, true);
    }
  }
}
