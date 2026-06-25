class ProductJoinAccount {
  final int? accountNo;
  final String accountNumber;
  final String alias;
  final int balance;
  final String status;

  const ProductJoinAccount({
    required this.accountNo,
    required this.accountNumber,
    required this.alias,
    required this.balance,
    required this.status,
  });

  factory ProductJoinAccount.fromJson(Map<String, dynamic> json) {
    int parseInt(dynamic value) {
      if (value == null) return 0;
      if (value is int) return value;
      if (value is double) return value.toInt();
      return int.tryParse(value.toString()) ?? 0;
    }

    return ProductJoinAccount(
      accountNo: parseInt(
        json['accountNo'] ??
            json['account_no'] ??
            json['ACCOUNT_NO'],
      ),
      accountNumber: (
        json['accountNumber'] ??
            json['account_number'] ??
            json['ACCOUNT_NUMBER'] ??
            ''
      ).toString(),
      alias: (
        json['accountAlias'] ??
            json['alias'] ??
            json['accountName'] ??
            json['account_name'] ??
            json['ALIAS'] ??
            '입출금계좌'
      ).toString(),
      balance: parseInt(
        json['balance'] ??
            json['BALANCE'],
      ),
      status: (
        json['accountStatus'] ??
            json['status'] ??
            json['ACCOUNT_STATUS'] ??
            json['STATUS'] ??
            'ACTIVE'
      ).toString(),
    );
  }
}