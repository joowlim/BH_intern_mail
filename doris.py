import imaplib, email, base64, mimetypes, os, datetime, pymysql, threading, sys, re, logging
from email.header import decode_header
from slacker import Slacker

month_name_list = ["dummy", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

def removeDoubleSpace(text):
	return re.sub(' +', ' ', text.replace("\t", " "))

def removeTag(text):
	while text.find("<") != -1:
		start_index = text.find("<")
		end_index = start_index
		while text[end_index] != '>':
			end_index += 1
		text = text[:start_index] + text[end_index+1:]
	while text.find("</") != -1:
		start_index = text.find("</")
		end_index = start_index
		while text[end_index] != '>':
			end_index += 1
		text = text[:start_index] + text[end_index+1:]
	return text

class SlackBot:

	def __init__(self, token):
		self.slacker = Slacker(token)
		self.current_color_idx = 0
		self.color = '#36a64f'

	def sendCustomizedMessage(self, _channel, _title, _text, _pretext = '', _link = '',):
		attachment = dict()
		attachment['pretext'] = _pretext
		attachment['title'] = _title
		attachment['title_link'] = _link
		attachment['fallback'] = _text
		attachment['text'] = _text
		attachment['mrkdwn_in'] = ['text', 'title_link']
		att = [attachment]

		self.slacker.chat.post_message(channel = _channel, text = None, attachments = att)

	def sendPlainMessage(self, _channel, _title, _text, _date, timezone, _from, _to, attachment, attach_url, max_char, filter_name):
		post_text = "Title : " + _title + "\nFrom : " + _from + "\nTo : " + _to + "\nDate : " + _date + " " + timezone + "\nText : \n" + _text[:max_char]
		if len(_text) > max_char :
			post_text += " ..."
		slacker_attachment = dict()
		slacker_attachment['pretext'] = "*" + filter_name + "*"
		slacker_attachment['color'] = self.color
		slacker_attachment['mrkdwn_in'] = ['pretext']
		
		attach_index = 1
		if attachment:
			post_text += "\nAttachment\n"
		for attach in attachment:
			attachment_text = "Attachment " + str(attach_index) + " : " + attach
			attach_index += 1
			link_string = "\n<" + attach_url + attach + "|" + attachment_text + ">"
			post_text += link_string
			
		inner_fields = dict()
		inner_fields['value'] = post_text
		inner_fields['short'] = 'false'
		
		slacker_attachment['fields'] = [inner_fields]
		att = [slacker_attachment]
		self.slacker.chat.post_message(channel = _channel, attachments = att, username = "mail_notification_bot");
		

class Mail:
	# to, attachment is a list, remainder is string
	def __init__(self, from_, to, cc, mail_date, timezone, title, inner_text, attachment):
		self.from_ = from_
		self.to = to
		self.cc = cc
		self.mail_date = mail_date
		self.title = title
		self.inner_text = inner_text
		self.attachment = attachment
		self.timezone = timezone

def decodeIfByte(str_, encoding):
	try:
		if type(str_) == type(b'\n'):
			if encoding != None:
				return str_.decode(encoding)
			else:
				return str_.decode('utf-8')
		else:
			return str_
	except:
		return ""

def getText(msg):
	if msg.is_multipart():
		return getText(msg.get_payload(0))
	else:
		return msg.get_payload(None, True)

def containsAll(keywords, content):
	for keyword in keywords:
		if keyword not in content:
			return False
	return True

def equalsAll(keywords, content):
	for keyword in keywords:
		if keyword != content:
			return False
	return True

def filterMailByDb(mail_list, f):
	if f["title_cond"] != []:
		mail_list = list(filter(lambda x: containsAll(f["title_cond"], x.title), mail_list))
	if f["inner_text_cond"] != []:
		mail_list = list(filter(lambda x: containsAll(f["inner_text_cond"], x.inner_text), mail_list))
	if f["sender_cond"] != []:
		mail_list = list(filter(lambda x: equalsAll(f["sender_cond"], x.from_), mail_list))
	return mail_list

def deleteAttachmentsIfExpired(inis):
	duration_day = inis['attachment_duration_day']
	duration_second = int(duration_day) * 24 * 60 * 60

	attachment_path = inis['attachment_path']
	current_time = datetime.datetime.now()
	
	for path, dirs, files in os.walk(attachment_path):
		for file in files:
			file_time = datetime.datetime.fromtimestamp(os.path.getmtime(os.path.join(path, file)))
			elapsed_time = (current_time - file_time).total_seconds()
			if elapsed_time > duration_second :
				os.remove(os.path.join(path, file))
				

def deleteMailIfExpired(inis):
	conn = pymysql.connect(host = inis['server'], user = inis['user'], password = inis['password'], db = inis['schema'], charset = 'utf8')
	curs = conn.cursor()
	
	mail_duration_day = inis['mail_log_duration_day']

# filter mail
	mail_log_delete_sql = "DELETE FROM mail_log WHERE mail_id in (SELECT mail_id FROM mail WHERE mail_date BETWEEN DATE_SUB(NOW(), INTERVAL " + mail_duration_day + " DAY) AND NOW())"
	mail_delete_sql = "DELETE FROM mail WHERE mail_date BETWEEN DATE_SUB(NOW(), INTERVAL " + mail_duration_day + " DAY) AND NOW()"

	curs.execute(mail_log_delete_sql)
	curs.execute(mail_delete_sql)
	
	conn.commit()
	conn.close()
	
def main(time_interval = 600):
    	# remove logfile if the size exceed 1M
    	if os.path.exists("./mail.log"):
		if os.path.getsize("./mail.log") > 10**6:    	
			open("./mail.log", "w").close()    	

    	# initialize logging
	logging.basicConfig(filename = "mail.log", level = logging.INFO, format = "%(message)s (%(asctime)s)", datefmt = "%Y/%m/%d %H:%M:%S %Z")
	logging.info("Mail parsing start!")

	# Open ini file
	ini_file = open('./user_config.ini', 'r')
	ini_lines = ini_file.readlines()
	ini_file.close()
	
	inis = dict()

	for ini_line in ini_lines:
		if ini_line[0] != '#' and ini_line != '\n':
			(var_name, var_value) = ini_line.split("=")
			inis[var_name.rstrip(" ")] = var_value.lstrip(" ").rstrip('\n')

	# get last parsing time
	try:
		time_file = open('./last_time', 'r')
	except IOError:
		sys.exit("Could not read file : %s" % "./last_time")
	time_line = time_file.readline().strip('\n')
	time_file.close()

	last_parse_time = datetime.datetime(int(time_line.split('-')[0]),
						int(time_line.split('-')[1]),
						int(time_line.split('-')[2].split()[0]),
						int(time_line.split('-')[2].split()[1].split(":")[0]),
						int(time_line.split('-')[2].split()[1].split(":")[1]),
						int(time_line.split('-')[2].split()[1].split(":")[2]))

	#account and passwords
	account_origin = inis['account_name']
	password_origin = inis['account_password']
	account_list = account_origin.split(',')
	password_list = password_origin.split(',')
	for accounts in account_list:
		mailGet(accounts, password_list[account_list.index(accounts)], inis, last_parse_time)

	# delete file if expired
	deleteAttachmentsIfExpired(inis)
	
	# logging
	logging.info("Mail parsing end!")
  
	# delete mail if expired 
	deleteMailIfExpired(inis)
  
	# start new connection simultaneously
	threading.Timer(time_interval, main, args = [time_interval,]).start() # in second

def mailGet(account, password, inis, last_parse_time):
	# login
	mail = imaplib.IMAP4_SSL('imap.gmail.com')
	#mail.login(inis['account_name'],inis['account_password'])
	mail.login(account, password)
	mail.list()
	mail.select('inbox', readonly = True)

	# get list of messages in inbox
	result, data = mail.search(None, "ALL")
	message_list = data[0].split()
	message_list.reverse()

	# list of mail instances
	mail_list = []
	parse_end = False

	last_time_saved = False

	# initialize slack bot
	slackBot = SlackBot(inis['slack_token'])
	
	for i in message_list: # messages I want to see
		typ, msg_data = mail.fetch(i, '(RFC822)')
		for response_part in msg_data:
			if isinstance(response_part, tuple):
				msg = email.message_from_bytes(response_part[1])
				# set 'subject', 'from', 'to'
				to_decode = decode_header(msg['subject'])
				title = decodeIfByte(to_decode[0][0], to_decode[0][1])
				to_decode = decode_header(msg['from'])
				# try to get email address of the sender
				from_ = ""
				try:
					from_ = decodeIfByte(to_decode[1][0], to_decode[1][1])
				except IndexError:
					from_ = decodeIfByte(to_decode[0][0], to_decode[0][1])
				
				if "<" in from_ and ">" in from_:
					from_ = from_[from_.index("<")+1:from_.index(">")]

				to_decode = decode_header(msg['date'])
				mail_date = decodeIfByte(to_decode[0][0], to_decode[0][1]).split()
				
				day = 0
				month = 0
				time  = []
				timezone = ""
				try:
					day = int(mail_date[1])
					month = month_name_list.index(mail_date[2])
					year = int(mail_date[3])
					time = mail_date[4].split(":")
					timezone = mail_date[5]
				except ValueError:
					day = int(mail_date[0])
					month = month_name_list.index(mail_date[1])
					year = int(mail_date[2])
					time = mail_date[3].split(":")
					timezone = mail_date[4]
					
				# timezone operation
				timezone_sign = timezone[0]
				timezone_deltatime = datetime.timedelta(hours = int(timezone[1:3]), minutes = int(timezone[3:]))
				dt = datetime.datetime(year, month, day, int(time[0]), int(time[1]), int(time[2]))
				
				# save timezone string before the operation
				mail_date = dt.strftime('%Y-%m-%d %H:%M:%S')
				timezone = "UTC " + timezone[:3] + ":" + timezone[3:]
				
				# UTC 00:00, for last_time file
				if timezone_sign == '+':
					dt = dt - timezone_deltatime
				else:
					dt = dt + timezone_deltatime
				
				if not last_time_saved:
					# New mail arrived
					if dt >= last_parse_time:
						# save last time
						time_file = open('last_time', 'w')
						time_file.write(str(dt.strftime('%Y-%m-%d %H:%M:%S')))
						time_file.close()
					last_time_saved = True
				
				if last_parse_time >= dt:
					parse_end = True
					break

				to_decode = decode_header(msg['to'])
				to = decodeIfByte(to_decode[0][0], to_decode[0][1])
				if "," in to:
					to = map(lambda x: x.strip()[1:-1], to.split(","))
				else:
					to = [to]
				
				try:
					to_decode = decode_header(msg['cc'])
					cc = decodeIfByte(to_decode[0][0], to_decode[0][1])
					if "," in cc:
						cc = map(lambda x: x.strip()[1:-1], cc.split(","))
					else:
						cc = [cc]
				except:
					cc = []

				# inner text
				inner_text = decodeIfByte(getText(msg), 'utf-8')
				if inner_text[0] == '<' :
					inner_text = removeDoubleSpace(removeTag(inner_text.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&quot;", '"').replace("&apos;", "'").replace("&cent;", "¢").replace("&copy;", "©").replace("&reg;", "®")).strip())

		# already parsed every mail
		if parse_end:
			break
		
		# download attachment
		attachment = []

		for part in msg.walk():
			if part.get_content_maintype() == 'multipart':
				continue
			path = inis['attachment_path']
			
			filename = decodeIfByte(part.get_filename(), 'UTF-8')
			if filename: # when there is attachment
				# check file existence
				splited_filename = filename[1:].split('?')
				if filename[:10] == "=?UTF-8?B?" or filename[:10] == "=?utf-8?B?":
					filename = base64.b64decode(filename[10:]).decode('utf-8')
				if os.path.exists(path + filename):
					# create numbering
					file_index = 1
					
					make_file_name = (lambda x, y : '.'.join(x.split(".")[:-1]) + "_(" + str(y) + ")." + x.split(".")[-1])
					while os.path.exists(path + make_file_name(filename, file_index)):
						file_index += 1
					filename = make_file_name(filename, file_index)
				try:
					with open(os.path.join(path, filename), 'wb') as fp:
						attachment.append(filename)
						fp.write(part.get_payload(decode = True))
				except IOError:
					sys.exit("Could not find directory : %s" % path)

		mail_one = Mail(from_, to, cc, mail_date, timezone, title, inner_text, attachment)
		mail_list.append(mail_one)
	
	# connect to db
	conn = pymysql.connect(host = inis['server'], user = inis['user'], password = inis['password'], db = inis['schema'], charset = 'utf8')
	curs = conn.cursor()

# filter mail
	mail_sql = "SELECT filter_id, title_cond, inner_text_cond, sender_cond, slack_channel, filter_name FROM filter ORDER BY filter_id ASC" 
	curs.execute(mail_sql)

	filters = []

	for (filter_id, title_cond, inner_text_cond, sender_cond, slack_channel, filter_name) in curs:
		temp_map = {}
		temp_map["filter_id"] = filter_id
		
		if title_cond:
			temp_map["title_cond"] = title_cond.split(", ")
		else:
			temp_map["title_cond"] = []
		if inner_text_cond:
			temp_map["inner_text_cond"] = inner_text_cond.split(", ")
		else:
			temp_map["inner_text_cond"] = []
		if sender_cond:
			temp_map["sender_cond"] = sender_cond.split(", ")
		else:
			temp_map["sender_cond"] = []
		temp_map["slack_channel"] = slack_channel
		temp_map["filter_name"] = filter_name
		filters.append(temp_map)
	
	for f in filters:
		mail_list_filtered = filterMailByDb(mail_list, f)
		
		for mail_instance in mail_list_filtered:
			# Update mail table
			mail_sql = "INSERT INTO mail (title, inner_text, mail_date, filter_id) VALUES (%s, %s, %s, %s)" #datetime.date(y,m,d)
			curs.execute(mail_sql, (mail_instance.title, mail_instance.inner_text, mail_instance.mail_date, f["filter_id"]))

			current_row_id = curs.lastrowid

			# Update mail_log table - to
			for receiver in mail_instance.to:
				mail_log_sql = "INSERT INTO mail_log (sender, receiver, mail_id) VALUES (%s, %s, %s)"
				curs.execute(mail_log_sql, (mail_instance.from_, receiver, str(current_row_id)))
			
			# Update mail_log table - cc
			for receiver in mail_instance.cc:
				mail_log_sql = "INSERT INTO mail_log (sender, receiver, mail_id, is_ref) VALUES (%s, %s, %s, %s)"
				curs.execute(mail_log_sql, (mail_instance.from_, receiver, str(current_row_id), 1))


			# commit the connection
			conn.commit()
			
			# post on slack
			slackBot.sendPlainMessage(f["slack_channel"], mail_instance.title, mail_instance.inner_text, mail_instance.mail_date, mail_instance.timezone, mail_instance.from_, account, mail_instance.attachment, inis['attachment_url'], int(inis['max_text_chars']), f['filter_name'])

	#close the connection
	conn.close()
	
	# terminate connection
	mail.close()
	mail.logout()

def wrongParameter():
	print("Wrong parameter")

def runI():
	time_file = open('last_time', 'w')
	time_file.write("1000-01-01 00:00:00")
	time_file.close()

def runH():
	print("python doris.py [-i | -h | -t [INT]] (python = python version 3)")
	print("--------------------------------------")
	print("command list : ")
	print("\t\t-i : initialize time stamp")
	print("\t\t-t [INT] : start program with given time interval for crawling (in second)")
	print("\t\t-h : show help command")

def runT(t):
	main(t)

def isInt(s):
	try: 
		int(s)
		return True
	except ValueError:
		return False

if __name__ == "__main__":
	# without argument
	if len(sys.argv) == 1:
		main()

	# one argument : -i, -h (for -h option, only works alone)
	elif len(sys.argv) == 2:
		# initialize
		if sys.argv[1] == "-i":
			runI()
			main()
		elif sys.argv[1] == "-h":
			runH()
		else:
			wrongParameter()

	# two argument : -t [INT]
	elif len(sys.argv) == 3:
		if sys.argv[1] == "-t" and isInt(sys.argv[2]):
			runT(int(sys.argv[2]))
		else:
			wrongParameter()

	# three argument : -t [INT] -i, -i -t [INT]
	elif len(sys.argv) == 4:
		if (sys.argv[1] == "-t" and isInt(sys.argv[2]) and sys.argv[3] == "-i"):
			runI()
			runT(int(sys.argv[2]))
		elif (sys.argv[1] == "-i" and sys.argv[2] == "-t" and isInt(sys.argv[3])):
			runI()
			runT(int(sys.argv[3]))
		else:
			wrongParameter()
	else:
		wrongParameter()
