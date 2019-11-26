import React, { Component } from 'react'
import {GameApp} from '../App'
import {User} from './Person'

interface Message {
    text: string;
    sender?: User;
}

export interface Chat {
    id: number;
    name: string;
    messages: Message[];
    users: User[];
}

class ChatInput extends Component<{app: GameApp, chat: number},{text?: string}> {

    constructor(props: any) {
        super(props);
        this.state = {}
    }

    /**
     * Send the entered message
     */
    send() {
        let {app, chat} = this.props;
        const {text} = this.state;

        if(text) {
            app.send('chat', {text, chat});
            this.setState({text: ''});
        }
    }

    /**
     * Called every time the input value is changed
     * Can also be used to set the input to a specific value
     * @param text the new text 
     */
    change(text: any) {
        this.setState({text});
    }

    render() {
        const {text} = this.state;

        return (
            <form onSubmit={(e) => {this.send(); e.preventDefault(); }}>
                <input onChange={(e) => this.change(e.target.value)} placeholder='Message' type='text' value={text}></input>
            </form>
        );
    }

}

class MessageBubble extends Component<{message: Message, isSender: boolean},{}> {

    render() {
        const {message, isSender} = this.props;
        const {sender} = message;

        return (
            <div className={'message-bubble' + (isSender ? ' sender' : '')}>
                <span>{message.text}</span>
                <span className='sender'>{sender ? sender.name : 'Anonymous'}</span>
            </div>
        );
    }

}

export class ChatComponent extends Component<{chat: Chat, visible: boolean, app: GameApp, panel: {back: () => void}},{}> {

    render() {
        const {chat, visible, panel, app} = this.props;
        const {name, messages, users} = chat;
        const token = app.token();

        return (
            <div className={`chat-panel ${visible ? 'visible' : ''}`}>
                <p className='chat-back' onClick={() => panel.back()}>Back</p>
                <p className='chat-users'>{users.map((u, i) => 
                    <span key={i} className='chat-user'>{u.name}</span>
                )}</p>
                <div className='messages'>
                    {messages.map((msg, i) => <MessageBubble key={i} message={msg} isSender={msg.sender != undefined && msg.sender.token == token} />)}
                </div>
                <ChatInput chat={chat.id} app={app} />
            </div>
        );
    }

}

type PanelProps = {app: GameApp, token: string | null, chats: Chat[]};
export class ChatPanel extends Component<PanelProps,{selected?: number}> {

    constructor(props: any) {
        super(props);
        this.state = {};
    }

    select(selected?: number) {
        this.setState({ selected })
    }

    back() {
        this.select();
    }

    shouldComponentUpdate(next: PanelProps): boolean {
        if(next.token != this.props.token) this.select();
        return true;
    }

    render() {
        const {app, chats} = this.props;
        const {selected} = this.state;

        return (
            <>
            <div className={`chat-panel ${selected ? '' : 'visible'}`}>
                {chats.map((chat) => 
                    <p key={chat.id} className='chat-name' onClick={() => this.select(chat.id)}>{chat.name}</p>
                )}
            </div>
            {chats.map((chat) => <ChatComponent app={app} visible={selected == chat.id} key={chat.id} chat={chat} panel={this} />)}
            </>
        );
    }

}

export default ChatPanel;