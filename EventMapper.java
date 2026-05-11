package gdelt.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class EventMapper extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text countryEventType = new Text();

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");
        if (fields.length > 27) {
            String country = fields[21];   // Pays (colonne 21 dans GDELT)
            String eventType = fields[27]; // Type d'événement (colonne 27 dans GDELT)
            countryEventType.set(country + "\t" + eventType);
            context.write(countryEventType, one);
        }
    }
}
