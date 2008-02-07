import module namespace hp="http://www.basex.org/" at "hp.xqm";

declare variable $dl external;
declare variable $sf := "http://downloads.sourceforge.net/basex/";
declare variable $file := "download";
declare variable $title := "Download";
declare variable $cont :=
  <div id="main">
  <h1>Download</h1>
  <h2>Current Version</h2>
  <p>
  <table cellspacing='0' cellpadding='0' border='0'>
    <tr>
      <td width='200'>
      <img src="gfx/download.gif"/> <a href="{ $sf }BaseX4.jar">BaseX4.jar</a></td>
      <td>Runnable JAR file (1.2 MB)</td>
    </tr><tr>
      <td>
      <img src="gfx/download.gif"/> <a href="{ $sf }BaseX4-Complete.zip">BaseX4-Complete.zip</a></td>
      <td>Including all runnables, sources and documentation (5.7 MB)</td>
    </tr>
  </table>

  <table cellspacing='0' cellpadding='0' border='0'>
    <tr><td>Release:</td><td width='12'> </td><td>4.0</td></tr>
    <tr><td>Date:</td><td> </td><td>23 Nov 2007</td></tr>
  </table>
  </p>

  <h2>Latest Version (unstable)</h2>
  <p>
  <table cellspacing='0' cellpadding='0' border='0'>
    <tr>
      <td width='200'>
      <img src="gfx/download.gif"/> <a href="download/BaseX-latest.jar">BaseX-latest.jar</a></td>
    </tr>
  </table>
  </p>

  <h2>Older Versions</h2>
  <ul>{
    let $files := (
      "BaseX.jar", "BaseX 3.07", "11 Mar 2007", "572 KB",
      "basex300.jar", "BaseX 3.00", "31 Jan 2007", "675 KB",
      "basex221.jar", "BaseX 2.21", "13 Dec 2006", "489 KB"
    )
    for $i in 1 to count($files) idiv 4
    let $j := $i * 4 - 3
    return <li><a href="{ $sf }{ $files[$j] }">{
      $files[$j + 1] }</a> ({ $files[$j + 2] }, { $files[$j + 3] })</li>
  }</ul>

  <h2>Sample XML documents</h2>
  <ul>{
    let $files := (
      "xmark114kb.xml", "114 KB",
      "factbook.xml", "1.7 MB",
      "xmark111mb.zip", "37 MB"
    )
    for $i in 1 to count($files) idiv 2
    let $j := $i * 2 - 1
    return <li><a href="downloadss/{ $files[$j] }">{
      $files[$j] }</a> ({ $files[$j + 1] })</li>
  }</ul>

  <h2>Source Code</h2>
  The BaseX sources are available in a Subversion repository via
  <a href="http://sourceforge.net/projects/basex">Sourceforge</a>.
  </div>;

declare variable $down := basex:eval("$dl");
hp:print(if($down) then concat($title, " ", $down) else $title, $file, $cont)
