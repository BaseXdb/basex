<?
function code($desc, $code) {
  $path = "code/$code.java";
  print "<h2>$desc (<a href='$path'>download</a>)</h2>\n";
  print "<pre>";
  $keywords = array("package", "import", "public", "final", "class",
      "static", "void", "throws", "new");

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
          $f = in_array($tok, $keywords);
          if($f) print "<span class='orange'>";
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

