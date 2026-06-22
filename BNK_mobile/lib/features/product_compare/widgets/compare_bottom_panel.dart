import 'package:flutter/material.dart';

import '../../../core/constants/app_colors.dart';
import '../../../data/models/product_model.dart';

class CompareBottomPanel extends StatelessWidget {
  final List<ProductModel> selectedProducts;
  final VoidCallback onCompareTap;
  final VoidCallback onClearTap;

  const CompareBottomPanel({
    super.key,
    required this.selectedProducts,
    required this.onCompareTap,
    required this.onClearTap,
  });

  @override
  Widget build(BuildContext context) {
    if (selectedProducts.isEmpty) {
      return const SizedBox.shrink();
    }

    return Container(
      margin: const EdgeInsets.fromLTRB(18, 8, 18, 16),
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 14),
      decoration: BoxDecoration(
        color: const Color(0xFF111827),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.12),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              '${selectedProducts.length}개 상품 선택됨',
              style: const TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w800,
                color: AppColors.white,
              ),
            ),
          ),
          TextButton(
            onPressed: onClearTap,
            child: const Text(
              '비우기',
              style: TextStyle(
                color: Color(0xFFB8C3D6),
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          const SizedBox(width: 8),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primaryRed,
              foregroundColor: AppColors.white,
              elevation: 0,
              padding: const EdgeInsets.symmetric(horizontal: 18),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(999),
              ),
            ),
            onPressed: onCompareTap,
            child: const Text(
              '비교하기',
              style: TextStyle(
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
        ],
      ),
    );
  }
}