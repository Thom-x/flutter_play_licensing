import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_play_licensing/flutter_play_licensing.dart';
import 'package:flutter_play_licensing/flutter_play_licensing_platform_interface.dart';
import 'package:flutter_play_licensing/flutter_play_licensing_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterPlayLicensingPlatform
    with MockPlatformInterfaceMixin
    implements FlutterPlayLicensingPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterPlayLicensingPlatform initialPlatform = FlutterPlayLicensingPlatform.instance;

  test('$MethodChannelFlutterPlayLicensing is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterPlayLicensing>());
  });

  test('getPlatformVersion', () async {
    FlutterPlayLicensing flutterPlayLicensingPlugin = FlutterPlayLicensing();
    MockFlutterPlayLicensingPlatform fakePlatform = MockFlutterPlayLicensingPlatform();
    FlutterPlayLicensingPlatform.instance = fakePlatform;

    expect(await flutterPlayLicensingPlugin.getPlatformVersion(), '42');
  });
}
