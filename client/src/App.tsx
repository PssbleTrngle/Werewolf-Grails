import React, { Component } from 'react'
import { Row } from 'reactstrap'

import {CLIENT_VERSION, REACT_VERSION, SERVER_URL} from './config'
import 'whatwg-fetch'
import JWT from 'jsonwebtoken'

import { Person, User } from './Person'
import { Game, NoGame, GameState, GamePreview } from './Game'
import { ChatPanel, Chat } from './Chat'

const UPDATE = 1000;

class Nav extends Component<{tabs: string[], active: string},{}> {

    render() {
        const {tabs, active} = this.props;

        return (
            <nav>
                <div className='nav nav-tabs nav-fill' id='nav-tab' role='tablist'>
                    {tabs.map((tab, i) => 
                        <a
                            key={tab}
                            className={'nav-item nav-link' + (active == tab ? ' active' : '')}
                            id={`nav-${tab}-tab`}
                            data-toggle="tab"
                            href={`#${tab}`}
                            role="tab"
                            aria-controls={`${tab}`}
                            aria-selected={active == tab}
                        >{tab}</a>
                    )}
                </div>
            </nav>
        );
    }

}

export interface GameApp {
    send(action: string, params: any): void;
    updateData(): void;
    token(): string | null;
}

type AppState = {user?: User, game?: GameState, chats: Chat[], games?: GamePreview[]};
class App extends Component<{token?: string},AppState> {

    initialState: AppState = {user: undefined, game: undefined, chats: [], games: undefined};

    token(): string | null {
        return this.props.token || localStorage.getItem('token');
    }

    constructor(props: any) {
        super(props)
        this.state = this.initialState;
    }

    updateData() {
        let token = this.token();
        if(token) 
            fetch(SERVER_URL + `/game?token=${token}`)
                .then(r => r.json())
                .then(json => this.setState(json))
                .catch(error => { this.setState(this.initialState)});
    }

    componentDidMount() {

        let update = () => {
            this.updateData();
            window.setInterval(() => this.updateData(), UPDATE);
        }

        if(this.token())
            update();
        else
            JWT.sign({ tam: 'tam' }, 'tamtam', {}, (err, token) => {
            localStorage.setItem('token', token);
        })
    }

    send(action: string, params: any, callback?: ((result: any) => void)) {
        const token = this.token();
        if(token) {
            params.token = token;
            fetch(SERVER_URL + '/' + action, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(params)
            })
            .then(r => r.json())
            .then(json => { 
                if(callback) callback(json);
                this.updateData();
            });
        }
    }

    login(user: string) {
        this.send('user', {user}, json => {
            if(json.token)
                localStorage.setItem('token', json.token);
        });
    }

    render() {
        const {user, game, chats, games} = this.state;

        if(user) document.title = `Werewolf - ${user.name}`;

        const panels: any = {};

        if(user) panels.sidebar = <Sidebar app={this} game={game} user={user} />
        panels.game = (game ? <Game app={this} game={game} /> : <NoGame games={games || []} user={user} app={this} />)
        if(chats.length > 0) panels.chat = (<ChatPanel token={this.token()} app={this} chats={chats || []}/>)

        let active = 'game';
        /* TODO This is aweful remove */
        let size: any = {game: chats.length ? 7 : (user ? 8 : 12), chat: chats.length ? 3 : 2, sidebar: 2};

        return (
            <div className={'h-100 ' + ((!game || game.night) ? 'night' : 'day')}>
                {game && <Nav tabs={Object.keys(panels)} active={active} />}
                <div className='row justify-content-center tab-content'>
                    {Object.keys(panels).map(id => 
                        <div key={id} id={`${id}`} role="tabpanel" className={`tab-pane col-${size[id]} ${id == active ? 'active' : ''}`}>
                            {panels[id]}
                        </div>
                    )}
                </div>
            </div>
        );
    }
}

class Sidebar extends Component<{game?: GameState, user?: User, app: {login(user: string): void} & Component},{}> {

    render() {
        const {user, game, app} = this.props;

        return (
            <>
            { user && <Person showRole={true} user={user} /> }
            { user && game && user.token == 'banana' && 
                <select defaultValue={user.name} onChange={(e) => app.login(e.target.value)}>
                    {game.users.map(user => {
                        return <option key={user.id}>{user.name}</option>
                    })}
                </select>
            }
            </>
        );
    }

}

export default App;
