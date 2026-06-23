import 'dart:async';

import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/secure_storage.dart';
import '../../data/services/auth_api.dart';
import '../auth/login_screen.dart';
import '../pin/pin_login_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  final AuthApi _authApi = AuthApi();

  @override
  void initState() {
    super.initState();
    _checkLoginState();
  }

  Future<void> _checkLoginState() async {
    await Future.delayed(const Duration(seconds: 1));

    final authCookie = await SecureStorage.getAuthCookie();
    final hasPin = await SecureStorage.hasPin();

    if (!mounted) return;

    if (authCookie == null || authCookie.isEmpty) {
      _goToLogin();
      return;
    }

    if (!hasPin) {
      _goToLogin();
      return;
    }

    try {
      final member = await _authApi.getMe();

      if (!mounted) return;

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => PinLoginScreen(
            memberName: member.memberName,
          ),
        ),
      );
    } catch (e) {
      await SecureStorage.clearAll();

      if (!mounted) return;

      _goToLogin();
    }
  }

  void _goToLogin() {
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => const LoginScreen(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.splashBackground,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 36),
          child: Column(
            children: [
              const Spacer(),

              const Text(
                'BNK',
                style: TextStyle(
                  fontSize: 56,
                  fontWeight: FontWeight.w800,
                  color: AppColors.primaryRed,
                  letterSpacing: 1,
                ),
              ),
              const SizedBox(height: 14),

              const Text(
                '부산은행 모바일뱅킹',
                style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.w700,
                  color: AppColors.white,
                ),
              ),

              const SizedBox(height: 48),

              Container(
                width: double.infinity,
                height: 52,
                decoration: BoxDecoration(
                  color: AppColors.splashButton,
                  borderRadius: BorderRadius.circular(28),
                ),
                alignment: Alignment.center,
                child: const Text(
                  '자동 로그인 정보를 확인하는 중...',
                  style: TextStyle(
                    color: AppColors.white,
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),

              const Spacer(),

              const Text(
                '저장된 토큰 있음 → 간편 로그인\n저장된 토큰 없음 → 최초 로그인',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 13,
                  height: 1.8,
                  color: Color(0xFFB8C3D6),
                ),
              ),

              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
