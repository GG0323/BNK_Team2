import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';

class ProductDetailScreen extends StatelessWidget {
  final ProductModel product;

  const ProductDetailScreen({
    super.key,
    required this.product,
  });

  String _formatRate(double rate) {
    if (rate == 0) return '-';
    return rate.toStringAsFixed(2);
  }

  void _showPreparingMessage(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 24, 22, 24),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'BNK',
            style: TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
          const SizedBox(height: 18),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
            decoration: BoxDecoration(
              color: const Color(0xFFFFF0F0),
              borderRadius: BorderRadius.circular(999),
            ),
            child: Text(
              product.productTypeText,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w900,
                color: AppColors.primaryRed,
              ),
            ),
          ),
          const SizedBox(height: 14),
          Text(
            product.productName,
            style: const TextStyle(
              fontSize: 24,
              height: 1.35,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            product.content.isEmpty ? '상품 설명을 확인해보세요.' : product.content,
            style: const TextStyle(
              fontSize: 14,
              height: 1.55,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRateSection() {
    return Row(
      children: [
        Expanded(
          child: _buildInfoBox(
            title: '최저금리',
            value: '${_formatRate(product.minInterestRate)}%',
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _buildInfoBox(
            title: '최고금리',
            value: '${_formatRate(product.maxInterestRate)}%',
            highlight: true,
          ),
        ),
      ],
    );
  }

  Widget _buildInfoBox({
    required String title,
    required String value,
    bool highlight = false,
  }) {
    return Container(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            value,
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: highlight ? AppColors.primaryRed : AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildJoinChannelCard() {
    final isMobile = product.mobileJoinYn == 'Y';
    final isBranch = product.branchJoinYn == 'Y';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '가입 채널',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              _buildJoinChip(
                icon: Icons.phone_android_rounded,
                label: '모바일',
                enabled: isMobile,
              ),
              const SizedBox(width: 10),
              _buildJoinChip(
                icon: Icons.storefront_outlined,
                label: '영업점',
                enabled: isBranch,
              ),
            ],
          ),
          const SizedBox(height: 16),
          Text(
            product.joinMethodText,
            style: const TextStyle(
              fontSize: 14,
              height: 1.5,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildJoinChip({
    required IconData icon,
    required String label,
    required bool enabled,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 13, vertical: 9),
      decoration: BoxDecoration(
        color: enabled ? const Color(0xFFFFF0F0) : const Color(0xFFF3F4F6),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: enabled ? AppColors.primaryRed : AppColors.border,
        ),
      ),
      child: Row(
        children: [
          Icon(
            icon,
            size: 17,
            color: enabled ? AppColors.primaryRed : AppColors.textSecondary,
          ),
          const SizedBox(width: 6),
          Text(
            enabled ? '$label 가능' : '$label 불가',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w800,
              color: enabled ? AppColors.primaryRed : AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMobileGuideCard(BuildContext context) {
    final isMobile = product.mobileJoinYn == 'Y';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: isMobile ? const Color(0xFFFFF8F8) : AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(
          color: isMobile ? AppColors.primaryRed : AppColors.border,
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            isMobile
                ? Icons.phone_iphone_rounded
                : Icons.info_outline_rounded,
            color: isMobile ? AppColors.primaryRed : AppColors.textSecondary,
            size: 30,
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  isMobile ? '모바일 가입 가능 상품' : '모바일 가입 제한 상품',
                  style: TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.w900,
                    color: isMobile
                        ? AppColors.primaryRed
                        : AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  isMobile
                      ? '이 상품은 모바일 앱에서 가입 신청을 진행할 수 있습니다. 실제 가입신청 절차는 이후 단계에서 연결합니다.'
                      : '이 상품은 현재 모바일 가입 대상이 아니거나 영업점 가입이 필요한 상품입니다.',
                  style: const TextStyle(
                    fontSize: 13,
                    height: 1.5,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDescriptionCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '상품 안내',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          Text(
            product.content.isEmpty ? '상품 설명 정보가 없습니다.' : product.content,
            style: const TextStyle(
              fontSize: 14,
              height: 1.65,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
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
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primaryRed,
            foregroundColor: AppColors.white,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          onPressed: () {
            _showPreparingMessage(
              context,
              '가입신청 기능은 이후 단계에서 연결합니다.',
            );
          },
          child: const Text(
            '가입하기',
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
          '상품 상세',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(22, 18, 22, 24),
        child: Column(
          children: [
            _buildHeader(),
            const SizedBox(height: 16),
            _buildRateSection(),
            const SizedBox(height: 16),
            _buildJoinChannelCard(),
            const SizedBox(height: 16),
            _buildMobileGuideCard(context),
            const SizedBox(height: 16),
            _buildDescriptionCard(),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomButton(context),
    );
  }
}