package werwolf


import grails.rest.*
import grails.converters.*

class ViewController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        [usercount: Role.count()]
    }
}
