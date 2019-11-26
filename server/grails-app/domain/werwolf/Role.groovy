package werwolf

abstract class Role {

    /**
     * @return boolean: true if death should be canceled
     */
    boolean onDeath(User user) { false }

    int id
    String name

    Screen nightScreen() { null }

    /*
    *   Some roles win if the game ends and a certain condition is met, but will not end the game on their own
    *   Therefor these two situtations are split into two methods
    */
    abstract boolean triggerWin(Game game);
    boolean hasWon(Game game) { triggerWin(game) }

}
