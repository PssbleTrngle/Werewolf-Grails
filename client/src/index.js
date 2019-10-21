import React from 'react';
import ReactDOM from 'react-dom';
import App from './App.tsx';
import 'bootstrap/dist/css/bootstrap.css';
import './css/main.css';
import './css/App.css';

let search = window.location.search;
let params = new URLSearchParams(search);
let token = params.get('token');

ReactDOM.render(<App token={token} />, document.getElementById('root'));