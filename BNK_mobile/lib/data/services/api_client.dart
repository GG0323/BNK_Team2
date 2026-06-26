import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:http/io_client.dart';

import '../../core/constants/api_constants.dart';
import '../../core/storage/secure_storage.dart';

class ApiClient {
  static final ApiClient instance = ApiClient._internal();

  ApiClient._internal();

  final http.Client _client = _createClient();

  static const Duration _timeout = Duration(seconds: 8);
  static const String _authCookieName = 'bnk_token';
  static const String _timeoutMessage =
      '서버 응답 시간이 초과되었습니다. Spring 서버와 네트워크 연결을 확인해주세요.';
  static const String _socketMessage =
      '서버에 연결할 수 없습니다. baseUrl, 와이파이, 방화벽, Spring 실행 상태를 확인해주세요.';
  static const String _formatMessage = '서버 응답 형식이 올바르지 않습니다.';

  static http.Client _createClient() {
    if (!kDebugMode || !ApiConstants.allowSelfSignedDevCertificate) {
      return http.Client();
    }

    final apiBaseUri = Uri.parse(ApiConstants.baseUrl);
    final httpClient = HttpClient()
      ..badCertificateCallback = (certificate, host, port) {
        return apiBaseUri.scheme == 'https' &&
            host == apiBaseUri.host &&
            port == apiBaseUri.port;
      };

    return IOClient(httpClient);
  }

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
    debugPrint('[API BODY] ${jsonEncode(_jsonBodyForLog(body ?? {}))}');
    debugPrint('==========================================');

    try {
      final response = await _client
          .post(uri, headers: headers, body: jsonEncode(body ?? {}))
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
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
          .post(uri, headers: headers, body: body)
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<void> postForRedirect(
    String path, {
    bool useAuthCookie = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(
      useAuthCookie: useAuthCookie,
      includeContentType: false,
    );

    debugPrint('================ API POST REDIRECT ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('===================================================');

    try {
      final request = http.Request('POST', uri)
        ..headers.addAll(headers)
        ..followRedirects = false;

      final streamed = await _client.send(request).timeout(_timeout);
      final response = await http.Response.fromStream(streamed);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);

      if (response.statusCode >= 200 && response.statusCode < 400) {
        return;
      }

      throw Exception('요청 처리 중 오류가 발생했습니다.');
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
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
          .get(uri, headers: headers)
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, dynamic>> put(
    String path, {
    Map<String, dynamic>? body,
    bool useAuthCookie = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useAuthCookie: useAuthCookie);

    debugPrint('================ API PUT ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('[API BODY] ${jsonEncode(_jsonBodyForLog(body ?? {}))}');
    debugPrint('=========================================');

    try {
      final response = await _client
          .put(uri, headers: headers, body: jsonEncode(body ?? {}))
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, dynamic>> delete(
    String path, {
    bool useAuthCookie = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useAuthCookie: useAuthCookie);

    debugPrint('================ API DELETE ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('===========================================');

    try {
      final response = await _client
          .delete(uri, headers: headers)
          .timeout(_timeout);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, dynamic>> postMultipart(
    String path, {
    required String filePath,
    String fieldName = 'image',
    bool useAuthCookie = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(
      useAuthCookie: useAuthCookie,
      includeContentType: false,
    );

    debugPrint('============= API MULTIPART POST =============');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] ${_headersForLog(headers)}');
    debugPrint('[API FILE] attached=true');
    debugPrint('==============================================');

    try {
      final request = http.MultipartRequest('POST', uri);
      request.headers.addAll(headers);
      request.files.add(await http.MultipartFile.fromPath(fieldName, filePath));

      final streamed = await _client.send(request).timeout(_timeout);
      final response = await http.Response.fromStream(streamed);

      debugPrint('================ API RESPONSE ================');
      debugPrint('[API STATUS] ${response.statusCode}');
      debugPrint('[API BODY] ${utf8.decode(response.bodyBytes)}');
      debugPrint('==============================================');

      await _saveAuthCookieFrom(response);
      return _handleResponse(response);
    } on TimeoutException {
      debugPrint('[API ERROR] TimeoutException');
      throw Exception(_timeoutMessage);
    } on SocketException catch (error) {
      debugPrint('[API ERROR] SocketException: $error');
      throw Exception(_socketMessage);
    } on FormatException catch (error) {
      debugPrint('[API ERROR] FormatException: $error');
      throw Exception(_formatMessage);
    } catch (error) {
      debugPrint('[API ERROR] $error');
      throw Exception(error.toString().replaceFirst('Exception: ', ''));
    }
  }

  Future<Map<String, String>> _makeHeaders({
    required bool useAuthCookie,
    bool includeContentType = true,
  }) async {
    final headers = <String, String>{'Accept': 'application/json'};

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

  Map<String, dynamic> _jsonBodyForLog(Map<String, dynamic> body) {
    final safeBody = Map<String, dynamic>.from(body);

    for (final key in [
      'pin',
      'answer1',
      'answer2',
      'password',
      'frontAnswer',
      'backAnswer',
    ]) {
      if (safeBody.containsKey(key)) {
        safeBody[key] = '***';
      }
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

    final message =
        decodedJson['message'] ?? decodedJson['error'] ?? '요청 처리 중 오류가 발생했습니다.';

    throw Exception(message.toString());
  }
}
