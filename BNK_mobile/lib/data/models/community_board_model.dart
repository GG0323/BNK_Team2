class CommunityBoardModel {
  final int boardNo;
  final int communityAccountNo;
  final String title;
  final String content;
  final int likeCount;
  final int viewCount;
  final int replyCount;
  final String nickname;
  final String createdAt;

  const CommunityBoardModel({
    required this.boardNo,
    required this.communityAccountNo,
    required this.title,
    required this.content,
    required this.likeCount,
    required this.viewCount,
    required this.replyCount,
    required this.nickname,
    required this.createdAt,
  });

  factory CommunityBoardModel.fromJson(Map<String, dynamic> json) {
    return CommunityBoardModel(
      boardNo: _toInt(json['boardNo'] ?? json['board_no']),
      communityAccountNo: _toInt(
        json['communityAccountNo'] ?? json['community_account_no'],
      ),
      title: (json['boardTitle'] ?? json['board_title'] ?? '').toString(),
      content: (json['boardContent'] ?? json['board_content'] ?? '').toString(),
      likeCount: _toInt(json['likeCount'] ?? json['like_count']),
      viewCount: _toInt(json['viewCount'] ?? json['view_count']),
      replyCount: _toInt(json['replyCount'] ?? json['reply_count']),
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
