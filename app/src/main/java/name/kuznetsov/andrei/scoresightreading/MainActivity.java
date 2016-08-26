package name.kuznetsov.andrei.scoresightreading;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Switch;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
import name.kuznetsov.andrei.scoresightreading.model.PolyChord;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;
import name.kuznetsov.andrei.scoresightreading.views.StaveView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final StaveView stave = (StaveView) findViewById(R.id.staveview);
        final Switch twoVoices = (Switch) findViewById(R.id.two_voices);

        stave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nVoices = 1;
                if (twoVoices.isChecked()) {
                    nVoices = 2;
                }
                PolySeqRep seq = genRandomPolySeqRep(nVoices);
                stave.renderNotes(seq);
            }
        });

    }

    private int getMinVal() {
        return 2 * 7; // 2nd octave
    }

    private int getMaxVal() {
        return 6 * 7; // 6th octave
    }

    private int getMaxSplit() {
        return 8;
    }

    @NonNull
    private PolySeqRep genRandomPolySeqRep(int nVoices) {
        List<PolyChord> chords = new LinkedList<>();
        PolySeqRep seq = new PolySeqRep(chords);

        final int minVal = getMinVal();
        final int maxVal = getMaxVal();

        final int vInterval = (maxVal - minVal) / nVoices;

        int minValByVoice[] = new int[nVoices];
        int maxValByVoice[] = new int[nVoices];
        int lastVoiceValue[] = new int[nVoices];

        for (int v = nVoices - 1, i = 0; i < nVoices; v--, i++) {
            minValByVoice[v] = minVal + vInterval * i;
            maxValByVoice[v] = minVal + vInterval * (i + 1);
            lastVoiceValue[v] = (minValByVoice[v] + maxValByVoice[v]) / 2;
        }
        maxValByVoice[0] = maxVal;

        Random rnd = new Random();

        for (int i = 0; i < 16; i++) {
            Note notes[] = new Note[nVoices];
            for (int v = 0; v < nVoices; v++) {
//                int nVal = rnd.nextInt(maxValByVoice[v] - minValByVoice[v]) + minValByVoice[v];
                int iVal = rnd.nextInt(getMaxSplit() * 2) - getMaxSplit();
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

                lastVoiceValue[v] = nVal;
                NotesEnum note = NotesEnum.values()[nVal % 7];
                int octave = nVal / 7;
                notes[v] = new Note(note, octave, 0, v);
            }
            chords.add(PolyChord.mkChord(notes));
        }

        return seq;
    }
}
