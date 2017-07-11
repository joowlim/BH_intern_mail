밸런스히어로 Development Intern Project

참여자 : Aleph, Dori, Kate, Rev

n. filtering 규칙
제목과 본문 내용은 필터링 하고 싶은 단어들 중 적어도 하나 포함되면 통과.
보낸이(이름) 와 받는이(메일)는 필터링하고 싶은 계정 중 적어도 하나 일치하면 통과. 

파일 filter_config.txt 참조
필터링 하고 싶은 항목이 있으면 각 # 태그 밑에 한 줄씩 추가하면 됨.
예시는 보낸 이 중 '임주원'에 해당하는 메일만 받아오고 싶을 때

-------- filter_config.txt --------
# subject
# inner_text
# sender
임주원
# receiver
#
-----------------------------------

