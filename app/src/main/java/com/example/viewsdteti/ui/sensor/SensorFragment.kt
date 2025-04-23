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
    private var accelerometer: Sensor? = null

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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            binding.xValue.text = String.format(Locale.US, "X: %.2f", event.values[0])
            binding.yValue.text = String.format(Locale.US,"Y: %.2f", event.values[1])
            binding.zValue.text = String.format(Locale.US,"Z: %.2f", event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}