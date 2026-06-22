import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/branch_reservation_model.dart';
import '../../data/services/branch_api.dart';

class BranchReservationListScreen extends StatefulWidget {
  const BranchReservationListScreen({super.key});

  @override
  State<BranchReservationListScreen> createState() =>
      _BranchReservationListScreenState();
}

class _BranchReservationListScreenState
    extends State<BranchReservationListScreen> {
  final BranchApi _branchApi = BranchApi();

  late Future<List<BranchReservationModel>> _reservationFuture;

  @override
  void initState() {
    super.initState();
    _reservationFuture = _branchApi.getMyReservations();
  }

  void _reload() {
    setState(() {
      _reservationFuture = _branchApi.getMyReservations();
    });
  }

  String _formatDateTime(String value) {
    if (value.isEmpty) return '-';

    return value
        .replaceFirst('T', ' ')
        .replaceAll('.000', '')
        .split('.')
        .first;
  }

  bool _canCancel(BranchReservationModel reservation) {
    return reservation.status == 'PENDING' ||
        reservation.status == 'CONFIRMED';
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'PENDING':
        return AppColors.primaryRed;
      case 'CONFIRMED':
        return AppColors.primaryRed;
      case 'CANCELED':
        return AppColors.textSecondary;
      case 'REJECTED':
        return AppColors.textSecondary;
      case 'REASSIGNED':
        return AppColors.primaryRed;
      default:
        return AppColors.textSecondary;
    }
  }

  Future<void> _cancelReservation(BranchReservationModel reservation) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('예약 취소'),
          content: const Text('해당 영업점 예약을 취소하시겠습니까?'),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context, false);
              },
              child: const Text('아니오'),
            ),
            TextButton(
              onPressed: () {
                Navigator.pop(context, true);
              },
              child: const Text('취소하기'),
            ),
          ],
        );
      },
    );

    if (confirm != true) return;

    try {
      await _branchApi.cancelReservation(
        reservationId: reservation.reservationId,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('예약이 취소되었습니다.'),
          behavior: SnackBarBehavior.floating,
        ),
      );

      _reload();
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceFirst('Exception: ', '')),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.fromLTRB(22, 18, 22, 14),
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(26),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'BNK',
            style: TextStyle(
              fontSize: 31,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
          SizedBox(height: 14),
          Text(
            '예약 내역',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.w900,
              color: AppColors.white,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '영업점 방문 상담 예약 내역을 확인하고 취소할 수 있습니다.',
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              color: Color(0xFFB8C3D6),
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildReservationCard(BranchReservationModel reservation) {
    final canCancel = _canCancel(reservation);

    return Container(
      margin: const EdgeInsets.fromLTRB(22, 0, 22, 14),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF0F0),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(
                  Icons.calendar_month_rounded,
                  color: AppColors.primaryRed,
                  size: 25,
                ),
              ),
              const SizedBox(width: 13),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      reservation.branchName.isEmpty
                          ? '영업점 예약'
                          : reservation.branchName,
                      style: const TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.w900,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 5),
                    Text(
                      reservation.bizTypeText,
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF0F0),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  reservation.statusText,
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w900,
                    color: _statusColor(reservation.status),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          _buildInfoRow(
            icon: Icons.schedule_rounded,
            text: _formatDateTime(reservation.reservedAt),
          ),
          const SizedBox(height: 8),
          _buildInfoRow(
            icon: Icons.edit_note_rounded,
            text: reservation.purpose.isEmpty
                ? '방문 목적 없음'
                : reservation.purpose,
          ),
          const SizedBox(height: 8),
          _buildInfoRow(
            icon: Icons.event_available_rounded,
            text: '신청일 ${_formatDateTime(reservation.createdAt ?? '')}',
          ),
          if (canCancel) ...[
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 46,
              child: OutlinedButton(
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.primaryRed,
                  side: const BorderSide(color: AppColors.primaryRed),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                onPressed: () {
                  _cancelReservation(reservation);
                },
                child: const Text(
                  '예약 취소',
                  style: TextStyle(
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildInfoRow({
    required IconData icon,
    required String text,
  }) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(
          icon,
          size: 18,
          color: AppColors.textSecondary,
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 13,
              height: 1.35,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildEmptyView() {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.fromLTRB(22, 16, 22, 0),
      padding: const EdgeInsets.all(26),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: const Column(
        children: [
          Icon(
            Icons.event_busy_rounded,
            size: 44,
            color: AppColors.textSecondary,
          ),
          SizedBox(height: 12),
          Text(
            '예약 내역이 없습니다.',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 6),
          Text(
            '영업점 찾기에서 상담 예약을 진행할 수 있습니다.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorView(Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(22),
        child: Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            color: AppColors.cardBackground,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: AppColors.border),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.error_outline_rounded,
                color: AppColors.primaryRed,
                size: 42,
              ),
              const SizedBox(height: 14),
              const Text(
                '예약 내역을 불러오지 못했습니다.',
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w900,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                error.toString().replaceFirst('Exception: ', ''),
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 13,
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: 18),
              ElevatedButton(
                onPressed: _reload,
                child: const Text('다시 시도'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildContent() {
    return FutureBuilder<List<BranchReservationModel>>(
      future: _reservationFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(
            child: CircularProgressIndicator(
              color: AppColors.primaryRed,
            ),
          );
        }

        if (snapshot.hasError) {
          return _buildErrorView(snapshot.error!);
        }

        final reservations = snapshot.data ?? [];

        return RefreshIndicator(
          color: AppColors.primaryRed,
          onRefresh: () async {
            _reload();
            await _reservationFuture;
          },
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(),
                const Padding(
                  padding: EdgeInsets.fromLTRB(22, 8, 22, 14),
                  child: Text(
                    '내 예약 목록',
                    style: TextStyle(
                      fontSize: 19,
                      fontWeight: FontWeight.w900,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ),
                if (reservations.isEmpty)
                  _buildEmptyView()
                else
                  ...reservations.map(_buildReservationCard),
                const SizedBox(height: 28),
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '예약 내역',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: _buildContent(),
    );
  }
}