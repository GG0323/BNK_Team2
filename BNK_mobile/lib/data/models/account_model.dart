class AccountModel {
  final int accountNo;
  final int memberNo;
  final String accountNumber;
  final String accountAlias;
  final int balance;
  final String accountStatus;
  final String? openedAt;

  AccountModel({
    required this.accountNo,
    required this.memberNo,
    required this.accountNumber,
    required this.accountAlias,
    required this.balance,
    required this.accountStatus,
    this.openedAt,
  });

  factory AccountModel.fromJson(Map<String, dynamic> json) {
    return AccountModel(
      accountNo: json['accountNo'] ?? json['account_no'] ?? 0,
      memberNo: json['memberNo'] ?? json['member_no'] ?? 0,
      accountNumber:
          (json['accountNumber'] ?? json['account_number'] ?? '').toString(),
      accountAlias:
          (json['accountAlias'] ?? json['account_alias'] ?? '입출금 계좌').toString(),
      balance: json['balance'] ?? 0,
      accountStatus:
          (json['accountStatus'] ?? json['account_status'] ?? '').toString(),
      openedAt: (json['openedAt'] ?? json['opened_at'])?.toString(),
    );
  }
}