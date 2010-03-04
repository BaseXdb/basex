use BaseX;

$session = new BaseX("localhost", 1984, "admin", "admin");
$session->execute("set info on");
$session->execute("xquery 1 + 2");
print $session->result();
print $session->info();
$session->close();