<?php
// Get Method
$method = $_SERVER['REQUEST_METHOD'];
$user_config = fopen('../user_config.ini', 'r');
if(!$user_config) {
  echo 'cannot read config file';
}
while(!feof($user_config)) {
  $each_line = fgets($user_config);
  if(strpos($each_line, 'user') !== false) {
    $db_user = trim(substr($each_line, strpos($each_line, '=') + 1));
    
  }
  else if(strpos($each_line, 'password') !== false) {
    if(strpos($each_line, 'account_password') === false) {
      $db_password = trim(substr($each_line, strpos($each_line, '=') + 1));
    }
  }
  else if(strpos($each_line, 'schema') !== false) {
    $db_schema = trim(substr($each_line, strpos($each_line, '=') + 1));
  }
}
fclose($user_config);
$mail_list = $_GET['mail_list'];
if(count($mail_list) == 0) {
  echo '사용법 : api.php/?mail_list[]=메일1&mail_list[]=메일2...로 호출해 주세요';
  return;
}
for($i = 0; $i < count($mail_list); $i++) {
  $inner_sql = $inner_sql . "'" . $mail_list[$i] . "'=receiver";
  if($i != count($mail_list) - 1) {
    $inner_sql = $inner_sql . " OR ";
  }
}
// Connect to the db
$link = mysqli_connect('localhost', $db_user, $db_password, $db_schema);
mysqli_set_charset($link, 'utf8');
switch ($method) {
  case 'GET':
  $sql = "SELECT temp_result.mail_id, temp_result.sender, real_receiver, title, inner_text, mail_date, receiver as 'ref_receiver',is_ref" .
    " FROM" .
    " (SELECT temp.mail_id,sender,real_receiver,title,inner_text,mail_date FROM" .
    " (SELECT mail_id,sender, receiver as 'real_receiver' FROM mail_log" .
    " WHERE is_ref=0 AND (" . $inner_sql . ")) as temp, mail" .
    " WHERE temp.mail_id=mail.mail_id" .
    " ORDER BY mail_date DESC" .
    " LIMIT 10) as temp_result LEFT JOIN mail_log ON temp_result.mail_id=mail_log.mail_id";
  break;
  case 'POST':
    break;
}
// Execute sql
$result = mysqli_query($link, $sql);
if(!$result) {
  // Return error
  http_response_code(404);
  die(mysqli_error($link));
}
if($method == 'GET') {
  $previous = -1;
    echo '[';
  $each_object = array();
    for($i = 0; $i < mysqli_num_rows($result); $i++) {
    
      $each_row = $result->fetch_assoc();
    if($previous == $each_row['mail_id']) {
      if($each_row['is_ref'] == 0){
        array_push($each_object['real_receiver'], $each_row['real_receiver']);
        $each_object['real_size']++;
      }
      else {
        if($each_object['ref_receiver'] == null) {
          $each_object['ref_receiver'] = array();
        }
        array_push($each_object['ref_receiver'], $each_row['ref_receiver']);
        $each_object['ref_size']++;
      }
    }
    else {
      if($i != 0) {
        echo json_encode($each_object) . ", ";
      }
      $each_object = array();
      $each_object['ref_size'] = 0;
      $each_object['real_size'] = 0;
      $each_object['mail_date'] = $each_row['mail_date'];
      $each_object['mail_id'] = $each_row['mail_id'];
      $each_object['title'] = $each_row['title'];
      $each_object['inner_text'] = $each_row['inner_text'];
      $each_object['sender'] = $each_row['sender'];
      if($each_row['is_ref'] == 0) {
        $each_object['real_receiver'] = array($each_row['real_receiver']);
        $each_object['real_size']++;
      }
      else{
        $each_object['ref_receiver'] = array($each_row['ref_receiver']);
        $each_object['ref_size']++;
      }
    }
    $previous = $each_row['mail_id'];
    }
  if(mysqli_num_rows($result) > 0) {
    echo json_encode($each_object);
  }
    echo ']';
}
// Close connection
mysqli_close($link);
?>