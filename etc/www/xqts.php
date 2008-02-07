<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$webpage= "index.php";
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<div id='main'>
  <h1>XQuery &ndash; Test Suite Results</h1>
	<ul>
	<li><a href="xqts/XQTSReportSimple.html">XQTS Report (html, 100KB)</a></li>
	<li><a href="xqts/XQTSReport.html">XQTS Report in detail (html, 9MB)</a></li>
	<li><a href="xqts/XQTS102-Results.zip">Serialized Results (zip, 4MB)</a></li>
	<li><a href="xqts/XQTS102-BaseX.xml">Submitted File (xml, 846KB)</a></li>
	</ul>
	<p>All queries were processed with BaseX 4.0 (2007-11-23).<br/>
	Version 1.0.2 of XQTS was used in this test.</p>
</div>

<? include("inc/footer.inc"); ?>
