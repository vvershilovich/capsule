package by.vladvdev.capsule

import android.util.Log
import by.vladvdev.capsule.emotions.EmotionalController
import by.vladvdev.capsule.neuroimpl.BrainBitController
import by.vladvdev.capsule.emotions.EmotionalMathConfig

import com.neurosdk2.neuro.types.SensorInfo
import com.neurosdk2.neuro.types.SensorState

import com.neurotech.emstartifcats.ArtifactDetectSetting
import com.neurotech.emstartifcats.MathLibSetting
import com.neurotech.emstartifcats.MentalAndSpectralSetting
import com.neurotech.emstartifcats.ShortArtifactDetectSetting

import androidx.lifecycle.MutableLiveData
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/** CapsulePlugin */
class CapsulePlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private val _sensors = MutableLiveData<List<SensorInfo>>()

  private var emotionalController: EmotionalController

  private var calibrated = false
  private var artifacted = false

  private var calibrationProgress = 0

  private var instRelaxation = 0.0
  private var instAttention = 0.0
  private var relRelaxation = 0.0
  private var relAttention = 0.0

  private var o1 = 0.0
  private var o2 = 0.0
  private var t3 = 0.0
  private var t4 = 0.0

  init {
    emotionalController = EmotionalController(getEmotionalConfig())
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "capsule")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    Log.d("log", "onMethodCall: "+call.method)
    when (call.method) {
      "startSearch" ->{
        startSearch()
        result.success(null)
      }
      "startResist" ->{
        startResist()
        result.success(null)
      }
      "startCalibration" ->{
        startCalibration()
        result.success(null)
      }
      "getDevices" -> result.success(_sensors.value?.map { it->it.serialNumber })
      "getData" ->{
        val rootObject= JSONObject()
        rootObject.put("calibrated",calibrated)
        rootObject.put("calibrationProgress",calibrationProgress)
        rootObject.put("instRelaxation",instRelaxation)
        rootObject.put("instAttention",instAttention)
        rootObject.put("relRelaxation",relRelaxation)
        rootObject.put("relAttention",relAttention)
        result.success(rootObject.toString())}
      "getResist" -> result.success(doubleArrayOf(o1, o2, t3, t4))
      "selectDevice" ->
        selectDevice(call.argument<String?>("serialNumber")!!, result)
      "getPlatformVersion" ->
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      else ->
        result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun startCalibration(){
    startSignal()
    emotionalController.startCalibration()
  }

  private fun startSearch(){
    BrainBitController.startSearch(sensorsChanged = { _, infos ->
      run {
        _sensors.postValue(infos)
      }
    })
  }

  private fun stopSearch(){
    BrainBitController.stopSearch()
  }

  private fun startResist(){
    stopSignal()
    BrainBitController.startResist {
      o1 = it.o1
      o2 = it.o2
      t3 = it.t3
      t4 = it.t4
    }
  }
  private fun stopResist(){
    BrainBitController.stopResist()
    o1 = 0.0
    o2 = 0.0
    t3 = 0.0
    t4 = 0.0
  }

  private fun selectDevice(serialNumber: String, result: MethodChannel.Result) {
    stopSearch()
    _sensors.value?.firstOrNull { item -> item.serialNumber == serialNumber }?.let { sensor ->
      BrainBitController.createAndConnect(sensor, onConnectionResult = { sensorState ->
        result.success(null)
      })
    }
  }

  private fun startSignal() {
    stopResist()
    emotionalController.isCalibrationSuccess = { calibrated = it }

    emotionalController.isArtifacted = {
      artifacted = it
    }

    emotionalController.lastMindData = {
      instRelaxation = it.instRelaxation
      relRelaxation = it.relRelaxation
      instAttention = it.instAttention
      relAttention = it.relAttention
    }

    emotionalController.calibrationProgress = {
      calibrationProgress = it
    }

    BrainBitController.startSignal {
      emotionalController.pushData(it)
    }
  }

  private fun stopSignal(){
    BrainBitController.stopSignal()
  }

  private fun getEmotionalConfig(): EmotionalMathConfig {
    val lastWinsToAvg = 3
    val poorSignalAvgSize = 5
    val poorSignalAvgTrigger = .8F

    val mathLibSetting = MathLibSetting(
      /* samplingRate = */        250,
      /* processWinFreq = */      25,
      /* fftWindow = */           500,
      /* nFirstSecSkipped = */    6,
      /* bipolarMode = */         true,
      /* channelsNumber = */      4,
      /* channelForAnalysis = */  0
    )

    val artifactDetectSetting = ArtifactDetectSetting(
      /* artBord = */                 110,
      /* allowedPercentArtpoints = */ 70,
      /* rawBetapLimit = */           800_000,
      /* totalPowBorder = */          30000000,
      /* globalArtwinSec = */         4,
      /* spectArtByTotalp = */        false,
      /* hanningWinSpectrum = */      false,
      /* hammingWinSpectrum = */      true,
      /* numWinsForQualityAvg = */    100
    )

    val shortArtifactDetectSetting = ShortArtifactDetectSetting(
      /* amplArtDetectWinSize = */    200,
      /* amplArtZerodArea = */        200,
      /* amplArtExtremumBorder = */   25
    )

    val mentalAndSpectralSetting = MentalAndSpectralSetting(
      /* nSecForInstantEstimation = */    2,
      /* nSecForAveraging = */            2
    )

    return EmotionalMathConfig(
      250,
      lastWinsToAvg,
      poorSignalAvgSize,
      poorSignalAvgTrigger,
      mathLibSetting,
      artifactDetectSetting,
      shortArtifactDetectSetting,
      mentalAndSpectralSetting
    )
  }
}
