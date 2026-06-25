import 'dart:async';

import 'package:app_links/app_links.dart';
import 'package:flutter/material.dart';

import 'core/constants/app_colors.dart';
import 'core/storage/secure_storage.dart';
import 'data/services/auth_api.dart';
import 'features/auth/login_screen.dart';
import 'features/pin/pin_login_screen.dart';
import 'features/pin/pin_setup_screen.dart';
import 'features/product_compare/product_join_intro_screen.dart';
import 'features/splash/splash_screen.dart';

class BnkMobileApp extends StatefulWidget {
  const BnkMobileApp({super.key});

  @override
  State<BnkMobileApp> createState() => _BnkMobileAppState();
}

class _BnkMobileAppState extends State<BnkMobileApp> {
  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey<NavigatorState>();
  final AppLinks _appLinks = AppLinks();
  final AuthApi _authApi = AuthApi();

  StreamSubscription<Uri>? _linkSubscription;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initDeepLinks();
    });
  }

  Future<void> _initDeepLinks() async {
    try {
      final initialLink = await _appLinks.getInitialLink();

      if (initialLink != null) {
        await _handleDeepLink(initialLink, isInitialLink: true);
      }

      _linkSubscription = _appLinks.uriLinkStream.listen(
        (uri) {
          _handleDeepLink(uri);
        },
        onError: (error) {
          debugPrint('[DEEP LINK ERROR] $error');
        },
      );
    } catch (error) {
      debugPrint('[DEEP LINK INIT ERROR] $error');
    }
  }

  Future<void> _handleDeepLink(
    Uri uri, {
    bool isInitialLink = false,
  }) async {
    debugPrint('[DEEP LINK] $uri');

    if (uri.scheme != 'bnkapp') return;
    if (uri.host != 'product') return;
    if (uri.pathSegments.isEmpty) return;
    if (uri.pathSegments.first != 'join') return;

    final productNoText =
        uri.queryParameters['product_no'] ?? uri.queryParameters['productNo'];

    final productNo = int.tryParse(productNoText ?? '');

    if (productNo == null || productNo <= 0) {
      debugPrint('[DEEP LINK] invalid product_no: $productNoText');
      return;
    }

    if (isInitialLink) {
      await Future.delayed(const Duration(milliseconds: 1200));
    }

    await _goToProductJoinFlow(productNo);
  }

  Future<void> _goToProductJoinFlow(int productNo) async {
    final navigator = _navigatorKey.currentState;

    if (navigator == null) {
      debugPrint('[DEEP LINK] navigator is null');
      return;
    }

    final authCookie = await SecureStorage.getAuthCookie();

    if (authCookie == null || authCookie.isEmpty) {
      navigator.push(
        MaterialPageRoute(
          builder: (_) => LoginScreen(
            redirectProductNo: productNo,
          ),
        ),
      );
      return;
    }

    try {
      final member = await _authApi.getMe();
      final hasPin = await SecureStorage.hasPin();

      if (hasPin) {
        navigator.push(
          MaterialPageRoute(
            builder: (_) => PinLoginScreen(
              memberName: member.memberName,
              redirectProductNo: productNo,
            ),
          ),
        );
        return;
      }

      navigator.push(
        MaterialPageRoute(
          builder: (_) => PinSetupScreen(
            memberName: member.memberName,
            redirectProductNo: productNo,
          ),
        ),
      );
    } catch (error) {
      debugPrint('[DEEP LINK AUTH CHECK ERROR] $error');

      await SecureStorage.clearAll();

      navigator.push(
        MaterialPageRoute(
          builder: (_) => LoginScreen(
            redirectProductNo: productNo,
          ),
        ),
      );
    }
  }

  @override
  void dispose() {
    _linkSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: _navigatorKey,
      title: 'BNK Mobile',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: AppColors.background,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.primaryRed,
          primary: AppColors.primaryRed,
        ),
      ),
      home: const SplashScreen(),
    );
  }
}