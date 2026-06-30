import 'dart:math';

import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/secure_storage.dart';
import '../../data/services/auth_api.dart';
import '../auth/login_screen.dart';
import '../home/home_screen.dart';

class PinLoginScreen extends StatefulWidget {
  final String memberName;
  final bool returnOnSuccess;

  const PinLoginScreen({
    super.key,
    required this.memberName,
    this.returnOnSuccess = false,
  });

  @override
  State<PinLoginScreen> createState() => _PinLoginScreenState();
}

class _PinLoginScreenState extends State<PinLoginScreen> {
  final AuthApi _authApi = AuthApi();

  String _currentPin = '';
  List<String> _keypadNumbers = [];

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
        _checkPin();
      });
    }
  }

  void _onDeleteTap() {
    if (_currentPin.isEmpty) return;

    setState(() {
      _currentPin = _currentPin.substring(0, _currentPin.length - 1);
    });
  }

  Future<void> _checkPin() async {
    final savedPin = await SecureStorage.getPin();

    if (savedPin == _currentPin) {
      if (!mounted) return;

      if (widget.returnOnSuccess) {
        Navigator.pop(context, true);
        return;
      }

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => HomeScreen(memberName: widget.memberName),
        ),
      );
      return;
    }

    setState(() {
      _currentPin = '';
    });

    _shuffleKeypad();

    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('간편비밀번호가 일치하지 않습니다.'),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  Future<void> _goToLoginAgain() async {
    await _authApi.logout();

    if (!mounted) return;

    if (widget.returnOnSuccess) {
      Navigator.pop(context, false);
      return;
    }

    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => const LoginScreen()),
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
            border: Border.all(color: AppColors.textPrimary, width: 2),
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
            side: const BorderSide(color: AppColors.border),
          ),
        ),
        onPressed: () => _onNumberTap(text),
        child: Text(
          text,
          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800),
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
            _buildTextButton(text: '삭제', onPressed: _onDeleteTap),
            _buildNumberButton(lastNumber),
            _buildTextButton(text: '재배열', onPressed: _shuffleKeypad),
          ],
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final guideText = widget.returnOnSuccess
        ? 'QR로 연결된 상품을 확인하기 전 PIN으로 본인 확인합니다.'
        : '저장된 로그인 토큰을 사용하기 전 PIN으로 본인 확인합니다.';

    final bottomGuideText = widget.returnOnSuccess
        ? 'PIN 인증 성공 시\nQR로 연결된 상품 화면으로 이동합니다.'
        : 'PIN 인증 성공 시\n저장된 토큰으로 앱 홈 화면에 진입합니다.';

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
                    vertical: 20,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.cardBackground,
                    borderRadius: BorderRadius.circular(28),
                  ),
                  child: const Row(
                    children: [
                      Text(
                        'BNK',
                        style: TextStyle(
                          fontSize: 26,
                          fontWeight: FontWeight.w800,
                          color: AppColors.primaryRed,
                        ),
                      ),
                      SizedBox(width: 18),
                      Text(
                        '간편 로그인',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 34),
                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    '${widget.memberName}님,\n간편비밀번호를 입력하세요',
                    style: const TextStyle(
                      fontSize: 22,
                      height: 1.35,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    guideText,
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),
                const SizedBox(height: 42),
                _buildPinDots(),
                const SizedBox(height: 38),
                _buildKeypad(),
                const SizedBox(height: 26),
                SizedBox(
                  width: double.infinity,
                  child: TextButton(
                    onPressed: _goToLoginAgain,
                    child: const Text(
                      '아이디 / 비밀번호로 다시 로그인',
                      style: TextStyle(
                        color: AppColors.textSecondary,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 18,
                    vertical: 16,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.white,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Text(
                    bottomGuideText,
                    style: const TextStyle(
                      fontSize: 13,
                      height: 1.5,
                      color: AppColors.textSecondary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                const SizedBox(height: 18),
              ],
            ),
          ),
        ),
      ),
    );
  }
}