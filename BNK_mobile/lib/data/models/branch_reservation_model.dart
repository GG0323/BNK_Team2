class BranchReservationModel {
  final int reservationId;
  final int memberNo;
  final int branchId;
  final String reservedAt;
  final String bizType;
  final String purpose;
  final String status;
  final String? createdAt;
  final String? updatedAt;
  final String memberName;
  final String branchName;

  const BranchReservationModel({
    required this.reservationId,
    required this.memberNo,
    required this.branchId,
    required this.reservedAt,
    required this.bizType,
    required this.purpose,
    required this.status,
    this.createdAt,
    this.updatedAt,
    required this.memberName,
    required this.branchName,
  });

  factory BranchReservationModel.fromJson(Map<String, dynamic> json) {
    return BranchReservationModel(
      reservationId: _toInt(json['reservation_id'] ?? json['reservationId']),
      memberNo: _toInt(json['member_no'] ?? json['memberNo']),
      branchId: _toInt(json['branch_id'] ?? json['branchId']),
      reservedAt: json['reserved_at']?.toString() ?? json['reservedAt']?.toString() ?? '',
      bizType: json['biz_type']?.toString() ?? json['bizType']?.toString() ?? '',
      purpose: json['purpose']?.toString() ?? '',
      status: json['status']?.toString() ?? '',
      createdAt: json['created_at']?.toString() ?? json['createdAt']?.toString(),
      updatedAt: json['updated_at']?.toString() ?? json['updatedAt']?.toString(),
      memberName: json['member_name']?.toString() ?? json['memberName']?.toString() ?? '',
      branchName: json['branch_name']?.toString() ?? json['branchName']?.toString() ?? '',
    );
  }

  String get statusText {
    switch (status) {
      case 'PENDING':
        return '예약 접수';
      case 'CONFIRMED':
        return '예약 확정';
      case 'CANCELED':
        return '예약 취소';
      case 'REJECTED':
        return '예약 거절';
      case 'REASSIGNED':
        return '예약 변경';
      default:
        return status;
    }
  }

  String get bizTypeText {
    switch (bizType) {
      case 'DEPOSIT':
        return '예금 상담';
      case 'LOAN':
        return '대출 상담';
      case 'CARD':
        return '카드 상담';
      case 'FX':
        return '외환 상담';
      case 'ETC':
        return '기타 상담';
      default:
        return bizType;
    }
  }

  static int _toInt(dynamic value) {
    if (value == null) return 0;
    if (value is int) return value;
    if (value is double) return value.toInt();
    return int.tryParse(value.toString()) ?? 0;
  }
}