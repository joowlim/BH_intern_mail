import imaplib
import email
from email.header import decode_header
import base64
import os, mimetypes

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

# START

# login
mail = imaplib.IMAP4_SSL('imap.gmail.com')
mail.login('dnflsmsdlsxjs@gmail.com','xmfnqoffjstm')
mail.list()
mail.select('inbox', readonly=True)

# get list of messages in inbox
result, data = mail.search(None, "ALL")
print (data)
messageList = map(lambda x: int(x), data[0].split())

for i in messageList: # messages I want to see 
	typ, msg_data = mail.fetch(str(i), '(RFC822)')
	for response_part in msg_data:
		if isinstance(response_part, tuple):
			msg = email.message_from_bytes(response_part[1])

	   	    # parse and print header
			for header in ['Subject', 'From', 'To']:
				print(header + " : ", end = "")
				to_decode = decode_header(msg[header])
				print(decode_if_byte(to_decode[0][0], to_decode[0][1]))
			# print message
			print("Message : ")
			# print(decode_if_byte(get_text(msg), 'utf-8'))
			print("-----------------------------------------------------------")

	# download attatchment
	counter = 1
	for part in msg.walk():
		if part.get_content_maintype() == 'multipart':
			continue
		filename = part.get_filename()
		if filename: # when there is attachment
			with open(os.path.join("./attachment", filename), 'wb') as fp:
				fp.write(part.get_payload(decode=True))	
	