package name.kuznetsov.andrei.scoresightreading.algs;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
import name.kuznetsov.andrei.scoresightreading.model.PolyChord;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;

/**
 * Created by andrei on 9/1/16.
 */
public class ConstrainedRandomGenerator implements PolySeqGenerator {
    @Override
    @NonNull
    public PolySeqRep genPolySeq(int nVoices, final Note minNote, final Note maxNote, final int maxSplit, int notesCount) {
        List<PolyChord> chords = new LinkedList<>();
        PolySeqRep seq = new PolySeqRep(chords);

        final int minVal = getNoteVal(minNote);
        final int maxVal = getNoteVal(maxNote);

        final int vInterval = (maxVal - minVal) / nVoices;

        int minValByVoice[] = new int[nVoices];
        int maxValByVoice[] = new int[nVoices];
        int lastVoiceValue[] = new int[nVoices];

        Random rnd = new Random();

        for (int v = nVoices - 1, i = 0; i < nVoices; v--, i++) {
            minValByVoice[v] = minVal + vInterval * i;
            maxValByVoice[v] = minVal + vInterval * (i + 1);

            final int voiceRange = maxValByVoice[v] - minValByVoice[v];
            if (voiceRange <= 0) {
                lastVoiceValue[v] = minValByVoice[v];
            } else {
                lastVoiceValue[v] = rnd.nextInt(voiceRange) + minValByVoice[v];
            }
        }
        maxValByVoice[0] = maxVal;


        for (int i = 0; i < notesCount; i++) {
            Note notes[] = new Note[nVoices];
            for (int v = 0; v < nVoices; v++) {
                final int range = Math.min(getMaxSplit(maxSplit), maxValByVoice[v] - minValByVoice[v]);
                int iVal;
                if (range <= 0) {
                    iVal = 0;
                } else {
                    iVal = rnd.nextInt(range * 2) - range;
                    if (iVal >= 0) {
                        iVal = iVal + 1;
                    }
                }

                int nVal = lastVoiceValue[v] + iVal;
                if (nVal > maxValByVoice[v]) {
                    if (lastVoiceValue[v] == maxValByVoice[v]) {
                        nVal = lastVoiceValue[v] - iVal;
                    } else {
                        nVal = maxValByVoice[v];
                    }
                } else if (nVal < minValByVoice[v]) {
                    if (lastVoiceValue[v] == minValByVoice[v]) {
                        nVal = lastVoiceValue[v] - iVal;
                    } else {
                        nVal = minValByVoice[v];
                    }
                }

                nVal = Math.max(nVal, minValByVoice[v]);
                nVal = Math.min(nVal, maxValByVoice[v]);
                lastVoiceValue[v] = nVal;
                NotesEnum note = NotesEnum.values()[nVal % 7];
                int octave = nVal / 7;
                notes[v] = new Note(note, octave, 0, v);
            }
            chords.add(PolyChord.mkChord(notes));
        }

        return seq;
    }

    private int getMaxSplit(int maxSplit) {
        return (maxSplit > 0) ? maxSplit : Integer.MAX_VALUE;
    }

    private int getNoteVal(Note note) {
        return (note.getOctave() - 1) * 7 + note.getNoteName().ordinal();
    }
}
