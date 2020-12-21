package KMeans_MR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class KMeansParallelCombiner extends Reducer<Text, DataRow, Text, DataRow> {

    @Override
    public void reduce(Text kCentroid, Iterable<DataRow> dataRows, Context context) throws IOException,
            InterruptedException {
        DataRow rowSum = DataRow.createNewDataRow(dataRows.iterator().next());
        while (dataRows.iterator().hasNext()) {
            rowSum.sumDataRow(dataRows.iterator().next());
        }

        context.write(kCentroid, rowSum);
    }

}
