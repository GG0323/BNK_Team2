import 'package:flutter/foundation.dart';

class AccountRefreshNotifier {
  AccountRefreshNotifier._();

  static final ValueNotifier<int> _version = ValueNotifier<int>(0);

  static ValueListenable<int> get listenable => _version;

  static void notifyChanged() {
    _version.value += 1;
  }
}
