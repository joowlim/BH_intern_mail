<?php

include("./db_account_info.php");

// Get request number
$request = trim($_SERVER['PATH_INFO']);
$request = str_replace("/", "", $request);

// Get Method
$method = $_SERVER['REQUEST_METHOD'];

// Connect to the db
$link = mysqli_connect($db_server, $db_user, $db_password, $db_schema);
mysqli_set_charset($link, 'utf8');

switch ($method) {
  case 'GET':
    $sql = "SELECT DISTINCT title, inner_text, mail_date, sender, receiver " .
           "FROM mail, mail_log " .
           "WHERE mail.mail_id = mail_log.mail_id AND receiver = '$request' " .
           "ORDER BY mail.mail_date DESC LIMIT 10";
    break;
  case 'POST':
    break;
}

// Execute sql
$result = mysqli_query($link, $sql);

if(!$result)
{
  // Return error
  http_response_code(404);
  die(mysqli_error($link));
}

if($method == 'GET') {
  echo '[';
  for($i = 0; $i < mysqli_num_rows($result); $i++) {
    echo ($i > 0 ? ',' : '').json_encode(mysqli_fetch_object($result));
  }
  echo ']';
}

// Close connection
mysqli_close($link);
?>
