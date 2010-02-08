<?php
include("ClientSession.php");
if (connect("localhost", 1920) == True) {
	//execute("SET INFO ON");
	echo execute("help");
	//execute("exit");
};
?>