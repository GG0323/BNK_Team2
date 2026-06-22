class ProductModel {
  final int productNo;
  final String productName;
  final String productType;
  final String productStatus;
  final double minInterestRate;
  final double maxInterestRate;
  final String mobileJoinYn;
  final String branchJoinYn;
  final String content;
  final String? imageUrl;
  final String? interestCalcType;

  ProductModel({
    required this.productNo,
    required this.productName,
    required this.productType,
    required this.productStatus,
    required this.minInterestRate,
    required this.maxInterestRate,
    required this.mobileJoinYn,
    required this.branchJoinYn,
    required this.content,
    this.imageUrl,
    this.interestCalcType,
  });

  factory ProductModel.fromJson(Map<String, dynamic> json) {
    return ProductModel(
      productNo: json['productNo'] ?? json['product_no'] ?? 0,
      productName: (json['productName'] ?? json['product_name'] ?? '').toString(),
      productType: (json['productType'] ?? json['product_type'] ?? '').toString(),
      productStatus: (json['productStatus'] ?? json['product_status'] ?? '').toString(),
      minInterestRate: _toDouble(
        json['minInterestRate'] ?? json['min_interest_rate'],
      ),
      maxInterestRate: _toDouble(
        json['maxInterestRate'] ?? json['max_interest_rate'],
      ),
      mobileJoinYn: (json['mobileJoinYn'] ?? json['mobile_join_yn'] ?? 'N').toString(),
      branchJoinYn: (json['branchJoinYn'] ?? json['branch_join_yn'] ?? 'N').toString(),
      content: (json['content'] ?? '').toString(),
      imageUrl: (json['imageUrl'] ?? json['image_url'])?.toString(),
      interestCalcType:
          (json['interestCalcType'] ?? json['interest_calc_type'])?.toString(),
    );
  }

  static double _toDouble(dynamic value) {
    if (value == null) return 0.0;

    if (value is int) {
      return value.toDouble();
    }

    if (value is double) {
      return value;
    }

    return double.tryParse(value.toString()) ?? 0.0;
  }

  bool get isDeposit => productType == 'DEPOSIT';
  bool get isSaving => productType == 'SAVINGS';

  String get productTypeText {
    if (productType == 'DEPOSIT') return '예금';
    if (productType == 'SAVINGS') return '적금';
    return productType;
  }

  String get joinMethodText {
    final methods = <String>[];

    if (mobileJoinYn == 'Y') {
      methods.add('모바일');
    }

    if (branchJoinYn == 'Y') {
      methods.add('영업점');
    }

    if (methods.isEmpty) {
      return '가입채널 확인';
    }

    return methods.join(' · ');
  }
}