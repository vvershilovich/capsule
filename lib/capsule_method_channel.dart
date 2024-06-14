import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'capsule_platform_interface.dart';

class MethodChannelCapsule extends CapsulePlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('capsule');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<List<String>?> getDevices() async {
    final devices = await methodChannel.invokeListMethod<String>('getDevices');
    return devices;
  }

  @override
  Future<List<double>?> getResist() async {
    final resists = await methodChannel.invokeListMethod<double>('getResist');
    return resists;
  }

  @override
  Future<String?> getData() async =>
      await methodChannel.invokeMethod<String>('getData');

  @override
  Future<void> selectDevice(String serialNumber) async {
    await methodChannel
        .invokeMethod('selectDevice', {"serialNumber": serialNumber});
  }

  @override
  Future<void> startSearch() => methodChannel.invokeMethod('startSearch');
  @override
  Future<void> startResist() => methodChannel.invokeMethod('startResist');
  @override
  Future<void> startCalibration() =>
      methodChannel.invokeMethod('startCalibration');
}
