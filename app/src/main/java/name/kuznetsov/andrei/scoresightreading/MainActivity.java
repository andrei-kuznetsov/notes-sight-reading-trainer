package name.kuznetsov.andrei.scoresightreading;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import name.kuznetsov.andrei.scoresightreading.algs.ConstrainedRandomGenerator;
import name.kuznetsov.andrei.scoresightreading.algs.PolySeqGenerator;
import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
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

    private final PolySeqGenerator generator = new ConstrainedRandomGenerator();

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
        PolySeqRep seq = generator.genPolySeq(nVoices, minNote, maxNote, maxInterval, stave.getMaxColumn());
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
                PolySeqRep seq = generator.genPolySeq(nVoices, minNote, maxNote, maxInterval, stave.getMaxColumn());
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

//        findViewById(R.id.bn_play).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playMidi();
//            }
//        });
    }

//    private void playMidi() {
//        PolySeqRep notesToPlay = stave.getNotes();
//
//        MidiManager m = (MidiManager)this.getSystemService(Context.MIDI_SERVICE);
//        MidiDeviceInfo[] infos = m.getDevices();
//
//    }

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
}
