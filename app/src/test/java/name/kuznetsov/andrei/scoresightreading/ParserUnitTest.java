package name.kuznetsov.andrei.scoresightreading;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

import java.io.InputStream;

import name.kuznetsov.andrei.scoresightreading.parser.arrays.ArraysBaseListener;
import name.kuznetsov.andrei.scoresightreading.parser.arrays.ArraysLexer;
import name.kuznetsov.andrei.scoresightreading.parser.arrays.ArraysParser;
import name.kuznetsov.andrei.scoresightreading.parser.lily.LilyPond219Lexer;
import name.kuznetsov.andrei.scoresightreading.parser.lily.LilyPond219Parser;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ParserUnitTest {
    class AListener extends ArraysBaseListener {
        int arrayCount = 0;
        int objectCount = 0;
        int propertyCount = 0;
        int stringsCount = 0;

        @Override
        public void enterArray(@NotNull ArraysParser.ArrayContext ctx) {
            arrayCount++;
        }

        @Override
        public void enterObject(@NotNull ArraysParser.ObjectContext ctx) {
            objectCount++;
        }

        @Override
        public void enterObject_property(@NotNull ArraysParser.Object_propertyContext ctx) {
            propertyCount++;
        }

        @Override
        public void enterQuoted_string(@NotNull ArraysParser.Quoted_stringContext ctx) {
            stringsCount++;
        }

        @Override
        public String toString() {
            return "AListener{" +
                    "arrayCount=" + arrayCount +
                    ", objectCount=" + objectCount +
                    ", propertyCount=" + propertyCount +
                    ", stringsCount=" + stringsCount +
                    '}';
        }
    }

    @Test
    public void arrays_canParse() throws Exception {
        String file = "mozart_measures_noslurs.array";
        AListener listener = new AListener() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void exitPropValue(@NotNull ArraysParser.PropValueContext ctx) {
                System.out.println(ctx.quoted_string().getText());
                sb.setLength(0);
                for (TerminalNode i : ctx.quoted_string().StringLiteral()) {
                    sb.append(i.getText(), 1, i.getText().length() - 1);
                }
                String lily = sb.toString();
                lily = lily.replace("\\\\", "\\");
                System.out.println(lily);

                ANTLRInputStream input = new ANTLRInputStream(lily);
                TokenStream tokens = new CommonTokenStream(new LilyPond219Lexer(input));
                LilyPond219Parser parser = new LilyPond219Parser(tokens);
                parser.music_list();

                System.out.println(parser.getNumberOfSyntaxErrors());
                assertEquals(0, parser.getNumberOfSyntaxErrors());

            }
        };

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
        assertNotNull(in);

        ANTLRInputStream input = new ANTLRInputStream(in);
        TokenStream tokens = new CommonTokenStream(new ArraysLexer(input));
        ArraysParser parser = new ArraysParser(tokens);
        parser.addParseListener(listener);
        parser.topLevel();

        System.out.println(listener.toString());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        assertEquals(17, listener.arrayCount);
        assertEquals(173, listener.objectCount);
        assertEquals(314, listener.propertyCount);
        assertEquals(628, listener.stringsCount);
    }
}