import React, { Component } from 'react';

export class Nav extends Component<{tabs: string[], active: string},{}> {

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