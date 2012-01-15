package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;

import org.basex.core.Command;
import org.basex.data.Nodes;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.layout.XMLSyntax;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.IO;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class offers a fast text view, using the {@link BaseXEditor} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextView extends View implements ActionListener {
  /** Find text field. */
  private final BaseXTextField find;
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
  /** Refresh flag. */
  private boolean refresh;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TextView(final ViewNotifier man) {
    super(TEXTVIEW, HELPTEXT, man);

    border(6, 6, 6, 6).layout(new BorderLayout(0, 4)).setFocusable(false);

    final BaseXBack b = new BaseXBack(Fill.NONE).layout(new BorderLayout());

    home = BaseXButton.command(GUICommands.HOME, gui);
    home.setEnabled(false);

    BaseXBack sp = new BaseXBack(Fill.NONE).layout(new TableLayout(1, 2));
    sp.add(home);
    sp.add(Box.createHorizontalStrut(8));
    b.add(sp, BorderLayout.WEST);

    header = new BaseXLabel(TEXTTIT, true, false);
    b.add(header, BorderLayout.CENTER);

    final BaseXButton save = new BaseXButton(gui, "save", HELPSAVE);
    save.addActionListener(this);
    find = new BaseXTextField(gui);
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
    if(refresh) refresh = false;
    else setText(gui.context.marked);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    setText(gui.context.current());
  }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    area.setFont(GUIConstants.mfont);
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
    ns = n;
    if(!visible()) return;
    try {
      final ArrayOutput ao =
        new ArrayOutput().max(gui.gprop.num(GUIProp.MAXTEXT));
      if(n != null) n.serialize(Serializer.get(ao));
      setText(ao, null);
      refresh = false;
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  /**
   * Sets the output text.
   * @param out cached output
   * @param c command
   */
  public void setText(final ArrayOutput out, final Command c) {
    final byte[] buf = out.buffer();
    final int size = (int) out.size();
    final byte[] chop = Token.token(DOTS);
    if(out.finished() && size >= chop.length) {
      System.arraycopy(chop, 0, buf, size - chop.length, chop.length);
    }
    area.setText(buf, size);
    header.setText(TEXTTIT + (out.finished() ? RESULTCHOP : ""));
    home.setEnabled(gui.context.data() != null);
    refresh = true;
    if(!out.finished()) {
      cmd = null;
      ns = null;
    } else {
      cmd = c;
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
        gui.gprop.get(GUIProp.SAVEPATH), gui);
    final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
    if(file == null) return;
    gui.gprop.set(GUIProp.SAVEPATH, file.path());

    PrintOutput out = null;
    try {
      out = new PrintOutput(file.toString());
      if(cmd != null) {
        cmd.execute(gui.context, out);
      } else if(ns != null) {
        ns.serialize(Serializer.get(out));
      } else {
        final byte[] txt = area.getText();
        for(final byte t : txt) if(t < 0 || t > ' ' || Token.ws(t))
          out.write(t);
      }
    } catch(final IOException ex) {
      Dialog.error(gui, NOTSAVED);
    } finally {
      if(out != null) try { out.close(); } catch(final IOException ex) { }
    }
  }
}
