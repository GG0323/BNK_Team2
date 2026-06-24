import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SecureStorage {
  static const FlutterSecureStorage _storage = FlutterSecureStorage();

  static const String _authCookieKey = 'bnk_token';
  static const String _pinKey = 'bnk_pin';

  static Future<void> saveAuthCookie(String cookieValue) async {
    await _storage.write(key: _authCookieKey, value: cookieValue);
  }

  static Future<String?> getAuthCookie() async {
    return await _storage.read(key: _authCookieKey);
  }

  static Future<void> deleteAuthCookie() async {
    await _storage.delete(key: _authCookieKey);
  }

  static Future<void> savePin(String pin) async {
    await _storage.write(key: _pinKey, value: pin);
  }

  static Future<String?> getPin() async {
    return await _storage.read(key: _pinKey);
  }

  static Future<void> deletePin() async {
    await _storage.delete(key: _pinKey);
  }

  static Future<bool> hasPin() async {
    final pin = await getPin();
    return pin != null && pin.isNotEmpty;
  }

  static Future<void> clearAll() async {
    await _storage.deleteAll();
  }
}
