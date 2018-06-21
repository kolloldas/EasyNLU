package coldash.easynlu.learn;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Dataset implements Iterable<Example>{

    List<Example> examples;
    Random random = new Random();

    private Dataset(){}

    public static Dataset fromText(String path){
        Dataset d = new Dataset();
        d.examples = parseTextFile(path, "\t");

        return d;
    }

    private static List<Example> parseTextFile(String path, String separator){
        List<Example> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.isEmpty())
                    continue;
                String[] items = line.split(separator);
                assert items.length == 2: String.format("Malformed line: %s", line);

                // System.out.println(line);
                result.add(new Example(items[0].trim(), items[1]));
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    public Dataset shuffle(){
        Collections.shuffle(examples);
        return this;
    }

    public Dataset[] split(float trainFraction, boolean shuffle){
        if(shuffle)
            shuffle();

        int offset = (int)(examples.size() * trainFraction);
        Dataset train = new Dataset();
        Dataset test = new Dataset();

        train.examples = examples.subList(0, offset);
        test.examples = examples.subList(offset, examples.size());

        return new Dataset[]{train, test};
    }

    public Example randomExample(){
        return examples.get(random.nextInt(examples.size()));
    }

    public Example get(int index){
        return examples.get(index);
    }

    @Override
    public Iterator<Example> iterator() {
        return examples.iterator();
    }
}
