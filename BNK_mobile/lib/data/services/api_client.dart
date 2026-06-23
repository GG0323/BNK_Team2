import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import '../../core/constants/api_constants.dart';
import '../../core/storage/secure_storage.dart';

class ApiClient {
  static final ApiClient instance = ApiClient._internal();

  ApiClient._internal();

  final http.Client _client = http.Client();

  static const Duration _timeout = Duration(seconds: 8);
  static const String _authCookieName = 'bnk_token';

  Future<Map<String, dynamic>> post(
      String path, {
        Map<String, dynamic>? body,
        bool useAuthCookie = false,
      }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useAuthCookie: useAuthCookie);

    debugPrint('================ API POST ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('[API BODY] ${jsonEncode(body ?? {})}');
    debugPrint('==========================================');

    try {
      final response = await _client
          .post(
        uri,
        headers: headers,
        body: jsonEncode(body ?? {}),
      )
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception('서버 응답 시간이 초과되었습니다. Spring 서버와 네트워크 연결을 확인해주세요.');
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception('서버에 연결할 수 없습니다. baseUrl, 와이파이, 방화벽, Spring 실행 상태를 확인해주세요.');
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception('서버 응답 형식이 올바르지 않습니다.');
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, dynamic>> postForm(
      String path, {
        required Map<String, String> body,
        bool useAuthCookie = false,
      }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(
      useAuthCookie: useAuthCookie,
      includeContentType: false,
    );

    debugPrint('================ API POST FORM ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('[API BODY] ${_formBodyForLog(body)}');
    debugPrint('================================================');

    try {
      final response = await _client
          .post(
        uri,
        headers: headers,
        body: body,
      )
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception('?쒕쾭 ?묐떟 ?쒓컙??珥덇낵?섏뿀?듬땲?? Spring ?쒕쾭? ?ㅽ듃?뚰겕 ?곌껐???뺤씤?댁＜?몄슂.');
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception('?쒕쾭???곌껐?????놁뒿?덈떎. baseUrl, ??댄뙆?? 諛⑺솕踰? Spring ?ㅽ뻾 ?곹깭瑜??뺤씤?댁＜?몄슂.');
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception('?쒕쾭 ?묐떟 ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.');
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, dynamic>> get(
      String path, {
        bool useAuthCookie = false,
      }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useAuthCookie: useAuthCookie);

    debugPrint('================ API GET ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('=========================================');

    try {
      final response = await _client
          .get(
        uri,
        headers: headers,
      )
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception('서버 응답 시간이 초과되었습니다. Spring 서버와 네트워크 연결을 확인해주세요.');
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception('서버에 연결할 수 없습니다. baseUrl, 와이파이, 방화벽, Spring 실행 상태를 확인해주세요.');
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception('서버 응답 형식이 올바르지 않습니다.');
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, String>> _makeHeaders({
    required bool useAuthCookie,
    bool includeContentType = true,
  }) async {
    final headers = <String, String>{
      'Accept': 'application/json',
    };

    if (includeContentType) {
      headers['Content-Type'] = 'application/json; charset=UTF-8';
    }

    if (useAuthCookie) {
      final cookieValue = await SecureStorage.getAuthCookie();

      if (cookieValue != null && cookieValue.isNotEmpty) {
        headers['Cookie'] = '$_authCookieName=$cookieValue';
      }
    }

    return headers;
  }

  Future<void> _saveAuthCookieFrom(http.Response response) async {
    final setCookie = response.headers['set-cookie'];

    if (setCookie == null || setCookie.isEmpty) {
      return;
    }

    final cookieValue = _extractCookieValue(setCookie, _authCookieName);

    if (cookieValue == null) {
      return;
    }

    if (cookieValue.isEmpty) {
      await SecureStorage.deleteAuthCookie();
      return;
    }

    await SecureStorage.saveAuthCookie(cookieValue);
  }

  String? _extractCookieValue(String setCookie, String cookieName) {
    final pattern = RegExp('(?:^|,\\s*)${RegExp.escape(cookieName)}=([^;]*)');
    final match = pattern.firstMatch(setCookie);
    return match?.group(1);
  }

  Map<String, String> _headersForLog(Map<String, String> headers) {
    final safeHeaders = Map<String, String>.from(headers);

    if (safeHeaders.containsKey('Cookie')) {
      safeHeaders['Cookie'] = '$_authCookieName=***';
    }

    return safeHeaders;
  }

  Map<String, String> _formBodyForLog(Map<String, String> body) {
    final safeBody = Map<String, String>.from(body);

    if (safeBody.containsKey('password')) {
      safeBody['password'] = '***';
    }

    return safeBody;
  }

  Map<String, dynamic> _handleResponse(http.Response response) {
    final decodedBody = utf8.decode(response.bodyBytes);

    if (decodedBody.trim().isEmpty) {
      throw Exception('서버 응답이 비어있습니다.');
    }

    final dynamic decodedJson = jsonDecode(decodedBody);

    if (decodedJson is! Map<String, dynamic>) {
      throw Exception('서버 응답 형식이 올바르지 않습니다.');
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decodedJson;
    }

    final message = decodedJson['message'] ??
        decodedJson['error'] ??
        '요청 처리 중 오류가 발생했습니다.';

    throw Exception(message.toString());
  }
}
