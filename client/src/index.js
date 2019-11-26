import React from 'react';
import ReactDOM from 'react-dom';
import App from './App.tsx';
import 'bootstrap/dist/css/bootstrap.css';
import './css/main.css';

import {DEV} from './config'

import './css/Chat.css';
import './css/App.css';
import './css/Game.css';
import './css/Sidebar.css';

let search = window.location.search;
let params = new URLSearchParams(search);
let token = DEV ? params.get('token') : undefined;

ReactDOM.render(<App token={token} />, document.getElementById('root'));