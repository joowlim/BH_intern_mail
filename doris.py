import imaplib, email, base64, mimetypes, pymysql
from email.header import decode_header

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

	mailList = []

	mail_count = int(mail.select('inbox', readonly=True)[1][0], 10)

	for i in range(1, mail_count+1): # messages I want to see 
		typ, msg_data = mail.fetch(str(i), '(RFC822)')
		for response_part in msg_data:
			if isinstance(response_part, tuple):
				msg = email.message_from_bytes(response_part[1])

				
				# set 'subject', 'from', 'to'
				to_decode = decode_header(msg['subject'])
				title = decode_if_byte(to_decode[0][0], to_decode[0][1])
				to_decode = decode_header(msg['from'])
				from_ = decode_if_byte(to_decode[0][0], to_decode[0][1])
				to_decode = decode_header(msg['to'])
				to = decode_if_byte(to_decode[0][0], to_decode[0][1])
				to_decode = decode_header(msg['date'])
				mail_date = decode_if_byte(to_decode[0][0], to_decode[0][1])
				inner_text = decode_if_byte(get_text(msg), 'utf-8')
				
		# download attachment
		attachment = []
		
		for part in msg.walk():
			if part.get_content_maintype() == 'multipart':
				continue
			path = "./attachment/"
			filename = part.get_filename()
			if filename: # when there is attachment
				with open(os.path.join(path, filename), 'wb') as fp:
					attachment.append(path+filename)
					fp.write(part.get_payload(decode=True))	

		mail_one = Mail(from_, to, mail_date, title, inner_text, attachment)
		mailList.append(mail_one)
		mail_one.printStatus()

if __name__ == "__main__":
	main()