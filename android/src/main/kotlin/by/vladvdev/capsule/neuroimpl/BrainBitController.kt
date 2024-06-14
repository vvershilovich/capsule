package by.vladvdev.capsule.neuroimpl

import com.neurosdk2.neuro.Headband
import com.neurosdk2.neuro.Scanner
import com.neurosdk2.neuro.Sensor
import com.neurosdk2.neuro.interfaces.HeadbandResistDataReceived
import com.neurosdk2.neuro.interfaces.HeadbandSignalDataReceived
import com.neurosdk2.neuro.types.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

object BrainBitController {

    //<editor-fold desc="Scanner">
    var scanner: Scanner? = null

    fun startSearch(sensorsChanged: (Scanner, List<SensorInfo>) -> Unit){
        try {
            if(scanner == null){
                scanner = Scanner(SensorFamily.SensorLEHeadband)
            }
            else{
                sensorsChanged(scanner!!, scanner!!.sensors)
            }

            disconnectCurrent()

            scanner!!.sensorsChanged = Scanner.ScannerCallback(sensorsChanged)
            scanner!!.start()
        }catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    fun stopSearch(){
        try {
            scanner!!.stop()
        }catch (ex: Exception){
            ex.printStackTrace()
        }
    }
    //</editor-fold>

    //<editor-fold desc="Sensor state">
    private var sensor: Headband? = null

    var connectionStateChanged: (SensorState) -> Unit = { }
    var batteryChanged: (Int) -> Unit = { }

    val hasDevice: Boolean get() { return sensor!=null}


    fun createAndConnect(sensorInfo: SensorInfo, onConnectionResult: (SensorState) -> Unit){
        thread {
            try {
                sensor = scanner!!.createSensor(sensorInfo) as Headband

                if(sensor != null){
                    sensor!!.sensorStateChanged = Sensor.SensorStateChanged(connectionStateChanged)
                    sensor!!.batteryChanged = Sensor.BatteryChanged(batteryChanged)

                    connectionStateChanged(SensorState.StateInRange)
                    onConnectionResult(SensorState.StateInRange)
                }
                else{
                    onConnectionResult(SensorState.StateOutOfRange)
                }
            }catch (ex: Exception){
                ex.printStackTrace()
            }

        }
    }

    fun connectCurrent(onConnectionResult: (SensorState) -> Unit){
            if(sensor?.state == SensorState.StateOutOfRange)
            {
                thread {
                    try {
                        sensor!!.connect()
                        onConnectionResult(sensor!!.state)
                    }catch (ex: Exception){
                        ex.printStackTrace()
                    }
                }
            }

    }

    fun disconnectCurrent(){
        try {
            if(sensor!!.state == SensorState.StateInRange)
                sensor!!.disconnect()
        }catch (ex: Exception){
            ex.printStackTrace()
        }

    }

    val connectionState: SensorState get() { return if(hasDevice) sensor!!.state else SensorState.StateOutOfRange}
    //</editor-fold>

    //<editor-fold desc="Parameters">
    fun fullInfo(): String {
        var info = "Error while read parameters!"
        try {
            info = buildString {
                append("Parameters: ")
                for (param in sensor!!.supportedParameter){
                    append("\n\tName: ${param.param.name}")
                    append("\n\t\tAccess: ${param.paramAccess}")
                    when (param.param!!){
                        SensorParameter.ParameterGain -> append("\n\t\tValue: ${sensor?.gain}")
                        SensorParameter.ParameterFirmwareMode -> append("\n\t\tValue: ${sensor?.firmwareMode}")
                        SensorParameter.ParameterSamplingFrequency -> append("\n\t\tValue: ${sensor?.samplingFrequency}")
                        SensorParameter.ParameterOffset -> append("\n\t\tValue: ${sensor?.dataOffset}")
                        SensorParameter.ParameterFirmwareVersion -> {
                            append("\n\t\tFW version: ${sensor?.version?.fwMajor}.${sensor?.version?.fwMinor}.${sensor?.version?.fwPatch}")
                            append("\n\t\tHW version: ${sensor?.version?.hwMajor}.${sensor?.version?.hwMinor}.${sensor?.version?.hwPatch}")
                            append("\n\t\tExtension: ${sensor?.version?.extMajor}")
                        }
                        SensorParameter.ParameterBattPower -> append("\n\t\tValue: ${sensor?.battPower}")
                        SensorParameter.ParameterSensorFamily -> append("\n\t\tValue: ${sensor?.sensFamily}")
                        SensorParameter.ParameterSensorMode -> append("\n\t\tValue: ${sensor?.firmwareMode}")
                        SensorParameter.ParameterSensorChannels -> append("\n\t\tValue: ${sensor?.channelsCount}")
                        SensorParameter.ParameterName -> append("\n\t\tValue: ${sensor?.name}")
                        SensorParameter.ParameterState -> append("\n\t\tValue: ${sensor?.state}")
                        SensorParameter.ParameterAddress ->  append("\n\t\tValue: ${sensor?.address}")
                        SensorParameter.ParameterSerialNumber -> append("\n\t\tValue: ${sensor?.serialNumber}")
                        else -> break
                    }
                }

                append("\n\nCommands: ")
                for(command in sensor?.supportedCommand!!){
                    append("\n\t${command.name}")
                }

                append("\n\nFeatures: ")
                for(feature in sensor?.supportedFeature!!){
                    append("\n\t${feature.name}")
                }
            }
        }catch (ex: Exception){
            ex.printStackTrace()
        }
        return info
    }

    //</editor-fold>

    //<editor-fold desc="Signal">
    fun startSignal(signalReceived: (Array<HeadbandSignalData>) -> Unit){
        sensor!!.headbandSignalDataReceived = HeadbandSignalDataReceived(signalReceived)
        executeCommand(SensorCommand.StartSignal)
    }

    fun stopSignal(){
        sensor!!.headbandSignalDataReceived =null
        executeCommand(SensorCommand.StopSignal)
    }
    //</editor-fold>

    //<editor-fold desc="Resist">
    fun startResist(resistReceived: (HeadbandResistData) -> Unit){
        sensor!!.headbandResistDataReceived = HeadbandResistDataReceived(resistReceived)
        executeCommand(SensorCommand.StartResist)
    }

    fun stopResist(){
        sensor!!.headbandResistDataReceived = null
        executeCommand(SensorCommand.StopResist)
    }
    //</editor-fold>

    @OptIn(DelicateCoroutinesApi::class)
    private fun executeCommand(command: SensorCommand) = runBlocking(newSingleThreadContext("dedicatedThread")) {
        try {
            if (sensor?.isSupportedCommand(command) == true) {
                sensor?.execCommand(command)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}