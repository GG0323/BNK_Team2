import 'package:flutter/material.dart';

import 'core/constants/app_colors.dart';
import 'features/splash/splash_screen.dart';

class BnkMobileApp extends StatelessWidget {
  const BnkMobileApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
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