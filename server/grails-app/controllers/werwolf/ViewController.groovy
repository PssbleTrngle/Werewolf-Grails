package werwolf

class ViewController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        [usercount: User.count()]
    }
}
