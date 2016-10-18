package name.kuznetsov.andrei.scoresightreading;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.junit.Test;

import name.kuznetsov.andrei.scoresightreading.parser.arrays.ArraysLexer;
import name.kuznetsov.andrei.scoresightreading.parser.arrays.ArraysParser;
import name.kuznetsov.andrei.scoresightreading.parser.lily.LilyPond219Lexer;
import name.kuznetsov.andrei.scoresightreading.parser.lily.LilyPond219Parser;
import name.kuznetsov.andrei.scoresightreading.parser.lily.model.LilyMusicListListenerImpl;

import static org.junit.Assert.assertEquals;

/**
 * Created by andrei on 9/26/16.
 */

public class LilyMusicListListenerImplTest {
    @Test
    public void canReconstructTest() throws Exception {
        LilyMusicListListenerImpl listener = new LilyMusicListListenerImpl();

        ANTLRInputStream input = new ANTLRInputStream("c4 r1 <c d>4");
        TokenStream tokens = new CommonTokenStream(new LilyPond219Lexer(input));
        LilyPond219Parser parser = new LilyPond219Parser(tokens);
        parser.addParseListener(listener);
        parser.music_list();

        System.out.println(listener.getSequence().toString());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        assertEquals( // fixme
                "LilySequence{chords=[{pitches=[c], duration=4}, {pitches=[rest], duration=1}, {pitches=[c, d], duration=4}]}",
                listener.getSequence().toString()
        );
    }
}
