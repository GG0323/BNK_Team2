import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/community_profile_model.dart';
import '../../data/services/community_api.dart';
import 'community_join_screen.dart';

class CommunityMoreSheet extends StatelessWidget {
  final CommunityProfileModel profile;
  final Future<void> Function() onProfileChanged;

  const CommunityMoreSheet({
    super.key,
    required this.profile,
    required this.onProfileChanged,
  });

  Future<void> _updateNickname(BuildContext context) async {
    final controller = TextEditingController(text: profile.nickname ?? '');
    final api = CommunityApi();

    final updated = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        final bottomInset = MediaQuery.of(dialogContext).viewInsets.bottom;

        return Padding(
          padding: EdgeInsets.only(bottom: bottomInset),
          child: AlertDialog(
            scrollable: true,
            title: const Text('닉네임 수정'),
            content: SingleChildScrollView(
              keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
              child: TextField(
                controller: controller,
                maxLength: 30,
                textInputAction: TextInputAction.done,
                decoration: const InputDecoration(labelText: '새 닉네임'),
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
                    await api.updateNickname(nickname: controller.text.trim());

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
                child: const Text('저장'),
              ),
            ],
          ),
        );
      },
    );

    controller.dispose();

    if (updated == true) {
      await onProfileChanged();
      if (context.mounted) {
        Navigator.pop(context);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final nickname = profile.nickname?.isNotEmpty == true
        ? profile.nickname!
        : '비회원';
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return Container(
      decoration: const BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(24),
          topRight: Radius.circular(24),
        ),
      ),
      child: SafeArea(
        top: false,
        child: SingleChildScrollView(
          keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
          padding: EdgeInsets.fromLTRB(22, 14, 22, bottomInset + 26),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 18),
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(999),
                ),
              ),
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.background,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Row(
                  children: [
                    CircleAvatar(
                      backgroundColor: profile.isMember
                          ? AppColors.primaryRed
                          : AppColors.textSecondary,
                      child: const Icon(Icons.person, color: AppColors.white),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            nickname,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w900,
                              color: AppColors.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            profile.isMember
                                ? 'BNK FearX 커뮤니티 회원'
                                : '커뮤니티 프로필이 없습니다.',
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 14),
              if (!profile.isMember)
                _SheetMenuItem(
                  icon: Icons.app_registration,
                  title: '커뮤니티 가입',
                  color: AppColors.primaryRed,
                  onTap: () async {
                    final result = await Navigator.push<bool>(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const CommunityJoinScreen(),
                      ),
                    );

                    if (result == true) {
                      await onProfileChanged();
                      if (context.mounted) {
                        Navigator.pop(context);
                      }
                    }
                  },
                )
              else
                _SheetMenuItem(
                  icon: Icons.edit_outlined,
                  title: '닉네임 수정',
                  color: AppColors.textPrimary,
                  onTap: () => _updateNickname(context),
                ),
              const Divider(height: 24),
              _SheetMenuItem(
                icon: Icons.help_outline,
                title: '고객센터 / 1:1 문의',
                color: AppColors.textPrimary,
                onTap: () {},
              ),
              _SheetMenuItem(
                icon: Icons.info_outline,
                title: '서비스 이용약관',
                color: AppColors.textPrimary,
                onTap: () {},
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SheetMenuItem extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color color;
  final VoidCallback onTap;

  const _SheetMenuItem({
    required this.icon,
    required this.title,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon, color: color),
      title: Text(
        title,
        style: TextStyle(color: color, fontWeight: FontWeight.w800),
      ),
      trailing: const Icon(Icons.chevron_right_rounded),
      onTap: onTap,
    );
  }
}
