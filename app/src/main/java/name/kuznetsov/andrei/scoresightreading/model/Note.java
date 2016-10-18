package name.kuznetsov.andrei.scoresightreading.model;

/**
 * Created by andrei on 9/23/16.
 */
public interface Note extends ObjectWithAttachableAttributes {
    NotesEnum getNoteName();

    void setNoteName(NotesEnum noteName);

    int getOctave();

    void setOctave(int octave);

    int getModifier();

    void setModifier(int modifier);

    int getMidiPitch();

    int getVoice();

    void setVoice(int voice);

    Note copy();

    DurationsEnum getDuration();

    void setDuration(DurationsEnum duration);
}
