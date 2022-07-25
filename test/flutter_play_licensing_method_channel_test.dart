import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_play_licensing/flutter_play_licensing_method_channel.dart';

void main() {
  MethodChannelFlutterPlayLicensing platform = MethodChannelFlutterPlayLicensing();
  const MethodChannel channel = MethodChannel('flutter_play_licensing');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
