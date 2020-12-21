package KMeans_MR;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class DataRow implements Writable {

    private float[] features;
    private int featuresDimension;
    private int numberOfRows;
    private int closestCentroid;

    public DataRow(String[] centroid) {
        this.setFeatures(centroid);
    }

    public DataRow() {
        this.featuresDimension = 0;
    }

    public DataRow(float[] features) {
        this.setFeatures(features);
    }

    private void setFeatures(float[] features) {
        this.features = features;
        this.featuresDimension = features.length;
        this.numberOfRows = 1;
    }

    public void setClosestCentroid(int closestCentroid) {
        this.closestCentroid = closestCentroid;
    }

    public static DataRow createNewDataRow(DataRow row) {
        DataRow newRow = new DataRow(row.features);
        newRow.numberOfRows = row.numberOfRows;
        return newRow;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.featuresDimension);
        dataOutput.writeInt(this.numberOfRows);
        for(int i = 0; i < this.featuresDimension; i++) {
            dataOutput.writeFloat(this.features[i]);
        }
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.featuresDimension = dataInput.readInt();
        this.numberOfRows = dataInput.readInt();
        this.features = new float[featuresDimension];
        for(int i = 0; i < this.featuresDimension; i++) {
            this.features[i] = dataInput.readFloat();
        }
    }

    public void setFeatures(String[] dataString) {
        this.features = new float[dataString.length];
        this.featuresDimension = dataString.length;
        this.numberOfRows = 1;
        for (int i = 0; i < dataString.length; i++) {
            this.features[i] = Float.parseFloat(dataString[i]);
        }
    }

    public float calculateDistance(DataRow centroid) {
        float distance = 0.0f;

        for (int i = 0; i < this.featuresDimension; i++) {
            distance += Math.pow(Math.abs(this.features[i] - centroid.features[i]), this.featuresDimension);
        }
        distance = (float)Math.round(Math.pow(distance, 1f/this.featuresDimension) * 100000) / 100000.0f;
        return distance;
    }

    public int findClosestCentroid(DataRow[] centroids) {
        float minimumDistance = Float.POSITIVE_INFINITY;
        int closestCentroid = -1;
        float distance;

        for(int i =0;i < centroids.length;i++) {
            distance = this.calculateDistance(centroids[i]);
            if(distance < minimumDistance) {
                minimumDistance = distance;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }

    public int findClosestCentroidList(List<DataRow> centroids) {
        float minimumDistance = Float.POSITIVE_INFINITY;
        int closestCentroid = -1;
        float distance;

        for(int i =0;i < centroids.size();i++) {
            distance = this.calculateDistance(centroids.get(i));
            if(distance < minimumDistance) {
                minimumDistance = distance;
                closestCentroid = i;
            }
        }
        return closestCentroid;
    }

    public void sumDataRow(DataRow row) {
        for (int i = 0; i < this.featuresDimension; i++) {
            this.features[i] += row.features[i];
        }
        this.numberOfRows += row.numberOfRows;
    }

    public void calculateNewCentroid() {
        for (int i = 0; i < this.featuresDimension; i++) {
            this.features[i] = (float)Math.round(this.features[i] / this.numberOfRows * 100000) / 100000.0f;
        }
        this.numberOfRows = 1;
    }

    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       for(float x: this.features){
         sb.append(x+",");
       }
       sb.substring(0,sb.length()-2);
      return sb.toString();
    }
}
