class ProductJoinEntryStatusModel {
  final int memberNo;
  final String memberStatus;
  final bool regularMember;
  final bool hasActiveAccount;
  final bool accountRequired;
  final int? activeAccountNo;
  final int productNo;
  final String productName;
  final String productType;
  final bool joinableProduct;
  final bool canEnterJoin;
  final String message;

  ProductJoinEntryStatusModel({
    required this.memberNo,
    required this.memberStatus,
    required this.regularMember,
    required this.hasActiveAccount,
    required this.accountRequired,
    required this.activeAccountNo,
    required this.productNo,
    required this.productName,
    required this.productType,
    required this.joinableProduct,
    required this.canEnterJoin,
    required this.message,
  });

  factory ProductJoinEntryStatusModel.fromJson(Map<String, dynamic> json) {
    return ProductJoinEntryStatusModel(
      memberNo: _toInt(json['memberNo']),
      memberStatus: (json['memberStatus'] ?? '').toString(),
      regularMember: json['regularMember'] == true,
      hasActiveAccount: json['hasActiveAccount'] == true,
      accountRequired: json['accountRequired'] == true,
      activeAccountNo: _toIntOrNull(json['activeAccountNo']),
      productNo: _toInt(json['productNo']),
      productName: (json['productName'] ?? '').toString(),
      productType: (json['productType'] ?? '').toString(),
      joinableProduct: json['joinableProduct'] == true,
      canEnterJoin: json['canEnterJoin'] == true,
      message: (json['message'] ?? '').toString(),
    );
  }

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
}
