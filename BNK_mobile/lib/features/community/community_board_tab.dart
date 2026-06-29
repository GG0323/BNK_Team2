import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/community_board_model.dart';
import '../../data/models/community_reply_model.dart';
import '../../data/services/community_api.dart';

class CommunityBoardTab extends StatefulWidget {
  const CommunityBoardTab({super.key});

  @override
  State<CommunityBoardTab> createState() => _CommunityBoardTabState();
}

class _CommunityBoardTabState extends State<CommunityBoardTab> {
  final CommunityApi _communityApi = CommunityApi();
  final TextEditingController _searchController = TextEditingController();

  late Future<List<CommunityBoardModel>> _boardsFuture;
  String _sort = 'latest';

  @override
  void initState() {
    super.initState();
    _boardsFuture = _loadBoards();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<List<CommunityBoardModel>> _loadBoards() {
    return _communityApi.getBoards(
      sort: _sort,
      keyword: _searchController.text,
    );
  }

  void _refresh() {
    setState(() {
      _boardsFuture = _loadBoards();
    });
  }

  Future<void> _openWriteDialog() async {
    final titleController = TextEditingController();
    final contentController = TextEditingController();

    final created = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        final bottomInset = MediaQuery.of(dialogContext).viewInsets.bottom;

        return Padding(
          padding: EdgeInsets.only(bottom: bottomInset),
          child: AlertDialog(
            scrollable: true,
            insetPadding: const EdgeInsets.symmetric(
              horizontal: 24,
              vertical: 24,
            ),
            title: const Text('게시글 작성'),
            content: SingleChildScrollView(
              keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: titleController,
                    decoration: const InputDecoration(labelText: '제목'),
                    maxLength: 80,
                    textInputAction: TextInputAction.next,
                  ),
                  TextField(
                    controller: contentController,
                    decoration: const InputDecoration(labelText: '내용'),
                    keyboardType: TextInputType.multiline,
                    minLines: 3,
                    maxLines: 5,
                  ),
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(dialogContext, false),
                child: const Text('취소'),
              ),
              ElevatedButton(
                onPressed: () async {
                  try {
                    await _communityApi.createBoard(
                      title: titleController.text.trim(),
                      content: contentController.text.trim(),
                    );

                    if (dialogContext.mounted) {
                      Navigator.pop(dialogContext, true);
                    }
                  } catch (error) {
                    if (dialogContext.mounted) {
                      ScaffoldMessenger.of(dialogContext).showSnackBar(
                        SnackBar(
                          content: Text(
                            error.toString().replaceFirst('Exception: ', ''),
                          ),
                          behavior: SnackBarBehavior.floating,
                        ),
                      );
                    }
                  }
                },
                child: const Text('등록'),
              ),
            ],
          ),
        );
      },
    );

    titleController.dispose();
    contentController.dispose();

    if (created == true) {
      _refresh();
    }
  }

  Future<void> _openDetail(CommunityBoardModel preview) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.cardBackground,
      builder: (_) {
        return _BoardDetailSheet(
          boardNo: preview.boardNo,
          communityApi: _communityApi,
        );
      },
    );

    _refresh();
  }

  Widget _buildSortButton(String value, String label) {
    final selected = _sort == value;

    return ChoiceChip(
      label: Text(label),
      selected: selected,
      selectedColor: const Color(0xFFFFE7E7),
      onSelected: (_) {
        setState(() {
          _sort = value;
          _boardsFuture = _loadBoards();
        });
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(18, 16, 18, 8),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: '게시글 검색',
                    prefixIcon: const Icon(Icons.search),
                    filled: true,
                    fillColor: AppColors.cardBackground,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: const BorderSide(color: AppColors.border),
                    ),
                  ),
                  onSubmitted: (_) => _refresh(),
                ),
              ),
              const SizedBox(width: 10),
              IconButton.filled(
                tooltip: '검색',
                onPressed: _refresh,
                icon: const Icon(Icons.search),
              ),
              const SizedBox(width: 8),
              IconButton.filled(
                tooltip: '글쓰기',
                onPressed: _openWriteDialog,
                icon: const Icon(Icons.edit_outlined),
              ),
            ],
          ),
        ),
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 6),
          child: Row(
            children: [
              _buildSortButton('latest', '최신순'),
              const SizedBox(width: 8),
              _buildSortButton('likes', '인기순'),
              const SizedBox(width: 8),
              _buildSortButton('oldest', '오래된순'),
            ],
          ),
        ),
        Expanded(
          child: FutureBuilder<List<CommunityBoardModel>>(
            future: _boardsFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(
                  child: CircularProgressIndicator(color: AppColors.primaryRed),
                );
              }

              if (snapshot.hasError) {
                return _BoardMessage(
                  icon: Icons.error_outline,
                  message: snapshot.error.toString().replaceFirst(
                    'Exception: ',
                    '',
                  ),
                  actionText: '다시 시도',
                  onActionTap: _refresh,
                );
              }

              final boards = snapshot.data ?? const [];

              if (boards.isEmpty) {
                return _BoardMessage(
                  icon: Icons.forum_outlined,
                  message: '아직 게시글이 없습니다.',
                  actionText: '첫 글 작성',
                  onActionTap: _openWriteDialog,
                );
              }

              return RefreshIndicator(
                onRefresh: () async => _refresh(),
                child: ListView.separated(
                  keyboardDismissBehavior:
                      ScrollViewKeyboardDismissBehavior.onDrag,
                  padding: const EdgeInsets.fromLTRB(18, 10, 18, 28),
                  itemCount: boards.length,
                  separatorBuilder: (context, index) =>
                      const SizedBox(height: 12),
                  itemBuilder: (context, index) {
                    return _BoardCard(
                      board: boards[index],
                      onTap: () => _openDetail(boards[index]),
                    );
                  },
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

class _BoardCard extends StatelessWidget {
  final CommunityBoardModel board;
  final VoidCallback onTap;

  const _BoardCard({required this.board, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              board.title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.w900,
                color: AppColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              board.content,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                height: 1.4,
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: AppColors.textSecondary,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: Text(
                    board.nickname,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ),
                _CountChip(icon: Icons.favorite_border, value: board.likeCount),
                const SizedBox(width: 8),
                _CountChip(
                  icon: Icons.visibility_outlined,
                  value: board.viewCount,
                ),
                const SizedBox(width: 8),
                _CountChip(
                  icon: Icons.comment_outlined,
                  value: board.replyCount,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _BoardDetailSheet extends StatefulWidget {
  final int boardNo;
  final CommunityApi communityApi;

  const _BoardDetailSheet({required this.boardNo, required this.communityApi});

  @override
  State<_BoardDetailSheet> createState() => _BoardDetailSheetState();
}

class _BoardDetailSheetState extends State<_BoardDetailSheet> {
  final TextEditingController _replyController = TextEditingController();

  late Future<void> _future;
  CommunityBoardModel? _board;
  List<CommunityReplyModel> _replies = const [];

  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  @override
  void dispose() {
    _replyController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    final board = await widget.communityApi.getBoard(widget.boardNo);
    final replies = await widget.communityApi.getReplies(widget.boardNo);

    if (!mounted) return;

    setState(() {
      _board = board;
      _replies = replies;
    });
  }

  Future<void> _like() async {
    final board = await widget.communityApi.likeBoard(widget.boardNo);

    if (!mounted) return;

    setState(() {
      _board = board;
    });
  }

  Future<void> _addReply() async {
    final content = _replyController.text.trim();
    if (content.isEmpty) return;

    try {
      await widget.communityApi.createReply(
        boardNo: widget.boardNo,
        content: content,
      );
      _replyController.clear();
      await _load();
    } catch (error) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString().replaceFirst('Exception: ', '')),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return AnimatedPadding(
      duration: const Duration(milliseconds: 180),
      curve: Curves.easeOut,
      padding: EdgeInsets.only(bottom: bottomInset),
      child: DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.88,
        minChildSize: 0.45,
        maxChildSize: 0.95,
        builder: (context, scrollController) {
          return FutureBuilder<void>(
            future: _future,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return const Center(
                  child: CircularProgressIndicator(color: AppColors.primaryRed),
                );
              }

              if (snapshot.hasError || _board == null) {
                return _BoardMessage(
                  icon: Icons.error_outline,
                  message:
                      snapshot.error?.toString().replaceFirst(
                        'Exception: ',
                        '',
                      ) ??
                      '게시글을 불러오지 못했습니다.',
                );
              }

              final board = _board!;

              return ListView(
                controller: scrollController,
                keyboardDismissBehavior:
                    ScrollViewKeyboardDismissBehavior.onDrag,
                padding: const EdgeInsets.fromLTRB(22, 18, 22, 22),
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        child: Text(
                          board.title,
                          style: const TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.w900,
                            color: AppColors.textPrimary,
                          ),
                        ),
                      ),
                      IconButton(
                        tooltip: '닫기',
                        onPressed: () => Navigator.pop(context),
                        icon: const Icon(Icons.close_rounded),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${board.nickname} · ${board.createdAt}',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 18),
                  Text(
                    board.content,
                    style: const TextStyle(
                      fontSize: 15,
                      height: 1.55,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 18),
                  Wrap(
                    spacing: 10,
                    crossAxisAlignment: WrapCrossAlignment.center,
                    children: [
                      OutlinedButton.icon(
                        onPressed: _like,
                        icon: const Icon(Icons.favorite_border),
                        label: Text('좋아요 ${board.likeCount}'),
                      ),
                      _CountChip(
                        icon: Icons.visibility_outlined,
                        value: board.viewCount,
                      ),
                    ],
                  ),
                  const Divider(height: 34),
                  const Text(
                    '댓글',
                    style: TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w900,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 12),
                  for (final reply in _replies)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: _ReplyTile(reply: reply),
                    ),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _replyController,
                          decoration: InputDecoration(
                            hintText: '댓글 입력',
                            filled: true,
                            fillColor: AppColors.background,
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(14),
                              borderSide: const BorderSide(
                                color: AppColors.border,
                              ),
                            ),
                          ),
                          keyboardType: TextInputType.multiline,
                          minLines: 1,
                          maxLines: 3,
                        ),
                      ),
                      const SizedBox(width: 10),
                      IconButton.filled(
                        tooltip: '댓글 등록',
                        onPressed: _addReply,
                        icon: const Icon(Icons.send_rounded),
                      ),
                    ],
                  ),
                ],
              );
            },
          );
        },
      ),
    );
  }
}

class _ReplyTile extends StatelessWidget {
  final CommunityReplyModel reply;

  const _ReplyTile({required this.reply});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            reply.nickname,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            reply.content,
            style: const TextStyle(
              fontSize: 14,
              height: 1.4,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }
}

class _CountChip extends StatelessWidget {
  final IconData icon;
  final int value;

  const _CountChip({required this.icon, required this.value});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 16, color: AppColors.textSecondary),
        const SizedBox(width: 3),
        Text(
          '$value',
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: AppColors.textSecondary,
          ),
        ),
      ],
    );
  }
}

class _BoardMessage extends StatelessWidget {
  final IconData icon;
  final String message;
  final String? actionText;
  final VoidCallback? onActionTap;

  const _BoardMessage({
    required this.icon,
    required this.message,
    this.actionText,
    this.onActionTap,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: AppColors.primaryRed, size: 40),
            const SizedBox(height: 12),
            Text(
              message,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary,
              ),
            ),
            if (actionText != null && onActionTap != null) ...[
              const SizedBox(height: 16),
              ElevatedButton(onPressed: onActionTap, child: Text(actionText!)),
            ],
          ],
        ),
      ),
    );
  }
}
