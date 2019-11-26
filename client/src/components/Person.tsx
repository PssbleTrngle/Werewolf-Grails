import React, { Component } from 'react';

export interface User {
	name: string;
	id: number;
	role?: {
		name: string
	};
	token?: string
	dead?: boolean
}

type PersonProps = {
	user?: User,
	click?: ((event: React.MouseEvent) => void),
	showRole?: boolean,
	vote?: string,
	votes?: string[],
}

type size = 'big' | 'small' | 'tiny';
export class People extends Component<{users: User[], size: size, action?: string, app: GameApp, decisions?: Decision[], voters?: boolean},{}> {

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

export class Person extends Component<PersonProps,{}> {

	render() {
		const {user, click, showRole, vote, votes} = this.props;
		if(!user) return null;

		let icon = require(`./images/roles/villager.svg`)
		if(user.role) try {
			icon = require(`./images/roles/${user.role.name.toLowerCase()}.svg`);
		} catch {};

		return (
			<div onClick={click} className={'person' + (click ? ' clickable' : '') + (user.dead ? ' dead' : '')}>
				<img src={icon}></img>
				<p>{user.name}</p>

				{vote && <Vote vote={vote} />}
				{votes && votes.map(v => <Voted key={v} by={v} />)}

				{showRole && user.role && <p className='text-capitalize'>{user.role.name}</p>}
			</div>
		);
	}

}

class Voted extends Component<{by: string},{}> {

	render() {
		const {by} = this.props;

		return (
			<div className="vote">
				<span className='vote-tooltip'>{by}</span>
			</div>
		);
	}
}

class Vote extends Component<{vote: string},{}> {

	render() {
		const {vote} = this.props;

		if(!isNaN(parseInt(vote))) return null;

		if(vote.toLowerCase() == 'yes')
			return (<span className="badge badge-success">Yes</span>);

		if(vote.toLowerCase() == 'no')
			return (<span className="badge badge-danger">No</span>);

		return (<p>{vote}</p>);
	}

}

export default Person;