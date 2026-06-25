import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';
import 'product_join_account_screen.dart';

class ProductJoinTermsScreen extends StatefulWidget {
  final ProductModel product;

  const ProductJoinTermsScreen({
    super.key,
    required this.product,
  });

  @override
  State<ProductJoinTermsScreen> createState() => _ProductJoinTermsScreenState();
}

class _ProductJoinTermsScreenState extends State<ProductJoinTermsScreen> {
  bool _agreeAll = false;
  bool _agreeProductTerms = false;
  bool _agreePersonalInfo = false;
  bool _agreeElectronicFinance = false;
  bool _agreeNotice = false;

  bool get _canContinue =>
      _agreeProductTerms &&
      _agreePersonalInfo &&
      _agreeElectronicFinance &&
      _agreeNotice;

  void _toggleAll(bool? value) {
    final checked = value ?? false;

    setState(() {
      _agreeAll = checked;
      _agreeProductTerms = checked;
      _agreePersonalInfo = checked;
      _agreeElectronicFinance = checked;
      _agreeNotice = checked;
    });
  }

  void _syncAgreeAll() {
    setState(() {
      _agreeAll = _canContinue;
    });
  }

  void _goNext() {
    if (!_canContinue) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('필수 약관에 모두 동의해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductJoinAccountScreen(
          product: widget.product,
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '가입 전 확인해주세요',
            style: TextStyle(
              fontSize: 24,
              height: 1.35,
              fontWeight: FontWeight.w900,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            widget.product.productName,
            style: const TextStyle(
              fontSize: 15,
              height: 1.5,
              fontWeight: FontWeight.w700,
              color: Color(0xFFD1D5DB),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAgreeAllCard() {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(
          color: _agreeAll ? AppColors.primaryRed : AppColors.border,
        ),
      ),
      child: CheckboxListTile(
        value: _agreeAll,
        onChanged: _toggleAll,
        activeColor: AppColors.primaryRed,
        contentPadding: EdgeInsets.zero,
        controlAffinity: ListTileControlAffinity.leading,
        title: const Text(
          '필수 약관에 모두 동의합니다',
          style: TextStyle(
            fontSize: 17,
            fontWeight: FontWeight.w900,
            color: AppColors.textPrimary,
          ),
        ),
        subtitle: const Padding(
          padding: EdgeInsets.only(top: 6),
          child: Text(
            '상품 가입을 위해 필요한 약관과 안내사항을 확인했습니다.',
            style: TextStyle(
              fontSize: 13,
              height: 1.5,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTermTile({
    required String title,
    required String description,
    required bool value,
    required ValueChanged<bool?> onChanged,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: CheckboxListTile(
        value: value,
        onChanged: (checked) {
          onChanged(checked);
          _syncAgreeAll();
        },
        activeColor: AppColors.primaryRed,
        contentPadding: EdgeInsets.zero,
        controlAffinity: ListTileControlAffinity.leading,
        title: Text(
          title,
          style: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w900,
            color: AppColors.textPrimary,
          ),
        ),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 5),
          child: Text(
            description,
            style: const TextStyle(
              fontSize: 12,
              height: 1.45,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
            ),
          ),
        ),
        secondary: const Icon(
          Icons.chevron_right_rounded,
          color: AppColors.textSecondary,
        ),
      ),
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
            Icons.info_outline_rounded,
            color: AppColors.primaryRed,
            size: 24,
          ),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              '가입 전 상품 설명, 금리, 중도해지 조건을 확인해주세요. 예금자보호 여부와 만기 전 해지 시 적용 금리는 상품별로 다를 수 있습니다.',
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

  Widget _buildBottomButton() {
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
          onPressed: _canContinue ? _goNext : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primaryRed,
            foregroundColor: AppColors.white,
            disabledBackgroundColor: const Color(0xFFE5E7EB),
            disabledForegroundColor: AppColors.textSecondary,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          child: const Text(
            '동의하고 계속하기',
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
          '약관 동의',
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
            _buildAgreeAllCard(),
            const SizedBox(height: 18),
            _buildTermTile(
              title: '상품 설명서 및 약관 확인',
              description: '상품의 가입조건, 금리, 만기 및 해지 조건을 확인합니다.',
              value: _agreeProductTerms,
              onChanged: (checked) {
                setState(() {
                  _agreeProductTerms = checked ?? false;
                });
              },
            ),
            _buildTermTile(
              title: '개인정보 수집 및 이용 동의',
              description: '상품 가입과 관리를 위해 필요한 정보를 확인합니다.',
              value: _agreePersonalInfo,
              onChanged: (checked) {
                setState(() {
                  _agreePersonalInfo = checked ?? false;
                });
              },
            ),
            _buildTermTile(
              title: '전자금융거래 이용 동의',
              description: '모바일 앱을 통한 금융거래 이용 조건을 확인합니다.',
              value: _agreeElectronicFinance,
              onChanged: (checked) {
                setState(() {
                  _agreeElectronicFinance = checked ?? false;
                });
              },
            ),
            _buildTermTile(
              title: '상품 주요 안내사항 확인',
              description: '예금자보호, 세금, 중도해지 시 유의사항을 확인합니다.',
              value: _agreeNotice,
              onChanged: (checked) {
                setState(() {
                  _agreeNotice = checked ?? false;
                });
              },
            ),
            const SizedBox(height: 8),
            _buildNoticeCard(),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomButton(),
    );
  }
}