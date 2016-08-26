package name.kuznetsov.andrei.scoresightreading.model;

/**
 * @author andrei
 */
public class Note {

    private NotesEnum noteName;
    private int octave;
    private int modifier;
    private int voice;

    public Note(NotesEnum noteName) {
        this(noteName, 4);
    }

    public Note(NotesEnum noteName, int octave) {
        this(noteName, octave, 0);
    }

    public Note(NotesEnum noteName, int octave, int modifier) {
        this(noteName, octave, modifier, 0);
    }

    public Note(NotesEnum noteName, int octave, int modifier, int voice) {
        this.noteName = noteName;
        this.octave = octave;
        this.modifier = modifier;
        this.voice = voice;
    }

    public NotesEnum getNoteName() {
        return noteName;
    }

    public void setNoteName(NotesEnum noteName) {
        this.noteName = noteName;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public int getMidiPitch() {
        return 12 * (octave + 1) + noteName.getMidiPitch() + modifier;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    @Override
    public String toString() {
        String modifierStr = "";
        int x = modifier;
        while (x != 0) {
            if (x > 0) {
                modifierStr += "#";
                x--;
            } else {
                modifierStr += "b";
                x++;
            }
        }
        return "Note{" + noteName + modifierStr + octave + '}';
    }

}
