class MemberModel {
  final int memberNo;
  final String loginId;
  final String memberName;
  final String memberType;
  final String memberStatus;
  final String? email;
  final String? phoneNumber;

  MemberModel({
    required this.memberNo,
    required this.loginId,
    required this.memberName,
    required this.memberType,
    required this.memberStatus,
    this.email,
    this.phoneNumber,
  });

  factory MemberModel.fromJson(Map<String, dynamic> json) {
    return MemberModel(
      memberNo: json['memberNo'] ?? json['member_no'] ?? 0,
      loginId: json['loginId'] ?? json['login_id'] ?? '',
      memberName: json['memberName'] ?? json['member_name'] ?? '',
      memberType: json['memberType'] ?? json['member_type'] ?? '',
      memberStatus: json['memberStatus'] ?? json['member_status'] ?? '',
      email: json['email'],
      phoneNumber: json['phoneNumber'] ?? json['phone_number'],
    );
  }
}
