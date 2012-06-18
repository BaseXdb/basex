package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.view.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.util.*;

/**
 * This class offers a fast text view, using the {@link BaseXEditor} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextView extends View implements ActionListener {
  /** Header string. */
  private final BaseXLabel header;
  /** Home button. */
  private final BaseXButton home;
  /** Text Area. */
  private final BaseXEditor area;
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

    border(6, 6, 6, 6).layout(new BorderLayout(0, 4)).setFocusable(false);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());

    home = BaseXButton.command(GUICommands.C_HOME, gui);
    home.setEnabled(false);

    BaseXBack sp = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 2));
    sp.add(home);
    sp.add(Box.createHorizontalStrut(8));
    b.add(sp, BorderLayout.WEST);

    header = new BaseXLabel(TEXT, true, false);
    b.add(header, BorderLayout.CENTER);

    final BaseXButton save = new BaseXButton(gui, "save", token(H_SAVE_RESULT));
    save.addActionListener(this);
    /* Find text field. */
    final BaseXTextField find = new BaseXTextField(gui);
    BaseXLayout.setHeight(find, (int) save.getPreferredSize().getHeight());

    sp = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 3));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(save);
    b.add(sp, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    area = new BaseXEditor(false, gui);
    area.setSyntax(new XMLSyntax());
    area.setSearch(find);
    add(area, BorderLayout.CENTER);

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
    header.setFont(lfont);
    area.setFont(mfont);
  }

  @Override
  public void refreshUpdate() {
    refreshContext(true, true);
  }

  @Override
  public boolean visible() {
    return gui.gprop.is(GUIProp.SHOWTEXT);
  }

  @Override
  public void visible(final boolean v) {
    gui.gprop.set(GUIProp.SHOWTEXT, v);
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
        final ArrayOutput ao = new ArrayOutput().max(gui.gprop.num(GUIProp.MAXTEXT));
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
   * Sets the output text.
   * @param out cached output
   * @param c command
   * @param r result
   */
  public void setText(final ArrayOutput out, final Command c, final Result r) {
    setText(out);
    // cache command or node set
    cmd = null;
    ns = null;
    final int mh = gui.context.prop.num(Prop.MAXHITS);
    if(mh >= 0 && r != null && r.size() >= mh) {
      cmd = c;
    } else if(out.finished()) {
      if(r instanceof Nodes) ns = (Nodes) r;
      else cmd = c;
    }
  }

  /**
   * Sets the output text.
   * @param out cached output
   */
  private void setText(final ArrayOutput out) {
    final byte[] buf = out.buffer();
    final int size = (int) out.size();
    final byte[] chop = token(DOTS);
    if(out.finished() && size >= chop.length) {
      System.arraycopy(chop, 0, buf, size - chop.length, chop.length);
    }
    area.setText(buf, size);
    header.setText(TEXT + (out.finished() ? CHOPPED : ""));
    home.setEnabled(gui.context.data() != null);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final BaseXFileChooser fc = new BaseXFileChooser(SAVE_AS,
        gui.gprop.get(GUIProp.SAVEPATH), gui);

    final IO file = fc.select(Mode.FSAVE);
    if(file == null) return;
    gui.gprop.set(GUIProp.SAVEPATH, file.path());

    PrintOutput out = null;
    gui.cursor(CURSORWAIT, true);
    final Prop prop = gui.context.prop;
    final int mh = prop.num(Prop.MAXHITS);
    prop.set(Prop.MAXHITS, -1);
    prop.set(Prop.CACHEQUERY, false);

    try {
      out = new PrintOutput(file.toString());
      if(cmd != null) {
        cmd.execute(gui.context, out);
      } else if(ns != null) {
        ns.serialize(Serializer.get(out));
      } else {
        final byte[] txt = area.getText();
        for(final byte t : txt) if(t < 0 || t > ' ' || ws(t)) out.write(t);
      }
    } catch(final IOException ex) {
      BaseXDialog.error(gui, FILE_NOT_SAVED);
    } finally {
      if(out != null) try { out.close(); } catch(final IOException ex) { }
      prop.set(Prop.MAXHITS, mh);
      prop.set(Prop.CACHEQUERY, true);
      gui.cursor(CURSORARROW, true);
    }
  }
}
