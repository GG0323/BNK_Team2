import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/branch_model.dart';
import '../../data/services/branch_api.dart';

class BranchReservationScreen extends StatefulWidget {
  final BranchModel branch;

  const BranchReservationScreen({
    super.key,
    required this.branch,
  });

  @override
  State<BranchReservationScreen> createState() =>
      _BranchReservationScreenState();
}

class _BranchReservationScreenState extends State<BranchReservationScreen> {
  final BranchApi _branchApi = BranchApi();
  final TextEditingController _purposeController = TextEditingController();

  DateTime? _selectedDate;
  String? _selectedTime;
  String _selectedBizType = 'DEPOSIT';

  bool _loadingSlots = false;
  bool _saving = false;

  List<String> _bookedSlots = [];

  final List<String> _timeSlots = const [
    '09:00',
    '09:30',
    '10:00',
    '10:30',
    '11:00',
    '11:30',
    '13:00',
    '13:30',
    '14:00',
    '14:30',
    '15:00',
    '15:30',
  ];

  final List<_BizTypeItem> _bizTypes = const [
    _BizTypeItem(code: 'DEPOSIT', title: '예금 상담'),
    _BizTypeItem(code: 'LOAN', title: '대출 상담'),
    _BizTypeItem(code: 'CARD', title: '카드 상담'),
    _BizTypeItem(code: 'FX', title: '외환 상담'),
    _BizTypeItem(code: 'ETC', title: '기타 상담'),
  ];

  @override
  void dispose() {
    _purposeController.dispose();
    super.dispose();
  }

  String _formatDate(DateTime date) {
    final year = date.year.toString().padLeft(4, '0');
    final month = date.month.toString().padLeft(2, '0');
    final day = date.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  String _formatReservedAt(DateTime date, String time) {
    return '${_formatDate(date)}T$time';
  }

  Future<void> _pickDate() async {
    final now = DateTime.now();

    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? now,
      firstDate: now,
      lastDate: now.add(const Duration(days: 30)),
    );

    if (picked == null) return;

    setState(() {
      _selectedDate = picked;
      _selectedTime = null;
      _bookedSlots = [];
      _loadingSlots = true;
    });

    try {
      final slots = await _branchApi.getBookedSlots(
        branchId: widget.branch.branchId,
        date: _formatDate(picked),
      );

      if (!mounted) return;

      setState(() {
        _bookedSlots = slots;
        _loadingSlots = false;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _loadingSlots = false;
      });

      _showSnack(e.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> _submitReservation() async {
    if (_selectedDate == null) {
      _showSnack('예약 날짜를 선택해주세요.');
      return;
    }

    if (_selectedTime == null) {
      _showSnack('예약 시간을 선택해주세요.');
      return;
    }

    setState(() {
      _saving = true;
    });

    try {
      await _branchApi.createReservation(
        branchId: widget.branch.branchId,
        reservedAt: _formatReservedAt(_selectedDate!, _selectedTime!),
        bizType: _selectedBizType,
        purpose: _purposeController.text.trim(),
      );

      if (!mounted) return;

      _showSnack('예약이 접수되었습니다.');

      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;

      _showSnack(e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) {
        setState(() {
          _saving = false;
        });
      }
    }
  }

  void _showSnack(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  Widget _buildBranchCard() {
    final branch = widget.branch;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(26),
      ),
      child: Row(
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              color: AppColors.primaryRed,
              borderRadius: BorderRadius.circular(18),
            ),
            child: const Icon(
              Icons.account_balance_rounded,
              color: AppColors.white,
              size: 30,
            ),
          ),
          const SizedBox(width: 15),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  branch.branchName,
                  style: const TextStyle(
                    fontSize: 19,
                    fontWeight: FontWeight.w900,
                    color: AppColors.white,
                  ),
                ),
                const SizedBox(height: 7),
                Text(
                  branch.address,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 12,
                    height: 1.35,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFFB8C3D6),
                  ),
                ),
                const SizedBox(height: 5),
                Text(
                  branch.phoneNumber,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFFB8C3D6),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(2, 24, 2, 12),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          title,
          style: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w900,
            color: AppColors.textPrimary,
          ),
        ),
      ),
    );
  }

  Widget _buildBizTypeSection() {
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: _bizTypes.map((item) {
        final selected = _selectedBizType == item.code;

        return InkWell(
          borderRadius: BorderRadius.circular(999),
          onTap: () {
            setState(() {
              _selectedBizType = item.code;
            });
          },
          child: Container(
            padding: const EdgeInsets.symmetric(
              horizontal: 14,
              vertical: 10,
            ),
            decoration: BoxDecoration(
              color: selected
                  ? const Color(0xFFFFF0F0)
                  : AppColors.cardBackground,
              borderRadius: BorderRadius.circular(999),
              border: Border.all(
                color: selected ? AppColors.primaryRed : AppColors.border,
              ),
            ),
            child: Text(
              item.title,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w900,
                color:
                    selected ? AppColors.primaryRed : AppColors.textSecondary,
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildDateBox() {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: _pickDate,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(18, 17, 18, 17),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: Row(
          children: [
            const Icon(
              Icons.calendar_month_outlined,
              color: AppColors.primaryRed,
              size: 24,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                _selectedDate == null
                    ? '예약 날짜를 선택하세요'
                    : _formatDate(_selectedDate!),
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
            ),
            const Icon(
              Icons.chevron_right_rounded,
              color: AppColors.textSecondary,
              size: 26,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimeSection() {
    if (_selectedDate == null) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: const Text(
          '날짜를 먼저 선택하면 예약 가능한 시간이 표시됩니다.',
          style: TextStyle(
            fontSize: 13,
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w600,
          ),
        ),
      );
    }

    if (_loadingSlots) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 20),
        child: Center(
          child: CircularProgressIndicator(
            color: AppColors.primaryRed,
          ),
        ),
      );
    }

    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: _timeSlots.map((time) {
        final booked = _bookedSlots.contains(time);
        final selected = _selectedTime == time;

        return InkWell(
          borderRadius: BorderRadius.circular(14),
          onTap: booked
              ? null
              : () {
                  setState(() {
                    _selectedTime = time;
                  });
                },
          child: Container(
            width: 82,
            padding: const EdgeInsets.symmetric(vertical: 12),
            decoration: BoxDecoration(
              color: booked
                  ? const Color(0xFFF1F3F5)
                  : selected
                      ? const Color(0xFFFFF0F0)
                      : AppColors.cardBackground,
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: selected ? AppColors.primaryRed : AppColors.border,
              ),
            ),
            child: Text(
              booked ? '$time\n마감' : time,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 12,
                height: 1.2,
                fontWeight: FontWeight.w900,
                color: booked
                    ? AppColors.textSecondary
                    : selected
                        ? AppColors.primaryRed
                        : AppColors.textPrimary,
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildPurposeInput() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: TextField(
        controller: _purposeController,
        maxLines: 4,
        decoration: const InputDecoration(
          border: InputBorder.none,
          hintText: '방문 목적을 입력하세요. 예) 적금 상품 상담',
          hintStyle: TextStyle(
            fontSize: 13,
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }

  Widget _buildSaveButton() {
    return Container(
      padding: const EdgeInsets.fromLTRB(22, 12, 22, 18),
      decoration: BoxDecoration(
        color: AppColors.background,
        border: Border(
          top: BorderSide(color: AppColors.border.withOpacity(0.7)),
        ),
      ),
      child: SizedBox(
        width: double.infinity,
        height: 54,
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primaryRed,
            foregroundColor: AppColors.white,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          onPressed: _saving ? null : _submitReservation,
          child: Text(
            _saving ? '예약 접수 중...' : '예약하기',
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildContent() {
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(22, 18, 22, 24),
      child: Column(
        children: [
          _buildBranchCard(),
          _buildSectionTitle('상담 업무'),
          _buildBizTypeSection(),
          _buildSectionTitle('예약 날짜'),
          _buildDateBox(),
          _buildSectionTitle('예약 시간'),
          _buildTimeSection(),
          _buildSectionTitle('방문 목적'),
          _buildPurposeInput(),
          const SizedBox(height: 90),
        ],
      ),
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
          '상담 예약',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: _buildContent(),
      bottomNavigationBar: _buildSaveButton(),
    );
  }
}

class _BizTypeItem {
  final String code;
  final String title;

  const _BizTypeItem({
    required this.code,
    required this.title,
  });
}