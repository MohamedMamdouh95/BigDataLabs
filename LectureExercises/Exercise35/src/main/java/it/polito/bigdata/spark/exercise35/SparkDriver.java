package it.polito.bigdata.spark.exercise35;

import org.apache.spark.api.java.*;

import java.util.List;

import org.apache.spark.SparkConf;

public class SparkDriver {

	public static void main(String[] args) {

		String inputPath;
		String outputPath;

		inputPath = args[0];
		outputPath = args[1];

		// Create a configuration object and set the name of the application
		SparkConf conf = new SparkConf().setAppName("Spark Exercise #34");

		// Create a Spark Context object
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Read the content of the input file
		JavaRDD<String> readingsRDD = sc.textFile(inputPath);

		// Extract the PM10 values
		// It can be implemented by using the map transformation
		JavaRDD<Double> pm10ValuesRDD = readingsRDD.map(PM10Reading -> {
			Double PM10value;

			// Split the line in fields
			String[] fields = PM10Reading.split(",");

			// fields[2] contains the PM10 value
			PM10value = new Double(fields[2]);
			return PM10value;
		});

		// Select the maximum value
		Double topValue = pm10ValuesRDD.reduce((value1, value2) -> {
			if (value1 > value2)
				return value1;
			else
				return value2;
		});

		// Filter the content of readingsRDD
		// Select only the line(s) associated with the topValue
		JavaRDD<String> selectedRecordsRDD = readingsRDD.filter(PM10Reading -> {
			Double PM10value;

			// Split the line in fields
			String[] fields = PM10Reading.split(",");

			// fields[2] contains the PM10 value
			PM10value = new Double(fields[2]);

			// If PM10value is equal to the maximum value
			// return true (the line must be selected)
			// Otherwise, return false (the line must be discarded)
			if (PM10value.equals(topValue))
				return true;
			else
				return false;
		});

		// Extract the dates from the selected records
		JavaRDD<String> datesRDD = selectedRecordsRDD.map(PM10Reading -> {
			String date;

			// Split the line in fields
			String[] fields = PM10Reading.split(",");

			// fields[1] contains the date of the current record
			date = fields[1];
			return date;
		});

		// Remove duplicates, if any
		JavaRDD<String> distinctDatesRDD = datesRDD.distinct();

		// Store the result in the output folder
		distinctDatesRDD.saveAsTextFile(outputPath);

		// Close the Spark context
		sc.close();
	}
}
