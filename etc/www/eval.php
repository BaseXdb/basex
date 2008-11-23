<?
print '<?xml version="1.0" encoding="UTF8" ?>';
$load = 'document.form.query.focus();';
$top = 'download';
$title = 'Live Demo';
$query = stripslashes($_POST[query]);
include("inc/header.inc");
?>

<h2>Input</h2>
<form name='form' method='post' action='http://phobos101.inf.uni-konstanz.de/basex/eval.php'>
<table cellspacing='0' cellpadding='0' border='0'>
<tr><td colspan='2'>
<textarea name='query' cols='70' rows='6'>
<?
if($query) {
	print $query;
} else {
	print "<calc>{\n";
	print "  for \$c in (1, 3, 5, 7, 9)\n";
	print "  return <square num=\"{ \$c }\">{ \$c * \$c}</square>\n";
	print "}</calc>";
}
?>
</textarea>
</td></tr><tr><td valign='top'>
<? if($query) print "<h2>Result</h2>"; ?>
</td><td align='right' valign='top'>
<input type='submit' value='Execute XQuery'>
</td></tr></table>

<?
if($query) {
  print "<textarea cols='70' rows='8' readonly='true'>";
  $nr = rand(64, 75);

	$f = fopen("/tmp/query$nr.xq", "w");
  fwrite($f, $query);
  fclose($f);

	system("java -cp /var/www/BaseX.jar org.basex.BaseXClient -o/tmp/res$nr -v /tmp/query$nr.xq >/tmp/log$nr 2>/tmp/err$nr");
	$err = file("/tmp/err$nr");
	foreach ($err as $e) {
		print $e;
	}
	if(!$err) {
  	foreach (file("/tmp/res$nr") as $e) print $e;
	}
  print "</textarea>";
	if(!$err) {
		print "<h2>Performance</h2>";
		print "<pre>";
  	foreach (file("/tmp/log$nr") as $e) {
			if(strrpos($e, " ms") === strlen($e) - strlen(" ms") - 1) print $e;
		}
		print "</pre>";
	}
}
?>

</form>
<? include("inc/footer.inc"); ?>

