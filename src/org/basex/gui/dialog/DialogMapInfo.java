package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.ViewRect;

/**
 * Dialog window for additional TreeMap information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogMapInfo extends Dialog {
  /** Format for decimal numbers. */
  DecimalFormat f = new DecimalFormat("#0.00");
  /** New Dimensions. */
  private final BaseXLabel ndim = new BaseXLabel(" ");
  /** Old Dimensions. */
  private final BaseXLabel odim = new BaseXLabel(" ");
  /** New Number of Nodes. */
  private final BaseXLabel nsize = new BaseXLabel(" ");
  /** Old Number of Nodes. */
  private final BaseXLabel osize = new BaseXLabel(" ");
  /** New Number of Nodes. */
  private final BaseXLabel naar = new BaseXLabel(" ");
  /** Old Aspect ratio. */
  private final BaseXLabel oaar = new BaseXLabel(" ");
  /** Execution time. */
  private final BaseXLabel ntime = new BaseXLabel(" ");
  /** Execution time. */
  private final BaseXLabel otime = new BaseXLabel(" ");
  /** New Algorithm Name. */
  private final BaseXLabel nname = new BaseXLabel(" ");
  /** Old Algorithm Name. */
  private final BaseXLabel oname = new BaseXLabel(" ");
  /** New nodes per milli second. */
  private final BaseXLabel nnps = new BaseXLabel(" ");
  /** Old nodes per milli second. */
  private final BaseXLabel onps = new BaseXLabel(" ");

  /** Last time. */
  private String timeo = "";
  /** Last number of nodes. */
  private int nno;
  /** Last aspect ratio. */
  private double aaro;
  /** Last rectangle. */
  private ViewRect recto;
  /** Last algorithm. */
  private String nameo = "";

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapInfo(final GUI main) {
    super(main, "Map Information", false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(8, 3, 5, 0));
    p.add(new BaseXLabel(""));
    p.add(new BaseXLabel("New Map          ", true, true));
    p.add(new BaseXLabel("Old Map          ", true, true));
    p.add(new BaseXLabel("Algorithm", true, true));
    p.add(nname);
    p.add(oname);
    p.add(new BaseXLabel("Size: ", true, true));
    p.add(ndim);
    p.add(odim);
    p.add(new BaseXLabel("Nodes painted: ", true, true));
    p.add(nsize);
    p.add(osize);
    p.add(new BaseXLabel("Average aspect ratio: ", true, true));
    p.add(naar);
    p.add(oaar);
    p.add(new BaseXLabel("Execution time: ", true, true));
    p.add(ntime);
    p.add(otime);
    p.add(new BaseXLabel("Nodes per ms: ", true, true));
    p.add(nnps);
    p.add(onps);
    p.add(new BaseXLabel("  ", true, true));

    //p.add(new BaseXLabel("Distance change: ", true, true));
    //p.add(dist);
    //p.add(new BaseXLabel(""));

    set(p, BorderLayout.NORTH);
    setResizable(true);
    finish(null);
    nameo = "";
    timeo = "";
  }

  /**
   * Sets the table values.
   * @param nn number of nodes
   * @param rect main rectangle
   * @param aar average aspect ratio
   * @param time execution time
   * @param name of used layoutalgorithm
   */
  public void setValues(final int nn, final ViewRect rect, final double aar,
      final String time, final String name) {

    final int nw = rect != null ? rect.w : 0;
    final int nh = rect != null ? rect.h : 0;
    final int ow = recto != null ? recto.w : 0;
    final int oh = recto != null ? recto.h : 0;
    ndim.setText(nw + " x " + nh);
    odim.setText(ow + " x " + oh);
    nname.setText(name);
    oname.setText(nameo);
    nsize.setText(Integer.toString(nn));
    osize.setText(Integer.toString(nno));
    if(GUIProp.perfinfo) {
      naar.setText("not available");
      oaar.setText("not available");
    } else {
      naar.setText(f.format(aar));
      oaar.setText(f.format(aaro));
    }
    ntime.setText(time);
    otime.setText(timeo);

    final double nps = nn / Double.valueOf(time.replace(" ms", "").
        replace(" (avg)", ""));
    onps.setText(nnps.getText());
    nnps.setText(f.format(nps));

    timeo = time;
    recto = rect;
    aaro = aar;
    nno = nn;
    nameo = name;
  }
}
