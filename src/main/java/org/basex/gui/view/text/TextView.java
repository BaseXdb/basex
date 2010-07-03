package org.basex.gui.view.text;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;

import org.basex.core.BaseXException;
import org.basex.core.Main;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.dialog.Dialog;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * This class offers a fast text view, using the {@link BaseXText} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  private final BaseXText area;
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

    setLayout(new BorderLayout(0, 4));
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(TEXTTIT, true, false);

    final BaseXBack b = new BaseXBack(Fill.NONE);
    b.setLayout(new BorderLayout());
    b.add(header, BorderLayout.CENTER);

    final BaseXButton save = new BaseXButton(gui, "save", HELPSAVE);
    home = BaseXButton.command(GUICommands.HOME, gui);
    home.setEnabled(false);
    save.addActionListener(this);
    find = new BaseXTextField(gui);
    BaseXLayout.setHeight(find, (int) save.getPreferredSize().getHeight());

    final BaseXBack sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(find);
    sp.add(Box.createHorizontalStrut(5));
    sp.add(save);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(home);
    b.add(sp, BorderLayout.EAST);
    add(b, BorderLayout.NORTH);

    area = new BaseXText(false, gui);
    area.setSyntax(new XMLSyntax());
    area.addSearch(find);
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
    setText(gui.context.current);
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
    return gui.prop.is(GUIProp.SHOWTEXT);
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
      final CachedOutput co = new CachedOutput(
          gui.context.prop.num(Prop.MAXTEXT));
      if(n != null) n.serialize(new XMLSerializer(co));
      setText(co, null);
      refresh = false;
    } catch(final IOException ex) {
      Main.debug(ex);
    }
  }

  /**
   * Sets the output text.
   * @param co cached output
   * @param c command
   */
  public void setText(final CachedOutput co, final Command c) {
    area.setText(co.buffer(), co.size());
    header.setText(TEXTTIT + (co.finished() ? RESULTCHOP : ""));
    home.setEnabled(gui.context.data != null);
    refresh = true;
    if(!co.finished()) {
      cmd = null;
      ns = null;
    } else {
      cmd = c;
    }
  }

  public void actionPerformed(final ActionEvent e) {
    final BaseXFileChooser fc = new BaseXFileChooser(GUISAVEAS,
        gui.prop.get(GUIProp.SAVEPATH), gui);
    final IO file = fc.select(BaseXFileChooser.Mode.FSAVE);
    if(file == null) return;
    gui.prop.set(GUIProp.SAVEPATH, file.path());

    PrintOutput out = null;
    try {
      out = new PrintOutput(file.toString());
      if(cmd != null) {
        cmd.execute(gui.context, out);
      } else if(ns != null) {
        ns.serialize(new XMLSerializer(out));
      } else {
        final byte[] txt = area.getText();
        for(final byte t : txt) if(t < 0 || t > ' ' || Token.ws(t))
          out.write(t);
      }
    } catch(final BaseXException ex) {
      Dialog.error(gui, NOTSAVED);
    } catch(final IOException ex) {
      Dialog.error(gui, NOTSAVED);
    } finally {
      try { if(out != null) out.close(); } catch(final IOException ex) { }
    }
  }
}
