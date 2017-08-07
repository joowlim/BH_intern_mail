<!DOCTYPE HTML>

<?php
	function table_header($add_tag) {
		return '
		<thead' . ($add_tag == 1? ' id = "filter_list"' : '') . '>
			<tr>
				<th>필터 이름</th>
				<th>제목 조건</th>
				<th>보낸이 조건</th>
				<th>내용 조건</th>
				<th>슬랙 채널</th>
			</tr>
		</thead>
		';
	} 
	function inputTable($ind, $filter_id) {
		global $conn;
		# ind = 0 (insert), ind = 1 (modify)
		if ($ind == 1) {
			$sql = 'SELECT * FROM filter WHERE filter_id = ' . $filter_id ;
			$result = mysqli_query($conn, $sql);
			$row = mysqli_fetch_array($result);
		}
		$table_body = '
				<tbody>
					<tr>
						<td><input type = text style="width:100%" id = "filter_name" name = "filter_name" ' . ($ind == 0 ? '' : 'value = "' . $row["filter_name"] . '" ') . '/></td>
						<td><input type = text style="width:100%" id = "title_cond" name = "title_cond" ' . ($ind == 0 ? '' : 'value = "' . $row["title_cond"] . '" ') . '/></td>
						<td><input type = text style="width:100%" id = "sender_cond" name = "sender_cond" ' . ($ind == 0 ? '' : 'value = "' . $row["sender_cond"] . '" ') . '/></td>
						<td><input type = text style="width:100%" id = "inner_text_cond" name = "inner_text_cond" ' . ($ind == 0 ? '' : 'value = "' . $row["inner_text_cond"] . '" ') . '/></td>
						<td><input type = text style="width:100%" id = "slack_channel" name = "slack_channel" ' . ($ind == 0 ? '' : 'value = "' . $row["slack_channel"] . '" ') . '/></td>
					</tr>
					<tr align = "center">
						<td colspan = "5"><button type = submit class = submit>' . ($ind == 0 ? '필터 추가' : '필터 수정') . '</button></td>
					</tr>
				</tbody>
				';
		return '<table border = 0>' . table_header(0) . $table_body . '</table>';
	}
	function sql($func) {
		global $conn;
		if ($func == 'insert') {
			if ($_POST["filter_name"] == "" && $_POST["title_cond"] == "" && $_POST["sender_cond"] == "" && $_POST["inner_text_cond"] == "" && $_POST["slack_channel"] == "")
				return;
			$sql = 'INSERT INTO filter (filter_name, title_cond, sender_cond, inner_text_cond, slack_channel) VALUES ("' . $_POST["filter_name"] . '", "' . $_POST["title_cond"] . '", "' . $_POST["sender_cond"] . '", "' . $_POST["inner_text_cond"] . '", "' . $_POST["slack_channel"] . '")';
		}
		else if ($func == 'delete') {	
			if ($_POST["filter_id"] == 0) 
				return;
			$sql = 'DELETE FROM filter WHERE filter_id = ' . $_POST["filter_id"];
		}
		else if ($func == 'modify' && $_POST["show_input"] != "true") {
			if ($_POST["filter_id"] == 0) 
				return;
			$sql = 'UPDATE filter SET filter_name = "' . $_POST["filter_name"] . '", title_cond = "' . $_POST["title_cond"] . '", sender_cond = "' . $_POST["sender_cond"] . '", inner_text_cond = "' . $_POST["inner_text_cond"] . '", slack_channel = "' . $_POST["slack_channel"] . '" WHERE filter_id = ' . $_POST["filter_id"];
		}
		mysqli_query($conn, $sql);
	}
?>
<?php
	// db data from user_config.ini
	$user_config = fopen('./user_config.ini','r');
	
	if(!$user_config) {
		echo 'cannot read config file : ' . $user_config ;
	}
	
	while(!feof($user_config)) {
		$each_line = fgets($user_config);

		if(strpos($each_line, 'server') !== false && strpos($each_line, 'server') < strpos($each_line, "=")) {
			$db_server = trim(substr($each_line,strpos($each_line,'=')+1));
		}
		else if(strpos($each_line, 'user') !== false && strpos($each_line, 'user') < strpos($each_line, "=")) {
			$db_user = trim(substr($each_line,strpos($each_line,'=')+1));
		}
		else if(strpos($each_line, 'password') !== false && strpos($each_line, 'password') < strpos($each_line, "=")) {
			if (strpos($each_line, 'account_password') === false)
				$db_password = trim(substr($each_line,strpos($each_line,'=')+1));
		}
		else if(strpos($each_line, 'schema') !== false && strpos($each_line, 'schema') < strpos($each_line, "=")) {
			$db_schema = trim(substr($each_line,strpos($each_line,'=')+1));
		}
	}
	fclose($user_config);

	// db connection
	$conn = mysqli_connect($db_server, $db_user, $db_password, $db_schema);
	mysqli_set_charset($conn, "utf8");

	if(mysqli_connect_errno($conn)) {
	    echo "데이터베이스 연결 실패: " . mysqli_connect_error();
	}

	// insert, delete, modify if button is clicked
	if ($_POST["func"] != null){
		sql($_POST["func"]);
	}
?>
<html>
	<head>
		<meta charset="utf-8">
		<link rel="stylesheet" type="text/css" href="./filter_modify.css">
		<title>필터 수정</title>
	</head>
	<body>
		<h1>필터 수정</h1>
		<!-- 필터 목룍 -->
		<div id="body_div">
			<div id="title_div">
				<h2>현재 필터 목록</h2>
			</div>
			<div id="filter_div">
				<table id="filter_table">
					<?php echo table_header(1); ?>
					<tbody>
						<?php
							$sql = 'SELECT * FROM filter ORDER BY filter_id ASC';
							$result = mysqli_query($conn, $sql);

							$ind = 0;
							while ($row = mysqli_fetch_array($result)) {
								$ind = $ind + 1;
								$id = ($ind % 2 == 0? 'even_td' : 'odd_td');
								echo '
						<tr>
							<td id="' . $id . '">'. $row["filter_name"] .'</td>
							<td id="' . $id . '">'. $row["title_cond"] .'</td>
							<td id="' . $id . '">'. $row["sender_cond"] .'</td>
							<td id="' . $id . '">'. $row["inner_text_cond"] .'</td>
							<td id="' . $id . '">'. $row["slack_channel"] .'</td>
						</tr>
							';
							}
						?>
					</tbody>
				</table>
			</div>

			<!-- 필터 추가 -->
			<div id="title_div">
				<h2>필터 추가</h2>
			</div>
			<div id="filter_div">
				<form action = "./filter_modify.php" method = "POST">
					<input type = hidden name = "func" value = "insert" />
						<?php 
							echo inputTable(0, 0); 
						?>
				</form>
			</div>

			<!-- 필터 삭제 -->
			<div id="title_div">
				<h2>필터 삭제</h2>
			</div>
			<div id="filter_div">
				<form action = "./filter_modify.php" method = "POST">	
					<input type = hidden name = "func" value = "delete" />		
					<select name = "filter_id">
						<option value = 0>필터 선택</option>
						<?php
							$sql = 'SELECT filter_id, filter_name FROM filter';
							$result = mysqli_query($conn, $sql);

							while ($row = mysqli_fetch_array($result)) {
								echo '
								<option value = ' . $row["filter_id"] . '>' . $row["filter_name"] . '</option>';
							}
						?>
					</select>
					<button type = submit class = submit>필터 삭제</button>
				</form>
			</div>

			<!-- 필터 수정 -->
			<div id="title_div">
				<h2>필터 수정</h2>
			</div>
			<div id="filter_div">
				<form action = "./filter_modify.php" method = "POST">	
					<input type = hidden name = "func" value = "modify" />	
					<select name = "filter_id">
						<option value = 0>필터 선택</option>
						<?php
							$sql = 'SELECT filter_id, filter_name FROM filter';
							$result = mysqli_query($conn, $sql);

							while ($row = mysqli_fetch_array($result)) {
								echo '
								<option value = ' . $row["filter_id"] . ' ' . (($_POST["show_input"] == "true" && $_POST["filter_id"] == $row["filter_id"]) ? 'selected="selected"' : '') .'>' . $row["filter_name"] . '</option>';
							}
						?>
					</select>
					<button type = submit name = "show_input" value = "true" class = submit>필터 선택</button>
					<?php
						
						if ($_POST["func"] == "modify" && $_POST["show_input"] == "true"){
							echo inputTable(1, $_POST["filter_id"]);
						}
					?>
				</form>
			</div>
		</div>
	</body>

</html>

<?php
	mysqli_close($conn);
?>