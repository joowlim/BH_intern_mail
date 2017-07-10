import imaplib, email, base64, mimetypes, os, datetime, pymysql, threading
from email.header import decode_header

month_name_list = ["dummy", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]

class Mail:
	# to, attachment is a list, remainder is string
	def __init__(self, from_, to, mail_date, title, inner_text, attachment):
		self.from_ = from_
		self.to = to
		self.mail_date = mail_date
		self.title = title
		self.inner_text = inner_text
		self.attachment = attachment

	def printStatus(self):
		print("from : %s" %self.from_)
		print("to : %s" %self.to)
		print("mail_date : %s" %self.mail_date)
		print("title : %s" %self.title)
		print("inner_text : %s" %self.inner_text)
		print("attachment : %s" %self.attachment)

def decode_if_byte(str, encoding):
	try:
		if type(str) == type(b'\n'):
			return str.decode(encoding)
		else:
			return str
	except:
		return ""

def get_text(msg):
	if msg.is_multipart():
		return get_text(msg.get_payload(0))
	else:
		return msg.get_payload(None, True)

def main():
	# START

	# login
	mail = imaplib.IMAP4_SSL('imap.gmail.com')
	mail.login('dnflsmsdlsxjs@gmail.com','xmfnqoffjstm')
	mail.list()
	mail.select('inbox', readonly=True)

	# get list of messages in inbox
	result, data = mail.search(None, "ALL")
	messageList = data[0].split()
	messageList.reverse()

	# list of mail instances
	mailList = []

	# get last parsing time
	time_file = open('./last_time', 'r')
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
	for i in messageList: # messages I want to see
		typ, msg_data = mail.fetch(i, '(RFC822)')
		for response_part in msg_data:
			if isinstance(response_part, tuple):
				msg = email.message_from_bytes(response_part[1])
				
				# set 'subject', 'from', 'to'
				to_decode = decode_header(msg['subject'])
				title = decode_if_byte(to_decode[0][0], to_decode[0][1])
				
				to_decode = decode_header(msg['from'])
				from_ = decode_if_byte(to_decode[0][0], to_decode[0][1])

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
				if ", " in to:
					to = to.split(", ")
				else:
					to = [to]
				
				try:
					to_decode = decode_header(msg['cc'])
					cc = decode_if_byte(to_decode[0][0], to_decode[0][1])
					if "," in cc:
						cc = cc.split(", ")
					else:
						cc = [cc]
				except:
					cc = []
				to = to + cc # concatenate reciever and cc

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
			path = "./attachment/"
			filename = part.get_filename()
			if filename: # when there is attachment
				# check file existence
				if os.path.exists(path + filename):
					# create numbering
					file_index = 1
					while os.path.exists(path + filename.split(".")[0] + "_[" + str(file_index) + "]." + filename.split(".")[1]):
						file_index += 1
					filename = filename.split(".")[0] + "_(" + str(file_index) + ")." + filename.split(".")[1]
				with open(os.path.join(path, filename), 'wb') as fp:
					attachment.append(filename)
					fp.write(part.get_payload(decode=True))

		mail_one = Mail(from_, to, mail_date, title, inner_text, attachment)
		mailList.append(mail_one)
		#mail_one.printStatus()

	for mail_instance in mailList:
                # connect to db
		conn = pymysql.connect(host='52.221.182.124',user='root', password='root', db='intern',charset='utf8')
		curs = conn.cursor()

		# Update mail table
		mail_sql = "INSERT INTO mail (title, inner_text, mail_date) VALUES (%s, %s, %s)" #datetime.date(y,m,d)
		curs.execute(mail_sql, (mail_instance.title, mail_instance.inner_text, mail_instance.mail_date))

		current_row_id = curs.lastrowid

		# Update mail_log table
		for receiver in mail_instance.to:
			mail_log_sql = "INSERT INTO mail_log (sender, receiver, mail_id) VALUES (%s, %s, %s)"
			curs.execute(mail_log_sql, (mail_instance.from_, receiver, str(current_row_id)))

		# Update attachment table
		for attachment_filename in mail_instance.attachment:
			mail_attachment_sql = "INSERT INTO attachment (each_attachment, mail_id) VALUES (%s, %s)"
			curs.execute(mail_attachment_sql, (attachment_filename, current_row_id));

		# commit and close the connection
		conn.commit()
		conn.close()

	# terminate connection
	mail.close()
	mail.logout()

	# start new connection simultaneously
	threading.Timer(300, main).start() # in second

if __name__ == "__main__":
	main()