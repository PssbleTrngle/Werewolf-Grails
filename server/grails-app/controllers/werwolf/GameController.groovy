package werwolf

class GameController {
	static responseFormats = ['json', 'xml']
    static allowedMethods = [index: 'GET', save: 'POST']
	
    def index() {
        String token = params['token']
        assert token != null

        User user = UserController.getUser(token)
        [user: user, game: user?.getGame()]
    }

    void createGame(User user) {
        Game.withTransaction({

            println "Creating Game for User $user.token"
            Game game = Game.createGame(user)
            game.save()

        })
    }

    void joinGame(User user, int gameID) {
        Game game = Game.get(gameID)
        assert game != null

        Game.withTransaction({
            game.join(user)
        })
    }

    def save() {
        def json = request.getJSON()
        String action = json['action']
        String token = json['token']
        User user = UserController.getUser(token)

        assert action != null
        assert user != null : 'Not logged in'

        switch(action) {
            case 'create':
                createGame(user)
                break
            case 'join':
                int gameID = json['game']
                joinGame(user, gameID)
                break
        }

        [ success: true ]
    }

}
