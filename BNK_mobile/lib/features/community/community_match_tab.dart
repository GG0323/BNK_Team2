import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';

class CommunityMatch {
  final String date;
  final String time;
  final String opponent;
  final String status;
  final String? outcome;

  const CommunityMatch({
    required this.date,
    required this.time,
    required this.opponent,
    required this.status,
    this.outcome,
  });
}

class CommunityMatchTab extends StatelessWidget {
  const CommunityMatchTab({super.key});

  @override
  Widget build(BuildContext context) {
    const matches = [
      CommunityMatch(
        date: '2026.05.27',
        time: '19:00',
        opponent: 'KRX',
        status: 'FINISHED',
        outcome: '2:1 승리',
      ),
      CommunityMatch(
        date: '2026.05.30',
        time: '17:00',
        opponent: 'T1',
        status: 'FINISHED',
        outcome: '0:2 패배',
      ),
      CommunityMatch(
        date: '2026.05.24',
        time: '15:00',
        opponent: 'GEN',
        status: 'FINISHED',
        outcome: '0:2 패배',
      ),
      CommunityMatch(
        date: '2026.05.20',
        time: '20:00',
        opponent: 'DK',
        status: 'FINISHED',
        outcome: '1:2 패배',
      ),
    ];

    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 28),
      itemCount: matches.length,
      separatorBuilder: (context, index) => const SizedBox(height: 12),
      itemBuilder: (context, index) {
        return _MatchCard(match: matches[index]);
      },
    );
  }
}

class _MatchCard extends StatelessWidget {
  final CommunityMatch match;

  const _MatchCard({required this.match});

  @override
  Widget build(BuildContext context) {
    final isUpcoming = match.status == 'UPCOMING';
    final statusColor = isUpcoming ? Colors.blue : AppColors.textSecondary;
    final statusText = isUpcoming ? '경기 예정' : '경기 종료';

    return Container(
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
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 5,
                  ),
                  decoration: BoxDecoration(
                    color: statusColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    statusText,
                    style: TextStyle(
                      color: statusColor,
                      fontSize: 12,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  '${match.date} ${match.time}',
                  style: const TextStyle(
                    color: AppColors.textSecondary,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  'BNK FearX vs ${match.opponent}',
                  style: const TextStyle(
                    fontSize: 17,
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Text(
            match.outcome ?? '-',
            style: TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w900,
              color: (match.outcome ?? '').contains('승리')
                  ? AppColors.primaryRed
                  : Colors.blue,
            ),
          ),
        ],
      ),
    );
  }
}
