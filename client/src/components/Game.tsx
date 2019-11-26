import React, { Component } from 'react';

import { User } from './Person'
import { Screen, IScreen } from './Screen'

export interface GameState {
	users: User[];
	screen?: IScreen;
	night: boolean;
}

class Sky extends Component<{message: string},{}> {

	render() {
		const {message} = this.props;
		let delay = 0;

		return (
			<div>
				<div className='sky'>
					<h1 className='text-center screen-message'>{message}</h1>
					<div className='scenery'></div>
				</div>
				<div className='orbits'></div>
				<div className='clouds'>
				{new Array(5).fill(0).map((_, i) => 
					<div className='cloud' key={i} style={{ 
						animationDelay: ((delay += (i % 3) + (i % 4 == 0 ? 2 : 0))) * -1 + 's',
						top: (i * 17) % 10 * 5 + 13
					}}></div>
				)}
				</div>
			</div>
		);
	}

}

export class Game extends Component<{game: GameState, app: GameApp},{}> {

	render() {
		let {screen, users} = this.props.game;

		if(!screen) screen = {
			message: `Waiting for Players ${users.length}/5`,
			targets: [],
			voters: users,
			options: [],
		}

		return (
			<div className={'game-container'}>
				<Sky message={screen.message} />
				<Screen app={this.props.app} screen={screen} />
			</div>
		)

	}

}

export class Icon extends Component<{},{}> {

	render() {
		return (
			<div className='icon-sky'>
				<div className='loading-icon'>
					{[0,1,2,3,4].map(i => <div key={i} className='icon-cloud'></div>)}
				</div>
			</div>
		)

	}

}

export interface GamePreview {
	name: string;
	id: number;
	userCount: number;
}

export class NoGame extends Component<{user?: User, app: GameApp, games: GamePreview[]},{}> {

	/**
	 * Ask the server to create a new game
	 * @param dev Create a dev game (Currently ignored on server)
	 */
	createGame(dev: boolean = false) {
		this.props.app.send('game', { action: 'create', dev });
	}

	/**
	 * Ask the server to join a specific game
	 * @param game the game id
	 */
	joinGame(game: number) {
		this.props.app.send('game', { action: 'join', game });
	}

	render() {
		const { user, games } = this.props;

		if(user)
			return (
				<div className='game-container loading night'>
					<Icon />
					<div className='row justify-content-center mt-5'>
						<button onClick={() => this.createGame()} className='create-game'>Create Game</button>
						<button onClick={() => this.createGame(true)} className='create-game'>Create Dev Game</button>
					</div>

					<h1 className='mt-5'>Or join a game</h1>
					<div className='games mt-2'>{games.map(game => 
						<div onClick={(() => this.joinGame(game.id))} className='join' key={game.id}>
						<span>{game.name} {game.id}</span>
						<span className='badge badge-info'>{game.userCount}/5</span>
					</div>
					)}</div>
				</div>
			);

		return (
			<div className='game-container loading night row'>
				<div className='col align-self-center'>
					<Icon />
					<p className='loading'>Loading</p>
				</div>
			</div>
		);

	}

}

export default Game;