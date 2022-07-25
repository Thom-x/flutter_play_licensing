import 'package:flutter/services.dart';

class FlutterPlayLicensing {
  static const MethodChannel _channel = MethodChannel('flutter_play_licensing');

  static Future<int> check({
    /// In hex
    /// Prefer to initialize PlayLicensing.salt in native
    String? salt,

    /// In base64
    String? publicKey,
  }) async {
    final int reason = await _channel.invokeMethod('check', {
      'salt': salt,
      'publicKey': publicKey,
    });
    return reason;
  }

  static Future<bool> isAllowed({
    /// In hex
    /// Prefer to initialize PlayLicensing.salt in native
    String? salt,

    /// In base64
    String? publicKey,
  }) async {
    return await _channel.invokeMethod('isAllowed', {
      'salt': salt,
      'publicKey': publicKey,
    });
  }

  static Future<Map<dynamic, dynamic>> serverSideCheck({
    /// In base64
    String? publicKey,
    String? nonce,
  }) async {
    return await _channel.invokeMethod('serverSideCheck', {
      'publicKey': publicKey,
      'nonce': nonce,
    });
  }
}
