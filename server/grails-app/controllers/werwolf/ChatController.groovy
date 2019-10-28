package werwolf


import grails.rest.*
import grails.converters.*

class ChatController {
    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: 'POST']
	
    def save() {
        def json = request.getJSON()
        String token = json['token']
        String chatID = json['chat']
        String text = json['text']
        User user = UserController.getUser(token)
        Chat chat = Chat.get(chatID)

        assert user != null
        assert chat != null
        assert chat.getUsers().find({ it.token == token })

        assert text != null && text.replaceAll(/\s*/, "").length() > 0
        text = text.replaceAll(/^\s*/, "")

        Chat.withTransaction({
            Message msg = new Message(text: text, sender: user)
            msg.save()
            chat.addToMessages(msg)
            chat.save()
        })

        [success: true]
    }
}
