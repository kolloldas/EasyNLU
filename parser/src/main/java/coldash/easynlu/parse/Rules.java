package coldash.easynlu.parse;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Rules {

    public static List<Rule> fromText(Reader reader) throws IOException {
        List<Rule> rules = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while((line = bufferedReader.readLine()) != null){
            line = line.trim();
            if(line.isEmpty()) continue;

            String[] items = line.split("\t");
            if(items.length == 3)
                rules.add(new Rule(items[0].trim(), items[1].trim(), items[2].trim()));
            else if(items.length == 2)
                rules.add(new Rule(items[0].trim(), items[1].trim()));
            else
                throw new IOException(String.format("Malformed line: %s", line));

        }

        return rules;
    }

    public static List<Rule> fromFile(String filePath){
        try {
            return fromText(new FileReader(new File(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    public static final List<Rule> BASE = Arrays.asList(
            new Rule("$To", "to"),
            new Rule("$For", "for"),
            new Rule("$From", "from"),
            new Rule("$Of", "of"),
            new Rule("$On", "on"),
            new Rule("$In", "in"),
            new Rule("$The", "the"),
            new Rule("$I", "i"),
            new Rule("$As", "as"),
            new Rule("$An", "an"),
            new Rule("$A", "a"),
            new Rule("$By", "by"),
            new Rule("$At", "at"),
            new Rule("$And", "and"),
            new Rule("$After", "after")
    );
}
