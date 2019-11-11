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