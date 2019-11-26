import React, { Component } from 'react'
import { Row } from 'reactstrap'

import {CLIENT_VERSION, REACT_VERSION, SERVER_URL, UPDATE_TIME, DEV} from './config'
import 'whatwg-fetch'
import JWT from 'jsonwebtoken'

import { User } from './components/Person'
import { Game, NoGame, GameState, GamePreview } from './components/Game'
import { ChatPanel, Chat } from './components/Chat'
import { Nav } from './components/Nav';
import { Sidebar } from './components/Sidebar';

export interface GameApp {
    send(action: string, params: any): void;
    updateData(): void;
    token(): string | null;
}

type AppState = {user?: User, game?: GameState, chats: Chat[], games?: GamePreview[]};
class App extends Component<{token?: string},AppState> {

    initialState: AppState = {user: undefined, game: undefined, chats: [], games: undefined};

    /**
     * @returns A JWT generated User token for authentification purposes 
     */
    token(): string | null {
        return this.props.token || localStorage.getItem('token');
    }

    constructor(props: any) {
        super(props)
        this.state = this.initialState;
    }

    /**
     * Fetches data from the server to update the appstate
     */
    updateData() {
        let token = this.token();
        if(token) 
            fetch(SERVER_URL + `/game?token=${token}`)
                .then(r => r.json())
                .catch(e => this.initialState)
                .then(state => this.setState(state))
    }

    /**
     * Called after initial rendering
     * Starts an interval calling the [[updateData]] function
     */
    componentDidMount() {

        /* TODO pause when tab loses focus */
        let update = () => {
            this.updateData();
            window.setInterval(() => this.updateData(), UPDATE_TIME);
        }

        if(this.token())
            update();
        else {
            /* Create a token and store it in the browsers localStorage */
            JWT.sign({ tam: 'tam' }, 'tamtam', {}, (_err: any, token: string) => {
                localStorage.setItem('token', token);
            })
        }
    }

    /**
     * Send a post request to the server
     * @param action the post uri
     * @param params the json params
     * @returns The JSON response
     */
    async send(action: string, params: any): Promise<any> {
        const token = this.token();
        if(token) {

            params.token = token;
            const result = await fetch(SERVER_URL + '/' + action, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(params)
            });

            const json = await result.json();
            this.updateData();
            return json;
        }
    }

    /**
     * Notifies the server of the created user
     * @param user The created user
     */    
    login(user: string) {
        this.send('user', {user})
            .then(json => {
                if(json.token)
                    localStorage.setItem('token', json.token);
            });
    }

    /**
     * Creates the visible panels for the currently available information
     */
    createPanels(): {key?: string, element: JSX.Element, size?: number}[] {
        const {user, game, chats, games} = this.state;

        const panels = [];

        if(user) panels.push({key: 'sidebar', size: 2, element: <Sidebar app={this} game={game} user={user} />});
        
        if(game) panels.push({key: 'game', element: <Game app={this} game={game} />});
        else panels.push({key: 'game', element: <NoGame games={games || []} user={user} app={this} />});
        
        if(chats.length > 0) panels.push({key: 'chat', size: 3, element: <ChatPanel token={this.token()} app={this} chats={chats || []}/>});
        else panels.push({size: 2, element: <div />});

        const sizes: any[] = panels.map(p => p.size).filter(s => s);
        const total = sizes.reduce((a, b) => a + b);
        const autos = panels.filter(p => !p.size);
        const size = Math.floor((12 - total) / autos.length);
        autos.forEach((_, i) => autos[i].size = size);

        return panels;
    }

    render() {
        const {user, game} = this.state;

        /* Show the username in the browser tab title */
        if(user) document.title = `Werewolf - ${user.name}`;
        
        const panels = this.createPanels();
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

export default App;
