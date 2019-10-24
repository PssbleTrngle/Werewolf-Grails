import React, { Component } from 'react'
import { Row } from 'reactstrap'

import {CLIENT_VERSION, REACT_VERSION, SERVER_URL} from './config'
import 'whatwg-fetch'
import JWT from 'jsonwebtoken'

import { Person, User } from './Person'
import { Game, NoGame, GameState } from './Game'
import { ChatPanel, Chat } from './Chat'

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

class App extends Component<{token?: string},{user?: User, game?: GameState, chats?: Chat[]}> {

    token(): string | null {
        return localStorage.getItem('token');
    }

    constructor(props: {token?: string}) {
        super(props)
        if(props.token) localStorage.setItem('token', props.token);
        this.state = {}
    }

    updateData() {
        let token = this.token();
        if(token)
            fetch(SERVER_URL + `/game?token=${token}`)
                .then(r => r.json())
                .then(json => this.setState(json))
                .catch(error => {});
    }

    componentDidMount() {

        let update = () => {
            this.updateData();
            window.setInterval(() => this.updateData(), 2000);
        }

        if(this.token())
            update();
        else
            JWT.sign({ tam: 'tam' }, 'tamtam', {}, (err, token) => {
            localStorage.setItem('token', token);
        })
    }

    sendAction(params: any) {
        let token = this.token();
        if(token) {
            params.token = token;
            fetch(SERVER_URL + '/action', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(params)
            })
            .then(r => r.json())
            .then(json => { this.updateData() });
        }
    }

    login(user: string) {
        fetch(SERVER_URL + '/user', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ user })
        })
        .then(r => r.json())
        .then(json => { 
            if(json.token)
                localStorage.setItem('token', json.token)
            this.updateData()
        });
    }

    render() {
        const {user, game, chats} = this.state;
        const token = this.token();

        const panels: any = {sidebar: <Sidebar app={this} game={game} user={user} />};

        panels.game = (game ? <Game app={this} game={game} /> : <NoGame />)
        if(token && chats) panels.chat = (<ChatPanel chats={chats} token={token}/>)

        let active = 'game';

        return (
            <>
            <Nav tabs={Object.keys(panels)} active={active} />
            <div className='row justify-content-center tab-content'>
                {Object.keys(panels).map(id => 
                    <div key={id} id={`${id}`} role="tabpanel" className={`tab-pane col-auto ${id == active ? 'active' : ''}`}>
                        {panels[id]}
                    </div>
                )}
            </div>
            </>
        );
    }
}

class Sidebar extends Component<{game?: GameState, user?: User, app: {login(user: string): void} & Component},{}> {

    render() {
        const {user, game, app} = this.props;

        return (
            <>
            { user && <Person showRole={true} user={user} /> }
            { user && game && 
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
