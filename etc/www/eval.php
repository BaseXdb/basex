<?
$load = 'document.form.query.focus();';
$top = 'download';
$title = 'Online Demo';
$query = stripslashes($_POST[query]);
$exec = "java -cp /var/www/BaseX.jar";
# get random query
while($_POST[rand] || !$query) {
  $queries = file('queries.xq');
  $nr = $query ? rand(0, sizeof($queries)) : 0;
  $query = chop(preg_replace("/~/", "\n", $queries[$nr]));
  if($query) break;
}
#$exec = "java -cp /home/db/projects/basex/bin";
include("inc/header.inc");
?>
<h2>Input</h2>
<form name='form' method='post' action='<? print $basex; ?>eval.php'>
<table cellspacing='0' cellpadding='0' border='0'>
<tr><td colspan='2'>
<textarea name='query' cols='72' rows='5'>
<? print $query; ?>
</textarea>
</td></tr><tr><td valign='top'>
<?
$nr = rand(64, 75);
$f = fopen("/tmp/qu$nr", "w");
fwrite($f, $query);
fclose($f);
system("$exec org.basex.BaseXClient -o/tmp/rs$nr -v /tmp/qu$nr >/tmp/lg$nr 2>/tmp/er$nr");
$err = file("/tmp/er$nr");
?>
In Database: <a href='download/xmark.xml'>xmark</a>,
<a href='download/factbook.xml'>factbook</a>
</td><td align='right' valign='top'>
<input type='submit' name='exec' value='Execute XQuery'>
<input type='submit' name='rand' value='Random Query'>
</td></tr></table>

<?
print "<h2>Output</h2>";
print "<textarea style='color:#666666' cols='72' rows='5' readonly='true'>";
if(!$err) {
  foreach (file("/tmp/rs$nr") as $e) print $e;
  unlink("/tmp/rs$nr");
}
print "</textarea>";
if($err) {
  print "<h2>Error</h2>";
  print "<code style='color:#CC3333'>";
  foreach ($err as $e) print preg_replace("/Error: /", "", $e);
  print "</code>";
} else {
  print "<h2>Performance</h2>";
  print "<pre>";
  foreach (file("/tmp/lg$nr") as $e) {
    if(strrpos($e, " ms") === strlen($e) - strlen(" ms") - 1) print $e;
  }
  print "</pre>";
}
unlink("/tmp/qu$nr");
unlink("/tmp/lg$nr");
unlink("/tmp/er$nr");
?>
</form>
<? include("inc/footer.inc"); ?>

