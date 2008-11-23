<?
$links = array("documentation", "commands", "faq", "sources", "links");
$title = $titles[$top ? $top : $id];
$titles = array(
  "documentation" => "Quick Start",
  "commands" => "Commands",
  "faq" => "FAQ",
  "sources" => "Code Examples",
  "links" => "Links"
);

if($sub) $id = $sub;
print "<h1>$title &ndash; ".$titles[$id]."</h1>\n";

$c = 0;
foreach($links as $l) {
  if($c++) print " | ";
  print "<a href='$l.php'>";
  if($id == $l) print "<strong>";
  print $titles[$l];
  if($id == $l) print "</strong>";
  print "</a>\n";
}

print "<br/>&nbsp;<br/>";

function code($desc, $code) {
  $path = "code/$code.java";
  print "<h2>$desc (<a href='$path'>download</a>)</h2>\n";
  print "<pre>";
  $kw1 = array("package", "import", "public", "final", "class",
      "static", "void", "throws", "new", "protected", "private",
			"try", "catch", "finally");
  $kw2 = array();

  $com1 = false;
  $com2 = false;
  foreach(file($path) as $line) {
    $line = chop($line);
    foreach(split(" ", $line) as $tok) {
      if($com1) {
        print $tok;
      } else if($com2) {
        print $tok;
        $com2 = $tok != '*/';
        if(!$com2) print "</span>";
      } else {
        $com1 = $tok == '//';
        $com2 = $tok == '/**' || $tok == '/*';
        if($com1 || $com2) {
          print "<span class='comment'>";
          print $tok;
        } else {
          $f = in_array($tok, $kw1);
          if($f) {
            print "<span class='orange'>";
          } else {
            $f = in_array($tok, $kw2);
            if($f) print "<span class='red'>";
          }
          print $tok;
          if($f) print "</span>";
        }
      }
      print " ";
    }
    if($com1) print "</span>";
    $com1 = false;
    print "\n";
  }
  print "</pre>";
}
?>
