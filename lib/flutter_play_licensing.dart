
import 'flutter_play_licensing_platform_interface.dart';

class FlutterPlayLicensing {
  Future<String?> getPlatformVersion() {
    return FlutterPlayLicensingPlatform.instance.getPlatformVersion();
  }
}
