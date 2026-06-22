import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';

class ProductCompareScreen extends StatelessWidget {
  final List<ProductModel> products;

  const ProductCompareScreen({
    super.key,
    required this.products,
  });

  String _formatRate(double rate) {
    if (rate == 0) return '-';
    return rate.toStringAsFixed(2);
  }

  Widget _buildProductHeader(ProductModel product) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppColors.cardBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.symmetric(
                horizontal: 9,
                vertical: 5,
              ),
              decoration: BoxDecoration(
                color: const Color(0xFFFFF0F0),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                product.productTypeText,
                style: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w800,
                  color: AppColors.primaryRed,
                ),
              ),
            ),
            const SizedBox(height: 12),
            Text(
              product.productName,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                fontSize: 15,
                height: 1.35,
                fontWeight: FontWeight.w900,
                color: AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCompareRow({
    required String title,
    required List<String> values,
    bool highlight = false,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 10),
            decoration: const BoxDecoration(
              color: Color(0xFFF7F8FA),
              borderRadius: BorderRadius.vertical(
                top: Radius.circular(18),
              ),
            ),
            child: Text(
              title,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w900,
                color: AppColors.textSecondary,
              ),
            ),
          ),
          IntrinsicHeight(
            child: Row(
              children: List.generate(values.length, (index) {
                return Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      border: Border(
                        right: index == values.length - 1
                            ? BorderSide.none
                            : const BorderSide(color: AppColors.border),
                      ),
                    ),
                    child: Text(
                      values[index],
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: highlight ? 18 : 13,
                        height: 1.45,
                        fontWeight:
                            highlight ? FontWeight.w900 : FontWeight.w700,
                        color: highlight
                            ? AppColors.primaryRed
                            : AppColors.textPrimary,
                      ),
                    ),
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(28),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'BNK',
            style: TextStyle(
              fontSize: 32,
              fontWeight: FontWeight.w900,
              color: AppColors.primaryRed,
            ),
          ),
          SizedBox(height: 16),
          Text(
            '상품 비교',
            style: TextStyle(
              fontSize: 25,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '선택한 예적금 상품의 조건을 한눈에 비교합니다.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.textSecondary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProductHeaderRow() {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        for (int i = 0; i < products.length; i++) ...[
          _buildProductHeader(products[i]),
          if (i != products.length - 1) const SizedBox(width: 10),
        ],
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final productNames = products.map((p) => p.productName).toList();
    final productTypes = products.map((p) => p.productTypeText).toList();

    final minRates =
        products.map((p) => '${_formatRate(p.minInterestRate)}%').toList();

    final maxRates =
        products.map((p) => '${_formatRate(p.maxInterestRate)}%').toList();

    final joinMethods = products.map((p) => p.joinMethodText).toList();

    final contents = products
        .map((p) => p.content.isEmpty ? '상품 설명 없음' : p.content)
        .toList();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        foregroundColor: AppColors.textPrimary,
        title: const Text(
          '상품 비교',
          style: TextStyle(
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHeader(),
              const SizedBox(height: 18),
              Text(
                '선택 상품 ${products.length}개',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w900,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 14),
              _buildProductHeaderRow(),
              const SizedBox(height: 18),
              _buildCompareRow(
                title: '상품명',
                values: productNames,
              ),
              _buildCompareRow(
                title: '상품유형',
                values: productTypes,
              ),
              _buildCompareRow(
                title: '최저금리',
                values: minRates,
              ),
              _buildCompareRow(
                title: '최고금리',
                values: maxRates,
                highlight: true,
              ),
              _buildCompareRow(
                title: '가입채널',
                values: joinMethods,
              ),
              _buildCompareRow(
                title: '상품설명',
                values: contents,
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}