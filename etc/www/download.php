<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$webpage= basename($_SERVER['SCRIPT_NAME']);

if($_GET['dl']) {
	$file = $_GET['dl'];
	$url = $file == 'latest' ?
		"http://www.inf.uni-konstanz.de/dbis/basex/download/BaseX-latest.jar" :
		"http://downloads.sourceforge.net/basex/$file";
	$meta = "<meta http-equiv=\"REFRESH\" content=\"0; URL=$url\">";
	$addr = $HTTP_SERVER_VARS['REMOTE_ADDR'];
	$host = gethostbyaddr($addr);

	if(!(preg_match("/googlebot/", $host) ||
	     preg_match("/search.live/", $host) ||
	     preg_match("/crawl.yahoo/", $host) ||
	     preg_match("/inktomisearch/", $host))) {
		$fp = fopen("/www_io_mnt/gruen/bx/basexdl.log", "a");
		fwrite($fp, date('j.m.y, G:i')." Uhr [$addr, $host] $file\n");
		fclose($fp);
	}
}

include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== ... ===== -->

<div id="main">
<h1>Download</h1>
<h2>Official Version</h2>
<p>
<table cellspacing='0' cellpadding='0' border='0'>
  <tr>
    <td width='200'>
		<img src="gfx/download.gif"/> <a href="download.php?dl=BaseX4.jar">BaseX4.jar</a></td>
		<td>Runnable JAR file (1.2 MB)</td>
  </tr><tr>
    <td>
		<img src="gfx/download.gif"/> <a href="download.php?dl=BaseX4-Complete.zip">BaseX4-Complete.zip</a></td>
    <td>Including all runnables, sources and documentation (5.7 MB)</td>
  </tr>
</table>
&nbsp;
<table cellspacing='0' cellpadding='0' border='0'>
	<tr><td>Release: &nbsp;</td><td>4.0</td></tr>
	<tr><td>Date:</td><td>23 Nov 2007</td></tr>
</table>
</p>

<h2>Latest Build (4.2)</h2>
<p>
<table cellspacing='0' cellpadding='0' border='0'>
  <tr>
    <td>
		<img src="gfx/download.gif"/>
		<a href="http://www.inf.uni-konstanz.de/dbis/basex/download/BaseX-latest.jar">BaseX-latest.jar</a>
		<!--<img src="gfx/download.gif"/> <a href="download.php?dl=latest">BaseX-latest.jar</a>-->
		(13 Oct 2008, 1.4 MB)</td>
  </tr>
</table>
</p>


<h2>Older Versions</h2>
<ul>
	<li><a href="http://downloads.sourceforge.net/basex/BaseX.jar">BaseX 3.07</a> (11 Mar 2007, 572 KB)</li>
	<li><a href="http://downloads.sourceforge.net/basex/basex300.jar">BaseX 3.00</a> (31 Jan 2007, 675 KB)</li>
	<li><a href="http://downloads.sourceforge.net/basex/basex221.jar">BaseX 2.21</a> (13 Dec 2006, 489 KB)</li>
</ul>

<h2>Sample XML documents</h2>
<ul>
<li><a href="download/xmark114kb.xml">xmark114kb.xml</a> (114 KB)</li>
<li><a href="download/factbook.xml">factbook.xml</a>     (1.7 MB)</li>
<li><a href="download/xmark111mb.zip">xmark111mb.zip</a> (37 MB)</li>
</ul>

<h2>Source Code</h2>
The BaseX sources are available in a Subversion repository via <a href="http://sourceforge.net/projects/basex">Sourceforge</a>.

<!-- ===== ... ===== -->
                

<? include("inc/footer.inc"); ?>
