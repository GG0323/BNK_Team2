import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_join_entry_status_model.dart';
import '../../data/models/product_model.dart';

class ProductJoinEntryScreen extends StatelessWidget {
  final ProductModel product;
  final ProductJoinEntryStatusModel entryStatus;

  const ProductJoinEntryScreen({
    super.key,
    required this.product,
    required this.entryStatus,
  });

  String get _productTypeText {
    if (product.productType == 'DEPOSIT') return '예금';
    if (product.productType == 'SAVINGS') return '적금';
    return product.productType.isEmpty ? '상품' : product.productType;
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7),
      child: Row(
        children: [
          SizedBox(
            width: 96,
            child: Text(
              label,
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
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w800,
                color: AppColors.textPrimary,
              ),
            ),
          ),
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
          '상품 가입',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(22, 18, 22, 28),
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: AppColors.cardBackground,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  '가입 진입 확인',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  entryStatus.message.isEmpty
                      ? '상품 가입을 시작할 수 있습니다.'
                      : entryStatus.message,
                  style: const TextStyle(
                    fontSize: 14,
                    height: 1.5,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              color: AppColors.cardBackground,
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: AppColors.border),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  product.productName,
                  style: const TextStyle(
                    fontSize: 18,
                    height: 1.35,
                    fontWeight: FontWeight.w900,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 12),
                _buildInfoRow('상품 유형', _productTypeText),
                _buildInfoRow('회원 상태', entryStatus.memberStatus),
                _buildInfoRow(
                  '입출금 계좌',
                  entryStatus.hasActiveAccount ? '보유' : '미보유',
                ),
                _buildInfoRow('진입 가능', entryStatus.canEnterJoin ? '가능' : '불가'),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              color: const Color(0xFFFFF8F8),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: AppColors.primaryRed),
            ),
            child: const Text(
              '현재 화면은 상품 가입 진입 확인까지만 연결되어 있습니다. 실제 가입 신청, 약관 저장, 계좌 생성, 가입 완료 처리는 아직 실행하지 않습니다.',
              style: TextStyle(
                fontSize: 13,
                height: 1.5,
                fontWeight: FontWeight.w700,
                color: AppColors.textSecondary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
