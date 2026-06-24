import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/constants/app_colors.dart';
import '../../data/models/product_model.dart';
import 'models/product_join_account.dart';
import 'product_join_confirm_screen.dart';

class ProductJoinInputScreen extends StatefulWidget {
  final ProductModel product;
  final ProductJoinAccount account;

  const ProductJoinInputScreen({
    super.key,
    required this.product,
    required this.account,
  });

  @override
  State<ProductJoinInputScreen> createState() => _ProductJoinInputScreenState();
}

class _ProductJoinInputScreenState extends State<ProductJoinInputScreen> {
  final TextEditingController _amountController = TextEditingController();

  int _selectedPeriodMonths = 12;

  final List<int> _periodOptions = [6, 12, 24, 36];

  int get _amount {
    final raw = _amountController.text.replaceAll(',', '').trim();
    return int.tryParse(raw) ?? 0;
  }

  bool get _canContinue {
    return _amount > 0 && _amount <= widget.account.balance;
  }

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  String _formatCurrency(int value) {
    final text = value.toString();
    return text.replaceAllMapped(
      RegExp(r'(\d)(?=(\d{3})+(?!\d))'),
      (match) => '${match[1]},',
    );
  }

  void _goNext() {
    if (_amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('가입금액을 입력해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    if (_amount > widget.account.balance) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('출금 가능금액을 확인해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ProductJoinConfirmScreen(
          product: widget.product,
          account: widget.account,
          joinAmount: _amount,
          periodMonths: _selectedPeriodMonths,
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
            '가입정보를 입력해주세요',
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

  Widget _buildAmountCard() {
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
            '가입금액',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '출금 가능금액 ${_formatCurrency(widget.account.balance)}원',
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _amountController,
            keyboardType: TextInputType.number,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
            ],
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: '가입금액 입력',
              suffixText: '원',
              filled: true,
              fillColor: AppColors.inputBackground,
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 18,
                vertical: 18,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: const BorderSide(color: AppColors.border),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: const BorderSide(color: AppColors.border),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: const BorderSide(color: AppColors.primaryRed),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPeriodCard() {
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
            '가입기간',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: _periodOptions.map((months) {
              final selected = _selectedPeriodMonths == months;

              return ChoiceChip(
                label: Text('$months개월'),
                selected: selected,
                onSelected: (_) {
                  setState(() {
                    _selectedPeriodMonths = months;
                  });
                },
                selectedColor: const Color(0xFFFFF0F0),
                backgroundColor: const Color(0xFFF8FAFC),
                side: BorderSide(
                  color: selected ? AppColors.primaryRed : AppColors.border,
                ),
                labelStyle: TextStyle(
                  fontWeight: FontWeight.w900,
                  color: selected ? AppColors.primaryRed : AppColors.textSecondary,
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildGuideCard() {
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
          ),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              '가입금액과 기간에 따라 적용 금리와 만기 예상 금액이 달라질 수 있습니다. 최종 확인 화면에서 입력한 내용을 다시 확인해주세요.',
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
            '다음',
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
          '가입정보 입력',
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
            _buildAmountCard(),
            const SizedBox(height: 16),
            _buildPeriodCard(),
            const SizedBox(height: 16),
            _buildGuideCard(),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomButton(),
    );
  }
}