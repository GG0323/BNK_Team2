class BranchModel {
  final int branchId;
  final String branchName;
  final String branchCode;
  final String address;
  final String phoneNumber;
  final double latitude;
  final double longitude;
  final String status;
  final String? createdAt;
  final String? updatedAt;

  const BranchModel({
    required this.branchId,
    required this.branchName,
    required this.branchCode,
    required this.address,
    required this.phoneNumber,
    required this.latitude,
    required this.longitude,
    required this.status,
    this.createdAt,
    this.updatedAt,
  });

  factory BranchModel.fromJson(Map<String, dynamic> json) {
    return BranchModel(
      branchId: _toInt(json['branch_id'] ?? json['branchId']),
      branchName: json['branch_name'] ?? json['branchName'] ?? '',
      branchCode: json['branch_code'] ?? json['branchCode'] ?? '',
      address: json['address'] ?? '',
      phoneNumber: json['phone_number'] ?? json['phoneNumber'] ?? '',
      latitude: _toDouble(json['latitude']),
      longitude: _toDouble(json['longitude']),
      status: json['status'] ?? '',
      createdAt: json['created_at']?.toString() ?? json['createdAt']?.toString(),
      updatedAt: json['updated_at']?.toString() ?? json['updatedAt']?.toString(),
    );
  }

  static int _toInt(dynamic value) {
    if (value == null) return 0;
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse(value.toString()) ?? 0;
  }

  static double _toDouble(dynamic value) {
    if (value == null) return 0;
    if (value is double) return value;
    if (value is int) return value.toDouble();
    return double.tryParse(value.toString()) ?? 0;
  }
}