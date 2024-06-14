import 'capsule_platform_interface.dart';

class Capsule {
  Future<String?> getPlatformVersion() {
    return CapsulePlatform.instance.getPlatformVersion();
  }

  Future<void> startSearch() {
    return CapsulePlatform.instance.startSearch();
  }

  Future<void> startResist() {
    return CapsulePlatform.instance.startResist();
  }

  Future<void> startCalibration() {
    return CapsulePlatform.instance.startCalibration();
  }

  Future<List<String>?> getDevices() {
    return CapsulePlatform.instance.getDevices();
  }

  Future<String?> getData() {
    return CapsulePlatform.instance.getData();
  }

  Future<List<double>?> getResist() {
    return CapsulePlatform.instance.getResist();
  }

  Future<void> selectDevice(String serialNumber) {
    return CapsulePlatform.instance.selectDevice(serialNumber);
  }
}
