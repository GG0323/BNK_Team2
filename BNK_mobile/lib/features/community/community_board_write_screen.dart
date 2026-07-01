import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/services/community_api.dart';

class CommunityBoardWriteScreen extends StatefulWidget {
  const CommunityBoardWriteScreen({super.key});

  @override
  State<CommunityBoardWriteScreen> createState() =>
      _CommunityBoardWriteScreenState();
}

class _CommunityBoardWriteScreenState extends State<CommunityBoardWriteScreen> {
  final CommunityApi _communityApi = CommunityApi();
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _contentController = TextEditingController();

  bool _submitting = false;

  @override
  void dispose() {
    _titleController.dispose();
    _contentController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final title = _titleController.text.trim();
    final content = _contentController.text.trim();

    if (title.isEmpty) {
      _showMessage('제목을 입력해주세요.');
      return;
    }

    if (content.isEmpty) {
      _showMessage('내용을 입력해주세요.');
      return;
    }

    setState(() => _submitting = true);

    try {
      await _communityApi.createBoard(title: title, content: content);

      if (!mounted) return;

      Navigator.pop(context, true);
    } catch (error) {
      _showMessage(error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: true,
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.cardBackground,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        title: const Text(
          '게시글 작성',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: ListView(
                keyboardDismissBehavior:
                    ScrollViewKeyboardDismissBehavior.onDrag,
                padding: const EdgeInsets.fromLTRB(18, 18, 18, 20),
                children: [
                  Container(
                    padding: const EdgeInsets.fromLTRB(18, 16, 18, 18),
                    decoration: BoxDecoration(
                      color: AppColors.cardBackground,
                      borderRadius: BorderRadius.circular(18),
                      border: Border.all(color: AppColors.border),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        TextField(
                          controller: _titleController,
                          maxLength: 80,
                          textInputAction: TextInputAction.next,
                          decoration: const InputDecoration(
                            labelText: '제목',
                            border: UnderlineInputBorder(),
                            counterText: '',
                          ),
                        ),
                        const SizedBox(height: 16),
                        TextField(
                          controller: _contentController,
                          keyboardType: TextInputType.multiline,
                          textInputAction: TextInputAction.newline,
                          minLines: 12,
                          maxLines: 18,
                          decoration: const InputDecoration(
                            labelText: '내용',
                            alignLabelWithHint: true,
                            border: OutlineInputBorder(),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(18, 12, 18, 14),
              decoration: const BoxDecoration(
                color: AppColors.background,
                border: Border(top: BorderSide(color: AppColors.border)),
              ),
              child: SizedBox(
                height: 52,
                child: ElevatedButton(
                  onPressed: _submitting ? null : _submit,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryRed,
                    foregroundColor: AppColors.white,
                    elevation: 0,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  child: _submitting
                      ? const SizedBox(
                          width: 22,
                          height: 22,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            color: AppColors.white,
                          ),
                        )
                      : const Text(
                          '등록',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
