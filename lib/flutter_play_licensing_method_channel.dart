import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_play_licensing_platform_interface.dart';

/// An implementation of [FlutterPlayLicensingPlatform] that uses method channels.
class MethodChannelFlutterPlayLicensing extends FlutterPlayLicensingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_play_licensing');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
