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

  Future<Map<String, dynamic>> post(
      String path, {
        Map<String, dynamic>? body,
        bool useToken = false,
      }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useToken: useToken);

    debugPrint('================ API POST ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] $headers');
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

  Future<Map<String, dynamic>> get(
      String path, {
        bool useToken = false,
      }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final headers = await _makeHeaders(useToken: useToken);

    debugPrint('================ API GET ================');
    debugPrint('[API URL] $uri');
    debugPrint('[API HEADERS] $headers');
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
    required bool useToken,
  }) async {
    final headers = <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
      'Accept': 'application/json',
    };

    if (useToken) {
      final token = await SecureStorage.getToken();

      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      }
    }

    return headers;
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