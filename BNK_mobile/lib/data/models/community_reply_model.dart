class CommunityReplyModel {
  final int replyNo;
  final int boardNo;
  final int communityAccountNo;
  final String content;
  final String nickname;
  final String createdAt;
  final String updatedAt;
  final bool modified;

  const CommunityReplyModel({
    required this.replyNo,
    required this.boardNo,
    required this.communityAccountNo,
    required this.content,
    required this.nickname,
    required this.createdAt,
    required this.updatedAt,
    required this.modified,
  });

  factory CommunityReplyModel.fromJson(Map<String, dynamic> json) {
    return CommunityReplyModel(
      replyNo: _toInt(json['replyNo'] ?? json['reply_no']),
      boardNo: _toInt(json['boardNo'] ?? json['board_no']),
      communityAccountNo: _toInt(
        json['communityAccountNo'] ?? json['community_account_no'],
      ),
      content: (json['replyContent'] ?? json['reply_content'] ?? '').toString(),
      nickname: (json['nickname'] ?? '익명').toString(),
      createdAt: (json['createdAt'] ?? json['created_at'] ?? '').toString(),
      updatedAt: (json['updatedAt'] ?? json['updated_at'] ?? '').toString(),
      modified: _toBool(json['modifiedYn'] ?? json['modified_yn']),
    );
  }

  String get displayDate {
    if (!modified) return createdAt;

    return '$createdAt · $updatedAt (수정)';
  }

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value?.toString() ?? '') ?? 0;
  }

  static bool _toBool(dynamic value) {
    if (value is bool) return value;
    final text = value?.toString().toUpperCase();
    return text == 'Y' || text == 'TRUE' || text == '1';
  }
}
