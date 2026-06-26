class AccountModel {
  final int accountNo;
  final int memberNo;
  final String accountNumber;
  final String accountAlias;
  final int balance;
  final String accountStatus;
  final String? accountPurpose;
  final String? openedAt;

  AccountModel({
    required this.accountNo,
    required this.memberNo,
    required this.accountNumber,
    required this.accountAlias,
    required this.balance,
    required this.accountStatus,
    this.accountPurpose,
    this.openedAt,
  });

  factory AccountModel.fromJson(Map<String, dynamic> json) {
    return AccountModel(
      accountNo: _toInt(json['accountNo'] ?? json['account_no']),
      memberNo: _toInt(json['memberNo'] ?? json['member_no']),
      accountNumber: (json['accountNumber'] ?? json['account_number'] ?? '')
          .toString(),
      accountAlias: (json['accountAlias'] ?? json['account_alias'] ?? '입출금 계좌')
          .toString(),
      balance: _toInt(json['balance']),
      accountStatus: (json['accountStatus'] ?? json['account_status'] ?? '')
          .toString(),
      accountPurpose: (json['accountPurpose'] ?? json['account_purpose'])
          ?.toString(),
      openedAt: (json['openedAt'] ?? json['opened_at'])?.toString(),
    );
  }

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse((value ?? '0').toString()) ?? 0;
  }
}
