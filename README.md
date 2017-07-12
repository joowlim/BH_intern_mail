# 밸런스히어로 Development Intern Project

### 참여자 : Aleph, Dori, Kate, Rev
---
#### 1. filtering 규칙

filter_config.txt 에 맞추어 필터링.  
제목과 본문 내용은 필터링 하고 싶은 단어들 중 적어도 하나 포함되면 통과.  
보낸이와 받는이는 필터링하고 싶은 계정(메일) 중 적어도 하나 일치하면 통과.  
  
아래 예시는 전체 메일 중 kate.lim@balancehero.com 이 보낸 메일만 통과시키는 configuration.  
  
-------- filter_config.txt --------  
\# subject  
\# inner_text  
\# sender  
kate.lim@balancehero.com  
\# receiver  
\#  
\-------------------------------------  
