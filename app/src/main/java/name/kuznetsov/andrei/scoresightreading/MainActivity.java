package name.kuznetsov.andrei.scoresightreading;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import name.kuznetsov.andrei.scoresightreading.algs.chk.CheckerEventListener;
import name.kuznetsov.andrei.scoresightreading.algs.chk.ScoreCheckerV1;
import name.kuznetsov.andrei.scoresightreading.algs.gen.ConstrainedRandomGenerator;
import name.kuznetsov.andrei.scoresightreading.algs.gen.PolySeqGenerator;
import name.kuznetsov.andrei.scoresightreading.midi.MIDIServiceConnection;
import name.kuznetsov.andrei.scoresightreading.model.NoteImpl;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;
import name.kuznetsov.andrei.scoresightreading.views.CheckStateColorMapper;
import name.kuznetsov.andrei.scoresightreading.views.StaveView;

public class MainActivity extends Activity implements AppConfigChangeListener, CheckerEventListener {
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

    private NoteImpl minNoteImpl = new NoteImpl(NotesEnum.C, 2);
    private NoteImpl maxNoteImpl = new NoteImpl(NotesEnum.C, 7);
    private int nVoices = 1;
    private int maxInterval = 3;

    private final PolySeqGenerator generator = new ConstrainedRandomGenerator();

    private MIDIServiceConnection midiService = null;

    @Override
    public void onAppConfigurationChanged() {
        nVoices = 1;
        if (twoVoices.isChecked()) {
            nVoices = 2;
        }

        NoteImpl noteImpl1 = parseNote(minNoteSpinner, minOctaveSpinner);
        NoteImpl noteImpl2 = parseNote(maxNoteSpinner, maxOctaveSpinner);

        if (noteImpl1.getMidiPitch() > noteImpl2.getMidiPitch()) {
            maxNoteImpl = noteImpl1;
            minNoteImpl = noteImpl2;
        } else {
            maxNoteImpl = noteImpl2;
            minNoteImpl = noteImpl1;
        }

        maxInterval = maxIntervalSpinner.getSelectedItemPosition();

        storeSettings();
        genNewExercise();
    }

    private void storeSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(KEY_MIN_NOTE_NAME, minNoteImpl.getNoteName().toString());
        editor.putInt(KEY_MIN_OCTAVE, minNoteImpl.getOctave());

        editor.putString(KEY_MAX_NOTE_NAME, maxNoteImpl.getNoteName().toString());
        editor.putInt(KEY_MAX_OCTAVE, maxNoteImpl.getOctave());

        editor.putInt(KEY_MAX_INTERVAL, maxInterval);
        editor.putInt(KEY_NVOICES, nVoices);

        editor.commit();
    }

    private void readSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String strMinNoteName = sharedPref.getString(KEY_MIN_NOTE_NAME, minNoteImpl.getNoteName().toString());
        int minNoteOctave = sharedPref.getInt(KEY_MIN_OCTAVE, minNoteImpl.getOctave());
        minNoteImpl.setNoteName(NotesEnum.valueOf(strMinNoteName));
        minNoteImpl.setOctave(minNoteOctave);

        String strMaxNoteName = sharedPref.getString(KEY_MAX_NOTE_NAME, maxNoteImpl.getNoteName().toString());
        int maxNoteOctave = sharedPref.getInt(KEY_MAX_OCTAVE, maxNoteImpl.getOctave());
        maxNoteImpl.setNoteName(NotesEnum.valueOf(strMaxNoteName));
        maxNoteImpl.setOctave(maxNoteOctave);


        maxInterval = sharedPref.getInt(KEY_MAX_INTERVAL, maxInterval);
        nVoices = sharedPref.getInt(KEY_NVOICES, nVoices);
    }

    private NoteImpl parseNote(Spinner noteSpinner, Spinner octaveSpinner) {
        int octave = octaveSpinner.getSelectedItemPosition() + 3;
        int noteSelected = noteSpinner.getSelectedItemPosition();

        if (noteSelected < 0) {
            noteSelected = 0;
        } else if (noteSelected >= NotesEnum.values().length) {
            noteSelected = NotesEnum.values().length - 1;
        }
        NotesEnum noteName = NotesEnum.values()[noteSelected];

        return new NoteImpl(noteName, octave);
    }

    private void initSpinnersFromNote(Spinner noteSpinner, Spinner octaveSpinner, NoteImpl noteImpl) {
        int octave = noteImpl.getOctave() - 3;
        int noteSelected = noteImpl.getNoteName().ordinal();

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
                clickHint.setVisibility(View.GONE);
                rDragHint.setVisibility(View.GONE);
                genNewExercise();
            }
        });

        twoVoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAppConfigurationChanged();
            }
        });

        ResolveInfo midiServiceMeta = getPackageManager().resolveService(MIDIServiceConnection.getIntent(), 0);
        if (midiServiceMeta != null) {
            findViewById(R.id.midi_parent).setVisibility(View.VISIBLE);

            findViewById(R.id.bn_connect_midi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectMidiService();
                }
            });


            findViewById(R.id.bn_disconnect_midi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disconnectMidiService();
                }
            });
        } else {
            findViewById(R.id.midi_parent).setVisibility(View.GONE);
        }

        findViewById(R.id.bn_play).setVisibility(View.GONE);

//        findViewById(R.id.bn_play).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playMidi();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        disconnectMidiService();
        super.onDestroy();
    }

    private void disconnectMidiService() {
        if (midiService != null) {
            midiService.disconnectMidiService();
        }
    }

    private void connectMidiService() {
        if (midiService == null) {
            midiService = new MIDIServiceConnection(this);
        }
        midiService.connectMidiService();
    }

    private void genNewExercise() {
        PolySeqRep seq = generator.genPolySeq(nVoices, minNoteImpl, maxNoteImpl, maxInterval, stave.getMaxColumn());

        ScoreCheckerV1 checker = new ScoreCheckerV1(seq);
        checker.setCheckerEventListener(this);
        if (midiService != null) {
            midiService.setListener(checker.getMidiListenerImpl());
        }
        stave.renderNotes(seq, new CheckStateColorMapper());
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

        initSpinnersFromNote(minNoteSpinner, minOctaveSpinner, minNoteImpl);
        initSpinnersFromNote(maxNoteSpinner, maxOctaveSpinner, maxNoteImpl);
        maxIntervalSpinner.setSelection(maxInterval);
    }

    @Override
    public void endOfExerciese() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                genNewExercise();
            }
        });
    }

    @Override
    public void modelChanged(final PolySeqRep model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                model.setChanged();
                model.notifyObservers();
            }
        });
    }
}
