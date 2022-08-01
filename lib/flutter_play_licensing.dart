import 'package:flutter/services.dart';

import 'license_check_response.dart';

class FlutterPlayLicensing {

  static const String errorInvalidPackageName = "1";
  static const String errorNonMatchingUid = "2";
  static const String errorNotMarketManaged = "3";
  static const String errorCheckInProgress = "4";
  static const String errorInvalidPublicKey = "5";
  static const String errorMissingPermission = "6";
  static const String errorConnectionError = "7";
  static const String errorInvalidResponse = "8";
  static const String unknownError = "99";

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

  static Future<LicenseCheckResponse> serverSideCheck({
    /// In base64
    String? publicKey,
    int? nonce,
  }) async {
    var response = await _channel.invokeMethod('serverSideCheck', {
      'publicKey': publicKey,
      'nonce': nonce,
    });
    return LicenseCheckResponse.fromJson(response);
  }
}
