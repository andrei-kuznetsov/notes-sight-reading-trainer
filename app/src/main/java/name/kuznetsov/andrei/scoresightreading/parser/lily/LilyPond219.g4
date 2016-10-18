// LilyPond parser
// version 2.19

// https://github.com/lilypond/lilypond/blob/master/lily/parser.yy
// https://github.com/lilypond/lilypond/blob/master/lily/lexer.ll
// http://lilypond.org/doc/v2.19/Documentation/contributor/lilypond-grammar


grammar LilyPond219;

SCORE: '\\score';
CURLY_OPEN: '{';
CURLY_CLOSE: '}';
SEQUENTIAL: '\\sequential';
SIMULTANEOUS: '\\simultaneous';
ANGLE_OPEN: '<';
ANGLE_CLOSE: '>';
DOUBLE_ANGLE_OPEN: '<<';
DOUBLE_ANGLE_CLOSE: '>>';
STAR: '*';
DOT: '.';
E_UNSIGNED: '\\'N+;
UNSIGNED: N+;
FRACTION: N+'/'N+;
N: [0-9];
RESTNAME: [rs];
NOTENAME_PITCH: [a-h]('s'|'es'|'is')*; // fixme
TONICNAME_PITCH: [a-h]('s'|'es'|'is')*; // fixme
PITCH_IDENTIFIER: [a-h]('s'|'es'|'is')*; // fixme
APOS: '\'';
COMMA: ',';
EXCLAM: '!';
QUESTION: '?';
EQUALS: '=';
DURATION_IDENTIFIER: [0-9]+; // fixme

EVENT_IDENTIFIER: '\\'[a-zA-Z]+; // fixme

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

//tokens {
//SCORE,
//CURLY_OPEN,
//CURLY_CLOSE,
//SEQUENTIAL,
//SIMULTANEOUS,
//ANGLE_OPEN,
//ANGLE_CLOSE,
//DOUBLE_ANGLE_OPEN,
//DOUBLE_ANGLE_CLOSE,
//STAR,
//UNSIGNED,
//FRACTION,
//RESTNAME,
//DOT,
//NOTENAME_PITCH,
//TONICNAME_PITCH,
//PITCH_IDENTIFIER,
//APOS,
//COMMA,
//EXCLAM,
//QUESTION,
//EQUALS,
//DURATION_IDENTIFIER
//}

lilypond: ( toplevel_expression 
          //| assignment 
          //| INVALID /* error | "\version-error" */
          )*;

toplevel_expression: (
                     //  header_block
                     //| book_block
                     //| bookpart_block
                     //| BOOK_IDENTIFIER
                      score_block
                     | composite_music
                     //| full_markup
                     //| full_markup_list
                     //| SCM_TOKEN
                     //| embedded_scm_active
                     //| output_def
        );

score_block:
	SCORE CURLY_OPEN score_body CURLY_CLOSE
	; 

score_body: (score_items);

score_items: (score_item)*;

score_item: //embedded_scm
             music
             //| output_def
          ;

music: music_assign
       //| lyric_element_music
       | pitch_as_music
     ;

pitch_as_music: pitch_or_music;


pitch_or_music: pitch
                        exclamations
                        questions
                        octave_check
                        maybe_notemode_duration
                        optional_rest
                        post_events
                 | new_chord post_events
  ;

optional_rest: '\rest'?;

new_chord: steno_tonic_pitch maybe_notemode_duration
         | steno_tonic_pitch 
                   optional_notemode_duration 
                   chord_separator 
                   chord_items 
         ;

chord_separator: ':'
               | '^'
               | '/' steno_tonic_pitch
               | '/+' steno_tonic_pitch
               ;

chord_items: chord_item*
           ;

chord_item: chord_separator
          | step_numbers
          //| CHORD_MODIFIER
          ;

step_numbers: step_number
            | step_numbers '.' step_number
            ;

step_number: UNSIGNED
           | UNSIGNED '+'
           | UNSIGNED '-'
;
  
music_assign: simple_music
        | composite_music
            ;

simple_music:
	event_chord
	//| music_property_def
	//| context_change
	; 

composite_music: basic_music
               //| contexted_basic_music
               //| basic_music new_lyrics
               ;

basic_music: //music_function_call
           //| repeated_music
           music_bare
           //| "\lyricsto" simple_string lyric_mode_music
           //| "\lyricsto" symbol '=' simple_string lyric_mode_music
           ;

music_bare: //mode_changed_music
          //| MUSIC_IDENTIFIER
          grouped_music_list
          ;
  
grouped_music_list: simultaneous_music
                  | sequential_music
                    ;
sequential_music:
	SEQUENTIAL? braced_music_list
	; 

simultaneous_music:
	SIMULTANEOUS braced_music_list
	| DOUBLE_ANGLE_OPEN music_list DOUBLE_ANGLE_CLOSE	
	; 

braced_music_list: CURLY_OPEN music_list CURLY_CLOSE
                 ;

music_list: (music_embedded)*
          ;

music_embedded: music
              //| music_embedded_backup
              //| music_embedded_backup 
              //    "(backed-up?)" 
              //    lyric_element_music 
              | multiplied_duration post_events
              ;

event_chord: simple_element post_events
           //| CHORD_REPETITION optional_notemode_duration post_events
           //| MULTI_MEASURE_REST optional_notemode_duration post_events
           //| tempo_event
           | note_chord_element
           ;


simple_element: //DRUM_PITCH optional_notemode_duration
              RESTNAME optional_notemode_duration
              ;


optional_notemode_duration: maybe_notemode_duration;

maybe_notemode_duration: (multiplied_duration)?
                       ;

multiplied_duration: steno_duration multipliers;
                     
multipliers:
	(STAR (UNSIGNED | FRACTION))*
	;

post_events: post_event*
	;

post_event: post_event_nofinger |
           '-' fingering
          ;

fingering: UNSIGNED;

post_event_nofinger: direction_less_event
                   //| script_dir music_function_call
                   | '--'
                   | '__'
                   | script_dir direction_reqd_event
                   | script_dir direction_less_event
                   | '^' fingering
                   | '_' fingering
;

script_dir: '_'
          | '^'
          | '-'
          ;

direction_less_event: string_number_event
                    | EVENT_IDENTIFIER
                    | tremolo_type
                    //| event_function_event
                    ;

direction_reqd_event: //gen_text_def |
                      script_abbreviation
                    ;

tremolo_type: ':'
            | ':' UNSIGNED
            ;


script_abbreviation: '^'
                   | '+'
                   | '-'
                   | '!'
                   | '>'
                   | '.'
                   | '_'
;

string_number_event: E_UNSIGNED;

steno_duration: UNSIGNED dots
              | DURATION_IDENTIFIER dots
              ;

dots: (DOT)*;

note_chord_element: chord_body optional_notemode_duration //post_events
                  ;

chord_body:
	ANGLE_OPEN chord_body_elements ANGLE_CLOSE
	;

chord_body_elements: chord_body_element*
	;

chord_body_element:
	pitch_or_tonic_pitch exclamations questions octave_check post_events
	//| DRUM_PITCH post_events 
	//| music_function_chord_body
	;


pitch_or_tonic_pitch: pitch
                    | steno_tonic_pitch
                    ;

steno_pitch: NOTENAME_PITCH quotes;

steno_tonic_pitch: TONICNAME_PITCH quotes;

pitch: steno_pitch
     | PITCH_IDENTIFIER quotes;

quotes: (sup_quotes | sub_quotes)?;

sup_quotes:
	APOS+
	;

sub_quotes:
	COMMA+
	;

exclamations:
		EXCLAM*
	;

questions:
		QUESTION*
	;

octave_check: (EQUALS quotes)?
            ;

