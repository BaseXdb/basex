package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogExport extends Dialog {
  /** Encodings. */
  private static final String[] ENCODINGS = {
    "UTF-8", "UTF-16BE", "UTF-16LE", "ISO-8859-1", "ISO-8859-2", "ISO-8859-3",
    "ISO-8859-4", "ISO-8859-5", "ISO-8859-9", "ISO-8859-7", "ISO-8859-8",
    "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "US-ASCII", "Windows-1250",
    "Windows-1251", "Windows-1252", "Windows-1253", "Windows-1257", "BIG5",
    "EUC-JP", "EUC-KR", "GB2312", "ISO-2020-JP", "Koi8-R", "Shift_JIS"
  };
  /** Directory path. */
  private final BaseXTextField path;
  /** Directory/File flag. */
  private final boolean file;
  /** Database info. */
  private final BaseXLabel info;
  /** Output label. */
  private final BaseXLabel out;
  /** XML Formatting. */
  private final BaseXCheckBox format;
  /** Encoding. */
  private final BaseXCombo encoding;
  /** Buttons. */
  private final BaseXBack buttons;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogExport(final GUI main) {
    super(main, GUIEXPORT);

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(3, 1, 0, 4));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 2, 6, 0));
    out = new BaseXLabel("", false, true);
    p.add(out);
    p.add(new BaseXLabel(""));

    final Data data = gui.context.data;
    file = data.doc().length == 1;

    IO io = gui.context.data.meta.file;
    final String fn = file ? io.path() : io.getDir();
    path = new BaseXTextField(fn, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(path, 240);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);
    pp.add(p);

    p = new BaseXBack();
    p.setLayout(new TableLayout(2, 1));
    p.add(new BaseXLabel(INFOENCODING + COL, false, true));

    final Prop prop = gui.context.prop;
    encoding = new BaseXCombo(ENCODINGS, this);
    encoding.setSelectedItem(prop.get(Prop.XMLENCODING));
    encoding.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(encoding, 240);
    p.add(encoding);
    pp.add(p);

    format = new BaseXCheckBox(INDENT, prop.is(Prop.XMLFORMAT), 0, this);
    pp.add(format);
    set(pp, BorderLayout.CENTER);
    
    // create buttons
    p = new BaseXBack();
    p.setLayout(new BorderLayout());
    info = new BaseXLabel(" ");
    info.setBorder(18, 0, 0, 0);
    p.add(info, BorderLayout.WEST);
    buttons = okCancel(this);
    p.add(buttons, BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  void choose() {
    final BaseXFileChooser fc = new BaseXFileChooser(DIALOGFC, path.getText(),
        gui);
    //if(file) fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);
    final IO io = fc.select(file ? BaseXFileChooser.Mode.FOPEN :
      BaseXFileChooser.Mode.DOPEN);
    if(io != null) path.setText(io.path());
  }

  /**
   * Returns the chosen XML file or directory path.
   * @return file or directory
   */
  public String path() {
    return path.getText().trim();
  }
  
  @Override
  public void action(final String cmd) {
    final Prop prop = gui.context.prop;
    prop.set(Prop.XMLFORMAT, format.isSelected());
    prop.set(Prop.XMLENCODING, encoding.getSelectedItem().toString());

    out.setText((file ? OUTFILE : OUTDIR) + COL);
    final IO io = IO.get(path());
    final boolean empty = path().isEmpty();
    final boolean exists = io.exists();
    ok = !empty && (file && (!exists || !io.isDir()) ||
        !file && (!exists || io.isDir()));

    String inf = ok && file && exists ? OVERFILE : !ok && !empty ?
        INVPATH : null;
    info.setError(inf, ok);
    enableOK(buttons, BUTTONOK, ok);
  }
}
