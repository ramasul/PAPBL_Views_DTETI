package com.example.viewsdteti.ui.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.viewsdteti.R
import com.example.viewsdteti.databinding.FragmentSensorBinding
import java.util.Locale

class SensorFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager

    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireActivity().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager

    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, event.values.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, event.values.size)
            }
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()  // rotation around Z axis
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()    // rotation around X axis
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()     // rotation around Y axis

//            Log.d("Orientation", "Azimuth: $azimuth, Pitch: $pitch, Roll: $roll")
            binding.xValue3.text = String.format(Locale.US, "azimuth: %.2f", azimuth)
            binding.yValue3.text = String.format(Locale.US,"pitch: %.2f", pitch)
            binding.zValue3.text = String.format(Locale.US,"roll: %.2f", roll)
        }

        binding.xValue.text = String.format(Locale.US, "accel X: %.2f", accelerometerReading[0])
        binding.yValue.text = String.format(Locale.US,"accel Y: %.2f", accelerometerReading[1])
        binding.zValue.text = String.format(Locale.US,"accel Z: %.2f", accelerometerReading[2])

        binding.xValue2.text = String.format(Locale.US, "magnetX: %.2f", magnetometerReading[0])
        binding.yValue2.text = String.format(Locale.US,"magnetY: %.2f", magnetometerReading[1])
        binding.zValue2.text = String.format(Locale.US,"magnetZ: %.2f", magnetometerReading[2])


    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}