package werwolf


import grails.rest.*
import grails.converters.*

class ChatController {
    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: 'POST']
	
    /**
    *    Handles POST request to /chat
    *    Called when a user sends a message
    */
    def save() {
        def json = request.getJSON()
        String token = json['token']
        String chatID = json['chat']
        String text = json['text']
        User user = UserController.getUser(token)
        Chat chat = Chat.get(chatID)

        assert user != null : 'User missing'
        assert chat != null : 'No chat selected'
        assert chat.getUsers().find({ it.token == token }) : 'User is not part of this chat'

        /* The message contains more than whitespace characters */
        assert text != null && text.replaceAll(/\s*/, "").length() > 0 : 'Invalid message'
        /* Replace whitespace at the beginning and the end of the message */
        text = text.replaceAll(/(^\s+)|(\s+$)/, "")

        Chat.withTransaction({
            Message msg = new Message(text: text, sender: user)
            msg.save()
            chat.addToMessages(msg)
            chat.save()
        })

        [success: true]
    }
}