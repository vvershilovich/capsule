import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'capsule_method_channel.dart';

abstract class CapsulePlatform extends PlatformInterface {
  /// Constructs a CapsulePlatform.
  CapsulePlatform() : super(token: _token);

  static final Object _token = Object();

  static CapsulePlatform _instance = MethodChannelCapsule();

  /// The default instance of [CapsulePlatform] to use.
  ///
  /// Defaults to [MethodChannelCapsule].
  static CapsulePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CapsulePlatform] when
  /// they register themselves.
  static set instance(CapsulePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> startSearch() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> startResist() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> startCalibration() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<String>?> getDevices() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getData() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<double>?> getResist() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> selectDevice(String serialNumber) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
