import React, { Component } from 'react';
import { GameApp } from '../App';
import { User, People } from './Person'

export interface Decision {
	selection: any,
	user?: User,
}

export interface IScreen {
	message: string,
	action?: string,
	targets: User[],
	voters: User[],
	options: string[],
	decisions?: Decision[],
}

export class Screen extends Component<{screen: IScreen, app: GameApp},{}> {

	render() {
		const {action, options, targets, voters, decisions} = this.props.screen;
		const {app} = this.props;

		return (
			<div className='screen pt-4'>
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