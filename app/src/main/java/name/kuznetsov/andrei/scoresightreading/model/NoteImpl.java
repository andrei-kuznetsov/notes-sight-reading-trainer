package name.kuznetsov.andrei.scoresightreading.model;

/**
 * @author andrei
 */
public class NoteImpl extends ObjectWithAttachableAttributesImpl implements Note {

    private NotesEnum noteName;
    private int octave;
    private int modifier;
    private int voice;

    public NoteImpl(NotesEnum noteName) {
        this(noteName, 4);
    }

    public NoteImpl(NotesEnum noteName, int octave) {
        this(noteName, octave, 0);
    }

    public NoteImpl(NotesEnum noteName, int octave, int modifier) {
        this(noteName, octave, modifier, 0);
    }

    public NoteImpl(NotesEnum noteName, int octave, int modifier, int voice) {
        this.noteName = noteName;
        this.octave = octave;
        this.modifier = modifier;
        this.voice = voice;
    }

    @Override
    public NotesEnum getNoteName() {
        return noteName;
    }

    @Override
    public void setNoteName(NotesEnum noteName) {
        this.noteName = noteName;
    }

    @Override
    public int getOctave() {
        return octave;
    }

    @Override
    public void setOctave(int octave) {
        this.octave = octave;
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    @Override
    public int getMidiPitch() {
        return 12 * (octave + 1) + noteName.getMidiPitch() + modifier;
    }

    @Override
    public int getVoice() {
        return voice;
    }

    @Override
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
        return "NoteImpl{" + noteName + modifierStr + octave + '}';
    }

}
