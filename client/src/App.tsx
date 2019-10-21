import React, { Component } from 'react'
import { Row } from 'reactstrap'

import {CLIENT_VERSION, REACT_VERSION, SERVER_URL} from './config'
import 'whatwg-fetch'
import JWT from 'jsonwebtoken'

import { Person, User } from './Person'
import { Game, NoGame, GameState } from './Game'

class App extends Component<{token?: string},{user?: User, game?: GameState}> {

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
                .then(json => this.setState({game: json.game, user: json.user}))
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
        const {user, game} = this.state;

        return (
            <>
            <div className='bg'></div>
            <div className='row container justify-content-center'>
                <Sidebar app={this} game={game} user={user} />
                { game && <Game app={this} game={game} /> }
                { !game && <NoGame /> }
            </div>
            </>
        );
    }
}

class Sidebar extends Component<{game?: GameState, user?: User, app: {login(user: string): void} & Component},{}> {

    render() {
        const {user, game, app} = this.props;

        return (
            <div className='col-auto sidebar'>
                { user && <Person showRole={true} user={user} /> }
                { user && game && 
                    <select defaultValue={user.name} onChange={(e) => app.login(e.target.value)}>
                        {game.users.map(user => {
                            return <option key={user.id}>{user.name}</option>
                        })}
                    </select>
                }
            </div>
        );
    }

}

export default App;