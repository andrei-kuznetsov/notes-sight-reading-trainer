package name.kuznetsov.andrei.scoresightreading.model;

/**
 *
 * @author andrei
 */
public enum NotesEnum {

    C, D, E, F, G, A, H;

    public int getMidiPitch(){
        switch (this){
            case C:
                return 0;
            case D:
                return 2;
            case E:
                return 4;
            case F:
                return 5;
            case G:
                return 7;
            case A:
                return 9;
            case H:
            default:
                return 11;
        }
    }
}
