package werwolf

class GameController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        String token = params['token']
        assert token != null

        User user = UserController.getUser(token)
        [user: user, game: user?.getGame()]
    }
}
