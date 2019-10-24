import React, { Component } from 'react'
import {User} from './Person'

interface Message {
    text: string;
    sender: User;
}

export interface Chat {
    id: number;
    name: string;
    messages: Message[];
}

class ChatInput extends Component<{},{}> {

    render() {
        return (
            <input placeholder='Message' type='text'></input>
        );
    }

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

export class ChatComponent extends Component<{token: string, chat: Chat, visible: boolean, panel: {back: () => void}},{}> {

    render() {
        const {chat, token, visible, panel} = this.props;
        const {name} = chat;

        const hans: User = {name: 'Hans', id: 1, token: token};
        const peter: User = {name: 'Peter', id: 2};

        const messages: Message[] = [
            {text: 'Ja ne chat kommt noch', sender: hans},
            {text: 'Das ist ja cool', sender: peter},
            {text: 'Danke ich weiß', sender: hans},
        ];

        return (
            <div className={`chat-panel ${visible ? 'visible' : ''}`}>
                <p><a onClick={() => panel.back()}>Back</a></p>
                {messages.map((msg, i) => <MessageBubble key={i} message={msg} isSender={msg.sender.token == token} />)}
                <ChatInput />
            </div>
        );
    }

}

export class ChatPanel extends Component<{token: string, chats: Chat[]},{selected?: number}> {

    constructor(props: {token: string, chats: Chat[]}) {
        super(props);
        this.state = {selected: undefined}
    }

    select(selected?: number) {
        this.setState({ selected })
    }

    back() {
        this.select();
    }

    render() {
        const {token, chats} = this.props;
        const {selected} = this.state;

        return (
            <>
            <div className={`chat-panel ${selected ? '' : 'visible'}`}>
                {chats.map((chat, i) => 
                    <p><a onClick={() => this.select(chat.id)}>{chat.name}</a></p>
            )}
            </div>
            {chats.map((chat, i) => <ChatComponent visible={selected == chat.id} token={token} key={i} chat={chat} panel={this} />)}
            </>
        );
    }

}

export default ChatPanel;