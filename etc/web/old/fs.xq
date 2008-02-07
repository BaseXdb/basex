import module namespace hp="http://www.basex.org/" at "hp.xqm"; 

declare variable $file := "frontend";
declare variable $title := "Visual BaseX";
declare variable $cont := 
  <div id="main">
  <h1>Visual BaseX: Import the filesystem</h1>
  <p>
  <ol>
  <li>Choose "File -&gt; Import Filesystem...". 
  <p><img src="gfx/Doc_ImportFS.gif"/></p></li>
  <li>Now you should see this screen...<p>
  <img src="gfx/Doc_FinishFS.png"/></p></li>
  </ol>
  </p>
  </div>
;

hp:print($title, $file, $cont)
