import module namespace hp="http://www.basex.org/" at "hp.xqm";

declare variable $file := "frontend";
declare variable $title := "Visual BaseX";
declare variable $cont :=
  <div id="main">
  <h1>Visual BaseX: Create a database instance</h1>
  <p>
  <h3>Use BaseX with an existing XML file</h3>
  <ol>
  <li>The easiest way for users to start experimenting with BaseX is to use an
  existing XML file. Just click on "File -> New...".
  <p><img src="gfx/Doc_New.gif"/></p></li>

  <li>Choose one of your XML documents on disk. In this example we used the
  111MB.xml file which you find in our <a href="{ hp:link("download") }">download section</a>.
  <p><img src="gfx/Doc_FS.png"/></p></li>
  <li>After you choose the XML file you can set some Options. You can use indexes
  to speed up your queries. If you got some parsing problems try it again but
  disable the option "Parsing entities".
  <p><img src="gfx/Doc_Option.png"/></p></li>
  <li>Now you should see this screen. If you need more information about how to
  use BaseX, have a look into the <a href="{ hp:link("documentation") }">Documentation</a>
  or use the help in the lower left of the BaseX window.
  <p><img src="gfx/Doc_Finish.png"/></p></li>
  </ol>
  </p>
  </div>
;

hp:print($title, $file, $cont)
