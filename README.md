# 밸런스히어로 Development Intern Project 1

### 참여자 : Aleph, Dori, Kate, Rev
---

#### 1. Email Parsing Program 이란?  
나의 계정으로 오는 메일 중 원하는 단어가 포함되었거나 특정 수신자로부터 온 메일을 찾고,  
데이터베이스에 저장한 뒤 Slack 으로 알림을 해주는 프로그램이다.  
  
#### 2. 필요한 파일 및 설정들  
-필요한 파일  
doris.py  
last_time  
slack_token.txt  
filter_config.txt  
api.php  
  
-필요한 폴더  
./attachment  
  
-필요한 프로그램  
BALANCE HERO 앱  
  
-필요한 설정  
서버의 데이터베이스를 규격에 맞게 설정한다.  
메일을 가져오고 싶은 계정의 환경설정으로 들어가 imap 설정을 킨다.  
  
#### 3. 사용 방법  
1) 안드로이드 소스 중 ActivityMain.java 66 줄의 url 을 수정한다.  
"http://<i></i>52.221.182.124/api.php/(원하는 계정)"  
  
2) 안드로이드 앱을 설치한다.  
  
3) 서버의 /var/www/html/ 에 api.php 를 복사하고 apache를 실행한다.  
  
4) filter_config.txt 를 열어 아래 filtering 규칙을 참조하여 파일을 수정한다.  

5) user_config.ini 파일을 열어 아래 userinfo 규칙을 참조하여 파일을 수정한다.
  
6) python3 으로 doris.py를 실행한다.  
맨 처음 실행할 때는 python3 doris.py -i 으로 실행하고, 메일을 가져오는 시간 간격을 설정하고 싶으면 -t [INT] 옵션을 추가하여 입력할 수 있다 (단위는 초).  
  
7) 안드로이드 앱에서 필터링 된 메일 중 최근 10개를 확인한다.  
  
#### 4. filtering 규칙  
  
제목(subject)과 본문 내용(inner_text)은 필터링 하고 싶은 단어들 중 적어도 하나 포함되면 통과.  
보낸이(sender)와 받는이(reciever)는 필터링하고 싶은 계정(메일) 중 적어도 하나 일치하면 통과.  
  
filter_config.txt 에 맞추어 필터링된다.  
\#으로 시작하는 태그 아래에 필터링 하고 싶은 단어를 입력하면 된다. 여러 줄도 가능.  
아래 예시는 전체 메일 중 kate.lim@<i></i>balancehero.com 이 보낸 메일만 통과시키는 configuration.  
  
-------- filter_config.txt --------  
\# subject  
\# inner_text  
\# sender  
kate.lim@<i></i>balancehero.com  
\# receiver  
\#  
\-------------------------------------  

#### 5. userinfo 규칙

attachment_path 는 첨부파일 저장 경로를 의미한다. url에는 your.domain.name 부분을 사용자의 domain 으로 수정하면 된다.
account information 에는 차례대로 구글 메일계정과 비밀번호를 입력하면 된다.
Database information 에 입력하는 것은 차례대로 db서버 ip와 db유저 id, passwd, schema 이름을 입력하면 된다.
Slack channel information 에는 알림 문자를 받고자 하는 슬랙 채널명과 토큰을 입력하면 된다.