class MyPageModel {
  final String memberName;
  final String loginId;
  final String memberType;
  final String memberStatus;
  final int accountCount;
  final int productCount;
  final int logCount;
  final int totalBalance;

  MyPageModel({
    required this.memberName,
    required this.loginId,
    required this.memberType,
    required this.memberStatus,
    required this.accountCount,
    required this.productCount,
    required this.logCount,
    required this.totalBalance,
  });

  factory MyPageModel.fromJson(Map<String, dynamic> json) {
    final member = json['member'] ?? {};

    return MyPageModel(
      memberName: member['member_name'] ?? member['memberName'] ?? '',
      loginId: member['login_id'] ?? member['loginId'] ?? '',
      memberType: member['member_type'] ?? member['memberType'] ?? '',
      memberStatus: member['member_status'] ?? member['memberStatus'] ?? '',
      accountCount: json['accountCount'] ?? 0,
      productCount: json['productCount'] ?? 0,
      logCount: json['logCount'] ?? 0,
      totalBalance: json['totalBalance'] ?? 0,
    );
  }
}