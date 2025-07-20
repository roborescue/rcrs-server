<?php
include("login.php");
if (!isset($_SESSION['logged_in'])||$_SESSION['logged_in']!="admin"){
    session_destroy();
echo "access denied!";
    exit;

}

?>
