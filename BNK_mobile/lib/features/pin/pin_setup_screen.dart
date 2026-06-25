import 'dart:math';

import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/secure_storage.dart';
import '../home/home_screen.dart';
import '../product_compare/product_join_intro_screen.dart';

class PinSetupScreen extends StatefulWidget {
  final String memberName;
  final int? redirectProductNo;

  const PinSetupScreen({
    super.key,
    required this.memberName,
    this.redirectProductNo,
  });

  @override
  State<PinSetupScreen> createState() => _PinSetupScreenState();
}

class _PinSetupScreenState extends State<PinSetupScreen> {
  String _firstPin = '';
  String _currentPin = '';
  bool _isConfirmStep = false;

  List<String> _keypadNumbers = [];

  bool get _hasRedirect => widget.redirectProductNo != null;

  @override
  void initState() {
    super.initState();
    _shuffleKeypad();
  }

  void _shuffleKeypad() {
    final numbers = List<String>.generate(10, (index) => index.toString());
    numbers.shuffle(Random());

    setState(() {
      _keypadNumbers = numbers;
    });
  }

  void _onNumberTap(String number) {
    if (_currentPin.length >= 6) return;

    setState(() {
      _currentPin += number;
    });

    if (_currentPin.length == 6) {
      Future.delayed(const Duration(milliseconds: 250), () {
        if (!mounted) return;
        _handlePinComplete();
      });
    }
  }

  void _onDeleteTap() {
    if (_currentPin.isEmpty) return;

    setState(() {
      _currentPin = _currentPin.substring(0, _currentPin.length - 1);
    });
  }

  Future<void> _handlePinComplete() async {
    if (!_isConfirmStep) {
      setState(() {
        _firstPin = _currentPin;
        _currentPin = '';
        _isConfirmStep = true;
      });

      _shuffleKeypad();
      return;
    }

    if (_firstPin != _currentPin) {
      setState(() {
        _firstPin = '';
        _currentPin = '';
        _isConfirmStep = false;
      });

      _shuffleKeypad();

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('간편비밀번호가 일치하지 않습니다. 다시 등록해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      return;
    }

    await SecureStorage.savePin(_currentPin);

    if (!mounted) return;

    if (_hasRedirect) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => ProductJoinIntroScreen(
            productNo: widget.redirectProductNo!,
            enteredFromQr: true,
          ),
        ),
      );
      return;
    }

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => HomeScreen(
          memberName: widget.memberName,
        ),
      ),
    );
  }

  Widget _buildPinDots() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(6, (index) {
        final bool filled = index < _currentPin.length;

        return Container(
          width: 15,
          height: 15,
          margin: const EdgeInsets.symmetric(horizontal: 6),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: filled ? AppColors.textPrimary : Colors.transparent,
            border: Border.all(
              color: AppColors.textPrimary,
              width: 2,
            ),
          ),
        );
      }),
    );
  }

  Widget _buildNumberButton(String text) {
    return SizedBox(
      width: 72,
      height: 56,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.white,
          foregroundColor: AppColors.textPrimary,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(18),
            side: const BorderSide(
              color: AppColors.border,
            ),
          ),
        ),
        onPressed: () => _onNumberTap(text),
        child: Text(
          text,
          style: const TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.w800,
          ),
        ),
      ),
    );
  }

  Widget _buildTextButton({
    required String text,
    required VoidCallback onPressed,
  }) {
    return SizedBox(
      width: 72,
      height: 56,
      child: TextButton(
        onPressed: onPressed,
        child: Text(
          text,
          style: const TextStyle(
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }

  Widget _buildKeypadRow(List<String> numbers) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: numbers.map(_buildNumberButton).toList(),
    );
  }

  Widget _buildKeypad() {
    if (_keypadNumbers.length < 10) {
      return const SizedBox.shrink();
    }

    final row1 = _keypadNumbers.sublist(0, 3);
    final row2 = _keypadNumbers.sublist(3, 6);
    final row3 = _keypadNumbers.sublist(6, 9);
    final lastNumber = _keypadNumbers[9];

    return Column(
      children: [
        _buildKeypadRow(row1),
        const SizedBox(height: 12),
        _buildKeypadRow(row2),
        const SizedBox(height: 12),
        _buildKeypadRow(row3),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            _buildTextButton(
              text: '삭제',
              onPressed: _onDeleteTap,
            ),
            _buildNumberButton(lastNumber),
            _buildTextButton(
              text: '재배열',
              onPressed: _shuffleKeypad,
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildRedirectGuide() {
    if (!_hasRedirect) {
      return const SizedBox.shrink();
    }

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(top: 22),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF8F8),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.primaryRed),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(
            Icons.qr_code_2_rounded,
            color: AppColors.primaryRed,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              'PIN 등록 후 선택하신 상품의 가입 안내 화면으로 이동합니다.',
              style: const TextStyle(
                fontSize: 13,
                height: 1.5,
                fontWeight: FontWeight.w700,
                color: AppColors.primaryRed,
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final title = _isConfirmStep
        ? '간편비밀번호를 다시 입력하세요'
        : '간편비밀번호를 등록하세요';

    final subtitle = _isConfirmStep
        ? '확인을 위해 한 번 더 입력해주세요'
        : _hasRedirect
            ? '상품 가입 안내를 계속 진행하기 위해 6자리 비밀번호를 등록해주세요'
            : '앞으로 앱 로그인 시 사용할 6자리 비밀번호';

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 18),
            child: Column(
              children: [
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 22,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.cardBackground,
                    borderRadius: BorderRadius.circular(28),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Column(
                    children: [
                      Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          widget.memberName.isEmpty
                              ? 'BNK'
                              : '${widget.memberName}님',
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.w900,
                            color: AppColors.primaryRed,
                          ),
                        ),
                      ),
                      const SizedBox(height: 24),
                      Text(
                        title,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 10),
                      Text(
                        subtitle,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 14,
                          height: 1.5,
                          color: AppColors.textSecondary,
                        ),
                      ),
                      _buildRedirectGuide(),
                      const SizedBox(height: 32),
                      _buildPinDots(),
                      const SizedBox(height: 34),
                      _buildKeypad(),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}