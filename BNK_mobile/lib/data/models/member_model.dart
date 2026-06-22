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
      memberNo: json['memberNo'] ?? 0,
      loginId: json['loginId'] ?? '',
      memberName: json['memberName'] ?? '',
      memberType: json['memberType'] ?? '',
      memberStatus: json['memberStatus'] ?? '',
      email: json['email'],
      phoneNumber: json['phoneNumber'],
    );
  }
}