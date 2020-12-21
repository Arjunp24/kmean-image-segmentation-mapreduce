package KMeans_MR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class KMeansReducer extends Reducer<IntWritable, DataRow, Text, Text> {

    private final Text centroidKey = new Text();
    private final Text centroidValue = new Text();

    @Override
    protected void reduce(IntWritable centroid, Iterable<DataRow> dataRows, Context context) throws IOException, InterruptedException {

        DataRow rowSum = DataRow.createNewDataRow(dataRows.iterator().next());
        while (dataRows.iterator().hasNext()) {
            rowSum.sumDataRow(dataRows.iterator().next());
        }

        rowSum.calculateNewCentroid();
        centroidKey.set(centroid.toString());
        centroidValue.set(rowSum.toString());
        context.write(centroidKey, centroidValue);

    }
}
