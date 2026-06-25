import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';
import 'models/product_join_account.dart';
import 'product_join_complete_screen.dart';

class ProductJoinConfirmScreen extends StatelessWidget {
  final ProductModel product;
  final ProductJoinAccount account;
  final int joinAmount;
  final int periodMonths;

  const ProductJoinConfirmScreen({
    super.key,
    required this.product,
    required this.account,
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

  String _formatRate(double rate) {
    if (rate == 0) return '-';
    return rate.toStringAsFixed(2);
  }

  String _maskAccountNumber(String accountNumber) {
    if (accountNumber.length <= 4) return accountNumber;
    final tail = accountNumber.substring(accountNumber.length - 4);
    return '•••• •••• •$tail';
  }

  void _submit(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductJoinCompleteScreen(
          productName: product.productName,
          joinAmount: joinAmount,
          periodMonths: periodMonths,
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(28),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '가입 내용을 확인해주세요',
            style: TextStyle(
              fontSize: 24,
              height: 1.35,
              fontWeight: FontWeight.w900,
              color: AppColors.white,
            ),
          ),
          SizedBox(height: 12),
          Text(
            '입력하신 내용을 확인한 뒤 가입 신청을 진행합니다.',
            style: TextStyle(
              fontSize: 14,
              height: 1.5,
              fontWeight: FontWeight.w700,
              color: Color(0xFFD1D5DB),
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
          _buildRow('상품명', product.productName),
          _buildDivider(),
          _buildRow('상품유형', product.productTypeText),
          _buildDivider(),
          _buildRow('가입금액', '${_formatCurrency(joinAmount)}원'),
          _buildDivider(),
          _buildRow('가입기간', '$periodMonths개월'),
          _buildDivider(),
          _buildRow('최고금리', '${_formatRate(product.maxInterestRate)}%'),
          _buildDivider(),
          _buildRow('출금계좌', '${account.alias}\n${_maskAccountNumber(account.accountNumber)}'),
        ],
      ),
    );
  }

  Widget _buildRow(String title, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 92,
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
      ),
    );
  }

  Widget _buildDivider() {
    return const Divider(
      height: 22,
      color: AppColors.border,
    );
  }

  Widget _buildNoticeCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF8F8),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFFFD0D0)),
      ),
      child: const Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.verified_user_outlined,
            color: AppColors.primaryRed,
          ),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              '가입 신청 후에는 상품 조건에 따라 취소 또는 해지가 제한될 수 있습니다. 입력하신 내용을 다시 한 번 확인해주세요.',
              style: TextStyle(
                fontSize: 13,
                height: 1.55,
                fontWeight: FontWeight.w700,
                color: AppColors.primaryRed,
              ),
            ),
          ),
        ],
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
          onPressed: () => _submit(context),
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primaryRed,
            foregroundColor: AppColors.white,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          child: const Text(
            '가입 신청하기',
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
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '가입내용 확인',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(22, 18, 22, 100),
        child: Column(
          children: [
            _buildHeader(),
            const SizedBox(height: 16),
            _buildSummaryCard(),
            const SizedBox(height: 16),
            _buildNoticeCard(),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomButton(context),
    );
  }
}