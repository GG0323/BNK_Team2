import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import 'product_list_screen.dart';

class ProductJoinCompleteScreen extends StatelessWidget {
  final String productName;
  final int joinAmount;
  final int periodMonths;

  const ProductJoinCompleteScreen({
    super.key,
    required this.productName,
    required this.joinAmount,
    required this.periodMonths,
  });

  String _formatCurrency(int value) {
    final text = value.toString();
    return text.replaceAllMapped(
      RegExp(r'(\d)(?=(\d{3})+(?!\d))'),
      (match) => '${match[1]},',
    );
  }

  void _goToProductList(BuildContext context) {
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (_) => const ProductListScreen(),
      ),
      (route) => false,
    );
  }

  Widget _buildCompleteCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 30, 22, 28),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(30),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          Container(
            width: 74,
            height: 74,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.primaryRed,
            ),
            child: const Icon(
              Icons.check_rounded,
              size: 42,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 24),
          const Text(
            '가입 신청이 완료되었습니다',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 24,
              height: 1.35,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            '선택하신 상품의 가입 신청이 정상적으로 접수되었습니다.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              height: 1.5,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          _buildRow('상품명', productName),
          const Divider(height: 24, color: AppColors.border),
          _buildRow('가입금액', '${_formatCurrency(joinAmount)}원'),
          const Divider(height: 24, color: AppColors.border),
          _buildRow('가입기간', '$periodMonths개월'),
        ],
      ),
    );
  }

  Widget _buildRow(String title, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 90,
          child: Text(
            title,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w800,
              color: AppColors.textSecondary,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            textAlign: TextAlign.right,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildNoticeCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border),
      ),
      child: const Text(
        '가입 내역은 가입상품 조회 메뉴에서 확인하실 수 있습니다. 상품별 적용 금리와 만기일은 가입 내역에서 다시 확인해주세요.',
        style: TextStyle(
          fontSize: 13,
          height: 1.55,
          fontWeight: FontWeight.w700,
          color: AppColors.textSecondary,
        ),
      ),
    );
  }

  Widget _buildBottomButton(BuildContext context) {
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
          onPressed: () => _goToProductList(context),
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primaryRed,
            foregroundColor: AppColors.white,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          child: const Text(
            '상품목록으로 이동',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w900,
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(22, 34, 22, 100),
          child: Column(
            children: [
              _buildCompleteCard(),
              const SizedBox(height: 16),
              _buildSummaryCard(),
              const SizedBox(height: 16),
              _buildNoticeCard(),
            ],
          ),
        ),
      ),
      bottomNavigationBar: _buildBottomButton(context),
    );
  }
}