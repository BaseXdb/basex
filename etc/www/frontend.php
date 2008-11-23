<?
include("inc/header.inc");

function fileLink($name, $ext) {
	$link = "media/$name.$ext";
	print "<a href='$link'>$ext, ";
	$sz = filesize($link);
	if($sz / 1048576 > 1) $sz = ((int) ($sz / 104857) / 10)." MB";
	else $sz = (int) ($sz / 1024)." KB";
	print "$sz</a>\n";
}

function mediaLink($desc, $name) {
  $dsc = urlencode($desc);
  print "<li>$desc: ";
  print "<a href='stream.php?desc=$dsc&name=$name'>stream</a> | ";
  print fileLink($name, 'mp4')." | ";
  print fileLink($name, 'avi')."</li>";
}

function imageLink($desc, $name) {
  $dsc = urlencode($desc);
  print "<a href='image.php?desc=$dsc&name=$name'>";
	print "<img border='0' src='gfx/".$name."0.png'/></a>\n";
}
?>

<h2>Images</h2>
<?
  imageLink("TreeMap & Scatterplot", "basex-scatter");
	print "&nbsp; &nbsp; ";
  imageLink("Browsing the Filesystem", "basex-win");
	print "&nbsp; &nbsp; ";
  imageLink("Browsing XML", "basex-mac");
?><br/>
...find more screenshots at <a href="http://sourceforge.net/project/screenshots.php?group_id=192179">Sourceforge</a>.<br/>
&nbsp;<br/>

<h2>Videos</h2>
<ul>
<?
  mediaLink("Evaluating XQuery", "XQuery");
  mediaLink("Performing Updates", "Updates");
  mediaLink("Using the TreeMap", "Browsing");
  mediaLink("Browsing the Filesystem", "Filesystem");
  mediaLink("Using the Scatterplot", "Scatterplot");
?>
</ul>

<? include("inc/footer.inc"); ?>
