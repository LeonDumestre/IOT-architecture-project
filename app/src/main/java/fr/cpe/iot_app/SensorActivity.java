package fr.cpe.iot_app;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLightSensor;

    private TextView textAccelerometer;
    private TextView textLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        TextView titleAccelerometer = findViewById(R.id.title_accelerometer);
        textAccelerometer = findViewById(R.id.text_accelerometer);
        TextView titleLight = findViewById(R.id.title_light);
        textLight = findViewById(R.id.text_light);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        titleAccelerometer.setText(mAccelerometer.getName());
        titleLight.setText(mLightSensor.getName());
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            textLight.setText(String.format("value: %s", event.values[0]));
            return;
        }

        float xValue = event.values[0];
        float yValue = event.values[1];
        float zValue = event.values[2];

        textAccelerometer.setText(String.format("x: %s\ny: %s\nz: %s", xValue, yValue, zValue));
    }
}

