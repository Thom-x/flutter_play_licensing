class LicenseCheckResponse {
  int? reason;
  String? rawData;
  String? signature;

  LicenseCheckResponse({int? reason, String? rawData, String? signature}) {
    if (reason != null) {
      this.reason = reason;
    }
    if (rawData != null) {
      this.rawData = rawData;
    }
    if (signature != null) {
      this.signature = signature;
    }
  }

  LicenseCheckResponse.fromJson(Map<String, dynamic> json) {
    reason = json['reason'];
    rawData = json['rawData'];
    signature = json['signature'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = <String, dynamic>{};
    data['reason'] = reason;
    data['rawData'] = rawData;
    data['signature'] = signature;
    return data;
  }
}
