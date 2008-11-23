<?
$top ="documentation";
include("inc/header.inc");

function codeLink($desc, $code) {
	$dsc = urlencode($desc);
	print "<li><a href='code.php?desc=$dsc&code=$code'>$desc</a></li>\n";
}
?>

<h2>Examples</h2>
<p>
Here you find some Java Examples which might help you to integrate BaseX in your own project:
<h3>General BaseX Examples</h3>
<ul><?
  codeLink("Creating a Database", "DBExample");
  codeLink("Executing XPath", "XPathExample");
  codeLink("Executing XQuery", "XQueryExample");
  codeLink("Inserting an XML Document", "InsertExample");
?></ul>
<h3>XML:DB API Examples</h3>
<ul><?
  codeLink("Executing XPath", "XMLDBExample1");
  codeLink("Inserting an XML Document", "XMLDBExample2");
?></ul>
<h3> XQuery API Examples</h3>
<ul><?
  codeLink("Executing XQuery", "XQueryAPIExample");
?></ul>
</p>

<h2>JavaDocs</h2>
<p>
Here you find the <a href="javadoc">JavaDocs</a> of the source code.
</p>

<? include("inc/footer.inc"); ?>
