package werwolf

class Vote {

    int id
    String result
    boolean done = false
    boolean anonymous = false

    String action

    boolean isStatic() {
        Action action = action()
        boolean noTargets = true

        if(action) users.each({ User voter ->
            noTargets = noTargets && action.getTargets(voter?.game?.users, voter).isEmpty()
        })

        action == null || (action.options.length == 0 && noTargets)
    }

    Action action() {
        Action.get(action)
    }

    def calculateResult() {

        Map<User, Integer> targets = [:]
        Map<String, Integer> options = [:]

        decisions.each({ Decision decision ->
            if(decision.target) targets.put(decision.target, targets.getOrDefault(decision.target, 0) + 1)
            else if(decision.selection != 'skip') options.put(decision.selection, options.getOrDefault(decision.selection, 0) + 1)
        })

        Map.Entry<User, Integer> target = targets.isEmpty() ? null : targets.max({ it.value })
        Map.Entry<String, Integer> option = options.isEmpty() ? null : options.max({ it.value })

        return target?.value > option?.value ? target?.key : option?.key
    }

    void checkDone() {
        Set<User> notVoted = this.users.findAll({ User voter ->
            !decisions.find({ Decision decision -> decision.user.id == voter.id })
        })

        if(notVoted.isEmpty()) {
            User.withTransaction({

                this.users.each({ User voter -> voter.screen = null })
                this.users.each({ User voter ->
                    if (voter.screen == null) {
                        Action next = action().nextAction(voter)

                        Vote screen = null
                        next.getVoters(voter.game.users, voter).each({ other ->
                            if(other.screen?.action == next.name)
                                screen = other.screen
                        })

                        if(!screen) screen = new Vote(action: next.name, game: voter.game).save()
                        voter.setProperty('screen', screen)
                        voter.save()
                    }
                })
            })
        }
    }

    static belongsTo = [ game: Game ]
    static hasMany = [ decisions: Decision, users: User ]

    static constraints = {
        result nullable: true
        result blank: false
    }

    static mapping = {
        decisions cascade: 'delete'
    }
}