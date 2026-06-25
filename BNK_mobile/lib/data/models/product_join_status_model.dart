class ProductJoinStatusModel {
  final int? subscriptionNo;
  final int productNo;
  final String productName;
  final String productType;
  final int minJoinAmount;
  final int maxJoinAmount;
  final int depositUnit;
  final int minTermMonths;
  final int maxTermMonths;
  final String subscriptionStatus;
  final String currentStep;
  final bool accountRequired;
  final int? accountNo;
  final int? linkedAccountId;
  final int? subscriptionAmount;
  final int? subscriptionMonths;
  final double? appliedInterestRate;
  final int requiredTermsAgreed;
  final int optionalTermsAgreed;
  final String message;

  ProductJoinStatusModel({
    required this.subscriptionNo,
    required this.productNo,
    required this.productName,
    required this.productType,
    required this.minJoinAmount,
    required this.maxJoinAmount,
    required this.depositUnit,
    required this.minTermMonths,
    required this.maxTermMonths,
    required this.subscriptionStatus,
    required this.currentStep,
    required this.accountRequired,
    required this.accountNo,
    required this.linkedAccountId,
    required this.subscriptionAmount,
    required this.subscriptionMonths,
    required this.appliedInterestRate,
    required this.requiredTermsAgreed,
    required this.optionalTermsAgreed,
    required this.message,
  });

  factory ProductJoinStatusModel.fromJson(Map<String, dynamic> json) {
    return ProductJoinStatusModel(
      subscriptionNo: _toIntOrNull(json['subscriptionNo']),
      productNo: _toInt(json['productNo']),
      productName: (json['productName'] ?? '').toString(),
      productType: (json['productType'] ?? '').toString(),
      minJoinAmount: _toInt(json['minJoinAmount']),
      maxJoinAmount: _toInt(json['maxJoinAmount']),
      depositUnit: _toInt(json['depositUnit']),
      minTermMonths: _toInt(json['minTermMonths']),
      maxTermMonths: _toInt(json['maxTermMonths']),
      subscriptionStatus: (json['subscriptionStatus'] ?? '').toString(),
      currentStep: (json['currentStep'] ?? '').toString(),
      accountRequired: json['accountRequired'] == true,
      accountNo: _toIntOrNull(json['accountNo']),
      linkedAccountId: _toIntOrNull(json['linkedAccountId']),
      subscriptionAmount: _toIntOrNull(json['subscriptionAmount']),
      subscriptionMonths: _toIntOrNull(json['subscriptionMonths']),
      appliedInterestRate: _toDoubleOrNull(json['appliedInterestRate']),
      requiredTermsAgreed: _toInt(json['requiredTermsAgreed']),
      optionalTermsAgreed: _toInt(json['optionalTermsAgreed']),
      message: (json['message'] ?? '').toString(),
    );
  }

  bool get requiredTermsDone => requiredTermsAgreed == 1;
  bool get optionalTermsDone => optionalTermsAgreed == 1;
  bool get readyToComplete => currentStep == 'READY_TO_COMPLETE';
  bool get complete => currentStep == 'COMPLETE';

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse((value ?? '0').toString()) ?? 0;
  }

  static int? _toIntOrNull(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse(value.toString());
  }

  static double? _toDoubleOrNull(dynamic value) {
    if (value == null) return null;
    if (value is double) return value;
    if (value is int) return value.toDouble();
    return double.tryParse(value.toString());
  }
}
