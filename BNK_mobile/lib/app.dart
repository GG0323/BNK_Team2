import 'dart:async';

import 'package:app_links/app_links.dart';
import 'package:flutter/material.dart';

import 'core/constants/app_colors.dart';
import 'core/storage/secure_storage.dart';
import 'data/models/product_model.dart';
import 'data/services/auth_api.dart';
import 'data/services/product_api.dart';
import 'features/auth/login_screen.dart';
import 'features/pin/pin_login_screen.dart';
import 'features/product_compare/product_detail_screen.dart';
import 'features/splash/splash_screen.dart';

class BnkMobileApp extends StatefulWidget {
  const BnkMobileApp({super.key});

  @override
  State<BnkMobileApp> createState() => _BnkMobileAppState();
}

class _BnkMobileAppState extends State<BnkMobileApp> {
  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey<NavigatorState>();
  final GlobalKey<ScaffoldMessengerState> _scaffoldMessengerKey =
      GlobalKey<ScaffoldMessengerState>();

  final AppLinks _appLinks = AppLinks();
  final ProductApi _productApi = ProductApi();
  final AuthApi _authApi = AuthApi();

  StreamSubscription<Uri>? _linkSubscription;

  bool _initialLinkChecked = false;
  bool _handlingDeepLink = false;
  int? _initialProductNo;

  @override
  void initState() {
    super.initState();
    _initDeepLinks();
  }

  @override
  void dispose() {
    _linkSubscription?.cancel();
    super.dispose();
  }

  Future<void> _initDeepLinks() async {
    int? initialProductNo;

    try {
      final initialUri = await _appLinks.getInitialLink();
      initialProductNo = _extractProductNo(initialUri);

      debugPrint('[DEEPLINK INITIAL URI] $initialUri');
      debugPrint('[DEEPLINK INITIAL PRODUCT_NO] $initialProductNo');
    } catch (error) {
      debugPrint('[DEEPLINK INITIAL ERROR] $error');
    }

    if (!mounted) return;

    setState(() {
      _initialLinkChecked = true;
      _initialProductNo = initialProductNo;
    });

    if (initialProductNo != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _handleProductDeepLink(
          initialProductNo!,
          fromInitialLink: true,
        );
      });
    }

    _linkSubscription = _appLinks.uriLinkStream.listen(
      (uri) {
        final productNo = _extractProductNo(uri);

        debugPrint('[DEEPLINK STREAM URI] $uri');
        debugPrint('[DEEPLINK STREAM PRODUCT_NO] $productNo');

        if (productNo == null) return;

        _handleProductDeepLink(
          productNo,
          fromInitialLink: false,
        );
      },
      onError: (Object error) {
        debugPrint('[DEEPLINK STREAM ERROR] $error');
      },
    );
  }

  int? _extractProductNo(Uri? uri) {
    if (uri == null) return null;

    final isBnkProductJoinLink = uri.scheme == 'bnkapp' &&
        uri.host == 'product' &&
        (uri.path == '/join' || uri.path.startsWith('/join/'));

    if (!isBnkProductJoinLink) return null;

    final rawProductNo =
        uri.queryParameters['product_no'] ?? uri.queryParameters['productNo'];

    if (rawProductNo == null || rawProductNo.trim().isEmpty) {
      return null;
    }

    return int.tryParse(rawProductNo.trim());
  }

  Future<void> _handleProductDeepLink(
    int productNo, {
    required bool fromInitialLink,
  }) async {
    if (_handlingDeepLink) {
      _showMessage('상품 이동을 처리하는 중입니다.');
      return;
    }

    _handlingDeepLink = true;

    try {
      await _waitForNavigator();

      final authenticated = await _ensureAuthenticatedForDeepLink();

      if (!authenticated) {
        if (fromInitialLink) {
          _releaseSplashNavigation();
        }
        return;
      }

      final product = await _productApi.getProductByNo(productNo);

      if (product == null) {
        _showMessage('해당 상품 정보를 찾을 수 없습니다.');

        if (fromInitialLink) {
          _releaseSplashNavigation();
        }
        return;
      }

      _openProductDetail(
        product,
        replaceStack: fromInitialLink,
      );
    } catch (error) {
      debugPrint('[DEEPLINK HANDLE ERROR] $error');
      _showMessage(error.toString().replaceFirst('Exception: ', ''));

      if (fromInitialLink) {
        _releaseSplashNavigation();
      }
    } finally {
      _handlingDeepLink = false;
    }
  }

  Future<void> _waitForNavigator() async {
    for (int i = 0; i < 30; i++) {
      if (_navigatorKey.currentState != null) return;
      await Future.delayed(const Duration(milliseconds: 80));
    }
  }

  Future<bool> _ensureAuthenticatedForDeepLink() async {
    final authCookie = await SecureStorage.getAuthCookie();

    if (authCookie == null || authCookie.isEmpty) {
      return _openLoginForDeepLink();
    }

    try {
      final member = await _authApi.getMe();
      final hasPin = await SecureStorage.hasPin();

      if (!hasPin) {
        return true;
      }

      final navigator = _navigatorKey.currentState;
      if (navigator == null) return false;

      final pinPassed = await navigator.push<bool>(
        MaterialPageRoute(
          builder: (_) => PinLoginScreen(
            memberName: member.memberName,
            returnOnSuccess: true,
          ),
        ),
      );

      return pinPassed == true;
    } catch (_) {
      await SecureStorage.clearAll();
      return _openLoginForDeepLink();
    }
  }

  Future<bool> _openLoginForDeepLink() async {
    final navigator = _navigatorKey.currentState;
    if (navigator == null) return false;

    final loggedIn = await navigator.push<bool>(
      MaterialPageRoute(
        builder: (_) => const LoginScreen(
          returnOnSuccess: true,
        ),
      ),
    );

    return loggedIn == true;
  }

  void _openProductDetail(
    ProductModel product, {
    required bool replaceStack,
  }) {
    final navigator = _navigatorKey.currentState;
    if (navigator == null) return;

    final route = MaterialPageRoute(
      builder: (_) => ProductDetailScreen(product: product),
    );

    if (replaceStack) {
      navigator.pushAndRemoveUntil(route, (route) => false);
      return;
    }

    navigator.push(route);
  }

  void _releaseSplashNavigation() {
    if (!mounted) return;

    setState(() {
      _initialProductNo = null;
      _initialLinkChecked = true;
    });
  }

  void _showMessage(String message) {
    _scaffoldMessengerKey.currentState?.showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final splashCanNavigate =
        _initialLinkChecked && _initialProductNo == null;

    return MaterialApp(
      title: 'BNK Mobile',
      navigatorKey: _navigatorKey,
      scaffoldMessengerKey: _scaffoldMessengerKey,
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: AppColors.background,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.primaryRed,
          primary: AppColors.primaryRed,
        ),
      ),
      home: SplashScreen(
        enableAutoNavigation: splashCanNavigate,
      ),
    );
  }
}