class ProductAiRecommendRequest {
  final int age;
  final int balance;
  final int monthlyAmount;
  final int periodMonths;
  final String purpose;
  final String preferredProductType;
  final String preferredChannel;
  final List<String> interestConditions;

  ProductAiRecommendRequest({
    required this.age,
    required this.balance,
    required this.monthlyAmount,
    required this.periodMonths,
    required this.purpose,
    required this.preferredProductType,
    required this.preferredChannel,
    required this.interestConditions,
  });

  Map<String, dynamic> toJson() {
    return {
      'age': age,
      'balance': balance,
      'monthlyAmount': monthlyAmount,
      'periodMonths': periodMonths,
      'purpose': purpose,
      'preferredProductType': preferredProductType,
      'preferredChannel': preferredChannel,
      'interestConditions': interestConditions,
    };
  }
}

class ProductAiRecommendResponse {
  final String summary;
  final List<ProductAiRecommendItem> recommendedProducts;

  ProductAiRecommendResponse({
    required this.summary,
    required this.recommendedProducts,
  });

  factory ProductAiRecommendResponse.fromJson(Map<String, dynamic> json) {
    final data = json['data'];

    if (data is Map<String, dynamic>) {
      return ProductAiRecommendResponse.fromDataJson(data);
    }

    return ProductAiRecommendResponse.fromDataJson(json);
  }

  factory ProductAiRecommendResponse.fromDataJson(Map<String, dynamic> json) {
    final products = json['recommendedProducts'] ??
        json['recommended_products'] ??
        json['products'] ??
        [];

    return ProductAiRecommendResponse(
      summary: (json['summary'] ?? '').toString(),
      recommendedProducts: products is List
          ? products
          .map(
            (item) => ProductAiRecommendItem.fromJson(
          item as Map<String, dynamic>,
        ),
      )
          .toList()
          : [],
    );
  }
}

class ProductAiRecommendItem {
  final int productNo;
  final String productName;
  final String productType;
  final String subtitle;

  final double minInterestRate;
  final double maxInterestRate;

  final int minJoinAmount;
  final int maxJoinAmount;

  final String branchJoinYn;
  final String internetJoinYn;
  final String mobileJoinYn;

  final int score;
  final int fitPercent;
  final int benefitChancePercent;

  final String reason;
  final List<String> evidence;
  final String detailUrl;

  ProductAiRecommendItem({
    required this.productNo,
    required this.productName,
    required this.productType,
    required this.subtitle,
    required this.minInterestRate,
    required this.maxInterestRate,
    required this.minJoinAmount,
    required this.maxJoinAmount,
    required this.branchJoinYn,
    required this.internetJoinYn,
    required this.mobileJoinYn,
    required this.score,
    required this.fitPercent,
    required this.benefitChancePercent,
    required this.reason,
    required this.evidence,
    required this.detailUrl,
  });

  factory ProductAiRecommendItem.fromJson(Map<String, dynamic> json) {
    return ProductAiRecommendItem(
      productNo: _toInt(json['productNo'] ?? json['product_no']),
      productName: (json['productName'] ?? json['product_name'] ?? '').toString(),
      productType: (json['productType'] ?? json['product_type'] ?? '').toString(),
      subtitle: (json['subtitle'] ?? '').toString(),
      minInterestRate: _toDouble(
        json['minInterestRate'] ?? json['min_interest_rate'],
      ),
      maxInterestRate: _toDouble(
        json['maxInterestRate'] ?? json['max_interest_rate'],
      ),
      minJoinAmount: _toInt(
        json['minJoinAmount'] ?? json['min_join_amount'],
      ),
      maxJoinAmount: _toInt(
        json['maxJoinAmount'] ?? json['max_join_amount'],
      ),
      branchJoinYn: (json['branchJoinYn'] ?? json['branch_join_yn'] ?? 'N')
          .toString(),
      internetJoinYn:
      (json['internetJoinYn'] ?? json['internet_join_yn'] ?? 'N')
          .toString(),
      mobileJoinYn: (json['mobileJoinYn'] ?? json['mobile_join_yn'] ?? 'N')
          .toString(),
      score: _toInt(json['score']),
      fitPercent: _toInt(
        json['fitPercent'] ?? json['fit_percent'] ?? json['score'],
      ),
      benefitChancePercent: _toInt(
        json['benefitChancePercent'] ??
            json['benefit_chance_percent'] ??
            0,
      ),
      reason: (json['reason'] ?? '').toString(),
      evidence: _toStringList(json['evidence']),
      detailUrl: (json['detailUrl'] ?? json['detail_url'] ?? '').toString(),
    );
  }

  String get productTypeText {
    if (productType == 'DEPOSIT') return '예금';
    if (productType == 'SAVINGS') return '적금';
    return productType.isEmpty ? '상품' : productType;
  }

  String get joinMethodText {
    final methods = <String>[];

    if (mobileJoinYn == 'Y') {
      methods.add('모바일');
    }

    if (internetJoinYn == 'Y') {
      methods.add('인터넷');
    }

    if (branchJoinYn == 'Y') {
      methods.add('영업점');
    }

    if (methods.isEmpty) {
      return '가입채널 확인';
    }

    return methods.join(' · ');
  }

  static int _toInt(dynamic value) {
    if (value == null) return 0;

    if (value is int) {
      return value;
    }

    if (value is double) {
      return value.toInt();
    }

    return int.tryParse(value.toString()) ?? 0;
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

  static List<String> _toStringList(dynamic value) {
    if (value == null) return [];

    if (value is List) {
      return value.map((item) => item.toString()).toList();
    }

    return [];
  }
}