import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../core/constants/app_colors.dart';

class CommunityHomeTab extends StatelessWidget {
  final ValueChanged<int> onTabChanged;

  const CommunityHomeTab({super.key, required this.onTabChanged});

  Future<void> _openGoodsStore() async {
    final url = Uri.parse('https://fearxstore.com/');

    if (!await launchUrl(url, mode: LaunchMode.externalApplication)) {
      throw Exception('Could not launch $url');
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 28),
      children: [
        InkWell(
          borderRadius: BorderRadius.circular(18),
          onTap: _openGoodsStore,
          child: Container(
            height: 190,
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              color: AppColors.cardBackground,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: AppColors.border),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: const [
                      Text(
                        'BNK FearX 굿즈 스토어',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      SizedBox(height: 10),
                      Text(
                        '공식 스토어와 팬 소식을 빠르게 확인하세요.',
                        style: TextStyle(
                          fontSize: 14,
                          height: 1.4,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 14),
                Image.asset(
                  'assets/community/goods_logo.png',
                  width: 116,
                  fit: BoxFit.contain,
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 18),
        _InfoCard(
          title: '인기글',
          child: Column(
            children: const [
              _PostPreview(title: '이번 경기 밴픽 이야기 같이 해요'),
              _PostPreview(title: '팬미팅 다녀온 후기 공유합니다'),
              _PostPreview(title: '오늘도 BNK FearX 응원 완료'),
            ],
          ),
        ),
        const SizedBox(height: 14),
        _InfoCard(
          title: '다음 경기',
          child: Row(
            children: [
              const Expanded(
                child: Text(
                  '아직 예정된 경기가 없습니다.',
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () => onTabChanged(1),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryRed,
                  foregroundColor: AppColors.white,
                  elevation: 0,
                ),
                child: const Text('경기 보기'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: AppColors.cardBackground,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: AppColors.border),
          ),
          child: Row(
            children: const [
              Icon(Icons.campaign_outlined, color: AppColors.primaryRed),
              SizedBox(width: 12),
              Expanded(
                child: Text(
                  '커뮤니티 이벤트와 공지는 추후 게시판 API와 연동됩니다.',
                  style: TextStyle(
                    fontSize: 14,
                    height: 1.4,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _InfoCard extends StatelessWidget {
  final String title;
  final Widget child;

  const _InfoCard({required this.title, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}

class _PostPreview extends StatelessWidget {
  final String title;

  const _PostPreview({required this.title});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7),
      child: Row(
        children: [
          const Icon(
            Icons.chat_bubble_outline,
            size: 18,
            color: AppColors.textSecondary,
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
