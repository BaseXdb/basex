import module namespace hp="http://www.basex.org/" at "hp.xqm";

declare variable $file := "frontend";
declare variable $title := "Visual BaseX";
declare variable $cont :=
<div id="main">
<h1>Visual BaseX</h1>

<h2>Screenshots</h2>

<table cellspacing="0" cellpadding="0"><tr>
<td><a href="gfx/basex-mac.png"><img border="0" src="gfx/basex-mac0.png"/></a></td>
<td width='16'></td>
<td><a href="gfx/basex-win.jpg"><img border="0" src="gfx/basex-win0.png"/></a></td>
</tr>
</table>
...find more screenshots at <a href="http://sourceforge.net/project/screenshots.php?group_id=192179">Sourceforge</a>.

<p>
<h2>Screencasts</h2>
<ul>
<li><a href='gfx/XQuery.avi'>Evaluating XQuery</a> (800 x 600, 650 KB, avi video)</li>
<li><a href='gfx/Updates.avi'>Performing Updates</a> (800 x 600, 2.4 MB, avi video)</li>
<li><a href='gfx/Browsing.avi'>Using the TreeMap</a> (800 x 600, 3.3 MB, avi video)</li>
<li><a href='gfx/Filesystem.avi'>Browsing the Filesystem (1)</a> (800 x 600, 5.8 MB, avi video)</li>
<li><a href='gfx/Media.avi'>Browsing the Filesystem (2)</a> (1024 x 768, 2.3 MB, avi video)</li>
</ul>
</p>

<h2>Illustrated step-by-step guides</h2>

<ul>
  <li><a href="{ hp:link("xml") }">How to create a new XML database</a></li>
  <li><a href="{ hp:link("fs") }">How to construct an XML representation
  of your filesystem</a></li>
</ul>
</div>
;

hp:print($title, $file, $cont)
