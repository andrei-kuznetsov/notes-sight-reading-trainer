package name.kuznetsov.andrei.scoresightreading;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
import name.kuznetsov.andrei.scoresightreading.model.PolyChord;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;
import name.kuznetsov.andrei.scoresightreading.views.StaveView;

public class MainActivity extends Activity implements AppConfigChangeListener {
    private StaveView stave;
    private Switch twoVoices;
    private View clickHint;
    private View rDragHint;

    private static final String KEY_MIN_NOTE_NAME = "learn.notes.alg.rnd.min.notename";
    private static final String KEY_MIN_OCTAVE = "learn.notes.alg.rnd.min.octave";
    private static final String KEY_MAX_NOTE_NAME = "learn.notes.alg.rnd.max.notename";
    private static final String KEY_MAX_OCTAVE = "learn.notes.alg.rnd.max.octave";
    private static final String KEY_MAX_INTERVAL = "learn.notes.alg.rnd.max.interval";
    private static final String KEY_NVOICES = "learn.notes.alg.rnd.count.voices";

    private Spinner minNoteSpinner;
    private Spinner minOctaveSpinner;
    private Spinner maxNoteSpinner;
    private Spinner maxOctaveSpinner;
    private Spinner maxIntervalSpinner;

    private Note minNote = new Note(NotesEnum.C, 2);
    private Note maxNote = new Note(NotesEnum.C, 7);
    private int nVoices = 1;
    private int maxInterval = 3;

    @Override
    public void onAppConfigurationChanged() {
        nVoices = 1;
        if (twoVoices.isChecked()) {
            nVoices = 2;
        }

        Note note1 = parseNote(minNoteSpinner, minOctaveSpinner);
        Note note2 = parseNote(maxNoteSpinner, maxOctaveSpinner);

        if (note1.getMidiPitch() > note2.getMidiPitch()) {
            maxNote = note1;
            minNote = note2;
        } else {
            maxNote = note2;
            minNote = note1;
        }

        maxInterval = maxIntervalSpinner.getSelectedItemPosition();

        storeSettings();
        PolySeqRep seq = genRandomPolySeqRep(nVoices, minNote, maxNote, stave.getMaxColumn());
        stave.renderNotes(seq);
    }

    private void storeSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(KEY_MIN_NOTE_NAME, minNote.getNoteName().toString());
        editor.putInt(KEY_MIN_OCTAVE, minNote.getOctave());

        editor.putString(KEY_MAX_NOTE_NAME, maxNote.getNoteName().toString());
        editor.putInt(KEY_MAX_OCTAVE, maxNote.getOctave());

        editor.putInt(KEY_MAX_INTERVAL, maxInterval);
        editor.putInt(KEY_NVOICES, nVoices);

        editor.commit();
    }

    private void readSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String strMinNoteName = sharedPref.getString(KEY_MIN_NOTE_NAME, minNote.getNoteName().toString());
        int minNoteOctave = sharedPref.getInt(KEY_MIN_OCTAVE, minNote.getOctave());
        minNote.setNoteName(NotesEnum.valueOf(strMinNoteName));
        minNote.setOctave(minNoteOctave);

        String strMaxNoteName = sharedPref.getString(KEY_MAX_NOTE_NAME, maxNote.getNoteName().toString());
        int maxNoteOctave = sharedPref.getInt(KEY_MAX_OCTAVE, maxNote.getOctave());
        maxNote.setNoteName(NotesEnum.valueOf(strMaxNoteName));
        maxNote.setOctave(maxNoteOctave);


        maxInterval = sharedPref.getInt(KEY_MAX_INTERVAL, maxInterval);
        nVoices = sharedPref.getInt(KEY_NVOICES, nVoices);
    }

    private Note parseNote(Spinner noteSpinner, Spinner octaveSpinner) {
        int octave = octaveSpinner.getSelectedItemPosition() + 3;
        int noteSelected = noteSpinner.getSelectedItemPosition();

        if (noteSelected < 0) {
            noteSelected = 0;
        } else if (noteSelected >= NotesEnum.values().length) {
            noteSelected = NotesEnum.values().length - 1;
        }
        NotesEnum noteName = NotesEnum.values()[noteSelected];

        return new Note(noteName, octave);
    }

    private void initSpinnersFromNote(Spinner noteSpinner, Spinner octaveSpinner, Note note) {
        int octave = note.getOctave() - 3;
        int noteSelected = note.getNoteName().ordinal();

        noteSpinner.setSelection(noteSelected);
        octaveSpinner.setSelection(octave);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readSettings();

        stave = (StaveView) findViewById(R.id.staveview);
        twoVoices = (Switch) findViewById(R.id.two_voices);
        clickHint = findViewById(R.id.img_hint_click);
        rDragHint = findViewById(R.id.img_hint_drag_r);

        minNoteSpinner = (Spinner) findViewById(R.id.min_note_spinner);
        minOctaveSpinner = (Spinner) findViewById(R.id.min_octave_spinner);
        maxNoteSpinner = (Spinner) findViewById(R.id.max_note_spinner);
        maxOctaveSpinner = (Spinner) findViewById(R.id.max_octave_spinner);
        maxIntervalSpinner = (Spinner) findViewById(R.id.max_interval_spinner);

        initMinMaxSpinners();
        twoVoices.setChecked(nVoices > 1);

        initSpinnerListeners();
        stave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PolySeqRep seq = genRandomPolySeqRep(nVoices, minNote, maxNote, stave.getMaxColumn());
                clickHint.setVisibility(View.GONE);
                rDragHint.setVisibility(View.GONE);
                stave.renderNotes(seq);
            }
        });

        twoVoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAppConfigurationChanged();
            }
        });
    }

    private void initSpinnerListeners() {
        AdapterView.OnItemSelectedListener changeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onAppConfigurationChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onAppConfigurationChanged();
            }
        };

        minNoteSpinner.setOnItemSelectedListener(changeListener);
        maxNoteSpinner.setOnItemSelectedListener(changeListener);
        minOctaveSpinner.setOnItemSelectedListener(changeListener);
        maxOctaveSpinner.setOnItemSelectedListener(changeListener);
        maxIntervalSpinner.setOnItemSelectedListener(changeListener);
    }

    private void initMinMaxSpinners() {
        ArrayAdapter<CharSequence> noteNameAdapter = ArrayAdapter.createFromResource(this, R.array.note_names_array, android.R.layout.simple_spinner_item);
        noteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minNoteSpinner.setAdapter(noteNameAdapter);
        maxNoteSpinner.setAdapter(noteNameAdapter);

        ArrayAdapter<CharSequence> noteOctaveAdapter = ArrayAdapter.createFromResource(this, R.array.note_octaves_array, android.R.layout.simple_spinner_item);
        noteOctaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minOctaveSpinner.setAdapter(noteOctaveAdapter);
        maxOctaveSpinner.setAdapter(noteOctaveAdapter);

        ArrayAdapter<CharSequence> noteIntervalAdapter = ArrayAdapter.createFromResource(this, R.array.note_intervals_array, android.R.layout.simple_spinner_item);
        noteIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxIntervalSpinner.setAdapter(noteIntervalAdapter);

        initSpinnersFromNote(minNoteSpinner, minOctaveSpinner, minNote);
        initSpinnersFromNote(maxNoteSpinner, maxOctaveSpinner, maxNote);
        maxIntervalSpinner.setSelection(maxInterval);
    }

    private int getMinVal() {
        return (minNote.getOctave() - 1) * 7 + minNote.getNoteName().ordinal();
    }

    private int getMaxVal() {
        return (maxNote.getOctave() - 1) * 7 + maxNote.getNoteName().ordinal();
    }

    private int getMaxSplit() {
        return (maxInterval > 0) ? maxInterval : Integer.MAX_VALUE;
    }

    @NonNull
    private PolySeqRep genRandomPolySeqRep(int nVoices, Note minNote, Note maxNote, int notesCount) {
        List<PolyChord> chords = new LinkedList<>();
        PolySeqRep seq = new PolySeqRep(chords);

        final int minVal = getMinVal();
        final int maxVal = getMaxVal();

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
                final int range = Math.min(getMaxSplit(), maxValByVoice[v] - minValByVoice[v]);
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
}
