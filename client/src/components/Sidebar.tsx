import React, { Component } from 'react';
import { Person, User } from './Person';
import { GameState } from './Game';

export class Sidebar extends Component<{game?: GameState, user?: User, app: {login(user: string): void} & Component},{}> {

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