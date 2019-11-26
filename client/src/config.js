import pjson from './../package.json';

export const SERVER_URL = 'http://werwolf.somethingcatchy.net:8080/';
export const CLIENT_VERSION = pjson.version;
export const REACT_VERSION = pjson.dependencies.react;
export const UPDATE_TIME = 1000;
export const DEV = true; /* App used in dev mode */