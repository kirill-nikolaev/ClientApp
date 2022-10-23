package org.example;


import org.example.models.Measurement;
import org.example.models.Sensor;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class ClientApp {

    private final static String SENSOR_REGISTRATION_URL = "http://localhost:8080/sensors/registration";
    private final static String ADD_MEASUREMENT_URL = "http://localhost:8080/measurements/add";
    private final static String GET_MEASUREMENTS_URL = "http://localhost:8080/measurements";
    private final static RestTemplate restTemplate = new RestTemplate();

    public static void main( String[] args ) {
        Sensor mySensor = new Sensor("sensor");
        try {

            registerSensor(mySensor);

            generate1000MeasurementRequests(mySensor);

            List<Measurement> measurementList = getAllMeasurements();

            drawDiagram(measurementList);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void registerSensor(Sensor sensor) {
        ResponseEntity<String> response = restTemplate.postForEntity(SENSOR_REGISTRATION_URL, sensor, String.class);
        System.out.println("Registration completed successfully");
    }

    private static void generate1000MeasurementRequests (Sensor sensor) {
        Random random = new Random();
        double currentValue = random.nextInt(200) - 100;
        boolean raining = random.nextBoolean();

        System.out.print("Request sent:  ");
        for (int i = 0; i < 1000; i++) {
            requestMeasurement(new Measurement(currentValue, raining, sensor));

            for (int j = 0; j < Math.log10(i + 1); j++) {
                System.out.print("\b");
            }

            System.out.print(i + 1);

            double valueChange;
            do {
                valueChange = (random.nextInt(200) - 100) / 15.0;
            } while (Math.abs(currentValue + valueChange) >= 100);

            currentValue = currentValue + valueChange;

            if (random.nextInt(20) == 0)
                raining = !raining;
        }
    }

    private static void requestMeasurement(Measurement measurement) {
        restTemplate.postForObject(ADD_MEASUREMENT_URL, measurement, String.class);
    }


    private static List<Measurement> getAllMeasurements() {
        Measurement[] measurement = restTemplate.getForObject(GET_MEASUREMENTS_URL, Measurement[].class);

        return Arrays.asList(measurement);
    }

    private static void drawDiagram(List<Measurement> measurements) {
        List<Double> list = measurements.stream().map(Measurement::getValue).collect(Collectors.toList());

        XYChart chart = new XYChartBuilder().xAxisTitle("measurement").yAxisTitle("temperature").width(1000).height(600).build();

        chart.getStyler().setYAxisMin(-100.0);
        chart.getStyler().setYAxisMax(100.0);
        XYSeries xySeries = chart.addSeries("value", list);
        xySeries.setMarker(SeriesMarkers.NONE);

        new SwingWrapper(chart).displayChart();
    }
}
