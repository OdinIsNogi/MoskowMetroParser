import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Prog {
    public static void main(String[] args) throws IOException, ParseException {
        MRoot root = new MRoot();
        root.JsonParse();
        root.createJson();
        root.parseFile("H:\\MoskovMetro\\lib\\Metro.json");

        root.JsonParser();
    }
}
