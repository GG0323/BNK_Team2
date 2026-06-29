class CommunityProfileModel {
  final bool isMember;
  final int communityAccountNo;
  final String? nickname;

  const CommunityProfileModel({
    required this.isMember,
    this.communityAccountNo = 0,
    this.nickname,
  });

  factory CommunityProfileModel.fromJson(Map<String, dynamic> json) {
    final data = json['data'];
    final source = data is Map<String, dynamic> ? data : json;

    return CommunityProfileModel(
      isMember:
          source['isMember'] == true ||
          source['is_member'] == true ||
          source['hasCommunityAccount'] == true ||
          source['has_community_account'] == true,
      communityAccountNo: _toInt(
        source['communityAccountNo'] ?? source['community_account_no'],
      ),
      nickname: source['nickname']?.toString(),
    );
  }

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value?.toString() ?? '') ?? 0;
  }
}
