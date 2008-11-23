<?
$top ="contact";
include("inc/header.inc");
?>

<form action="form.php" method="post">
<?php
         session_start();
         $mailto = 'info@basex.org';
         $subject = 'Message from www.BaseX.org';
         $_SESSION['message'] = $_POST['message'];
         $_SESSION['from'] = $_POST['from'];
         $message = $_SESSION['message'];
         $from = $_SESSION['from'];
         $sent = $_POST['sent'];
         $random = mt_rand(100,999);

         echo '<input type="hidden" name="sent" value="yes">';
         echo '<p><strong>Your Mail Address:</strong></p>';
         echo '<p><input name="from" type="text" value="'.$_SESSION['from'].'" size="60" maxlength="60"></p>';
         echo '<p><strong>Your Message:</strong></p> ';
         echo '<p><textarea name="message" cols="60" rows="10">'.$_SESSION['message'].'</textarea></p>';
         echo '<input type="hidden" name="code" value="'.$random.'">';
         echo '<strong>Security Code: '.$random.' </strong> <input type="text" name="code1" size="5" maxlength="5"><br>';

         if ($sent == 'yes') {
            if ((empty($from) || (!ereg("^.+@.+\\..+$", $from))) && empty($message)) {
            echo '<br><font color=#FF0000>Please fill out Email-Address and provide a message.</font><br>';
            } else if (empty($from) || (!ereg("^.+@.+\\..+$", $from))) {
            echo '<br><font color=#FF0000>Please fill out Email-Address.</font><br>';
            } else if (empty($message)) {
            echo '<br><font color=#FF0000>Please provide a Message.</font><br>';
            } else {
                 if(($_POST['code']) == $_POST['code1']) {
                         mail("$mailto", "$subject", "$message", "From:$from");
                         unset ($_SESSION['from']);
                         unset ($_SESSION['message']);
                         header ("location: thankyou.php");
                 } else {
                 echo '<br><font color=#FF0000>Wrong Security-Code.</font><br>';
                         }
                 }
            }
?>
<br>
<input type="submit" value="Send Message">
</form>

<? include("inc/footer.inc"); ?>
