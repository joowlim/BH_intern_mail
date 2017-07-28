import imaplib, email, base64, mimetypes, os, datetime, pymysql, threading, sys
from email.header import decode_header
from slacker import Slacker

month_name_list = ["dummy", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

class SlackBot:

    def __init__(self,token):
        self.slacker = Slacker(token)

    def sendCustomizedMessage(self,_channel, _title, _text, _pretext='', _link='',):
        attachment = dict()
        attachment['pretext'] = _pretext
        attachment['title'] = _title
        attachment['title_link'] = _link
        attachment['fallback'] = _text
        attachment['text'] = _text
        attachment['mrkdwn_in'] = ['text', 'title_link']
        att = [attachment]

        self.slacker.chat.post_message(channel=_channel, text=None, attachments=att)

    def sendPlainMessage(self, _channel, _title, _text, _date, _from, attachment, attach_url, max_char):
        post_text = "```Title : " + _title + "\nFrom : " + _from + "\nDate : " + _date + "\nText : \n" + _text[:max_char]
        if len(_text) > max_char :
            post_text += " ..."
        attach_index = 1
        for attach in attachment:
            post_text += "\nattachment " + str(attach_index) + " : " + attach_url + attach
            attach_index += 1
        post_text += '```'
        self.slacker.chat.post_message(_channel, post_text, "Mail_parrot")

class Mail:
	# to, attachment is a list, remainder is string
	def __init__(self, from_, to, cc, mail_date, title, inner_text, attachment):
		self.from_ = from_
		self.to = to
		self.cc = cc
		self.mail_date = mail_date
		self.title = title
		self.inner_text = inner_text
		self.attachment = attachment

def decode_if_byte(str_, encoding):
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

def get_text(msg):
	if msg.is_multipart():
		return get_text(msg.get_payload(0))
	else:
		return msg.get_payload(None, True)

# return true if content contain at least one keyword from keywords
def contains_multi(keywords, content):
	for keyword in keywords:
		if keyword in content:
			return True
	return False

# return true if content is equal to at least one keyword from keywords
def equals_multi(keywords, content):
	for keyword in keywords:
		if keyword == content:
			return True
	return False

def filter_mail(mailList, config):
	config_data = []
	temp_data = []
	tag = ["# subject", "# inner_text", "# sender", "# receiver", "# cc", "#"]	

	try:
		fp = open(config)
	except IOError:
		sys.exit("Could not read file : %s" % config)

	line = fp.readline()
	for index in range(1, len(tag)):
		temp_data = []
		while (line.strip() != tag[index]):
			temp_data.append(line.strip())
			line = fp.readline()
		config_data.append(temp_data[1:])
	fp.close()
	

	if config_data[0]: # subject
		mailList = list(filter(lambda x: contains_multi(config_data[0], x.title), mailList))
	if config_data[1]: # inner_text
		mailList = list(filter(lambda x: contains_multi(config_data[1], x.inner_text), mailList))
	if config_data[2]: # sender
		mailList = list(filter(lambda x: equals_multi(config_data[2], x.from_), mailList))
	if config_data[3]: # receiver
		mailList = list(filter(lambda x: equals_multi(config_data[3], x.to), mailList))
	if config_data[4]: # cc
		mailList = list(filter(lambda x: equals_multi(config_data[4], x.cc), mailList))
		
	return mailList

def main(time_interval = 300):
	# Open ini file
	ini_file = open('./user_config.ini', 'r')
	ini_lines = ini_file.readlines()

	inis = dict()

	for ini_line in ini_lines:
		if ini_line[0] != '#' and ini_line != '\n' :
			(var_name, var_value) = ini_line.split("=")
			inis[var_name.rstrip(" ")] = var_value.lstrip(" ").rstrip('\n')

	# login
	mail = imaplib.IMAP4_SSL('imap.gmail.com')
	mail.login(inis['account_name'],inis['account_password'])
	mail.list()
	mail.select('inbox', readonly=True)

	# get list of messages in inbox
	result, data = mail.search(None, "ALL")
	messageList = data[0].split()
	messageList.reverse()

	# list of mail instances
	mailList = []

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
	parse_end = False

	last_time_saved = False

	# initialize slack bot
	slackBot = SlackBot(inis['slack_token'])
	
	for i in messageList: # messages I want to see
		typ, msg_data = mail.fetch(i, '(RFC822)')
		for response_part in msg_data:
			if isinstance(response_part, tuple):
				msg = email.message_from_bytes(response_part[1])
				
				# set 'subject', 'from', 'to'
				to_decode = decode_header(msg['subject'])
				title = decode_if_byte(to_decode[0][0], to_decode[0][1])
				
				to_decode = decode_header(msg['from'])
				
				# try to get email address of the sender
				from_ = ""
				try:
					from_ = decode_if_byte(to_decode[1][0], to_decode[1][1])
				except IndexError:
					from_ = decode_if_byte(to_decode[0][0], to_decode[0][1])
				
				if "<" in from_ and ">" in from_:
					from_ = from_[from_.index("<")+1:from_.index(">")]

				to_decode = decode_header(msg['date'])
				mail_date = decode_if_byte(to_decode[0][0], to_decode[0][1]).split()
				day = int(mail_date[1])
				month = month_name_list.index(mail_date[2])
				year = int(mail_date[3])
				time = mail_date[4].split(":")
				dt = datetime.datetime(year, month, day, int(time[0]), int(time[1]), int(time[2]))
				mail_date = dt.strftime('%Y-%m-%d %H:%M:%S')
				
				if not last_time_saved:
					# New mail arrived
					if dt > last_parse_time :
						# save last time
						time_file = open('last_time', 'w')
						time_file.write(str(mail_date))
						time_file.close()
					last_time_saved = True
				
				if last_parse_time >= dt :
					parse_end = True
					break
				
				to_decode = decode_header(msg['to'])
				to = decode_if_byte(to_decode[0][0], to_decode[0][1])
				if "," in to:
					to = map(lambda x: x.strip()[1:-1], to.split(","))
				else:
					to = [to]
				
				try:
					to_decode = decode_header(msg['cc'])
					cc = decode_if_byte(to_decode[0][0], to_decode[0][1])
					if "," in cc:
						cc = map(lambda x: x.strip()[1:-1], cc.split(","))
					else:
						cc = [cc]
				except:
					cc = []

				# inner text
				inner_text = decode_if_byte(get_text(msg), 'utf-8')

		# already parsed every mail
		if parse_end :
			break
		
		# download attachment
		attachment = []

		for part in msg.walk():
			if part.get_content_maintype() == 'multipart':
				continue
			path = inis['attachment_path']
			filename = decode_if_byte(part.get_filename(), 'utf-8')
			if filename: # when there is attachment
				# check file existence
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
						fp.write(part.get_payload(decode=True))
				except IOError:
					sys.exit("Could not find directory : %s" % path)

		mail_one = Mail(from_, to, cc,  mail_date, title, inner_text, attachment)
		mailList.append(mail_one)

	# filter mail
	mailList = filter_mail(mailList, "./filter_config.txt")
	
	for mail_instance in mailList:
                # connect to db
		conn = pymysql.connect(host='localhost',user=inis['user'], password=inis['password'], db=inis['schema'],charset='utf8')
		curs = conn.cursor()

		# Update mail table
		mail_sql = "INSERT INTO mail (title, inner_text, mail_date) VALUES (%s, %s, %s)" #datetime.date(y,m,d)
		curs.execute(mail_sql, (mail_instance.title, mail_instance.inner_text, mail_instance.mail_date))

		current_row_id = curs.lastrowid

		# Update mail_log table - to
		for receiver in mail_instance.to:
			mail_log_sql = "INSERT INTO mail_log (sender, receiver, mail_id) VALUES (%s, %s, %s)"
			curs.execute(mail_log_sql, (mail_instance.from_, receiver, str(current_row_id)))
		
		# Update mail_log table - cc
		for receiver in mail_instance.cc:
			mail_log_sql = "INSERT INTO mail_log (sender, receiver, mail_id, is_ref) VALUES (%s, %s, %s, %s)"
			curs.execute(mail_log_sql, (mail_instance.from_, receiver, str(current_row_id), 1))

		# Update attachment table
		for attachment_filename in mail_instance.attachment:
			mail_attachment_sql = "INSERT INTO attachment (each_attachment, mail_id) VALUES (%s, %s)"
			curs.execute(mail_attachment_sql, (attachment_filename, current_row_id));

		# commit and close the connection
		conn.commit()
		conn.close()
		
		# post on slack
		slackBot.sendPlainMessage(inis['channel'], mail_instance.title, mail_instance.inner_text, mail_instance.mail_date, mail_instance.from_, mail_instance.attachment, inis['attachment_url'], int(inis['max_text_chars']))
	
	# terminate connection
	mail.close()
	mail.logout()
	
	# notification to user
	print ("Mail updated!")

	# start new connection simultaneously
	threading.Timer(time_interval, main).start() # in second

def wrong_parameter():
	print("Wrong parameter")

def run_i():
	time_file = open('last_time', 'w')
	time_file.write("1000-01-01 00:00:00")
	time_file.close()

def run_h():
	print("python doris.py [-i | -h | -t [INT]] (python = python version 3)")
	print("--------------------------------------")
	print("command list : ")
	print("\t\t-i : initialize time stamp")
	print("\t\t-t [INT] : start program with given time interval for crawling (in second)")
	print("\t\t-h : show help command")

def run_t(t):
	main(t)

def is_int(s):
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
			run_i()
			main()
		elif sys.argv[1] == "-h":
			run_h()
		else:
			wrong_parameter()

	# two argument : -t [INT]
	elif len(sys.argv) == 3:
		if sys.argv[1] == "-t" and is_int(sys.argv[2]):
			run_t(int(sys.argv[2]))
		else:
			wrong_parameter()

	# three argument : -t [INT] -i, -i -t [INT]
	elif len(sys.argv) == 4:
		if (sys.argv[1] == "-t" and is_int(sys.argv[2]) and sys.argv[3] == "-i"):
			run_i()
			run_t(int(sys.argv[2]))
		elif (sys.argv[1] == "-i" and sys.argv[2] == "-t" and is_int(sys.argv[3])):
			run_i()
			run_t(int(sys.argv[3]))
		else:
			wrong_parameter()
	else:
		wrong_parameter()
