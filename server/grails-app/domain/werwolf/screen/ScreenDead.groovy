package werwolf.screen

class ScreenDead extends ScreenStatic {

    @Override
    String getMessage() { 'You are dead'  }

    @Override
    String getKey() {
        return 'ready'
    }

}
