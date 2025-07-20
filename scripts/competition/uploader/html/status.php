<?php
include("login_admin.php");
?>

<head>
    <meta http-equiv="refresh" content="5" />

</head>
<?php
$cmd="ssh control@c11 'source ~/.profile && showRuns.sh'";
exec($cmd, $out, $ret);
echo "<pre>";
if ($ret == 0){
foreach ($out as $line) {
    echo $line . "\n";
}
}else{
echo "error: return code=$ret";
}
echo "</pre>";
