import React, { Component } from 'react';
import {CLIENT_VERSION, REACT_VERSION, SERVER_URL} from './config'

import { Person, User } from './Person'

interface Decision {
	selection: any,
	user?: User,
}

interface Screen {
	message: string,
	action: string,
	targets: User[],
	voters: User[],
	options: string[],
	result?: string,
	decisions?: Decision[],
}

export interface GameState {
	users: User[];
	screen: Screen;
	night: boolean;
}

interface App {
	sendAction(params: Object): void
}

type size = 'big' | 'small' | 'tiny';
class People extends Component<{users: User[], size: size, action?: string, app?: App, decisions?: Decision[], voters?: boolean},{}> {

	render() {
		const {users, size, action, app, decisions, voters} = this.props;

		let people = users.map(user => {

			let click = !action ? undefined : () => {
				let target = user.id;
				if(app) app.sendAction({ action, target, })
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

class ScreenComponent extends Component<{screen: Screen, app?: App},{}> {

	render() {
		const {message, action, options, targets, voters, result, decisions} = this.props.screen;
		const {app} = this.props;

		return (
			<>
			<div className='screen mb-5'>
				<h1 className='text-center screen-message'>{message}</h1>
				<div className='row justify-content-center'>
					{options.map(option =>	{
						let click = () => {
							if(app) app.sendAction({ action, option })
						};
						return <button onClick={click} key={option} className='option'>{option}</button>
					})}
				</div>
			</div>
			<People app={this.props.app} size={'small'} users={targets} action={action} decisions={decisions} />
			{ voters.length > 1 && <People app={this.props.app} size={'tiny'} users={voters} decisions={decisions} voters={true} /> }
			</>
		);
	}

}

export class Game extends Component<{game: GameState, app: App},{}> {

	render() {
		const {screen, users, night} = this.props.game;

		let delay = 0;

		return (
			<div className={'game' + (night ? ' night' : ' day')}>
				<div className='sky-container'>
					<div className='sky'></div>
					<div className='orbits'></div>
					<div className='clouds'>
					{[0,1,2,3,4].map(i => 
						<div className='cloud' key={i} style={{ 
							animationDelay: (delay += (i % 3) + (i % 4 == 0 ? 2 : 0)) + 's',
							top: (i * 17) % 10 * 5 + 13
						}}></div>
					)}
					</div>
				</div>
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

export class NoGame extends Component<{},{}> {

	render() {
		return (
			<div className='game night'>
				<Icon />
				<h1 className='loading'>Loading</h1>
			</div>
		)

	}

}

export default Game;