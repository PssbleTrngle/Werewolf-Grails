import React, { Component } from 'react';
import {CLIENT_VERSION, REACT_VERSION, SERVER_URL} from './config'

import { Person, User } from './Person'
import { GameApp } from './App'

interface Decision {
	selection: any,
	user?: User,
}

interface Screen {
	message: string,
	action?: string,
	targets: User[],
	voters: User[],
	options: string[],
	result?: string,
	decisions?: Decision[],
}

export interface GameState {
	users: User[];
	screen?: Screen;
	night: boolean;
}

type size = 'big' | 'small' | 'tiny';
class People extends Component<{users: User[], size: size, action?: string, app: GameApp, decisions?: Decision[], voters?: boolean},{}> {

	render() {
		const {users, size, action, app, decisions, voters} = this.props;

		let people = users.map(user => {

			let click = !action ? undefined : () => {
				let target = user.id;
				app.send('action', { action, target, })
			};

			let out = [ <Person key={user.id} click={click} user={user} /> ];

			let vote = undefined;
			let votes: string[] = [];

			if(decisions) {

				if(voters) {
					let decision = decisions.find(d => {
						return d.user && d.user.id == user.id;
					});
					vote = decision ? decision.selection : undefined;
				} else {
					votes = decisions.filter(d => {
						let selection = parseInt(d.selection)
						return !isNaN(selection) && selection == user.id;
					}).map(d => d.user ? d.user.name : '???')
				}

			}

			return (<Person key={user.id} click={click} user={user} vote={vote} votes={votes} />);
		})

		return (
			<div className={'row justify-content-center ' + (size ? size : '')}>
				{people}
			</div>
		)
	}

}

class ScreenComponent extends Component<{screen: Screen, app: GameApp},{}> {

	render() {
		const {action, options, targets, voters, result, decisions} = this.props.screen;
		const {app} = this.props;

		return (
			<div className='screen pt-4'>
				{result && <h1 className='result'>{result}</h1>}
				{options.length > 0 &&<div className='row justify-content-center mb-5'>
					{options.map(option =>	{
						let click = () => {
							app.send('action', { action, option })
						};
						return <button onClick={click} key={option} className='option'>{option}</button>
					})}
				</div>}
			<People app={this.props.app} size={'small'} users={targets} action={action} decisions={decisions} />
			{ voters.length > 1 && <People app={this.props.app} size={'tiny'} users={voters} decisions={decisions} voters={true} /> }
			
			</div>
		);
	}

}

class Sky extends Component<{message: string},{}> {

	render() {
		const {message} = this.props;
		let delay = 0;

		return (
			<div className='sky-container'>
				<div className='sky'>
					<h1 className='text-center screen-message'>{message}</h1>
				</div>
				<div className='orbits'></div>
				<div className='clouds'>
				{[0,1,2,3,4].map(i => 
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
		let {screen, users, night} = this.props.game;

		if(!screen) screen = {
			message: 'Waiting for Players',
			targets: [],
			voters: users,
			options: [],
			result: `${users.length}/5`,
		}

		return (
			<div className={'game-container ' + (night ? 'night' : 'day')}>
				<Sky message={screen.message} />
				<ScreenComponent app={this.props.app} screen={screen} />
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

	createGame(dev: boolean = false) {
		this.props.app.send('game', { action: 'create', dev });
	}

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
			<div className='game-container loading night'>
				<Icon />
				<p className='loading'>Loading</p>
			</div>
		);

	}

}

export default Game;