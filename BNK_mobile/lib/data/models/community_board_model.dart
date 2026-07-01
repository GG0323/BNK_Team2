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
  final String updatedAt;
  final bool modified;

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
    required this.updatedAt,
    required this.modified,
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
