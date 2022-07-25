import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_play_licensing_method_channel.dart';

abstract class FlutterPlayLicensingPlatform extends PlatformInterface {
  /// Constructs a FlutterPlayLicensingPlatform.
  FlutterPlayLicensingPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterPlayLicensingPlatform _instance = MethodChannelFlutterPlayLicensing();

  /// The default instance of [FlutterPlayLicensingPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterPlayLicensing].
  static FlutterPlayLicensingPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterPlayLicensingPlatform] when
  /// they register themselves.
  static set instance(FlutterPlayLicensingPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
