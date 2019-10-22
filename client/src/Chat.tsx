import React, { Component } from 'react'
import {User} from './Person'

interface Message {
    text: string;
    sender: User;
}

class MessageBubble extends Component<{message: Message, isSender: boolean},{}> {

    render() {
        const {message, isSender} = this.props;

        return (
            <div className={'message-bubble' + (isSender ? ' sender' : '')}>
                <span>{message.text}</span>
                <span className='sender'>{message.sender.name}</span>
            </div>
        );
    }

}

export class Chat extends Component<{token: string},{}> {

    render() {
        const {token} = this.props;

        const hans: User = {name: 'Hans', id: 1, token: token};
        const peter: User = {name: 'Peter', id: 2};

        const messages: Message[] = [
            {text: 'Ja ne chat kommt noch', sender: hans},
            {text: 'Das ist ja cool', sender: peter},
            {text: 'Danke ich weiß', sender: hans},
        ];

        return (
            <div className='col-auto chat'>
                {messages.map((msg, i) => <MessageBubble key={i} message={msg} isSender={msg.sender.token == token} />)}
            </div>
        );
    }

}

export default Chat;