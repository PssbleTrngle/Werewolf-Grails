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

        const panels: {key?: string, element: JSX.Element, size?: number}[] = [];

        if(user) panels.push({key: 'sidebar', size: 2, element: <Sidebar app={this} game={game} user={user} />});
        
        if(game) panels.push({key: 'game', element: <Game app={this} game={game} />});
        else panels.push({key: 'game', element: <NoGame games={games || []} user={user} app={this} />});
        
        if(chats.length > 0) panels.push({key: 'chat', size: 3, element: <ChatPanel token={this.token()} app={this} chats={chats || []}/>});
        else panels.push({size: 2, element: <div />});

        const sizes: any[] = panels.map(p => p.size).filter(s => s);
        const total = sizes.reduce((a, b) => a + b);
        const autos = panels.filter(p => !p.size);
        const size = Math.floor((12 - total) / autos.length);
        autos.forEach((p, i) => autos[i].size = size);

        let active = 'game';
        const tabs: any[] = panels.filter(p => p.key !== undefined).map(p => p.key);

        return (
            <div className={'h-100 ' + ((!game || game.night) ? 'night' : 'day')}>
                {game && <Nav tabs={tabs} active={active} />}
                <div className='row justify-content-center tab-content'>
                    {panels.map(panel => 
                        <div key={panel.key} id={panel.key} role="tabpanel" className={`tab-pane col-${panel.size} ${panel.key == active ? 'active' : ''}`}>
                            {panel.element}
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
