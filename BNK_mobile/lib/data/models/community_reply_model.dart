class CommunityReplyModel {
  final int replyNo;
  final int boardNo;
  final int communityAccountNo;
  final String content;
  final String nickname;
  final String createdAt;

  const CommunityReplyModel({
    required this.replyNo,
    required this.boardNo,
    required this.communityAccountNo,
    required this.content,
    required this.nickname,
    required this.createdAt,
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
    );
  }

  static int _toInt(dynamic value) {
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value?.toString() ?? '') ?? 0;
  }
}
