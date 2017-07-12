from slacker import Slacker

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

    def sendPlainMessage(self, _channel, _title, _text):
        self.slacker.chat.post_message(_channel, _title, _text)

token = 'xoxb-210760362642-wnqRaOX86vYOuz3rpbMOVC02'

slackBot = SlackBot(token)

